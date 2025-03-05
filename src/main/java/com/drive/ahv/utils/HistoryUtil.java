package com.drive.ahv.utils;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.drive.ahv.config.Configuracion;

import java.io.IOException;

/**
 * Clase utilitaria para gestionar el historial de archivos en el servidor FTP.
 * Incluye la creación del directorio de historial y el movimiento de archivos al mismo.
 */
public class HistoryUtil {

    private static final Configuracion config = Configuracion.getConfig(); // Instancia de Configuration para acceder a las propiedades

    /**
     * Crea el directorio de historial en el servidor FTP si no existe.
     * El nombre del directorio de historial se obtiene de la configuración (history.dir).
     * El directorio de historial se crea dentro del directorio remoto principal configurado (ftp.remoteDir).
     * @param clienteFTP Cliente FTP conectado.
     * @return true si el directorio de historial fue creado o ya existía, false en caso de error.
     */
    public static boolean createHistoryDirectory(FTPClient clienteFTP) {
        String remoteDir = config.getProperty("ftp.remoteDir"); // Obtiene el directorio remoto configurado
        String nombreDirectorioHistory = config.getProperty("history.dir"); // Obtiene el nombre del directorio de historial configurado
        String directorioHistory = remoteDir + "/" + nombreDirectorioHistory; // Construye la ruta completa del directorio de historial.

        try {
            // Verifica si el directorio de historial ya existe. Si existe, termina el método.
            if (FTPUtil.directoryExists(clienteFTP, nombreDirectorioHistory)) {
                System.out.println("El directorio de historial ya existe: " + directorioHistory);
                return true;
            }
            // Solo crea el directorio si no existe.
            int replyCode = clienteFTP.mkd(nombreDirectorioHistory);
            if (FTPReply.isPositiveCompletion(replyCode)) {
                System.out.println("Directorio de historial creado exitosamente: " + directorioHistory);
                return true;
            } else {
                System.out.println("Fallo al crear el directorio de historial: " + directorioHistory);
                return false;
            }

        } catch (IOException e) {
            System.out.println("Error al crear el directorio de historial: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mueve un archivo al directorio de historial en el servidor FTP.
     * El nombre del directorio de historial se obtiene de la configuración (history.dir).
     * El archivo se mueve dentro del directorio de historial, manteniendo su nombre original.
     * @param clienteFTP Cliente FTP conectado.
     * @param nombreArchivo El nombre del archivo a mover al historial (nombre relativo dentro del directorio remoto principal).
     * @return true si el archivo fue movido exitosamente al historial, false en caso de error.
     */
    public static boolean moveFileToHistory(FTPClient clienteFTP, String nombreArchivo) {
        String historyDirName = config.getProperty("history.dir"); // Obtiene solo el nombre del directorio de historial
        String destinationPath = historyDirName + "/" + nombreArchivo; // Ruta de destino: directorio de historial + nombre del archivo

        try {
            // Verifica si el directorio de historial existe.
            if (!FTPUtil.directoryExists(clienteFTP, historyDirName)) {
                // Si no existe, intenta crearlo
                if (!createHistoryDirectory(clienteFTP)) {
                    System.out.println("Fallo al crear o encontrar el directorio de historial, no se puede mover el archivo."); // Mensaje de error
                    return false;
                }
            }

            // Renombra el archivo al directorio de historial
            boolean renamed = clienteFTP.rename(nombreArchivo, destinationPath);
            if (renamed) {
                System.out.println("Archivo movido al historial: " + nombreArchivo + " -> " + destinationPath);
                return true; 
            } else {
                System.out.println("Fallo al mover el archivo al historial: " + nombreArchivo + " -> " + destinationPath);
                return false;
            }
        } catch (IOException e) {
            System.out.println("Error al mover el archivo al historial: " + e.getMessage());
            return false;
        }
    }
}