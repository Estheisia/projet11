package fr.uga.miashs.projetqcm.storage;

import org.apache.commons.beanutils.BeanUtils;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.validation.constraints.PositiveOrZero;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/* Une seule instance est créée par l'environnement d'exécution */
@Singleton
/* Chaque méthode = une transaction.
C'est obligé d'avoir des transactions pour JPA
*/
@Transactional
/**
 * Dao générique pour accéder à des entités JPA (dont l'id est constitué d'un seul attribut)
 * Les méthodes de base fournies sont les classiques CRUD (create, read, update, delete) ainsi qu'une méthode
 * listAll paginée, count, ainsi qu'une méthode permettant d'exécuter une requête retournant un sous ensemble
 * des entités (executeQuery).
 */
public abstract class GenericJpaDao<K, T> {

    @PersistenceContext
    private EntityManager em;

    /**
     * Type de l'entité.
     * C'est utile de l'avoir pour JPA (find, TypedQueries, etc.)
     */
    private final Class<T> clazz;

    /**
     * Le nom de la colonne en BD qui correspond à l'attribut annoté avec @Id
     */
    //private String idColumnName;

    // liste des attributs "non relation" (i.e. non annoté via ManyToOne, OneToMany, ManyToMany)
    // Map le nom de l'attribut vers le nom de la colonne en BD
    private Map<String, String> fieldNames2ColumnNames;

    private Query deleteQuery;
    private TypedQuery<Long> countQuery;

    /**
     * Constucteur qui prend en paramètre la classe du type géré par le DAO.
     * Dans les sous classes, on définira un constructeur sans paramètre qui appellera
     * celui-ci avec la classe gérée en paramètre
     *
     * @param clazz
     */
    public GenericJpaDao(Class<T> clazz) {
        this.clazz = clazz;
        initFieldNames();
    }

    public GenericJpaDao() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        clazz = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
        initFieldNames();
    }

    private void initFieldNames() {
        // Recherche des attributs "relation", ie. ceux annotés avec
        // ManyToOne, OneToMany, ManyToMany, OneToOne
        fieldNames2ColumnNames = new HashMap<>();
        for (Field f : clazz.getDeclaredFields()) {
            /*if (f.isAnnotationPresent(Id.class)) {
                idColumnName =getColumnName(f);
            }*/
            if (!(f.isAnnotationPresent(ManyToOne.class) ||
                    f.isAnnotationPresent(OneToMany.class) ||
                    f.isAnnotationPresent(ManyToMany.class) ||
                    f.isAnnotationPresent(OneToOne.class))) {
                fieldNames2ColumnNames.put(f.getName(), getColumnName(f));
            }
        }
    }

    protected String getColumnName(Field f) {
        if (f.isAnnotationPresent(Column.class)) {
            String name = f.getAnnotation(Column.class).name();
            if (!"".equals(name)) return name;
        }
        return f.getName();
    }

    @PostConstruct
    public void init() {
        // put here some initialisation that would be done after injection
    }

    private TypedQuery<Long> getCountQuery() {
        if (countQuery == null) {
            // query to count
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Long> cq = qb.createQuery(Long.class);
            cq.select(qb.count(cq.from(clazz)));
            countQuery = em.createQuery(cq);
        }
        return countQuery;
    }

    // solution en SQL natif. Ne fonctionne pas avec des clés composites (i.e. avec plusieurs attributs)
    /*private Query getDeleteQuery() {
        if (deleteQuery == null) {
            StringBuilder sb = new StringBuilder("DELETE FROM ");
            sb.append(clazz.getSimpleName()).append(" WHERE ").append(idColumnName).append(" = :id");
            deleteQuery = em.createQuery(sb.toString());
        }
        return deleteQuery;
    }*/

    public EntityManager getEm() {
        return em;
    }


    public K getId(T obj) {
        return (K) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(obj);
    }


    public K create(T obj) {
        em.persist(obj);
        // le flush oblige a envoyer les modifs à la BD
        // si la maj provoque une violation de contrainte d'intégrité
        // alors une Exception du type PersitenceException est levée
        em.flush();
        // on détache l'entité - et on met null aux propriétés non chargées
        setNullForAllLazyLoadEntities(obj);

        return getId(obj);
    }

    /**
     * Méthode de lecture d'une entité.
     * Cette méthode prend en paramètre un "fetch graph"
     * afin de personnaliser le chargement de l'entité.
     *
     * @param id
     * @param fetchGraph
     * @return
     */
    public T read(K id, EntityGraph<T> fetchGraph) {
        T res = em.find(clazz, id, Collections.singletonMap("javax.persistence.fetchgraph", fetchGraph));
        // on détache l'entité - et on met null aux propriétés non chargées
        setNullForAllLazyLoadEntities(res);
        return res;
    }

    /**
     * Méthode de lecture d'une entité.
     *
     * @param id
     * @return
     */
    public T read(K id) {
        T res = em.find(clazz, id);
        // on détache l'entité - et on met null aux propriétés non chargées
        setNullForAllLazyLoadEntities(res);
        return res;
    }

    /**
     * Méthode mise à jour (partielle) d'une entité.
     * Les informations sont données dans un Map propriété -> valeur.
     * Le Map peut provenir d'un objet JSON.
     *
     * @param id  l'Id de l'entité à mettre à jour
     * @param obj Le map des propriété à mettre à jour
     * @return
     */
    public void updatePartial(K id, Map<String, Object> obj) {
        // solution qui produit 1 select + 1 full update
        // a l'avantage de vérifier les contraintes BeanValidation
        T toUpdate = em.getReference(clazz, id);
        try {
            BeanUtils.populate(toUpdate, obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // le flush oblige a envoyer les modifs à la BD
        // on peut donc récuperer l'exception via le ExceptionMapper
        em.flush();

        //throw new UnsupportedOperationException("User updateFull instead of updatePartial");
        /*
        // Solution de update en une seule requête
        // mais pas de check sur les contraintes BeanValidation
        obj.remove(idColumnName);
        // only retain information about our field names
        // remove the other for security reason (SQL injection)
        obj.keySet().retainAll(fieldNames2ColumnNames.keySet());

        // construction de la requête update en SQL natif
        StringBuilder qs = new StringBuilder();
        qs.append("UPDATE ").append(clazz.getSimpleName()).append(" SET ");
        obj.keySet().forEach(k -> qs.append(fieldNames2ColumnNames.get(k)).append(" = :").append(k));
        qs.append(" WHERE ").append(idColumnName).append("=").append(id);
        Query q = em.createQuery(qs.toString());
        obj.forEach((k, v) -> q.setParameter(k, v));

        q.executeUpdate();
        */
    }

    /**
     * Méthode mise à jour (totale) d'une entité.
     * Le Map peut provenir d'un objet JSON.
     *
     * @param id  l'Id de l'entité à mettre à jour
     * @param obj Le map des propriété à mettre à jour
     * @return
     */
    public void updateFull(K id, T obj) {
        // solution qui produit 1 select + 1 full update
        // a l'avantage de vérifier les contraintes BeanValidation
        T toUpdate = em.getReference(clazz, id);
        try {
            BeanUtils.copyProperties(toUpdate, obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // le flush oblige a envoyer les modifs à la BD
        // on peut donc récuperer l'exception via le ExceptionMapper
        em.flush();
    }


    /**
     * Méthode pour supprimer une entité.
     *
     * @param id
     * @return
     */
    public boolean delete(K id) {
        // Solution en SQL natif
        /*Query q = getDeleteQuery();
        q.setParameter("id", id);
        int nb = q.executeUpdate();
        if (nb == 0) return false;
        // si on utilise le cache 2nd niveau
        em.getEntityManagerFactory().getCache().evict(clazz);*/

        // Solution JPA
        em.remove(em.getReference(clazz, id));
        // le flush oblige a envoyer les modifs à la BD
        // on peut donc récuperer l'exception via le ExceptionMapper
        em.flush();
        return true;
    }

    /**
     * Méthode retournant une liste (paginée) des entités.
     *
     * @param limit  le nombre max d'entités à retourner
     * @param offset la position de la prmière entité à retourner
     * @return
     */
    public List<T> list(int limit, int offset) {
        CriteriaQuery<T> cq = em.getCriteriaBuilder().createQuery(clazz);
        Root<T> c = cq.from(clazz);
        cq.select(c);

        TypedQuery<T> q = em.createQuery(cq);

        return executeQuery(q, limit, offset);
    }

    public List<T> listAll() {
        return list(0, 0);
    }

    /**
     * Retourne le nombre d'entités de ce type dans la BD
     *
     * @return
     */
    public long count() {
        return getCountQuery().getSingleResult();
    }

    /**
     * Méthode générique pour l'éxécution de requete retournant une liste d'entités.
     * La méthode supporte la pagination. voir https://www.baeldung.com/jpa-pagination
     * Cette méthode permet d'ajouter un lien vers
     * les pages suivante et précédente dans les entêtes de la réponse.
     *
     * @param q
     * @param limit
     * @param offset
     * @return
     */
    public List<T> executeQuery(TypedQuery<T> q, @PositiveOrZero int limit, @PositiveOrZero int offset) {
        if (limit != 0) {
            q.setMaxResults(limit);
        }
        if (offset >= 0) {
            q.setFirstResult(offset);
        }

        List<T> qRes = q.getResultList();
        // détachement "safe" des entités
        qRes.forEach(x -> setNullForAllLazyLoadEntities(x));

        return qRes;
        /*Response.ResponseBuilder rb = Response
                .status(Response.Status.PARTIAL_CONTENT)
                .entity(qRes);

        // on ajoute des liens de navigation dans les entetes
        // ces liens permettent d'obtenir les resultats avant et après
        // il reste des elements ensuite
        //if (qRes.size()==limit) {
        long count = count();
        if (offset + qRes.size() < count) {
            rb.link(uriInfo.getRequestUriBuilder()
                    .replaceQueryParam("limit", Math.min(limit, count - offset))
                    .replaceQueryParam("offset", offset + qRes.size())
                    .build(), "next");
        }
        // il y a des elements avant
        if (offset > 0) {
            int computedMinOffset = Math.max(offset - qRes.size(), 0);
            rb.link(uriInfo.getRequestUriBuilder()
                    .replaceQueryParam("limit", offset - computedMinOffset)
                    .replaceQueryParam("offset", computedMinOffset)
                    .build(), "previous");
        }

        return rb.build();*/
    }

    /**
     * Méthode qui permet d'assigner "null" aux propriétés non chargées (lazy loading).
     * Cela évite d'avoir des LazyIntitializationException lorsque l'on accède
     * aux propriétés de l'entité en dehors de la transaction.
     * Cette méthode provoque le détachement de l'entité passée en paramètre.
     *
     * @param source l'entité à détacher
     */
    protected void setNullForAllLazyLoadEntities(Object source) {
        if (source == null) return;
        PersistenceUnitUtil unitUtil = getEm().getEntityManagerFactory().getPersistenceUnitUtil();
        //Logger.getAnonymousLogger().log(Level.WARNING,"setNullForAllLazyLoadEntities");
        if (!getEm().contains(source)) {
            System.out.println("Coucou");
            return;
        }
        getEm().detach(source);
        for (Field field : source.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            try {
                Object fieldValue = field.get(source);
                if (!unitUtil.isLoaded(source, field.getName()) && !field.getType().isPrimitive()) {
                    fieldValue = null;
                    field.set(source, null);
                }

                if (fieldValue != null) {
                    /*boolean isEntity = Arrays.asList(field.getType().getAnnotations())
                            .stream()
                            .filter(x -> x.annotationType().equals(Entity.class))
                            .count() > 0;*/
                    if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                        setNullForAllLazyLoadEntities(fieldValue);
                    } else if (field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToMany.class)) {
                        boolean isCollection = Arrays.stream(field.getType().getInterfaces())
                                .sequential()
                                .anyMatch(x -> x.equals(Collection.class));
                        if (isCollection) {
                            Collection<Object> c = (Collection<Object>) fieldValue;
                            c.forEach(x -> setNullForAllLazyLoadEntities(x));
                        } else {

                            boolean isMap = Arrays.stream(field.getType().getInterfaces())
                                    .sequential()
                                    .anyMatch(x -> x.equals(Map.class));
                            if (isMap) {
                                Map<Object, Object> m = (Map<Object, Object>) fieldValue;
                                m.values().forEach(x -> setNullForAllLazyLoadEntities(x));
                            } else {
                                Logger.getAnonymousLogger().log(Level.WARNING, field.getName() + " ignored");
                            }
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                /*Errors errors = new Errors();
                errors.addMessage(e, "Failed to map field %s ", field.getType());
                throw new MappingException(errors.getMessages());*/
                throw new RuntimeException(e);
            }

        }
    }

}
