package fr.uga.miashs.projetqcm.controllers;


import fr.uga.miashs.projetqcm.model.Person;
import fr.uga.miashs.projetqcm.storage.GenericJpaDao;
import fr.uga.miashs.projetqcm.storage.PersonDao;

import javax.inject.Inject;
import javax.persistence.EntityGraph;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("/persons2")
public class Person2Endpoint extends GenericRestCRUDController<Long, Person> {

    @Inject
    PersonDao dao;

    @Override
    public GenericJpaDao<Long, Person> getDao() {
        return dao;
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public Response create(Person p, @Context UriInfo uriInfo) {
        return super.create(p, uriInfo);
    }

    @GET
    @Path("/{id: [0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public Person read(@PathParam("id") Long id) {
        // solution avec entitygraph (qui n'empeche pas le N+1 select avec eclipse link)
        return getDao().read(id, (EntityGraph<Person>) getDao().getEm().getEntityGraph("Person.withContacts"));
        // solution avec JPQL qui fait 1 seule requete
        //EntityManager em = getDao().getEm();
        //return em.createQuery("SELECT p FROM Person p LEFT JOIN FETCH p.contacts WHERE p.id=:id",Person.class).setParameter("id",id).getSingleResult();
    }

    @PATCH
    @Path("/{id: [0-9]+}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public Response update(@PathParam("id") Long id, Map<String, Object> upd) {
        return super.update(id, upd);
    }

    @DELETE
    @Path("/{id: [0-9]+}")
    @Override
    public Response delete(@PathParam("id") Long id) {
        return super.delete(id);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    /*public Response listAll() {
        return super.listAll();
    }*/
    // version paginée
    public Response list(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        return super.list(limit, offset);
    }
}
