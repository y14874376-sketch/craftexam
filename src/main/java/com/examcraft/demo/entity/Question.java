package com.examcraft.demo.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    private String unit;
    private String topic;
    private String difficulty;
    private String type;
    private String answer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    private List<String> options = new ArrayList<>();

    protected Question() {
    }

    public Question(String text, String unit, String topic, String difficulty, String type, List<String> options, String answer) {
        this.text = text;
        this.unit = unit;
        this.topic = topic;
        this.difficulty = difficulty;
        this.type = type;
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
        this.answer = answer == null ? "" : answer;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getUnit() { return unit; }
    public String getTopic() { return topic; }
    public String getDifficulty() { return difficulty; }
    public String getType() { return type; }
    public List<String> getOptions() { return options; }
    public String getAnswer() { return answer; }
}
