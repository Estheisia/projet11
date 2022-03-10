package fr.uga.miashs.projetqcm.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
public class CopieExam {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private Exam exam;

    @OneToMany(mappedBy = "copie", cascade = CascadeType.ALL)
    @OrderColumn(name = "position")
    private List<CopieQuestion> questions;

    @Column(name = "debut", columnDefinition = "TIMESTAMP")
    private LocalDateTime debut;

    private CopieExam() {
    }

    public CopieExam(Exam exam) {
        this.exam = exam;
        questions = new ArrayList<>();
        for (Question q : exam.getQuestions()) {
            questions.add(new CopieQuestion(this, q));
        }
        if (exam.isShuffleQuestions()) Collections.shuffle(questions);
        debut = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    protected Exam getExam() {
        return exam;
    }

    public List<CopieQuestion> getQuestions() {
        return questions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CopieExam copieExam = (CopieExam) o;
        return id == copieExam.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
