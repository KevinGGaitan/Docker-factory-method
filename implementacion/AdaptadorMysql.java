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
public class AdaptadorMysql implements IntAdaptador  {

    private static final String DB_PROPERTIES = "Propiedades/DatosMysql.properties";

    //Propiedades del archivo de configuraciòn para Mysql
    private static final String DB_NAME_PROP = "dbname";
    private static final String DB_HOST_PROP = "host";
    private static final String DB_PASSWORD_PROP = "password";
    private static final String DB_PORT_PROP = "port";
    private static final String DB_USER_PROP = "user";

    static {
        //Bloque para registrar el Driver de MySQL
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            String connectionString = createConnectionString();
            return DriverManager.getConnection(connectionString);
        } catch (Exception e) {
            throw new SQLException("No fue posible conectar con MySQL. Revise Propiedades/DatosMysql.properties", e);
        }
    }

    private String createConnectionString() {
        Properties prop = UtilidadesAcceso.loadProperty(DB_PROPERTIES);
        String host = setting(prop, DB_HOST_PROP, "MYSQL_HOST");
        String port = setting(prop, DB_PORT_PROP, "MYSQL_PORT");
        String db = setting(prop, DB_NAME_PROP, "MYSQL_DATABASE");
        String user = setting(prop, DB_USER_PROP, "MYSQL_USER");
        String password = setting(prop, DB_PASSWORD_PROP, "MYSQL_PASSWORD");

        String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + db + "?user=" + user + "&password=" + password + "&allowPublicKeyRetrieval=true&sslMode=DISABLED";
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
