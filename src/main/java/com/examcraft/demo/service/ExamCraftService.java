package com.examcraft.demo.service;

import com.examcraft.demo.entity.Question;
import com.examcraft.demo.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamCraftService {
    private final QuestionRepository questionRepository;

    public ExamCraftService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
        seedQuestions();
    }

    public List<Question> findQuestions(String search, String difficulty, String unit) {
        return questionRepository.findAll().stream()
                .filter(question -> matches(question, search, difficulty, unit))
                .toList();
    }

    public Question addQuestion(String text, String unit, String topic, String difficulty, String type, List<String> options, String answer) {
        return questionRepository.save(new Question(text, unit, topic, difficulty, type, options, answer));
    }

    public void deleteQuestion(long id) {
        if (!questionRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Question not found");
        }
        questionRepository.deleteById(id);
    }

    public PaperResult generatePaper(int total, int easy, int medium, int hard, String topic) {
        List<Question> selected = new ArrayList<>();
        Set<Long> used = new HashSet<>();
        addByDifficulty(selected, used, "Easy", easy, topic);
        addByDifficulty(selected, used, "Medium", medium, topic);
        addByDifficulty(selected, used, "Hard", hard, topic);
        if (selected.size() < total) {
            questionRepository.findAll().stream()
                    .filter(question -> matches(question, null, null, topic) && used.add(question.getId()))
                    .limit(total - selected.size())
                    .forEach(selected::add);
        }
        Collections.shuffle(selected);
        return new PaperResult(selected.stream().limit(total).toList(), selected.size() >= total);
    }

    public AnalyticsResult analytics() {
        List<Question> questions = questionRepository.findAll();
        Map<String, Long> byDifficulty = questions.stream().collect(Collectors.groupingBy(Question::getDifficulty, LinkedHashMap::new, Collectors.counting()));
        Map<String, Long> byUnit = questions.stream().collect(Collectors.groupingBy(Question::getUnit, LinkedHashMap::new, Collectors.counting()));
        return new AnalyticsResult(questions.size(), byDifficulty, byUnit, 78, 4);
    }

    private void addByDifficulty(List<Question> selected, Set<Long> used, String difficulty, int count, String topic) {
        questionRepository.findAll().stream()
                .filter(question -> question.getDifficulty().equalsIgnoreCase(difficulty) && matches(question, null, null, topic) && used.add(question.getId()))
                .limit(count)
                .forEach(selected::add);
    }

    private boolean matches(Question question, String search, String difficulty, String unit) {
        return (search == null || search.isBlank() || (question.getText() + question.getTopic() + question.getUnit()).toLowerCase().contains(search.toLowerCase()))
                && (difficulty == null || difficulty.isBlank() || question.getDifficulty().equalsIgnoreCase(difficulty))
                && (unit == null || unit.isBlank() || question.getUnit().equalsIgnoreCase(unit));
    }

    private void seedQuestions() {
        if (questionRepository.count() > 0) return;
        addQuestion("What is the primary purpose of normalization?", "Database Systems", "Data Modeling", "Easy", "MCQ", List.of("Reduce redundancy", "Increase duplication", "Remove indexes", "Encrypt tables"), "Reduce redundancy");
        addQuestion("Explain the difference between a process and a thread.", "Operating Systems", "Concurrency", "Medium", "Short answer", List.of(), "");
        addQuestion("Which protocol secures HTTP traffic?", "Computer Networks", "Web Security", "Easy", "MCQ", List.of("FTP", "SSH", "TLS", "SMTP"), "TLS");
        addQuestion("Design an API for a library checkout system.", "Software Engineering", "API Design", "Hard", "Long answer", List.of(), "");
        addQuestion("What does ACID stand for in database transactions?", "Database Systems", "Transactions", "Medium", "MCQ", List.of("Atomicity, Consistency, Isolation, Durability", "Access, Control, Identity, Data", "Audit, Cache, Index, Delete", "None of these"), "Atomicity, Consistency, Isolation, Durability");
        addQuestion("What is the time complexity of binary search?", "Algorithms", "Searching", "Medium", "MCQ", List.of("O(n)", "O(log n)", "O(n log n)", "O(1)"), "O(log n)");
    }

    public record PaperResult(List<Question> questions, boolean distributionMet) {}
    public record AnalyticsResult(int totalQuestions, Map<String, Long> byDifficulty, Map<String, Long> byUnit, int averageScore, int papersThisWeek) {}
}
