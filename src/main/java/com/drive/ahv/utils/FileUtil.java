package com.drive.ahv.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Clase utilitaria para operaciones relacionadas con archivos.
 */
public class FileUtil {
    /**
     * Determina si un archivo es probablemente un archivo de texto.
     * Intenta leer el archivo como texto; si tiene éxito sin errores de codificación, se considera un archivo de texto.
     * @param rutaArchivo La ruta al archivo a verificar.
     * @return true si se considera un archivo de texto, false de lo contrario.
     */
    public static boolean isTextFile(String rutaArchivo) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(rutaArchivo)); // Lee todos los bytes del archivo
            new String(bytes); // Intenta crear un String a partir de los bytes (verifica si es texto)
            return true; // Si no hay excepción, se considera un archivo de texto
        } catch (IOException e) {
            // Captura IOException si hay un error al leer el archivo
            System.out.println("Error al leer el archivo " + rutaArchivo + ": " + e.getMessage()); // Imprime mensaje de error de lectura
            return false; // Si hay un error al leer, no es un archivo de texto
        } catch (Exception e) {
            // Captura otras excepciones generales
            System.out.println("Error al procesar el archivo " + rutaArchivo + ": " + e.getMessage()); // Imprime mensaje de error de procesamiento
            return false; // Si hay un error de procesamiento, no es un archivo de texto
        }
    }

    /**
     * Genera un nombre de archivo con versión, añadiendo "_v[versión]" antes de la extensión.
     * Ejemplo: "documento.txt" con versión 2 -> "documento_v2.txt". Si no tiene extensión, añade "_v[versión]" al final.
     * @param NombreArchivo El nombre de archivo base.
     * @param version El número de versión.
     * @return El nombre de archivo con versión.
     */
    public static String generateVersionedFilename(String NombreArchivo, int version) {
        int dotIndex = NombreArchivo.lastIndexOf('.'); // Encuentra el índice del último punto (para separar nombre base y extensión)
        String baseName = (dotIndex == -1) ? NombreArchivo : NombreArchivo.substring(0, dotIndex); // Nombre base: todo antes del punto, o el nombre completo si no hay punto
        String extension = (dotIndex == -1) ? "" : NombreArchivo.substring(dotIndex); // Extensión: desde el punto hasta el final, o cadena vacía si no hay punto
        return baseName + "_v" + version + extension; // Combina nombre base, "_v[versión]" y la extensión para el nombre versionado
    }
}