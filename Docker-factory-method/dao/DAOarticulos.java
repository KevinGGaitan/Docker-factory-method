package dao;

/*
 * Asignatura: Patrones de Diseño de Software
 * Patròn Creacional - > Factory Method
 * DAO-> Data Access Object
 * Còdigo para configurar el acceso a las tablas de la Base de datos (Mysql/Postgresql)
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import entidades.articulos;
import patronfactorymethod.IntAdaptador;
import patronfactorymethod.FactoryBaseDatos;

/**
 *
 * @author Fabrizio Bolaño
 */
public class DAOarticulos {
     private IntAdaptador dbAdapter;


     public DAOarticulos(){
        dbAdapter = FactoryBaseDatos.getDefaultDBAdapter();
    }

    public List<articulos> findAllProducts() throws SQLException {
        List<articulos> productList = new ArrayList<>();
        try (Connection connection = dbAdapter.getConnection();
             PreparedStatement statement = connection
                    .prepareStatement("SELECT idarticulo ,nombrearticulo"
                            + ",valor FROM articulos");
             ResultSet results = statement.executeQuery()) {
            while(results.next()){
                productList.add(new articulos(results.getLong(1),
                        results.getString(2), results.getDouble(3)));
            }
            return productList;
        }
    }

    public boolean saveProduct(articulos articulo){
        try (Connection connection = dbAdapter.getConnection();
             PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO articulos(idarticulo,"
                            + "nombrearticulo,valor) VALUES (?,?,?)")) {
            statement.setLong(1, articulo.getIdarticulo());
            statement.setString(2, articulo.getNombrearticulo());
            statement.setDouble(3, articulo.getValor());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException("No fue posible guardar el artículo " + articulo.getIdarticulo(), e);
        }
    }

}
