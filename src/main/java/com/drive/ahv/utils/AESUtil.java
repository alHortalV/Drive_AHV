package com.drive.ahv.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Clase utilitaria para operaciones de cifrado y descifrado AES.
 */
public class AESUtil {
    private static final String AES_ALGORITMO = "AES"; // Algoritmo AES que se va a usar

    /**
     * Cifra datos utilizando el algoritmo AES y una clave proporcionada.
     * @param data Los datos a cifrar en formato byte array.
     * @param key La clave de cifrado como String. Debe tener una longitud de 16, 24 o 32 bytes.
     * @return Los datos cifrados en formato byte array.
     * @throws Exception Si ocurre algún error durante el proceso de cifrado.
     */
    public static byte[] encrypt(byte[] data, String key) throws Exception {
        SecretKeySpec KeySecreta = generateKey(key); // Genera una clave secreta a partir del String clave
        Cipher cipher = Cipher.getInstance(AES_ALGORITMO); // Obtiene una instancia de Cipher para el algoritmo AES
        cipher.init(Cipher.ENCRYPT_MODE, KeySecreta); // Inicializa el Cipher en modo de cifrado con la clave secreta
        byte[] datoEncriptado = cipher.doFinal(data); // Realiza el cifrado de los datos
        System.out.println("Datos cifrados exitosamente.");
        return datoEncriptado;
    }

    /**
     * Descifra datos cifrados utilizando el algoritmo AES y una clave proporcionada.
     * @param data Los datos cifrados a descifrar en formato byte array.
     * @param key La clave de descifrado (debe ser la misma que se usó para cifrar) como String.
     * @return Los datos descifrados en formato byte array.
     * @throws Exception Si ocurre algún error durante el proceso de descifrado.
     */
    public static byte[] decrypt(byte[] data, String key) throws Exception {
        SecretKeySpec llaveSecreta = generateKey(key); // Genera una clave secreta a partir del String clave
        Cipher cipher = Cipher.getInstance(AES_ALGORITMO); // Obtiene una instancia de Cipher para el algoritmo AES
        cipher.init(Cipher.DECRYPT_MODE, llaveSecreta); // Inicializa el Cipher en modo de descifrado con la clave secreta
        byte[] datoEncriptado = cipher.doFinal(data); // Realiza el descifrado de los datos
        System.out.println("Datos descifrados exitosamente.");
        return datoEncriptado;
    }

    /**
     * Genera una SecretKeySpec a partir de un String clave para el algoritmo AES.
     * Valida que la longitud de la clave sea válida (16, 24 o 32 bytes).
     * @param key El String clave para generar la SecretKeySpec.
     * @return La SecretKeySpec generada.
     * @throws Exception Si la longitud de la clave no es válida.
     */
    private static SecretKeySpec generateKey(String key) throws Exception {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8); // Convierte el String clave a un byte array usando UTF-8
        // Valida la longitud de la clave AES (debe ser 16 bytes para que sea AES-128)
        if (keyBytes.length != 16) {
            throw new IllegalArgumentException("La clave AES debe tener una longitud de 16"); // Lanza excepción si la longitud es incorrecta
        }
        SecretKeySpec llaveSecreta = new SecretKeySpec(keyBytes, AES_ALGORITMO); // Crea la SecretKeySpec usando los bytes de la clave y el algoritmo AES
        return llaveSecreta;
    }
}