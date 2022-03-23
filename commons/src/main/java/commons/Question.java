package commons;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

/**
 * Abstract base class for all Question-related classes
 * - id: long attribute that is auto-generated by the DB
 * - title: the title of this specific question (different for each child class).
 */
public abstract class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String title;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return id == question.id
                && Objects.equals(title, question.title);
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    private int number;


    public Question() {
    }

    public abstract long getAnswer();

    public abstract long getCorrectAnswer();

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    @Override
    public String toString() {
        return "id=" + id +
                ", title='" + title;
    }
}
