package org.image.core.util;

import org.image.core.dto.model.ImageInfo;

import java.util.List;

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

    public static double getSizeInMb(ImageInfo imageInfo) {
        return (double) imageInfo.filesSize() / (1024 * 1024);
    }
}
