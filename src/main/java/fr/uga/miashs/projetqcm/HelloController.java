package fr.uga.miashs.projetqcm;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Classe "EndPoint" fournissant un service accessible via le chemin "data/hello" et la méthode HTTP GET.
 * Ce service retourne seulement la chaîne de caractères "Hello" au client.
 */
@Path("/hello")
@Singleton
public class HelloController {


    @GET
    public String sayHello() {
        return "Hello MIASHS ";
    }

}
