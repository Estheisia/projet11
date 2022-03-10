package fr.uga.miashs.projetqcm.model;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;


@Entity
@IdClass(Choix.ChoixId.class)
public class Choix {

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public void setEnonce(String enonce) {
        this.enonce = enonce;
    }

    public static class ChoixId implements Serializable {
        private long question;
        private int id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChoixId choixId = (ChoixId) o;
            return question == choixId.question && id == choixId.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(question, id);
        }
    }


    @Id
    @GeneratedValue
    private int id;

    @Id
    @ManyToOne
    private Question question;

    @NotNull
    private String enonce;

    private boolean valid;

    public Choix() {
    }

    public Choix(Question question, String enonce, boolean valid) {
        this.question = question;
        this.enonce = enonce;
        this.valid = valid;
    }

    public int getId() {
        return id;
    }

    @JsonbTransient
    public Question getQuestion() {
        return question;
    }

    public String getEnonce() {
        return enonce;
    }

    @JsonbTransient
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Choix choix = (Choix) o;
        return id == choix.id && Objects.equals(question, choix.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, question);
    }
}
