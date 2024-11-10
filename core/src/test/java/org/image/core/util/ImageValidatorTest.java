package org.image.core.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ImageValidatorTest {

    @BeforeAll
    public static void setUp() {
        Set<String> permittedFormats = Set.of("jpg", "png", "gif");
        ReflectionTestUtils.setField(ImageValidator.class, "PERMITTED_FORMAT_IMAGES", permittedFormats);
    }

    @Test
    public void testIsValidImage_CorrectNameFormat() {
        assertTrue(ImageValidator.isValidImage("image.jpg"));
        assertTrue(ImageValidator.isValidImage("image.PNG"));
        assertTrue(ImageValidator.isValidImage("image.Gif"));
    }

    @Test
    public void testIsValidImage_InvalidNameFormat() {
        assertFalse(ImageValidator.isValidImage("image.txt"));
        assertFalse(ImageValidator.isValidImage("document.pdf"));
    }

    @Test
    public void testIsValidImage_NullImageName() {
        assertFalse(ImageValidator.isValidImage(null));
    }

    @Test
    public void testIsValidImage_EmptyImageName() {
        assertFalse(ImageValidator.isValidImage(""));
    }
}