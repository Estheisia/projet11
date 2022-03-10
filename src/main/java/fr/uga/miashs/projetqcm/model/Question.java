package fr.uga.miashs.projetqcm.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Question {
    @Id
    @GeneratedValue
    private long id;

    @NotBlank
    private String enonce;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    @OrderColumn
    private List<Choix> choix;

    public Question() {
    }

    public Question(String enonce) {
        this.enonce = enonce;
    }

    public long getId() {
        return id;
    }

    public void setChoix(List<Choix> choix) {
        this.choix = choix;
    }

    public void setEnonce(String enonce) {
        this.enonce = enonce;
    }

    public String getEnonce() {
        return enonce;
    }

    public List<Choix> getChoix() {
        if (choix == null) {
            choix = new ArrayList<>();
        }
        return choix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return id == question.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
