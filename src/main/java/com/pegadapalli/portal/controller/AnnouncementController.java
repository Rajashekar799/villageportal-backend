package com.pegadapalli.portal.controller;

import com.pegadapalli.portal.model.Announcement;
import com.pegadapalli.portal.repository.AnnouncementRepository;
import com.pegadapalli.portal.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository announcementRepository;
    private final AdminAuthService adminAuthService;

    public AnnouncementController(AnnouncementRepository announcementRepository, AdminAuthService adminAuthService) {
        this.announcementRepository = announcementRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll();
        announcements.sort(Comparator.comparing(Announcement::getId).reversed());
        return announcements;
    }

    @PostMapping
    @Nullable
    @SuppressWarnings("null")
    public Announcement createAnnouncement(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @Valid @RequestBody Announcement announcement) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }
        return announcementRepository.save(announcement);
    }
}
