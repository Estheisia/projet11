package fr.uga.miashs.projetqcm.controllers;

import fr.uga.miashs.projetqcm.dto.ExamDetails;
import fr.uga.miashs.projetqcm.model.*;
import fr.uga.miashs.projetqcm.model.CopieQuestion;
import fr.uga.miashs.projetqcm.util.JWTUtils;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.security.enterprise.identitystore.PasswordHash;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Path("/utils")
public class ExemplesEtUtils {

    @PersistenceContext
    private EntityManager em;
    @Inject
    private PasswordHash hashAlgo;
    @Inject
    private JWTUtils jwtUtils;
    @Inject
    private JsonWebToken callerPrincipal;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/copie/{id: [0-9]+}")
    public List<CopieQuestion> getExam(@PathParam("id") long copieId) {
        return em.find(CopieExam.class, copieId).getQuestions();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/copie/{copieId: [0-9]+}/{position: [0-9]+}")
    public CopieQuestion getQuestion(@PathParam("copieId") long copieId, @PathParam("position") int position) {

        return em.createNamedQuery("CopieQuestion.byCopieIdAndPos", CopieQuestion.class)
                .setHint("eclipselink.join-fetch", "cq.copieChoix.choix")
                //.setHint("eclipselink.batch","cq.copieChoix.choix")
                .setParameter("copieId", copieId)
                .setParameter("position", position)
                .getSingleResult();
    }

    /* Exemple de requete avec utilisation de DTO (Data Transfer Object) */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qcm")
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

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // @Path("/qcm/{idExam: [0-9]+}/inscrits")
    // public List<Person> getInscrits(@PathParam("idExam") long idExam) {
    //     /* TODO : trouver le lien entre un inscrit et un exam */
    // }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qcmencours")
    @RolesAllowed("user")
    public List<Exam> getQcmEnCours () {
        TypedQuery<Person> qu = em.createNamedQuery("Person.login",Person.class);
        qu.setParameter("email",callerPrincipal.getClaim(Claims.upn));
        Person u = qu.getSingleResult();
        List<Exam> results = em
                      .createQuery("SELECT e FROM Exam e WHERE CURRENT_DATE BETWEEN e.debut AND e.fin", Exam.class)
                      .getResultList();
        List<Exam> qcmEnCours = new ArrayList<>();
        for( Exam exam : results) {
            if(exam.getListeInscrits().contains(u)) {
                qcmEnCours.add(exam);
            }
        }
        return qcmEnCours;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/qcmencours/{idExam: [0-9]+}")
    @RolesAllowed("user")
    public Exam getQcmEnCours_Id(@PathParam("idExam") long idExam) {
        TypedQuery<Person> qu = em.createNamedQuery("Person.login",Person.class);
        qu.setParameter("email",callerPrincipal.getClaim(Claims.upn));
        Person u = qu.getSingleResult();
        Exam result = em
                      .createQuery("SELECT e FROM Exam e WHERE e.id = " + idExam + " AND CURRENT_DATE BETWEEN e.debut AND e.fin", Exam.class)
                      .getSingleResult();
        if(result.getListeInscrits().contains(u)) {
            return result;
        }
        else {
            return null;
        }
    }

    /* basic Query tester... */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jpql")
    public List<?> executeJPQL(String requete) {
        try {
            return em.createQuery(requete).getResultList();
        }
        catch (Exception e) {
            return Collections.singletonList(e.getMessage());
        }
    }

    @POST
    @Transactional
    @Path("/init")
    public void init() {

        Exam ex = new Exam("QCM2",
                LocalDateTime.of(2021, 10, 25, 10, 00),
                LocalDateTime.of(2021, 10, 30, 10, 0),
                Duration.ofMinutes(15),
                true, true);

        for (int i = 0; i < 30; i++) {
            Question q = new Question("question" + i);
            Choix qc1 = new Choix(q, "choix1", false);
            Choix qc2 = new Choix(q, "choix2", false);
            Choix qc3 = new Choix(q, "choix3", true);

            q.getChoix().add(qc1);
            q.getChoix().add(qc2);
            q.getChoix().add(qc3);


            em.persist(q);
            em.persist(qc1);
            em.persist(qc2);
            em.persist(qc3);
            ex.getQuestions().add(q);

        }

        em.persist(ex);

        CopieExam c = new CopieExam(ex);
        em.persist(c);
    }
    @GET
    @Path("/login")
    public Response authenticate(@QueryParam("email") String email, @QueryParam("passwd")
    String password) {
        TypedQuery<Person> q = em.createNamedQuery("Person.login",Person.class);
        q.setParameter("email",email);
        try {
            Person u = q.getSingleResult();
            if (!hashAlgo.verify(password.toCharArray(),u.getPasswordHash())) {
                throw new NotAuthorizedException("unknown user");
            }
            // doc https://vertx.io/docs/vertx-auth-jwt/java/
            List<String> roles = new ArrayList<>();
            Person.Role role = u.getRole();
            if(role.equals(Person.Role.user)) {
                roles.add("user");
            } else if(role.equals(Person.Role.teacher)) {
                roles.add("teacher");
            }
            String jwt = jwtUtils.generateToken(u.getEmail(),roles,30000);
            return Response.ok().entity(jwt).build();
        }
        catch (NoResultException e) {
            throw new NotAuthorizedException("unknown user");
        }
    }
}