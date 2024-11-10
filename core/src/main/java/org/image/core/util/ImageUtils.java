package org.image.core.util;

import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.model.FileInfo;

import java.util.List;

import static org.image.core.dto.model.TextConstant.TEXT_START_WORK;

@Slf4j
public class ImageUtils {

    public static String getNameImagesList(List<String> images) {
        StringBuilder sb = new StringBuilder();
        sb.append(images.getFirst());
        if (images.size() > 1) {
            for (int i = 1; i < images.size(); i++) {
                sb.append(", ");
                sb.append(images.get(i));
            }
        }
        sb.append(".");
        return String.valueOf(sb);
    }

    /**
     * Метод перевода размера файла в Мегабайты
     * @param fileInfo информация о файлах
     * @return значение размера в Мегабайтах
     */
    public static double getSizeInMb(FileInfo fileInfo) {
        log.info(TEXT_START_WORK.formatted("login"));
        return (double) fileInfo.filesSize() / (1024 * 1024);
    }
}
