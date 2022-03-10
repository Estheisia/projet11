package fr.uga.miashs.projetqcm.controllers;

import fr.uga.miashs.projetqcm.dto.ExamDetails;
import fr.uga.miashs.projetqcm.model.Exam;
import fr.uga.miashs.projetqcm.model.Person;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Path("/qcm")

public class ExamEndpoint {

    @PersistenceContext(unitName = "cours3PU")
    private EntityManager em;

    @Inject
    private JsonWebToken callerPrincipal;

    @Inject
    @Claim(value = "upn")
    private String email;

    public ExamEndpoint(){
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @RolesAllowed("teacher")
    public List<ExamDetails> getExam() {
        TypedQuery<Person> qu = em.createNamedQuery("Person.login",Person.class);
        qu.setParameter("email",callerPrincipal.getClaim(Claims.upn));
        Person u = qu.getSingleResult();
        long id = u.getId();
        TypedQuery<ExamDetails> q = em.createQuery(
                "SELECT new "+ExamDetails.class.getName()+"(e.id,e.title,e.debut,e.fin,e.duree,e.idCreator, 0) " +
                        "FROM Exam e WHERE e.idCreator = "+id+"  GROUP BY e.id"
                ,ExamDetails.class);
        return q.getResultList();
    }

    @GET
    @Path("/{id: [0-9]+}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response read(@PathParam("id") long id) {
        Exam e = em.find(Exam.class, id);
        if (e == null) throw new NotFoundException();
        return Response.ok(e).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("teacher")
    @Transactional
    public Response create(@Valid Exam exam, @Context UriInfo uriInfo) {
        try {
            TypedQuery<Person> q = em.createNamedQuery("Person.login",Person.class);
            q.setParameter("email",callerPrincipal.getClaim(Claims.upn));
            Person u = q.getSingleResult();
            long id = u.getId();
            exam.setIdCreator(id);
            em.persist(exam);
            em.flush();
            URI location = uriInfo.getRequestUriBuilder()
                    .path(String.valueOf(exam.getId()))
                    .build();
            return Response.created(location).entity(exam).build();
        } catch (PersistenceException e) {
            return Response.ok(e).build();
        }
    }

    @PATCH
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("teacher")
    @Path("/{idExam: [0-9]+}/add/{idPersonne: [0-9]+}")
    @Transactional
    public Response add(@PathParam("idExam") long idExam, @PathParam("idPersonne") long idPersonne, @Context UriInfo uriInfo) {
        Person p = em.find(Person.class, idPersonne);
        Exam e = em.find(Exam.class, idExam);
        e.addInscrits(p);
        em.persist(e);
        return Response.ok(e.getListeInscrits()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{idExam: [0-9]+}/inscrits")
    public Set<Person> getInscrits(@PathParam("idExam") long idExam, @Context UriInfo uriInfo){
        Exam e = em.find(Exam.class, idExam);
        return e.getListeInscrits();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("teacher")
    @Transactional
    @Path("/{idExam: [0-9]+}/inscrits/{idPersonne: [0-9]+}")
    public Set<Person> deleteInscrit(@PathParam("idExam") long idExam, @PathParam("idPersonne") long idPersonne, @Context UriInfo uriInfo){
        Exam e = em.find(Exam.class, idExam);
        e.removeInscrits(em.find(Person.class, idPersonne));
        em.persist(e);
        return e.getListeInscrits();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("teacher")
    @Transactional
    @Path("/{idExam: [0-9]+}/inscrits")
    public Set<Person> deleteInscrits(@PathParam("idExam") long idExam, @Context UriInfo uriInfo){
        Exam e = em.find(Exam.class, idExam);
        e.getListeInscrits().clear();
        em.persist(e);
        return e.getListeInscrits();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @RolesAllowed("teacher")
    @Path("/{idExam: [0-9]+}/inscrits")
    public Set<Person> putInscrits(@PathParam("idExam") long idExam, ArrayList<Long> idInscrits, @Context UriInfo uriInfo){
        Exam e = em.find(Exam.class, idExam);
        e.getListeInscrits().clear();
        for(long idInscrit : idInscrits){
            Person p = em.find(Person.class, idInscrit);
            e.addInscrits(p);
        }
        em.persist(e);
        return e.getListeInscrits();
    }
}
