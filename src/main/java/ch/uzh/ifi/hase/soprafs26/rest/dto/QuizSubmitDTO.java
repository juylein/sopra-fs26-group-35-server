package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class QuizSubmitDTO { //Submit answers request
    private List<Integer> answers;

    public List<Integer> getAnswers() { return answers; }
    public void setAnswers(List<Integer> answers) { this.answers = answers; }
}