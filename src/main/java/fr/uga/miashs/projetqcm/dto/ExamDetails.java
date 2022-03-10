package fr.uga.miashs.projetqcm.dto;


import fr.uga.miashs.projetqcm.model.Person;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

public class ExamDetails {

    public long id;
    public String title;
    public LocalDateTime debut;
    public LocalDateTime fin;
    public Duration duree;
    public long nbInscrits;
    public long idCreator;
    public Set<Person> listeInscrits;

    public ExamDetails(long id, String title, LocalDateTime debut, LocalDateTime fin, Duration duree, long idCreator, Number nbInscrits) {
        this.id = id;
        this.title = title;
        this.debut = debut;
        this.fin = fin;
        this.duree = duree;
        this.nbInscrits = nbInscrits.longValue();
        this.idCreator = idCreator;
    }
}
