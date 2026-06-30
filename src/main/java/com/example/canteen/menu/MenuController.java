package com.example.canteen.menu;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController

@RequestMapping("/menu")
public class MenuController {

    private final MenuRepository menuRepository;

    private final MenuService service;

    public MenuController(MenuService service, MenuRepository menuRepository) {
        this.service = service;
        this.menuRepository = menuRepository;
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
                return ResponseEntity.ok("Item already Exists ");
            }
            MenuItem item = new MenuItem();
            item.setName(name);
            item.setDescription(description);
            item.setPrice(price);
            item.setImage(image.getBytes()); // ✅ store image

            menuRepository.save(item);

            return ResponseEntity.ok("Item added successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving item");
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
            if (item.getImage() != null) {
                String base64 = Base64.getEncoder()
                        .encodeToString(item.getImage());
                map.put("image", base64);
            } else {
                map.put("image", null);
            }

            return map;
        }).toList();
    }

    @CacheEvict(value = "menu", allEntries = true)
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable UUID id) {

        if (!menuRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Item not found");
        }

        menuRepository.deleteById(id);
        return ResponseEntity.ok("Item deleted successfully");
    }
}