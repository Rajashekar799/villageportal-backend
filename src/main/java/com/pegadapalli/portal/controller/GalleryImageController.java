package com.pegadapalli.portal.controller;

import com.pegadapalli.portal.model.GalleryImage;
import com.pegadapalli.portal.repository.GalleryImageRepository;
import com.pegadapalli.portal.service.AdminAuthService;
import com.pegadapalli.portal.service.ImageStorageService;
import com.pegadapalli.portal.service.ImageStorageService.StoredImage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/gallery-images")
public class GalleryImageController {

    private final GalleryImageRepository galleryImageRepository;
    private final AdminAuthService adminAuthService;
    private final ImageStorageService imageStorageService;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public GalleryImageController(
            GalleryImageRepository galleryImageRepository,
            AdminAuthService adminAuthService,
            ImageStorageService imageStorageService) {
        this.galleryImageRepository = galleryImageRepository;
        this.adminAuthService = adminAuthService;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public List<GalleryImage> getAllImages() {
        List<GalleryImage> images = galleryImageRepository.findAll();
        images.sort(Comparator.comparing(GalleryImage::getId).reversed());
        return images;
    }

    @PostMapping
    @Nullable
    @SuppressWarnings("null")
    public GalleryImage createImage(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @RequestBody GalleryImage image) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }
        return galleryImageRepository.save(image);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Nullable
    @SuppressWarnings("null")
    public GalleryImage uploadImage(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("file") MultipartFile file) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }

        StoredImage storedImage = imageStorageService.storeImage(file);
        GalleryImage image = new GalleryImage();
        image.setTitle(title);
        image.setCategory(category);
        image.setImageUrl(publicBaseUrl + "/uploads/" + storedImage.originalFileName());
        image.setThumbnailUrl(publicBaseUrl + "/uploads/" + storedImage.thumbnailFileName());
        return galleryImageRepository.save(image);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteImage(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @PathVariable @NonNull Long id) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }

        Optional<GalleryImage> optionalImage = galleryImageRepository.findById(id);
        if (optionalImage.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Image not found");
        }

        GalleryImage image = optionalImage.get();
        imageStorageService.deleteStoredFileByUrl(image.getImageUrl());
        imageStorageService.deleteStoredFileByUrl(image.getThumbnailUrl());
        galleryImageRepository.delete(image);
        return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
    }
}
