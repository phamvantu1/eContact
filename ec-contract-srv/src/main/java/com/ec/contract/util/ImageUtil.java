package com.ec.contract.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;

@Slf4j
public final class ImageUtil {
    public static void scaleImage(File image, int width, int height, OutputStream out) throws IOException {
        String imgType = getImgType(new FileInputStream(image));
        if (imgType != null && !imgType.equals("unknown")) {
            Thumbnails
                    .of(image)
                    .size(width, height)
                    .outputFormat(imgType)
                    .toOutputStream(out);
        } else {
            Thumbnails
                    .of(image)
                    .size(width, height)
                    .toOutputStream(out);
        }

    }

    public static void scaleImage(URL image, int width, int height, OutputStream out) throws IOException {
        String imgType = getImgType(image.openStream());

        if (imgType != null && !imgType.equals("unknown")) {
            Thumbnails
                    .of(image)
                    .size(width, height)
                    .outputFormat(imgType)
                    .toOutputStream(out);
        } else {
            Thumbnails
                    .of(image)
                    .size(width, height)
                    .toOutputStream(out);
        }
    }

    public static void scaleImageV2(InputStream image, int width, int height, OutputStream out) throws IOException {
        String imgType = getImgType(image);

        if (imgType != null && !imgType.equals("unknown")) {
            Thumbnails
                    .of(image)
                    .size(width, height)
                    .outputFormat(imgType)
                    .toOutputStream(out);
        } else {
            Thumbnails
                    .of(image)
                    .size(width, height)
                    .toOutputStream(out);
        }
    }

    private static String getImgType(InputStream image)  {
        try {
            var iis = ImageIO.createImageInputStream(image);
            var iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
                return "unknown";
            }
            var reader = iter.next();
            iis.close();
            return reader.getFormatName();
        } catch(IOException ex) {
            log.error("err: {}", ex);
        }

        return null;
    }
}
