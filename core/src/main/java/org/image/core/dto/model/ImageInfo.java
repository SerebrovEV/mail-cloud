package org.image.core.dto.model;

import java.util.List;

public record ImageInfo(List<String> successImageNames, List<String> errorsImageNames, long filesSize) {

}
