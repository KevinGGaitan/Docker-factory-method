/*
* Asignatura: Patrones de Diseño de Software
 * Patròn Creacional - > Factory Method
 */

package utilidades;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author Fabrizio Bolaño
 */
public class UtilidadesAcceso {
    public static Properties loadProperty(String propertiesURL){
        try {
            Properties properties = new Properties();
            InputStream inputStream = UtilidadesAcceso.class.getClassLoader().getResourceAsStream(propertiesURL);
            if (inputStream == null) {
                throw new IllegalArgumentException("No se encontró el recurso " + propertiesURL);
            }
            try (inputStream) {
                properties.load(inputStream);
            }
            return properties;
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible cargar " + propertiesURL, e);
        }
    }
}
