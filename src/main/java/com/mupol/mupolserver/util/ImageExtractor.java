package com.mupol.mupolserver.util;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

public class ImageExtractor {
    public static MultipartFile getImageFile(String imageUrl) throws IOException {
        BufferedImage img = ImageIO.read(new URL(imageUrl));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return new MockMultipartFile("profile_image","profile_image.jpg" ,"image/jpg", baos.toByteArray());
    }
}
