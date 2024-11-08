package org.image.core.util;

import java.util.List;

public class ImageNameBuilder {
    public static String buildSuccessImageDownload(List<String> images) {
        StringBuilder sb = new StringBuilder();
        sb.append(images.getFirst());
        if (images.size() > 1) {
            for (int i = 1; i < images.size(); i++) {
                sb.append(",");
                sb.append(images.get(i));
            }
        }
        sb.append(".");
        return String.valueOf(sb);
    }
}
