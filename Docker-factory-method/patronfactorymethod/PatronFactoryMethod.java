
package patronfactorymethod;

/*
 * Asignatura: Patrones de Diseño de Software
 * Patròn Creacional - > Factory Method
 */

import java.sql.SQLException;
import java.util.List;

import dao.DAOarticulos;
import entidades.articulos;

/**
 *
 * @author Fabrizio Bolaño
 */
public class PatronFactoryMethod {


    public static void main(String[] args) throws SQLException {

        //Creation of the DAO instance
        DAOarticulos daoarticulos = new DAOarticulos();

        List<articulos> art = daoarticulos.findAllProducts();
        long totalregistros = art.stream()
                .mapToLong(articulo -> articulo.getIdarticulo())
                .max()
                .orElse(0) + 1;
        //Creamos los nuevos productos a registrar
        articulos elementoA = new articulos(totalregistros, "Articulo A"+totalregistros, 120);
        totalregistros=totalregistros+1;
        articulos elementoB = new articulos(totalregistros, "Articulo B"+totalregistros, 130);
        // Product productA = new Product(1L, "Product A", 120);
        //Product productB = new Product(2L, "Product B", 130);


        //Product persist
        if (!daoarticulos.saveProduct(elementoA) || !daoarticulos.saveProduct(elementoB)) {
            throw new SQLException("No se pudieron guardar los artículos de ejemplo.");
        }

        //Create the products
        List<articulos> elementos = daoarticulos.findAllProducts();
        System.out.println("Cantidad Articulos ==> " + elementos.size());
        for(articulos registro : elementos){
            System.out.println(registro);
        }

    }

}
