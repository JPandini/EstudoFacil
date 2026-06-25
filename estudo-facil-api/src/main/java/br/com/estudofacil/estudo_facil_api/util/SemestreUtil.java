package br.com.estudofacil.estudo_facil_api.util;

import java.util.regex.Pattern;

public final class SemestreUtil {

    private static final Pattern FORMATO = Pattern.compile("^(\\d{4})-0?([12])$");

    private SemestreUtil() {}

    public static String normalizar(String semestre) {
        if (semestre == null || semestre.isBlank()) {
            return semestre;
        }
        var matcher = FORMATO.matcher(semestre.trim());
        if (matcher.matches()) {
            return matcher.group(1) + "-" + matcher.group(2);
        }
        return semestre.trim();
    }

    public static boolean isValido(String semestre) {
        if (semestre == null || semestre.isBlank()) {
            return true;
        }
        return FORMATO.matcher(semestre.trim()).matches();
    }
}
