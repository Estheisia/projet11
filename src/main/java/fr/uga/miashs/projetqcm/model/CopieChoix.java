package fr.uga.miashs.projetqcm.model;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(CopieChoix.CopieChoixId.class)
public class CopieChoix {

    public static class CopieChoixId {
        private CopieQuestion.CopieQuestionId copieQuestion;
        private Choix.ChoixId choix;
    }

    @Id
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "question_id", referencedColumnName = "question_id"),
            @JoinColumn(name = "copie_id", referencedColumnName = "copie_id")
    })
    private CopieQuestion copieQuestion;

    @Id
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "question_id", referencedColumnName = "question_id", insertable = false, updatable = false),
            @JoinColumn(name = "choix_id", referencedColumnName = "id")
    })
    private Choix choix;

    private int position;

    private boolean cochee;

    private CopieChoix() {
    }

    public CopieChoix(CopieQuestion q, Choix c) {
        copieQuestion = q;
        choix = c;
        cochee = false;
    }

    @JsonbProperty("_id")
    public int getPosition() {
        return position;
    }

    public String getEnonce() {
        return choix.getEnonce();
    }

    public boolean isCochee() {
        return cochee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopieChoix that = (CopieChoix) o;
        return Objects.equals(copieQuestion, that.copieQuestion) && Objects.equals(choix, that.choix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(copieQuestion, choix);
    }
}
