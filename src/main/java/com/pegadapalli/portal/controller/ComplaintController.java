package com.pegadapalli.portal.controller;

import com.pegadapalli.portal.model.Complaint;
import com.pegadapalli.portal.repository.ComplaintRepository;
import com.pegadapalli.portal.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplaintController.class);

    private final ComplaintRepository complaintRepository;
    private final AdminAuthService adminAuthService;

    public ComplaintController(ComplaintRepository complaintRepository, AdminAuthService adminAuthService) {
        this.complaintRepository = complaintRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findAll();
        complaints.sort(Comparator.comparing(Complaint::getId).reversed());
        return complaints;
    }

    @PostMapping
    @Nullable
    @SuppressWarnings("null")
    public Complaint createComplaint(@Valid @RequestBody Complaint complaint) {
        if (complaint.getComplaintStatus() == null || complaint.getComplaintStatus().isBlank()) {
            complaint.setComplaintStatus("NEW");
        }
        return complaintRepository.save(complaint);
    }

    @PatchMapping("/{id}/status")
    @SuppressWarnings("null")
    public ResponseEntity<Map<String, String>> updateComplaintStatus(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @PathVariable @NonNull Long id,
            @RequestParam("status") String status) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }

        String normalizedStatus = status == null ? "" : status.trim().toUpperCase();
        if (!isValidStatus(normalizedStatus)) {
            throw new ResponseStatusException(BAD_REQUEST, "Status must be NEW, IN_PROGRESS, or RESOLVED");
        }

        Optional<Complaint> optionalComplaint = complaintRepository.findById(id);
        if (optionalComplaint.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Complaint not found");
        }

        Complaint complaint = optionalComplaint.get();

        String currentStatus = complaint.getComplaintStatus() == null
                ? "NEW"
                : complaint.getComplaintStatus().trim().toUpperCase();

        if ("RESOLVED".equals(currentStatus) && !"RESOLVED".equals(normalizedStatus)) {
            throw new ResponseStatusException(BAD_REQUEST, "Resolved complaints cannot be moved back");
        }

        complaint.setComplaintStatus(normalizedStatus);
        complaintRepository.save(complaint);

        if (!"RESOLVED".equals(currentStatus) && "RESOLVED".equals(normalizedStatus)) {
            triggerResolvedNotification(complaint);
        }

        return ResponseEntity.ok(Map.of("message", "Complaint status updated"));
    }

    private boolean isValidStatus(String status) {
        return "NEW".equals(status) || "IN_PROGRESS".equals(status) || "RESOLVED".equals(status);
    }

    private void triggerResolvedNotification(Complaint complaint) {
        // Placeholder notification trigger. Can be replaced by SMS/WhatsApp provider integration.
        LOGGER.info("[Complaint-Notification] Issue resolved for {} ({})", complaint.getName(), complaint.getPhone());
    }
}
