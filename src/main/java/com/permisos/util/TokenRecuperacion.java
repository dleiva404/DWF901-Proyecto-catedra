package com.permisos.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public final class TokenRecuperacion {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private TokenRecuperacion() {
    }

    public static String generar() {
        byte[] aleatorio = new byte[32];
        ALEATORIO.nextBytes(aleatorio);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio);
    }

    public static String hash(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("El código de recuperación es obligatorio.");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexadecimal = new StringBuilder(digest.length * 2);
            for (byte valor : digest) {
                hexadecimal.append(String.format("%02x", valor & 0xff));
            }
            return hexadecimal.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible.", ex);
        }
    }

    public static boolean coincide(String token, String hashGuardado) {
        if (token == null || hashGuardado == null) {
            return false;
        }
        return MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.US_ASCII),
                hashGuardado.getBytes(StandardCharsets.US_ASCII));
    }
}
