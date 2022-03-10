package fr.uga.miashs.projetqcm.controllers;

import fr.uga.miashs.projetqcm.storage.GenericJpaDao;

import javax.persistence.PersistenceException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

/**
 * Controlleur REST générique qui fourni les méthodes CRUD de base + findAll paginé
 *
 * @param <K> type de la clé
 * @param <T> type de l'entité
 */
public abstract class GenericRestCRUDController<K, T> {

    public abstract GenericJpaDao<K, T> getDao();

    public Response create(T obj, UriInfo uriInfo) {
        try {
            K id = getDao().create(obj);
            URI u = uriInfo.getRequestUriBuilder().path(String.valueOf(id)).build();
            return Response.created(u).build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    public T read(K id) {
        return getDao().read(id);
    }

    public Response update(K id, Map<String, Object> upd) {
        try {
            getDao().updatePartial(id, upd);
            return Response.ok().build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    public Response delete(K id) {
        if (getDao().delete(id)) {
            return Response.noContent().build();
        }
        throw new NotFoundException();
        //return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Response listAll() {
        return Response.ok(getDao().listAll()).build();
    }

    /**
     * Méthode retournant une liste (paginée) des entités.
     *
     * @param limit  le nombre max d'entités à retourner
     * @param offset la position de la prmière entité à retourner
     * @return
     */
    public Response list(int limit, int offset) {
        return Response.ok(getDao().list(limit, offset)).build();
    }

}
