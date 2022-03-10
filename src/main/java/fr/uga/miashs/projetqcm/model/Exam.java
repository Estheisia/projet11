package fr.uga.miashs.projetqcm.model;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
public class Exam {

    @Id
    @GeneratedValue
    private long id;

    private String title;

    @ManyToMany
    private Set<Person> listeInscrits;

    @Column(name = "debut", columnDefinition = "TIMESTAMP")
    private LocalDateTime debut;

    @Column(name = "fin", columnDefinition = "TIMESTAMP")
    private LocalDateTime fin;

    @Column(name = "duree", columnDefinition = "TIME")
    private Duration duree;

    private boolean shuffleQuestions;
    private boolean shuffleChoix;
    private long idCreator;

    public Exam() {
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @OrderColumn
    private List<Question> questions;

    public Exam(String title, LocalDateTime debut, LocalDateTime fin, Duration duree, boolean shuffleQuestions, boolean shuffleChoix) {
        this.title = title;
        this.debut = debut;
        this.fin = fin;
        this.duree = duree;
        this.shuffleQuestions = shuffleQuestions;
        this.shuffleChoix = shuffleChoix;
    }

    public List<Question> getQuestions() {
        if (questions == null) questions = new ArrayList<>();
        return questions;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getDebut() {
        return debut;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public Duration getDuree() {
        return duree;
    }

    public void setShuffleQuestions(boolean shuffleQuestions) {
        this.shuffleQuestions = shuffleQuestions;
    }

    public void setShuffleChoix(boolean shuffleChoix) {
        this.shuffleChoix = shuffleChoix;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDebut(LocalDateTime debut) {
        this.debut = debut;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public void setDuree(Duration duree) {
        this.duree = duree;
    }

    public long getIdCreator() { return idCreator; }

    public void setIdCreator(long idCreator) { this.idCreator = idCreator; }

    public boolean isShuffleQuestions() {
        return shuffleQuestions;
    }

    public boolean isShuffleChoix() {
        return shuffleChoix;
    }

    public void setListeInscrits(Set<Person> listeInscrits) {
        this.listeInscrits = listeInscrits;
    }

    public Set<Person> getListeInscrits() {
        return listeInscrits;
    }

    public void addInscrits(Person inscrit) {
        this.listeInscrits.add(inscrit);
    }

    public void removeInscrits(Person inscrit) {
        this.listeInscrits.remove(inscrit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return id == exam.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
