package org.image.core.dto.model;

import java.util.List;

public record ImageInfo(List<String> successfulImageNames, List<String> errorsImageNames, long filesSize) {

}
