package com.pegadapalli.portal.controller;

import com.pegadapalli.portal.model.Contact;
import com.pegadapalli.portal.repository.ContactRepository;
import com.pegadapalli.portal.service.AdminAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactRepository contactRepository;
    private final AdminAuthService adminAuthService;

    public ContactController(ContactRepository contactRepository, AdminAuthService adminAuthService) {
        this.contactRepository = contactRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<Contact> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        contacts.sort(Comparator.comparing(Contact::getId).reversed());
        return contacts;
    }

    @PostMapping
    @Nullable
    @SuppressWarnings("null")
    public Contact createContact(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @RequestBody Contact contact) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }
        return contactRepository.save(contact);
    }

    @PutMapping("/{id}")
    @Nullable
    @SuppressWarnings("null")
    public Contact updateContact(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @PathVariable @NonNull Long id,
            @RequestBody Contact contact) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }

        Optional<Contact> optionalContact = contactRepository.findById(id);
        if (optionalContact.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Contact not found");
        }

        Contact existingContact = optionalContact.get();
        existingContact.setName(contact.getName());
        existingContact.setRole(contact.getRole());
        existingContact.setPhone(contact.getPhone());
        return contactRepository.save(existingContact);
    }

    @DeleteMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<Map<String, String>> deleteContact(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @PathVariable @NonNull Long id) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }

        Optional<Contact> optionalContact = contactRepository.findById(id);
        if (optionalContact.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Contact not found");
        }

        contactRepository.delete(optionalContact.get());
        return ResponseEntity.ok(Map.of("message", "Contact deleted successfully"));
    }
}
