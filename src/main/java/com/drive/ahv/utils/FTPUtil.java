package com.drive.ahv.utils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import com.drive.ahv.config.Configuracion;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Clase utilitaria para operaciones relacionadas con FTP utilizando Apache Commons Net FTPClient.
 */
public class FTPUtil {

    private static final Configuracion config = Configuracion.getConfig(); // Instancia de Configuration para acceder a las propiedades

    /**
     * Establece una conexión FTP al servidor configurado.
     * @return Un objeto FTPClient conectado y listo para usar.
     * @throws IOException Si ocurre un error durante la conexión o el login.
     */
    public static FTPClient connectFTP() throws IOException {
        FTPClient clienteFTP = new FTPClient(); // Crea una nueva instancia de FTPClient

        try {
            String host = config.getProperty("ftp.host"); // Obtiene el host del servidor FTP desde la configuración
            String user = config.getProperty("ftp.user"); // Obtiene el usuario FTP desde la configuración
            String password = config.getProperty("ftp.password"); // Obtiene la contraseña FTP desde la configuración
            String directorioRemoto = config.getProperty("ftp.remoteDir"); // Obtiene el directorio remoto FTP desde la configuración

            clienteFTP.connect(host); // Intenta conectar al servidor FTP
            int replyCode = clienteFTP.getReplyCode(); // Obtiene el código de respuesta del servidor tras la conexión
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                // Si el código de respuesta no indica una conexión exitosa
                clienteFTP.disconnect(); // Desconecta el cliente FTP
                throw new IOException("El servidor FTP rechazó la conexión."); // Lanza excepción indicando que el servidor rechazó la conexión
            }

            boolean login = clienteFTP.login(user, password); // Intenta iniciar sesión en el servidor FTP con usuario y contraseña
            if (!login) {
                // Si el login falla
                clienteFTP.disconnect(); // Desconecta el cliente FTP
                throw new IOException("Fallo al iniciar sesión en el servidor FTP."); // Lanza excepción indicando fallo de login
            }

            clienteFTP.enterLocalPassiveMode(); // Configura el modo pasivo local para la transferencia de datos (recomendado para la mayoría de firewalls)
            clienteFTP.setFileType(FTP.BINARY_FILE_TYPE); // Configura el tipo de archivo a binario (para evitar corrupción en transferencias de archivos no texto)

            // Crear el directorio remoto si no existe
            if (!directoryExists(clienteFTP, directorioRemoto)) { // Verifica si el directorio remoto ya existe
                createDirectory(clienteFTP, directorioRemoto); // Si no existe, lo crea
            }

            clienteFTP.changeWorkingDirectory(directorioRemoto); // Cambia el directorio de trabajo al directorio remoto configurado
            System.out.println("Conectado al servidor FTP: " + host); // Mensaje de éxito de conexión
            return clienteFTP; // Devuelve el cliente FTP conectado

        } catch (IOException e) {
            // Captura excepciones de IO durante la conexión FTP
            System.out.println("Error al conectar con el servidor FTP: " + e.getMessage()); // Imprime mensaje de error de conexión
            throw e; // Relanza la excepción para que se maneje en el nivel superior
        }
    }

    /**
     * Sube un archivo local al servidor FTP.
     * @param clienteFTP Cliente FTP conectado.
     * @param rutaLocal Ruta local del archivo a subir.
     * @param nombreArchivoRemoto Nombre que tendrá el archivo en el servidor remoto.
     * @return true si la subida fue exitosa, false en caso contrario.
     */
    public static boolean uploadFile(FTPClient clienteFTP, String rutaLocal, String nombreArchivoRemoto) {
        try (InputStream input = new FileInputStream(rutaLocal)) {
            // Intenta abrir un InputStream para el archivo local
            clienteFTP.storeFile(nombreArchivoRemoto, input); // Sube el archivo al servidor FTP
            System.out.println("Archivo subido: " + rutaLocal + " -> " + nombreArchivoRemoto); // Mensaje de éxito de subida
            return true; // Retorna true indicando éxito
        } catch (IOException e) {
            // Captura excepciones de IO durante la subida del archivo
            System.out.println("Error al subir el archivo " + rutaLocal + ": " + e.getMessage()); // Imprime mensaje de error de subida
            return false; // Retorna false indicando fallo
        }
    }

    /**
     * Elimina un archivo remoto del servidor FTP.
     * @param clienteFTP Cliente FTP conectado.
     * @param nombreArchivoRemoto Nombre del archivo remoto a eliminar.
     * @return true si el borrado fue exitoso, false en caso contrario.
     */
    public static boolean deleteFile(FTPClient clienteFTP, String nombreArchivoRemoto) {
        try {
            boolean deleted = clienteFTP.deleteFile(nombreArchivoRemoto); // Intenta borrar el archivo remoto
            if (deleted) {
                // Si el borrado fue exitoso
                System.out.println("Archivo eliminado: " + nombreArchivoRemoto); // Mensaje de éxito de borrado
            } else {
                System.out.println("Archivo no encontrado o no pudo ser eliminado: " + nombreArchivoRemoto); // Mensaje si el archivo no se encontró o no se pudo borrar
            }
            return deleted; // Retorna true si el borrado fue exitoso, false en caso contrario
        } catch (IOException e) {
            // Captura excepciones de IO durante el borrado del archivo
            System.out.println("Error al eliminar el archivo " + nombreArchivoRemoto + ": " + e.getMessage()); // Imprime mensaje de error de borrado
            return false; // Retorna false indicando fallo
        }
    }

    /**
     * Verifica si un directorio existe en el servidor FTP.
     * @param clienteFTP Cliente FTP conectado.
     * @param ruta Ruta del directorio a verificar.
     * @return true si el directorio existe, false en caso contrario.
     * @throws IOException Si ocurre un error de IO durante la operación.
     */
    public static boolean directoryExists(FTPClient clienteFTP, String ruta) throws IOException {
        try {
            return clienteFTP.changeWorkingDirectory(ruta); // Intenta cambiar al directorio especificado
        } catch (IOException e) {
            // El directorio no existe si changeWorkingDirectory falla (lanza excepción)
            return false; // Retorna false si la excepción indica que el directorio no existe
        } finally {
            // Si el directorio existe, o si hubo un error al intentar acceder (y por lo tanto, no existe),
            // siempre regresa al directorio remoto configurado para mantener el estado del cliente FTP consistente.
            clienteFTP.changeWorkingDirectory(config.getProperty("ftp.remoteDir"));
        }
    }

    /**
     * Crea un directorio en el servidor FTP.
     * @param clienteFTP Cliente FTP conectado.
     * @param ruta Ruta del directorio a crear.
     * @return true si la creación fue exitosa, false en caso contrario.
     */
    public static boolean createDirectory(FTPClient clienteFTP, String ruta) {
        try {
            int reply = clienteFTP.mkd(ruta); // Intenta crear el directorio en el servidor FTP
            boolean created = FTPReply.isPositiveCompletion(reply); // Verifica si el código de respuesta indica éxito
            if (created) {
                // Si la creación fue exitosa
                System.out.println("Directorio creado: " + ruta); // Mensaje de éxito de creación de directorio
            } else {
                System.out.println("El directorio no pudo ser creado: " + ruta); // Mensaje si no se pudo crear el directorio
            }
            return created; // Retorna true si la creación fue exitosa, false en caso contrario
        } catch (IOException e) {
            // Captura excepciones de IO durante la creación del directorio
            System.out.println("Error al crear el directorio " + ruta + ": " + e.getMessage()); // Imprime mensaje de error de creación de directorio
            return false; // Retorna false indicando fallo
        }
    }

    /**
     * Desconecta el cliente FTP del servidor.
     * @param clienteFTP Cliente FTP a desconectar.
     */
    public static void disconnectFTP(FTPClient clienteFTP) {
        try {
            if (clienteFTP.isConnected()) {
                // Si el cliente FTP está conectado
                clienteFTP.logout(); // Cierra la sesión FTP (logout)
                clienteFTP.disconnect(); // Desconecta del servidor FTP
                System.out.println("Desconectado del servidor FTP."); // Mensaje de desconexión
            }
        } catch (IOException e) {
            // Captura excepciones de IO durante la desconexión
            System.out.println("Error al desconectar del servidor FTP: " + e.getMessage()); // Imprime mensaje de error de desconexión
        }
    }
}