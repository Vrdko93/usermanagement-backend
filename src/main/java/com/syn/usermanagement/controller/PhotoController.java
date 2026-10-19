package com.syn.usermanagement.controller;
import com.syn.usermanagement.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
@RestController
@RequestMapping("/api/users/{id}/photo")
@CrossOrigin(origins = "*")
public class PhotoController {
    private final PhotoService photos;
    public PhotoController(PhotoService photos) { this.photos = photos; }
    @PostMapping public ResponseEntity<Void> upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        photos.upload(id, file); return ResponseEntity.noContent().build();
    }
    @GetMapping public Map<String,String> get(@PathVariable Long id) { return Map.of("url", photos.url(id)); }
    @DeleteMapping public ResponseEntity<Void> delete(@PathVariable Long id) {
        photos.delete(id); return ResponseEntity.noContent().build();
    }
}
