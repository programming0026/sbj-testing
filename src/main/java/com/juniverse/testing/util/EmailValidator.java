package com.juniverse.testing.util;

import java.util.regex.Pattern;

/**
 * Validación de correos. Es una clase de utilidades con métodos estáticos (se prueba con {@code mockStatic}).
 */
public final class EmailValidator {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private EmailValidator() {
    }

    /**
     * Comprueba el formato de un correo.
     *
     * @param email texto a validar
     * @return {@code true} si tiene un formato válido
     */
    public static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }
}
