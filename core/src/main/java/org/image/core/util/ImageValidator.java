package org.image.core.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class ImageValidator {
    
    @Value("${images.permitted-format}")
    private Set<String> permittedFormatImages;
    
    private static Set<String> PERMITTED_FORMAT_IMAGES;
    
    @PostConstruct
    public void init() {
        PERMITTED_FORMAT_IMAGES = permittedFormatImages;
    }
    
    public static boolean isValidImage(String imageName) {
        for (String format : PERMITTED_FORMAT_IMAGES) {
            if (imageName.toUpperCase().endsWith(format.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
