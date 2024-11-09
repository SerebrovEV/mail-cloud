package org.image.core.util;

import org.junit.jupiter.api.Test;
import org.image.core.dto.model.ImageInfo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageUtilsTest {

    @Test
    void testGetNameImagesList_SingleImage() {
        List<String> images = List.of("image1.jpg");
        String result = ImageUtils.getNameImagesList(images);
        assertEquals("image1.jpg.", result);
    }

    @Test
    void testGetNameImagesList_MultipleImages() {
        List<String> images = List.of("image1.jpg", "image2.png", "image3.gif");
        String result = ImageUtils.getNameImagesList(images);
        assertEquals("image1.jpg, image2.png, image3.gif.", result);
    }

    @Test
    void testGetSizeInMb() {
        ImageInfo imageInfo = new ImageInfo(List.of(), List.of(),1048576);
        double result = ImageUtils.getSizeInMb(imageInfo);
        assertEquals(1.0, result, 0.0001);
    }


}