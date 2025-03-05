package com.drive.ahv.sync;

import com.drive.ahv.config.Configuracion;
import com.drive.ahv.utils.AESUtil;
import com.drive.ahv.utils.FTPUtil;
import com.drive.ahv.utils.HistoryUtil;
import org.apache.commons.net.ftp.FTPClient;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Sincronizacion {

    private static final Configuracion config = Configuracion.getConfig();
    private static final String LOCAL_DIR = config.getProperty("local.dir");
    private static final String AES_KEY = config.getProperty("aes.key");
    private static final String LOCAL_DOWNLOAD_DIR = config.getProperty("local.downloadDir");
    private static final String LOCAL_ENCRYPTED_DIR = config.getProperty("local.encryptedDir");

    public Sincronizacion() {
        // Asegurar que el directorio de encriptados local existe al inicio
        Path RutaDirectorioEncriptada = Paths.get(LOCAL_ENCRYPTED_DIR);
        if (!Files.exists(RutaDirectorioEncriptada)) {
            try {
                Files.createDirectories(RutaDirectorioEncriptada);
                System.out.println("Directorio local para archivos encriptados creado: " + LOCAL_ENCRYPTED_DIR);
            } catch (IOException e) {
                System.err.println("Error al crear el directorio local para archivos encriptados: " + e.getMessage());
            }
        }
    }

    /** 
     * Realiza la sincronización inicial de los archivos en el directorio local.
     * Carga cada archivo del directorio local al servidor FTP.
     * @throws IOException Si ocurre un error de entrada/salida durante la sincronización.
     */
    public void initialSynchronize() throws IOException {
        FTPClient ftpClient = null;

        try {
            ftpClient = FTPUtil.connectFTP();
            if (ftpClient == null) {
                System.err.println("Fallo al conectar con el servidor FTP. Saliendo.");
                return;
            }

            Path localDirPath = Paths.get(LOCAL_DIR);
            if (!Files.exists(localDirPath)) {
                Files.createDirectories(localDirPath);
                System.out.println("Directorio local creado: " + LOCAL_DIR);
            }

            File localDir = new File(LOCAL_DIR);
            File[] files = localDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        synchronizeFile(file, "initial");
                    }
                }
            } else {
                System.out.println("No se encontraron archivos en el directorio local: " + LOCAL_DIR);
            }

        } catch (Exception e) {
            System.err.println("Error durante la sincronización inicial: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void stop() throws IOException {
        FTPClient ftpClient = FTPUtil.connectFTP();
        FTPUtil.disconnectFTP(ftpClient);
    }
    /**
     * Sincroniza un archivo individual con el servidor FTP.
     * Cifra el contenido del archivo, lo guarda localmente en la carpeta 'encrypted', y lo sube al servidor.
     * Mueve la versión anterior del archivo al historial (si no es sincronización inicial o creación).
     * @param archivo El archivo local a sincronizar.
     * @param tipoEvento El tipo de evento que desencadena la sincronización ("initial", "created", "modified", etc.).
     * @throws IOException Si ocurre un error de entrada/salida durante la sincronización del archivo.
     */
    public void synchronizeFile(File archivo, String tipoEvento) throws IOException {
        FTPClient clienteFTP = null;
        try {
            clienteFTP = FTPUtil.connectFTP();
            if (clienteFTP == null) {
                System.err.println("Fallo al conectar con el servidor FTP para sincronizar el archivo.");
                return;
            }

            String nombreArchivo = archivo.getName();
            String nombreArchivoEncriptado = nombreArchivo + ".enc";
            Path RutaArchivoLocalEncriptado = Paths.get(LOCAL_ENCRYPTED_DIR, nombreArchivoEncriptado); // Ruta local para el archivo encriptado

            // 1. Leer y Encriptar el archivo
            byte[] contenidoArchivo = Files.readAllBytes(archivo.toPath());
            byte[] contenidoEncriptado = AESUtil.encrypt(contenidoArchivo, AES_KEY);

            // 2. Guardar el archivo encriptado LOCALMENTE en la carpeta 'encriptados'
            Files.write(RutaArchivoLocalEncriptado, contenidoEncriptado);
            System.out.println("Archivo encriptado guardado localmente: " + RutaArchivoLocalEncriptado);


            // 3. Subir el archivo ENCRIPTADO (desde la carpeta 'encriptados' local) al servidor FTP
            boolean subidaCorrecta = FTPUtil.uploadFile(clienteFTP, RutaArchivoLocalEncriptado.toString(), nombreArchivoEncriptado); // Subir DESDE la carpeta local 'encriptados'

            if (subidaCorrecta) {
                if (!"initial".equals(tipoEvento) && !"created".equals(tipoEvento)) {
                    HistoryUtil.moveFileToHistory(clienteFTP, nombreArchivoEncriptado);
                    System.out.println("Versión anterior movida al historial para: " + nombreArchivo);
                }
                System.out.println("Archivo sincronizado (" + tipoEvento + "): " + nombreArchivo);
            } else {
                System.err.println("Fallo al subir el archivo: " + nombreArchivoEncriptado);
            }

        } catch (Exception e) {
            System.err.println("Error al sincronizar el archivo: " + archivo.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Elimina un archivo remoto en el servidor FTP.
     * Asume que los archivos remotos tienen la extensión ".enc" (para archivos cifrados).
     * Mueve el archivo eliminado al historial antes de borrarlo.
     * @param remoteFilename El nombre del archivo remoto a eliminar (sin la extensión .enc).
     * @throws IOException Si ocurre un error de entrada/salida durante la operación de borrado.
     */
    public void deleteRemoteFile(String remoteFilename) throws IOException {
        FTPClient ftpClient = null;
        try {
            ftpClient = FTPUtil.connectFTP();
            if (ftpClient == null) {
                System.err.println("Fallo al conectar con el servidor FTP para la operación de borrado.");
                return;
            }

            String encryptedFilename = remoteFilename;
            if (FTPUtil.deleteFile(ftpClient, encryptedFilename)) {
                System.out.println("Archivo remoto eliminado: " + encryptedFilename);
                HistoryUtil.moveFileToHistory(ftpClient, encryptedFilename);
            } else {
                System.out.println("Archivo remoto no encontrado o no pudo ser eliminado: " + encryptedFilename);
            }

        } catch (Exception e) {
            System.err.println("Error al eliminar el archivo remoto: " + remoteFilename + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Descarga un archivo desde el servidor FTP.
     * @param remoteFilename El nombre del archivo remoto a descargar.
     * @return true si la descarga fue exitosa, false en caso contrario.
     */
    public boolean downloadFileFromFTP(String remoteFilename) {
        FTPClient ftpClient = null;
        boolean downloaded = false;
        try {
            ftpClient = FTPUtil.connectFTP();
            if (ftpClient == null) {
                System.err.println("Fallo al conectar con el servidor FTP para la descarga.");
                return false;
            }

            Path downloadDirPath = Paths.get(LOCAL_DOWNLOAD_DIR);
            if (!Files.exists(downloadDirPath)) {
                Files.createDirectories(downloadDirPath);
                System.out.println("Directorio de descarga local creado: " + LOCAL_DOWNLOAD_DIR);
            }
            String localFilePath = Paths.get(LOCAL_DOWNLOAD_DIR, remoteFilename).toString();
            downloaded = downloadFile(ftpClient, remoteFilename, localFilePath);

        } catch (IOException e) {
            System.err.println("Error al descargar el archivo desde FTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return downloaded;
    }

    /**
     * Método privado para realizar la descarga real del archivo FTP.
     * @param ftpClient Cliente FTP conectado.
     * @param remoteFile Nombre del archivo remoto a descargar.
     * @param localFile Ruta local donde guardar el archivo descargado.
     * @return true si la descarga fue exitosa, false en caso contrario.
     */
    private boolean downloadFile(FTPClient ftpClient, String remoteFile, String localFile) {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile))) {
            boolean success = ftpClient.retrieveFile(remoteFile, outputStream);
            if (success) {
                System.out.println("Archivo descargado exitosamente: " + remoteFile + " -> " + localFile);
                return true;
            } else {
                System.err.println("Fallo al descargar el archivo: " + remoteFile);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error al descargar el archivo " + remoteFile + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Descifra un archivo encriptado y guarda la versión descifrada.
     * @param encryptedFilePath Ruta al archivo encriptado.
     * @param decryptedFilePath Ruta donde guardar el archivo descifrado.
     * @return true si el descifrado y guardado fueron exitosos, false en caso contrario.
     */
    public boolean decryptAndSaveFile(String encryptedFilePath, String decryptedFilePath) {
        try {
            byte[] encryptedContent = Files.readAllBytes(Paths.get(encryptedFilePath));
            byte[] decryptedContent = AESUtil.decrypt(encryptedContent, AES_KEY);
            Files.write(Paths.get(decryptedFilePath), decryptedContent);
            System.out.println("Archivo descifrado exitosamente: " + encryptedFilePath + " -> " + decryptedFilePath);
            return true;
        } catch (Exception e) {
            System.err.println("Error al descifrar el archivo " + encryptedFilePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}