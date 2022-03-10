package fr.uga.miashs.projetqcm.storage;


import fr.uga.miashs.projetqcm.model.Person;

import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.security.enterprise.identitystore.PasswordHash;

public class PersonDao extends GenericJpaDao<Long, Person> {

    @Inject
    private PasswordHash hashAlgo;


    public PersonDao() {
        super(Person.class);
    }

    @Override
    public Long create(Person p) {
        if (p.getPassword() != null) {
            p.setPasswordHash(hashAlgo.generate(p.getPassword().toCharArray()));
            // pour ne pas renvoyer le password dans la réponse
            p.setPassword(null);
        } else {
            // pour etre sur que l'on enregistre pas un mauvais hash
            p.setPasswordHash(null);
        }
        return super.create(p);
    }

    public boolean authenticate(String email, String password) {
        TypedQuery<Person> q = getEm().createNamedQuery("Person.login", Person.class);
        q.setParameter("email", email);
        Person u = q.getSingleResult();
        return hashAlgo.verify(password.toCharArray(), u.getPasswordHash());
    }
}
