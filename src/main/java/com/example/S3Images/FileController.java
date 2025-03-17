package com.example.S3Images;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class FileController {
    @Autowired
    private FileService service;

    @PostMapping("/upload")
    public ResponseEntity<Image> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Image uploadedImage = service.uploadImage(file);
            return new ResponseEntity<>(uploadedImage, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listImages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        List<Image> images = service.listImages(page, size);
        int totalImages = service.getTotalImages();
        int totalPages = (int) Math.ceil((double) totalImages / size);

        Map<String, Object> response = new HashMap<>();
        response.put("images", images);
        response.put("currentPage", page);
        response.put("totalItems", totalImages);
        response.put("totalPages", totalPages);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
