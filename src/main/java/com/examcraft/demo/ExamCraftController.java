package com.examcraft.demo;

import com.examcraft.demo.entity.Question;
import com.examcraft.demo.service.ExamCraftService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class ExamCraftController {
    private final ExamCraftService examCraftService;
    private final AtomicLong paperIds = new AtomicLong(1);

    public ExamCraftController(ExamCraftService examCraftService) {
        this.examCraftService = examCraftService;
    }

    @GetMapping("/questions")
    public List<QuestionResponse> listQuestions(@RequestParam(required = false) String search,
                                                @RequestParam(required = false) String difficulty,
                                                @RequestParam(required = false) String unit) {
        return examCraftService.findQuestions(search, difficulty, unit).stream().map(this::toResponse).toList();
    }

    @PostMapping("/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionResponse addQuestion(@Valid @RequestBody QuestionRequest request) {
        return toResponse(examCraftService.addQuestion(request.text(), request.unit(), request.topic(), request.difficulty(), request.type(), request.options(), request.answer()));
    }

    @DeleteMapping("/questions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable long id) {
        try {
            examCraftService.deleteQuestion(id);
        } catch (jakarta.persistence.EntityNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @PostMapping("/papers/generate")
    public GeneratedPaper generatePaper(@Valid @RequestBody PaperRequest request) {
        var result = examCraftService.generatePaper(request.total(), request.easy(), request.medium(), request.hard(), request.topic());
        return new GeneratedPaper("PAPER-" + String.format("%04d", paperIds.getAndIncrement()), result.questions().stream().map(this::toResponse).toList(), result.distributionMet());
    }

    @GetMapping("/analytics")
    public Analytics analytics() {
        var result = examCraftService.analytics();
        return new Analytics(result.totalQuestions(), result.byDifficulty(), result.byUnit(), result.averageScore(), result.papersThisWeek());
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(question.getId(), question.getText(), question.getUnit(), question.getTopic(), question.getDifficulty(), question.getType(), question.getOptions(), question.getAnswer());
    }

    public record QuestionResponse(long id, String text, String unit, String topic, String difficulty, String type, List<String> options, String answer) {}
    public record QuestionRequest(@NotBlank String text, @NotBlank String unit, @NotBlank String topic, @NotBlank String difficulty,
                                  @NotBlank String type, List<String> options, String answer) {}
    public record PaperRequest(@Min(1) @Max(100) int total, @Min(0) int easy, @Min(0) int medium, @Min(0) int hard, String topic) {}
    public record GeneratedPaper(String id, List<QuestionResponse> questions, boolean distributionMet) {}
    public record Analytics(int totalQuestions, Map<String, Long> byDifficulty, Map<String, Long> byUnit, int averageScore, int papersThisWeek) {}
}
