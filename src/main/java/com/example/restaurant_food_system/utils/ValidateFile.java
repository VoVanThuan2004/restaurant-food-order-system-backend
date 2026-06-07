package com.example.restaurant_food_system.utils;

import com.example.restaurant_food_system.exception.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Component
public class ValidateFile {

    public void validateFileImage(MultipartFile file) {
        // Kiểm tra định dạng file
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File vượt quá 5MB");
        }

        String contentType = file.getContentType();
        List<String> allowedTypes = List.of("image/jpeg", "image/png", "image/jpg");
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BadRequestException("Chỉ chấp nhận file có định dạng JPG, PNG, JPEG");
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BadRequestException("File không phải ảnh hợp lệ");
            }
        } catch (IOException e) {
            throw new BadRequestException("Không đọc được file");
        }
    }
}
