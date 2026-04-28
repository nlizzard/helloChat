package com.nlizzard.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 */
public class QrCodeUtils {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    /**
     * 生成二维码，返回字节数组
     */
    public static byte[] generateQRCode(String data) {
        return generateQRCode(data, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码，返回字节数组
     */
    public static byte[] generateQRCode(String data, int width, int height) {
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("二维码内容不能为空");
        }

        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    width,
                    height,
                    hints
            );

            BufferedImage image = new BufferedImage(
                    width,
                    height,
                    BufferedImage.TYPE_INT_RGB
            );

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(
                            x,
                            y,
                            bitMatrix.get(x, y)
                                    ? Color.BLACK.getRGB()
                                    : Color.WHITE.getRGB()
                    );
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("二维码生成失败", e);
        }
    }

    /**
     * 生成二维码，返回输入流
     */
    public static InputStream generateQRCodeInputStream(String data) {
        byte[] bytes = generateQRCode(data);
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 生成二维码，返回输入流
     */
    public static InputStream generateQRCodeInputStream(String data, int width, int height) {
        byte[] bytes = generateQRCode(data, width, height);
        return new ByteArrayInputStream(bytes);
    }
}