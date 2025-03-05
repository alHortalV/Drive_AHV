# DriveAHV - Sincronización de Archivos con FTP y Cifrado AES

DriveAHV es una aplicación Java que sincroniza archivos locales con un servidor FTP, utilizando cifrado AES para proteger la información. Permite la monitorización de cambios en el directorio local y la sincronización automática con el servidor FTP.

## Funcionalidades Principales

* **Sincronización Automática:** Monitoriza un directorio local y sincroniza automáticamente los cambios con un servidor FTP.
* **Cifrado AES:** Cifra los archivos antes de subirlos al servidor FTP para asegurar la confidencialidad.
* **Gestión de Historial:** Mueve versiones anteriores de los archivos al historial antes de sobrescribirlos.
* **Configuración:** Utiliza un archivo `config.properties` para gestionar las configuraciones de la aplicación.
* **Descarga y Descifrado:** Permite descargar archivos cifrados desde el servidor FTP y descifrarlos localmente.

## Clases Principales

### `com.drive.ahv.config.Configuracion`

* **Propósito:** Gestiona la carga y el acceso a las propiedades de configuración desde el archivo `config.properties`. Implementa el patrón Singleton para asegurar una única instancia.
* **Métodos Principales:**
    * `getConfig()`: Obtiene la instancia única de la clase `Configuracion`.
    * `getProperty(String key)`: Obtiene el valor de una propiedad por su clave.

### `com.drive.ahv.sync.MonitorDeArchivos`

* **Propósito:** Monitoriza un directorio local para detectar cambios en los archivos (creación, modificación, eliminación) y dispara la sincronización con el servidor FTP.
* **Métodos Principales:**
    * `MonitorDeArchivos(Sincronizacion syncService)`: Inicializa el monitor de archivos con el servicio de sincronización.
    * `run()`: Inicia el bucle de monitorización de archivos.
    * `stopMonitor()`: Detiene el monitor de archivos.

### `com.drive.ahv.sync.Sincronizacion`

* **Propósito:** Realiza la sincronización de archivos entre el directorio local y el servidor FTP, incluyendo el cifrado y descifrado AES.
* **Métodos Principales:**
    * `initialSynchronize()`: Realiza la sincronización inicial de todos los archivos en el directorio local.
    * `synchronizeFile(File archivo, String tipoEvento)`: Sincroniza un archivo individual con el servidor FTP.
    * `deleteRemoteFile(String remoteFilename)`: Elimina un archivo remoto del servidor FTP.
    * `downloadFileFromFTP(String remoteFilename)`: Descarga un archivo desde el servidor FTP.
    * `decryptAndSaveFile(String encryptedFilePath, String decryptedFilePath)`: Descifra un archivo y lo guarda localmente.

### `com.drive.ahv.utils.AESUtil`

* **Propósito:** Proporciona utilidades para cifrar y descifrar datos utilizando el algoritmo AES.
* **Métodos Principales:**
    * `encrypt(byte[] data, String key)`: Cifra datos utilizando AES.
    * `decrypt(byte[] data, String key)`: Descifra datos utilizando AES.

### `com.drive.ahv.utils.FileUtil`

* **Propósito:** Proporciona utilidades para operaciones con archivos, como la detección de archivos de texto y la generación de nombres de archivo versionados.
* **Métodos Principales:**
    * `isTextFile(String rutaArchivo)`: Determina si un archivo es un archivo de texto.
    * `generateVersionedFilename(String NombreArchivo, int version)`: Genera un nombre de archivo con versión.

### `com.drive.ahv.utils.FTPUtil`

* **Propósito:** Proporciona utilidades para operaciones con FTP, como la conexión, subida, descarga y eliminación de archivos.
* **Métodos Principales:**
    * `connectFTP()`: Establece una conexión con el servidor FTP.
    * `uploadFile(FTPClient clienteFTP, String rutaLocal, String nombreArchivoRemoto)`: Sube un archivo al servidor FTP.
    * `deleteFile(FTPClient clienteFTP, String nombreArchivoRemoto)`: Elimina un archivo del servidor FTP.
    * `directoryExists(FTPClient clienteFTP, String ruta)`: verifica si un directorio existe dentro del servidor FTP.
    * `createDirectory(FTPClient clienteFTP, String ruta)`: Crea un directorio en el servidor FTP.
    * `disconnectFTP(FTPClient clienteFTP)`: Desconecta el cliente FTP del servidor.

### `com.drive.ahv.utils.HistoryUtil`

* **Propósito:** Gestiona el historial de archivos en el servidor FTP, incluyendo la creación del directorio de historial y el movimiento de archivos al mismo.
* **Métodos Principales:**
    * `createHistoryDirectory(FTPClient clienteFTP)`: Crea el directorio de historial en el servidor FTP.
    * `moveFileToHistory(FTPClient clienteFTP, String nombreArchivo)`: Mueve un archivo al directorio de historial.

### `com.drive.ahv.Main`

* **Propósito:** Clase principal de la aplicación. Inicia la sincronización inicial, el monitor de archivos y maneja la entrada del usuario para detener la aplicación o descargar archivos.
* **Métodos Principales:**
    * `main(String[] args)`: Punto de entrada de la aplicación.

## Uso Básico

1.  **Configuración:**
    * Crea un archivo `config.properties` en el classpath con las configuraciones del servidor FTP, directorio local, clave AES y directorio de historial.
2.  **Ejecución:**
    * Ejecuta la clase `com.drive.ahv.Main` para iniciar la aplicación.
3.  **Comandos:**
    * Escribe `stop` para detener la aplicación.
    * Escribe `descargar` para descargar y descifrar un archivo desde el servidor FTP.
