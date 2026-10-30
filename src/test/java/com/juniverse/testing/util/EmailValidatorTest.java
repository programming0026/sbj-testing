package com.juniverse.testing.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Pruebas parametrizadas con {@code @ValueSource} y {@code @NullAndEmptySource}. */
@DisplayName("EmailValidator")
class EmailValidatorTest {

    @ParameterizedTest(name = "\"{0}\" es válido")
    @ValueSource(strings = {"ana@mail.com", "ana.lopez+tag@sub.dominio.mx", "a_b-c@x.io"})
    void acceptsValidEmails(String email) {
        assertTrue(EmailValidator.isValid(email));
    }

    @ParameterizedTest(name = "\"{0}\" NO es válido")
    @ValueSource(strings = {"sin-arroba.com", "@sin-usuario.com", "ana@", "ana@dominio", "ana @mail.com"})
    void rejectsInvalidEmails(String email) {
        assertFalse(EmailValidator.isValid(email));
    }

    @ParameterizedTest(name = "[{index}] valor nulo o vacío")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("@NullAndEmptySource añade null y \"\" a los casos")
    void rejectsNullAndBlank(String email) {
        assertFalse(EmailValidator.isValid(email));
    }
}
