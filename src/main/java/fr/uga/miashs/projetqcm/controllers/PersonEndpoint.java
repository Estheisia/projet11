package fr.uga.miashs.projetqcm.controllers;

import fr.uga.miashs.projetqcm.model.Person;
import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.microprofile.jwt.Claim;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.security.enterprise.identitystore.PasswordHash;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;

@Path("/persons")
@RequestScoped
public class PersonEndpoint {

    @Inject
    private PasswordHash hashAlgo;

    @PersistenceContext(unitName = "cours3PU")
    private EntityManager em;

    @Inject
    @Claim("sub")
    private String email;

    public PersonEndpoint() {
    }

    // Ne peut pas etre géré par les transactions
    @PostConstruct
    private void init() {
        //Person p = new Person("toto","toto@dudule.org", LocalDate.of(2010,03,01));
        //addPerson(p);
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response readAll() {
        TypedQuery<Person> q = em.createQuery("SELECT p FROM Person p", Person.class);
        return Response.ok(q.getResultList()).build();
    }

    @GET
    @Path("/{id: [0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response read(@PathParam("id") long id) {
        Person p = em.find(Person.class, id);
        if (p == null) throw new NotFoundException();
        return Response.ok(p).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid Person p, @Context UriInfo uriInfo) {

        try {
            if (p.getPassword() != null) {
                p.setPasswordHash(hashAlgo.generate(p.getPassword().toCharArray()));
                // pour ne pas renvoyer le password ans la réponse
                p.setPassword(null);
            } else {
                // pour etre sur que l'on enregistre pas un mauvais hash
                p.setPasswordHash(null);
            }
            // Enregistre l'instance auprès de JPA
            em.persist(p);
            // Envoie les modifs à la base de données
            // ce n'est pas obligatoire, mais si il y a un soucis, alors l'exception sera levée après
            em.flush();

            // on construit l'URI correspondant à la personne
            URI location = uriInfo.getRequestUriBuilder()
                    .path(String.valueOf(p.getId()))
                    .build();
            // On retourne la réponse (la méthode entity(p) permet de retourner le json de l'instance ajoutée
            // ce n'est pas forcément nécessaire... ca surcharge le réseau pour pas grand chose
            return Response.created(location).entity(p).build();
        } catch (PersistenceException e) {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id: [0-9]+}")
    public Response updateFull(@PathParam("id") long id, @Valid Person p, @Context UriInfo uriInfo) {
        // On remet l'id à la valeur de la ressource modifiée
        p.setId(id);
        em.merge(p);
        // On retourne la réponse (idem p n'est pas nécessaire..)
        //return Response.ok(p).build();
        return Response.ok().build();
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id: [0-9]+}")
    public Response updatePartial(@PathParam("id") long id, Map<String, Object> upd, @Context UriInfo uriInfo) {
        // on enleve le mapping id (si il y est) pour ne pas changer l'id de la personne
        upd.remove("id");
        //Person p = data.get(id);
        Person p = em.find(Person.class, id);
        if (p == null) throw new NotFoundException();
        try {
            // méthode fournie par la librairie apache commons-beanutils
            // permet de copier les données d'un map dans un java bean
            // par contre aucune verif de validation ne peut etre faite... a modifier
            BeanUtils.populate(p, upd);
            em.merge(p);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return Response.serverError().entity(e).build();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id: [0-9]+}")
    public Response delete(@PathParam("id") long id) {
        // Solution JPA
        // Problème, si l'instance n'existe pas, EntityNotFoundException n'est pas levée (avec EclipseLink)
        //em.remove(em.getReference(Person.class, id));
        Person p = em.find(Person.class, id);
        if (p != null) {
            em.remove(p);
        } else {
            throw new NotFoundException();
        }
        return Response.noContent().build();

        // Solution avec requete native
        /*Query q = em.createNativeQuery("DELETE FROM Person WHERE id=?id");
        q.setParameter("id",id);
        int nb = q.executeUpdate();
        // si on utilise le cache 2nd niveau
        em.getEntityManagerFactory().getCache().evict(Person.class);
        if  (nb>0) {
            return Response.noContent().build();
        }
        throw new NotFoundException();*/
    }
}
