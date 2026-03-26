package com.pegadapalli.portal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    public StoredImage storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String suffix = (extension == null || extension.isBlank()) ? "jpg" : extension.toLowerCase();
        String baseName = UUID.randomUUID().toString();
        String fileName = baseName + "." + suffix;
        String thumbnailName = "thumb-" + baseName + ".jpg";

        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);
            Path destination = uploadPath.resolve(fileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }

            Path thumbnailDestination = uploadPath.resolve(thumbnailName);
            createCompressedThumbnail(destination, thumbnailDestination);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store image", exception);
        }

        return new StoredImage(fileName, thumbnailName);
    }

    public void deleteStoredFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        String fileName = extractUploadFileName(fileUrl);
        if (fileName == null) {
            return;
        }

        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Path targetFile = uploadPath.resolve(fileName).normalize();
        if (!targetFile.startsWith(uploadPath)) {
            return;
        }

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image file", exception);
        }
    }

    private String extractUploadFileName(String fileUrl) {
        try {
            URI uri = new URI(fileUrl);
            String path = uri.getPath();
            return extractFromPath(path);
        } catch (URISyntaxException ignored) {
            return extractFromPath(fileUrl);
        }
    }

    private String extractFromPath(String path) {
        if (path == null) {
            return null;
        }

        String normalized = path.replace('\\', '/');
        int marker = normalized.indexOf("/uploads/");
        if (marker < 0) {
            return null;
        }

        String fileName = normalized.substring(marker + "/uploads/".length());
        if (fileName.isBlank() || fileName.contains("/")) {
            return null;
        }
        return fileName;
    }

    private void createCompressedThumbnail(Path source, Path target) throws IOException {
        BufferedImage original = ImageIO.read(source.toFile());
        if (original == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image file");
        }

        int targetWidth = Math.min(480, original.getWidth());
        int targetHeight = Math.max(1, (int) Math.round((double) original.getHeight() * targetWidth / original.getWidth()));

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No JPEG writer available");
        }

        ImageWriter writer = writers.next();
        try (OutputStream outputStream = Files.newOutputStream(target);
             ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(imageOutputStream);
            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(0.75f);
            }
            writer.write(null, new IIOImage(resized, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    public record StoredImage(String originalFileName, String thumbnailFileName) {
    }
}
