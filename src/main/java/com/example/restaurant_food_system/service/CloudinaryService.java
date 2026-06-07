package com.example.restaurant_food_system.service;

import com.cloudinary.Cloudinary;
import com.example.restaurant_food_system.dto.response.UploadResult;
import com.example.restaurant_food_system.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public UploadResult uploadFile(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of(
                            "folder", "restaurant-food-system",
                            "resource_type", "auto"
                    )
            );

            // Trả về url sau khi upload
            String secureUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();

            return UploadResult.builder()
                    .secureUrl(secureUrl)
                    .publicId(publicId)
                    .build();
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (IOException e) {
            throw new BadRequestException("Delete file failed");
        }
    }
}
