package org.image.core.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

import static org.image.core.dto.model.TextConstant.TEXT_START_WORK;

@Slf4j
public class PasswordValidator {
    /**
     * Пароль должен состоять не менее чем 8 символов, хотя бы одна заглавная буква, цифра и спец.символ.
     */
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static boolean isValidPassword(String password) {
        log.info(TEXT_START_WORK.formatted("login"));
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
