package com.example.canteen.menu;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController

@RequestMapping("/menu")
public class MenuController {

    private final MenuRepository menuRepository;

    private final MenuService service;

    private final Cloudinary cloudinary;

    public MenuController(MenuService service,
            MenuRepository menuRepository,
            Cloudinary cloudinary) {
        this.service = service;
        this.menuRepository = menuRepository;
        this.cloudinary = cloudinary;
    }

    @CacheEvict(value = "menu", allEntries = true)
    @PostMapping("/admin/add")
    public ResponseEntity<?> addMenuItem(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam MultipartFile image) {

        try {

            if (menuRepository.existsByName(name)) {
                return ResponseEntity.ok("Item already Exists");
            }
            // 2 MB = 2 * 1024 * 1024 bytes
            long maxSize = 5 * 1024 * 1024;

            if (image.getSize() > maxSize) {
                return ResponseEntity.badRequest()
                        .body("Image size must be less than 5 MB");
            }

            Map uploadResult = cloudinary.uploader().upload(
                    image.getBytes(),
                    ObjectUtils.emptyMap());

            String imageUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            MenuItem item = new MenuItem();
            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);
            item.setImageUrl(imageUrl);
            item.setPublicId(publicId);

            menuRepository.save(item);

            return ResponseEntity.ok("Item added successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @Cacheable("menu")
    @GetMapping("/all")
    public List<Map<String, Object>> getAll() {
        return service.getAll().stream().map(item -> {
            Map<String, Object> map = new HashMap<>();

            map.put("id", item.getId());
            map.put("name", item.getName());
            map.put("description", item.getDescription());
            map.put("price", item.getPrice());
            map.put("available", item.isAvailable());

            // ✅ Convert image to Base64
            map.put("imageUrl", item.getImageUrl());

            return map;
        }).toList();
    }

    @CacheEvict(value = "menu", allEntries = true)
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable UUID id) {

        try {
            MenuItem item = menuRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            cloudinary.uploader().destroy(
                    item.getPublicId(),
                    ObjectUtils.emptyMap());

            menuRepository.delete(item);

            return ResponseEntity.ok("Item deleted successfully");

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body("Failed to delete image from Cloudinary");
        }
    }

    @CacheEvict(value = "menu", allEntries = true)
    @PutMapping("/admin/availability/{id}")
    public ResponseEntity<?> updateAvailability(@PathVariable UUID id) {

        MenuItem menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        menu.setAvailable(!menu.isAvailable()); // Toggle

        menuRepository.save(menu);

        return ResponseEntity.ok(menu);
    }
}