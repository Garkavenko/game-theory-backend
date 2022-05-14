package ru.gameTheory.server.routes.task2.models;

import java.util.LinkedList;
import java.util.List;

public class User {
    private Long id;
    private List<Double> evaluations = new LinkedList<>();
    private List<Double> results = new LinkedList<>();
    private List<Double> distributions = new LinkedList<>();
    private Double cost;
    private Long roomId;
    private Integer order;

    public boolean isFirstEvaluationInit() {
        return this.evaluations.size() > 1;
    }

    public void calcResult(final Double lambda) {
        if (this.results.size() == this.distributions.size()) {
            return;
        }
        Double lastDist = this.distributions.get(this.distributions.size() - 1);
        this.results.add(getLastResult(lastDist, lambda));
    }

    public Double getLastResult(Double lastDist, Double lambda) {
        return (lambda * lastDist) - (Math.pow(lastDist, 2) / (2 * this.cost));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Double> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Double> evaluations) {
        this.evaluations = evaluations;
    }

    public List<Double> getResults() {
        return results;
    }

    public void setResults(List<Double> results) {
        this.results = results;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<Double> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<Double> distributions) {
        this.distributions = distributions;
    }
}
