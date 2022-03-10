package fr.uga.miashs.projetqcm.model;

import javax.json.bind.annotation.JsonbProperty;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@NamedQueries({
        @NamedQuery(name = "CopieQuestion.byCopieIdAndPos",
                query = "SELECT DISTINCT cq " +
                        "FROM CopieQuestion cq LEFT JOIN FETCH cq.copieChoix cr LEFT JOIN FETCH cq.question q WHERE cq.copie.id=:copieId AND cq.position=:position")
})
@IdClass(CopieQuestion.CopieQuestionId.class)
public class CopieQuestion {

    public static class CopieQuestionId {
        private long copie;
        private long question;
    }

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    private CopieExam copie;

    @Id
    @ManyToOne
    private Question question;

    private int position;

    @OneToMany(mappedBy = "copieQuestion", cascade = CascadeType.ALL)
    @OrderColumn(name = "position")
    private List<CopieChoix> copieChoix;

    private CopieQuestion() {
    }

    public CopieQuestion(CopieExam copie, Question question) {
        this.copie = copie;
        this.question = question;
        copieChoix = new ArrayList<>();
        for (Choix r : question.getChoix()) {
            copieChoix.add(new CopieChoix(this, r));
        }
        if (copie.getExam().isShuffleChoix()) Collections.shuffle(copieChoix);
    }

    @JsonbProperty("_id")
    public int getPosition() {
        return position;
    }

    public String getEnonce() {
        return question.getEnonce();
    }

    public List<CopieChoix> getCopieChoix() {
        return copieChoix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopieQuestion that = (CopieQuestion) o;
        return Objects.equals(copie, that.copie) && Objects.equals(question, that.question);
    }

    @Override
    public int hashCode() {
        return Objects.hash(copie, question);
    }
}
