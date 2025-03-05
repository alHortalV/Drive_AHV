package com.drive.ahv.sync;

import com.drive.ahv.config.Configuracion;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class MonitorDeArchivos implements Runnable { 

    private static final Configuracion config = Configuracion.getConfig(); // Instancia de Configuration
    private static final String LOCAL_DIR = config.getProperty("local.dir"); // Directorio local a monitorizar
    private WatchService watcher; // Servicio de vigilancia de archivos de Java
    private Path dir; // Ruta del directorio a monitorizar
    private Sincronizacion syncService; // Servicio de sincronización que usará el monitor

    /**
     * Constructor de FileMonitor.
     * Inicializa el servicio de vigilancia de archivos para el directorio local configurado.
     * @param syncService Instancia de AdvancedSync para realizar la sincronización.
     * @throws IOException Si ocurre un error al inicializar el WatchService o registrar el directorio.
     */
    public MonitorDeArchivos(Sincronizacion syncService) throws IOException {
        this.syncService = syncService; // Guarda la instancia de AdvancedSync
        this.dir = Paths.get(LOCAL_DIR); // Obtiene la ruta del directorio local
        this.watcher = FileSystems.getDefault().newWatchService(); // Crea un nuevo WatchService
        // Registra el directorio para monitorizar eventos de creación, modificación y borrado de entradas (archivos o directorios)
        dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
    }

    /**
     * Método run, implementado de la interfaz Runnable.
     * Inicia el proceso de monitorización de archivos en un bucle infinito.
     */
    @Override
    public void run() {
        System.out.println("Monitor de archivos iniciado para el directorio: " + LOCAL_DIR);
        try {
            startMonitoring(); // Llama al método que contiene el bucle de monitorización principal
        } catch (IOException | InterruptedException e) {
            if (e instanceof ClosedWatchServiceException) {
                System.out.println("Servicio de vigilancia del monitor de archivos cerrado.");
            } else {
                System.err.println("Error durante la monitorización de archivos: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        } finally {
            System.out.println("Hilo del monitor de archivos finalizado.");
        }
    }

    /**
     * Inicia el bucle principal de monitorización de archivos.
     * Espera por eventos de archivos y los procesa.
     * @throws IOException Si ocurre un error de entrada/salida durante la monitorización.
     * @throws InterruptedException Si el hilo es interrumpido mientras espera por un evento.
     */
    private void startMonitoring() throws IOException, InterruptedException {
        while (true) { 
            WatchKey key;
            try {
                key = watcher.take(); // Espera y recupera la próxima clave de vigilancia (bloqueante hasta que haya un evento)
            } catch (InterruptedException ex) {
                System.err.println("Servicio de vigilancia interrumpido: " + ex.getMessage());
                Thread.currentThread().interrupt(); // Re-interrumpe el hilo
                return;
            } catch (ClosedWatchServiceException e) {
                System.out.println("Servicio de vigilancia cerrado, deteniendo bucle del monitor.");
                return;
            }

            // Itera sobre cada evento pendiente para la clave
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind(); // Obtiene el tipo de evento
                if (kind == OVERFLOW) {
                    continue; // Si el evento es OVERFLOW, lo ignora y continua (puede ocurrir si hay demasiados eventos)
                }
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path nombreArchivo = ev.context(); // Obtiene el nombre del archivo que generó el evento
                Path hijo = dir.resolve(nombreArchivo); // Resuelve la ruta completa del archivo en el directorio monitorizado

                if (!Files.isDirectory(hijo)) {
                    try {
                        if (kind == ENTRY_CREATE) {
                            // Evento de creación de archivo
                            System.out.println("Archivo creado: " + hijo); 
                            syncService.synchronizeFile(hijo.toFile(), "created"); // Llama al servicio de sincronización para el archivo creado
                        } else if (kind == ENTRY_MODIFY) {
                            // Evento de modificación de archivo
                            System.out.println("Archivo modificado: " + hijo);
                            syncService.synchronizeFile(hijo.toFile(), "modified"); // Llama al servicio de sincronización para el archivo modificado
                        } else if (kind == ENTRY_DELETE) {
                            // Evento de borrado de archivo
                            System.out.println("Archivo borrado: " + hijo); 
                            syncService.deleteRemoteFile(nombreArchivo.toString()); // Llama al servicio para eliminar el archivo remoto
                        }
                    } catch (IOException e) {
                        System.err.println("Error al procesar el evento del archivo: " + e.getMessage());
                    }
                }
            }

            boolean valido = key.reset(); // Resetea la clave para recibir más eventos
            if (!valido) {
                System.out.println("La clave de vigilancia ya no es válida.");
                break;
            }
        }
    }

    /**
     * Detiene el monitor de archivos, cerrando el WatchService.
     * @throws IOException Si ocurre un error al cerrar el WatchService.
     */
    public void stopMonitor() throws IOException {
        if (watcher != null) {
            // Si el WatchService existe (no es null)
            watcher.close(); // Cierra el WatchService, liberando recursos y deteniendo la monitorización
            System.out.println("Monitor de archivos detenido para el directorio: " + LOCAL_DIR); // Mensaje de detención del monitor
        }
    }
}