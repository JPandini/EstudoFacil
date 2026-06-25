package br.com.estudofacil.estudo_facil_api.util;

public final class EmailUtil {

    private EmailUtil() {}

    public static String normalizar(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }
}
