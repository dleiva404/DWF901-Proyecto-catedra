package com.permisos.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 600000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(String clave) {
        return hash(clave, ITERACIONES);
    }

    static String hash(String clave, int iteraciones) {
        if (clave == null) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);

        byte[] resultado = derivar(clave, sal, iteraciones);

        return PREFIJO + "$" + iteraciones + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(resultado);
    }

    public static boolean esHash(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO + "$");
    }

    public static boolean verificar(String clave, String guardado) {
        if (clave == null || !esHash(guardado)) {
            return false;
        }

        try {
            String[] partes = guardado.split("\\$");

            if (partes.length != 4) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);

            byte[] calculado = derivar(clave, sal, iteraciones);

            return MessageDigest.isEqual(calculado, esperado);

        } catch (IllegalArgumentException ex) {
            // Valor guardado dañado: nunca se acepta.
            return false;
        }
    }

    public static boolean coincideTextoPlano(String clave, String guardado) {
        if (clave == null || guardado == null) {
            return false;
        }

        return MessageDigest.isEqual(
                clave.getBytes(StandardCharsets.UTF_8),
                guardado.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean necesitaActualizar(String guardado) {
        if (!esHash(guardado)) {
            return true;
        }

        try {
            String[] partes = guardado.split("\\$");
            return partes.length != 4
                    || Integer.parseInt(partes[1]) < ITERACIONES;

        } catch (NumberFormatException ex) {
            return true;
        }
    }

    public static void simularVerificacion(String clave) {
        derivar(clave == null ? "" : clave, new byte[BYTES_SAL], ITERACIONES);
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(
                clave.toCharArray(), sal, iteraciones, BITS_HASH);

        try {
            return SecretKeyFactory.getInstance(ALGORITMO)
                    .generateSecret(especificacion)
                    .getEncoded();

        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash de la contraseña.", ex);

        } finally {
            especificacion.clearPassword();
        }
    }
}