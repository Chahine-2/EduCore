package models;

import java.util.List;

/**
 * Model representing a single AI-generated multiple-choice question.
 */
public class AIQuestion {
    private String question;
    private List<String> choices;
    private String correctAnswer; // should be one of the choices or an index/letter
    private String explanation;

    public AIQuestion() {}

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}

