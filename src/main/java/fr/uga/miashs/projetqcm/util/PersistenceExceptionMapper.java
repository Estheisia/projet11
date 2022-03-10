package fr.uga.miashs.projetqcm.util;

import javax.transaction.TransactionalException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.util.Map;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<TransactionalException> {

    /**
     * Méthode qui permet de "fouiller" dans la pile d'exception et de trouver des erreurs
     * spécifique à SQL qui ne sont pas gérées par des PersistenceException spécifiques.
     * C'est utile par exxemple pour les violations de contraintes d'unicité.
     *
     * @param e : l'erreur à analyser
     * @return un texte de l'erreur
     */
    private static String analyseSQLException(Exception e) {
        Throwable t = e.getCause();
        while (t != null) {
            if (t instanceof SQLException) {
                SQLException sqlEx = (SQLException) t;
                if ("23505".equals(sqlEx.getSQLState())) {
                    return "Valeur dupliquée";
                }
            }
            t = t.getCause();
        }
        return e.getMessage();
    }


    @Override
    public Response toResponse(TransactionalException e) {
        Map<String, Object> errorObj = Map.of(
                "type", "https://example.net/validation-error",
                "title", "Your request object didn't validate.",
                "detail", analyseSQLException(e));

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorObj).type(MediaType.APPLICATION_JSON)
                .build();
    }
}
