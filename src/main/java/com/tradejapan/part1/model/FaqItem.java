package com.tradejapan.cms.model;
import jakarta.persistence.*;

@Entity
@Table(name = "faq_items")
public class FaqItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Column(name = "display_order")
    private Integer order;

    @Column(nullable = false)
    private boolean active = true;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}