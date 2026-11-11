/*
 * Asignatura: Patrones de Diseño de Software
 * Patròn Creacional - > Factory Method
 */
package implementacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import patronfactorymethod.IntAdaptador;
import utilidades.UtilidadesAcceso;
/**
 *
 * @author Fabrizio Bolaño
 */
public class AdaptadorPostgresql implements IntAdaptador {

    private static final String DB_PROPERTIES = "Propiedades/DatosPOstgresql.properties";

    private static final String DB_SERVICE_PROP = "service";
    private static final String DB_HOST_PROP = "host";
    private static final String DB_PASSWORD_PROP = "password";
    private static final String DB_PORT_PROP = "port";
    private static final String DB_USER_PROP = "user";

    static {
        //Bloque para registrar el Driver de Oracle
        try {
             Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            Properties prop = UtilidadesAcceso.loadProperty(DB_PROPERTIES);
            String user = setting(prop, DB_USER_PROP, "PG_USER");
            String password = setting(prop, DB_PASSWORD_PROP, "PG_PASSWORD");
            return DriverManager.getConnection(createConnectionString(prop), user, password);
        } catch (Exception e) {
            throw new SQLException("No fue posible conectar con PostgreSQL. Revise Propiedades/DatosPOstgresql.properties", e);
        }
    }


    private String createConnectionString(Properties prop) {
        String host = setting(prop, DB_HOST_PROP, "PG_HOST");
        String port = setting(prop, DB_PORT_PROP, "PG_PORT");
        String service = setting(prop, DB_SERVICE_PROP, "PG_DATABASE");
        String connectionString = "jdbc:postgresql://"+host+":"+port+"/"+service;
        return connectionString;
    }

    private String setting(Properties properties, String key, String environmentVariable) {
        String value = System.getenv(environmentVariable);
        if (value == null || value.isBlank()) {
            value = properties.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta configurar " + environmentVariable);
        }
        return value;
    }


}
