package com.redia.back.dto.chatbot;

import java.util.List;

public class FaqResponseDTO {

    private String id;
    private String category;
    private String question;
    private String answer;
    private List<String> keywords;
    private List<String> relatedQuestions;

    public FaqResponseDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getRelatedQuestions() {
        return relatedQuestions;
    }

    public void setRelatedQuestions(List<String> relatedQuestions) {
        this.relatedQuestions = relatedQuestions;
    }
}
