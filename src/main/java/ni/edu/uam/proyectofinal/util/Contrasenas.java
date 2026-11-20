package ni.edu.uam.proyectofinal.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Las contraseñas nunca se guardan en texto plano: se guarda un hash PBKDF2 con sal aleatoria.
 * Formato guardado: {@code pbkdf2$iteraciones$sal$hash} (sal y hash en Base64).
 */
public final class Contrasenas {

    private static final String PREFIJO = "pbkdf2";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Contrasenas() {
    }

    /** Devuelve el hash que se guarda en la base de datos. */
    public static String hashear(String contrasena) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /** true si la contraseña escrita corresponde al hash guardado. */
    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null || !estaHasheada(guardado)) {
            return false;
        }
        String[] partes = guardado.split("\\$");
        int iteraciones = Integer.parseInt(partes[1]);
        byte[] sal = Base64.getDecoder().decode(partes[2]);
        byte[] esperado = Base64.getDecoder().decode(partes[3]);
        // Comparación en tiempo constante para no dar pistas por el tiempo de respuesta
        return MessageDigest.isEqual(esperado, pbkdf2(contrasena, sal, iteraciones));
    }

    public static boolean estaHasheada(String valor) {
        return valor != null && valor.startsWith(PREFIJO + "$") && valor.split("\\$").length == 4;
    }

    private static byte[] pbkdf2(String contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("El sistema no soporta PBKDF2WithHmacSHA256", e);
        } finally {
            spec.clearPassword();
        }
    }
}
