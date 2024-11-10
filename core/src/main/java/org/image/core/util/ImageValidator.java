package org.image.core.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.image.core.dto.model.TextConstant.TEXT_START_WORK;

@Slf4j
@Component
public class ImageValidator {
    /**
     * Разрешенные форматы изображений
     */
    @Value("${images.permitted-format}")
    private Set<String> permittedFormatImages;
    
    private static Set<String> PERMITTED_FORMAT_IMAGES;
    
    @PostConstruct
    public void init() {
        PERMITTED_FORMAT_IMAGES = permittedFormatImages;
    }

    /**
     * Метод для проверки разрешенных форматов изображений
     * @param imageName имя изображения
     * @return валидность формата изображения
     */
    public static Boolean isValidImage(String imageName) {
        log.info(TEXT_START_WORK.formatted("login"));
        for (String format : PERMITTED_FORMAT_IMAGES) {
            if (imageName!= null && imageName.toUpperCase().endsWith(format.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
