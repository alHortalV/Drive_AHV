package com.drive.ahv.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuracion {
    private static Configuracion config; // Instancia única de la clase Configuration (Singleton)
    private final Properties propiedades; // Objeto para almacenar las propiedades de configuración

    /**
     * Constructor privado para asegurar el patrón Singleton.
     * Carga la configuración desde el archivo config.properties.
     */
    private Configuracion() {
        propiedades = new Properties(); // Inicializa el objeto Properties
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            // Intenta cargar el archivo config.properties desde el classpath
            if (input == null) {
                System.err.println("Error: config.properties no encontrado.");
                System.exit(1);
                return;
            }
            propiedades.load(input); // Carga las propiedades desde el InputStream
            System.out.println("config.properties cargado con éxito");

        } catch (IOException ex) {
            System.err.println("Error loading configuration file: " + ex.getMessage());
            System.exit(1);
        }
    }

    /**
     * Método estático sincronizado para obtener la instancia única de Configuration (Singleton).
     * Crea la instancia si no existe, o devuelve la instancia existente.
     * @return La instancia única de Configuration.
     */
    public static synchronized Configuracion getConfig() {
        if (config == null) {
            // Si la instancia no existe, la crea
            config = new Configuracion();
        }
        return config;
    }

    /**
     * Obtiene una propiedad de configuración por su clave.
     * @param key La clave de la propiedad a obtener.
     * @return El valor de la propiedad, o null si la clave no se encuentra.
     */
    public String getProperty(String key) {
        String value = propiedades.getProperty(key); // Intenta obtener la propiedad por su clave
        if (value == null) {
          
            System.err.println("Propiedad '" + key + "'no encontrada en config.properties");
        }
        return value;
    }
}