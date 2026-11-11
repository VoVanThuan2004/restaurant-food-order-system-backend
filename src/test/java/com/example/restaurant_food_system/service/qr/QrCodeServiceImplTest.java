package com.example.restaurant_food_system.service.qr;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrCodeServiceImplTest {
    private final QrCodeServiceImpl qrCodeService =
            new QrCodeServiceImpl("http://localhost:5173/");

    @Test
    void generateCode_ShouldHavePrefixAndValidLength() {
        String qrCode = qrCodeService.generateCode();

        assertTrue(qrCode.startsWith("TBL_"));
        assertTrue(qrCode.length() <= 64);
    }

    @Test
    void generateCode_ShouldReturnDifferentCodes() {
        assertNotEquals(qrCodeService.generateCode(), qrCodeService.generateCode());
    }

    @Test
    void buildQrUrl_ShouldUseOrderRouteAndTableQueryParam() {
        assertEquals(
                "http://localhost:5173/order?table=TBL_TEST",
                qrCodeService.buildQrUrl("TBL_TEST")
        );
    }

    @Test
    void renderPng_ShouldProduceDecodableQrCode() throws Exception {
        String content = "http://localhost:5173/order?table=TBL_TEST";
        byte[] png = qrCodeService.renderPng(content);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));
        BinaryBitmap bitmap = new BinaryBitmap(
                new HybridBinarizer(new BufferedImageLuminanceSource(image))
        );
        Result result = new MultiFormatReader().decode(bitmap);

        assertEquals(content, result.getText());
    }
}
