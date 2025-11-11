package com.ec.contract.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@Slf4j
public class ImageUtils {
    public static BufferedImage getRotatedImage(byte[] dataIcon) throws ImageProcessingException, IOException, MetadataException {
        ByteArrayInputStream bis = new ByteArrayInputStream(dataIcon);
        Metadata metadata = ImageMetadataReader.readMetadata(bis);

        ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientation = 1;
        if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
        }

        bis.reset();
        BufferedImage icon = ImageIO.read(bis);

        switch (orientation) {
            case 1:
                break;  // top left
            case 3:
                icon = rotateImage(icon, 180);  // bottom right
                break;
            case 6:
                icon = rotateImage(icon, 90);  // right top
                break;
            case 8:
                icon = rotateImage(icon, 270);  // left bottom
                break;
            default:
                break;
        }

        return icon;
    }

    private static BufferedImage rotateImage(BufferedImage img, double angle) {
        double radian = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radian)), cos = Math.abs(Math.cos(radian));
        int w = img.getWidth(), h = img.getHeight();
        int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
        BufferedImage result = new BufferedImage(neww, newh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((neww - w) / 2, (newh - h) / 2);
        at.rotate(radian, w / 2, h / 2);
        g.drawRenderedImage(img, at);
        g.dispose();
        return result;
    }

    /**
     * Hàm chuyển đổi định loại ảnh
     * @param base64Image base64 ảnh muốn chuyển đổi
     * @param type loại muốn chuyển đổi (JPG,PNG,JPEG,...)
     * @return base64 ảnh mới đã chuyển đổi thành công
     */
    public static Optional<String> convertImageType(String base64Image, String type) {
        try {
            byte[] dataFileImage = Base64.getDecoder().decode(base64Image);
            ByteArrayOutputStream fileReplaceOutput = new ByteArrayOutputStream();
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(dataFileImage));
            ImageIO.write(bufferedImage, type, fileReplaceOutput);
            var dataFile = fileReplaceOutput.toByteArray();
            return Optional.of(Base64.getEncoder().encodeToString(dataFile));
        } catch (Exception e) {
            log.error("Đã có lỗi xảy ra trong quá trình convert ảnh sang định dạng jpg", e);
        }
        return Optional.empty();
    }
}
