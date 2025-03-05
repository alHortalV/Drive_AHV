package com.drive.ahv;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;
import com.drive.ahv.config.Configuracion;
import com.drive.ahv.sync.Sincronizacion;
import com.drive.ahv.sync.MonitorDeArchivos;

/**
 * Clase principal de la aplicación de sincronización avanzada.
 * Inicia la sincronización inicial, el monitor de archivos y maneja la entrada del usuario.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Sincronizacion servicioSincronizacion = new Sincronizacion(); // Crea una instancia del servicio de sincronización avanzada
            servicioSincronizacion.initialSynchronize(); // Realiza la sincronización inicial al inicio de la aplicación

            MonitorDeArchivos monitor = new MonitorDeArchivos(servicioSincronizacion); // Crea una instancia del monitor de archivos, pasándole el servicio de sincronización
            Thread hiloMonitor = new Thread(monitor); // Crea un nuevo hilo para ejecutar el monitor de archivos en segundo plano
            hiloMonitor.start(); // Inicia el hilo del monitor

            System.out.println("Aplicación iniciada. Monitorización de archivos activa en segundo plano.");
            System.out.println("Escriba 'stop' para terminar la aplicación.");

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String command = scanner.nextLine();
                if ("stop".equalsIgnoreCase(command)) {
                    monitor.stopMonitor(); // Detiene el monitor de archivos de forma controlada
                    hiloMonitor.interrupt(); // Interrumpe el hilo del monitor para asegurar su finalización
                    Sincronizacion.stop(); // Detiene el servicio FTP
                    break;

                } else if ("descargar".equalsIgnoreCase(command)) {
                    System.out.print("Ingrese el nombre del archivo encriptado a descargar (ej., prueba.txt.enc): ");
                    String NombreArchivoEncriptadoADescargar = scanner.nextLine();
                    if (servicioSincronizacion.downloadFileFromFTP(NombreArchivoEncriptadoADescargar)) {
                        Configuracion config = Configuracion.getConfig(); // Obtiene la instancia de Configuration para acceder a las propiedades
                        String rutaFicheroDescargado = Paths
                                .get(config.getProperty("local.downloadDir"), NombreArchivoEncriptadoADescargar).toString(); // Construye la ruta completa del archivo descargado
                        String rutaFicheroDesencriptada = Paths.get(config.getProperty("local.downloadDir"),
                                NombreArchivoEncriptadoADescargar.substring(0, NombreArchivoEncriptadoADescargar.length() - 4))
                                .toString(); // Construye la ruta para el archivo descifrado (quitando la extensión .enc)
                        if (servicioSincronizacion.decryptAndSaveFile(rutaFicheroDescargado, rutaFicheroDesencriptada)) {
                            System.out.println("Archivo descargado y descifrado exitosamente en: " + rutaFicheroDesencriptada);
                        } else {
                            System.err.println("Fallo al descifrar el archivo descargado.");
                        }
                    } else {
                        System.err.println("Fallo al descargar el archivo desde FTP.");
                    }
                }
            }
            scanner.close();
            System.out.println("Aplicación terminada.");

        } catch (IOException e) {
            System.err.println("Error al iniciar la aplicación: " + e.getMessage());
        }
    }
}