package com.example.TTDN.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final Cloudinary cloudinary;

    public FileUploadController() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dzdp1wot",
                "api_key", "675144467141957",
                "api_secret", "ukddC6C55LprygdKeKyGHybl4uo",
                "secure", true
        ));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
            String url = uploadResult.get("secure_url").toString();

            Map<String, String> response = new HashMap<>();
            response.put("url", url);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi upload: " + e.getMessage());
        }
    }
}