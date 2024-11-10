package org.image.core.dto.model;

import java.util.List;

public record FileInfo(List<String> successFileNames, List<String> errorsFileNames, long filesSize) {

}
