package fr.uga.miashs.projetqcm.util;

import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private String getPropertyName(Path path) {
        String ps = path.toString();
        return ps.substring(ps.lastIndexOf('.') + 1);
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        // construit un map represant l'erreur.
        // utilise le style "fonctionnel" de java 9
        Map<String, Object> errorObj = Map.of(
                "type", "https://example.net/validation-error",
                "title", "Your request object didn't validate.",
                "invalid-params", exception.getConstraintViolations().stream().map(e -> Map.of(
                        "name", getPropertyName(e.getPropertyPath()),
                        "reason", e.getMessage()
                )).toArray());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorObj).type(MediaType.APPLICATION_JSON)
                .build();
    }
}


