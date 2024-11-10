package org.image.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

import static org.image.core.dto.model.TextConstant.TEXT_START_WORK;

@Slf4j
public class EmailValidator {
    /**
     * Разрешенный формат email
     */
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]+(-[a-zA-Z0-9]+)*\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        log.info(TEXT_START_WORK.formatted("isValidEmail"));
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
