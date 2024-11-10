package org.image.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailValidatorTest {

    @Test
    void testIsValidEmail_ValidEmail() {
        assertTrue(EmailValidator.isValidEmail("test@example.com"));
        assertTrue(EmailValidator.isValidEmail("user.name+tag+sorting@list.com"));
        assertTrue(EmailValidator.isValidEmail("user-name@mail.ru"));
        assertTrue(EmailValidator.isValidEmail("user_name@bk.com"));
        assertTrue(EmailValidator.isValidEmail("user123@gmail.com"));
    }

    @Test
    void testIsValidEmail_IncorrectEmail() {
        assertFalse(EmailValidator.isValidEmail("plainaddress"));
        assertFalse(EmailValidator.isValidEmail("@missingusername.com"));
        assertFalse(EmailValidator.isValidEmail("username@.com"));
        assertFalse(EmailValidator.isValidEmail("username@.com."));
        assertFalse(EmailValidator.isValidEmail("username@com"));
        assertFalse(EmailValidator.isValidEmail("username@com."));
        assertFalse(EmailValidator.isValidEmail("username@-example.com"));
        assertFalse(EmailValidator.isValidEmail("username@example..com"));
        assertFalse(EmailValidator.isValidEmail("username@.example.com"));
    }
}