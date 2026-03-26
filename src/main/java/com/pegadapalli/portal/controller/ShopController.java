package com.pegadapalli.portal.controller;

import com.pegadapalli.portal.model.Shop;
import com.pegadapalli.portal.repository.ShopRepository;
import com.pegadapalli.portal.service.AdminAuthService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopRepository shopRepository;
    private final AdminAuthService adminAuthService;

    public ShopController(ShopRepository shopRepository, AdminAuthService adminAuthService) {
        this.shopRepository = shopRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<Shop> getAllShops() {
        List<Shop> shops = shopRepository.findAll();
        shops.sort(Comparator.comparing(Shop::getId).reversed());
        return shops;
    }

    @PostMapping
    @Nullable
    @SuppressWarnings("null")
    public Shop createShop(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @Valid @RequestBody Shop shop) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }
        return shopRepository.save(shop);
    }

    @DeleteMapping("/{id}")
    @SuppressWarnings("null")
    public ResponseEntity<Map<String, String>> deleteShop(
            @RequestHeader(value = "X-Admin-Token", required = false) String adminToken,
            @PathVariable @NonNull Long id) {
        if (!adminAuthService.isValidToken(adminToken)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid admin token");
        }

        Optional<Shop> optionalShop = shopRepository.findById(id);
        if (optionalShop.isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Shop not found");
        }

        shopRepository.delete(optionalShop.get());
        return ResponseEntity.ok(Map.of("message", "Shop deleted successfully"));
    }
}
