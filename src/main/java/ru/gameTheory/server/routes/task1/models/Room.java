package ru.gameTheory.server.routes.task1.models;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Room {
    private Long id;
    private String priorityMethod;
    private Double penalty;
    private Double step;
    private String status;
    private Boolean started;
    private List<Double> centerResults = new LinkedList<>();
    private Double resource;
    private Long nextTickAfter;
    private Long nextTickAt;
    private Integer currentStepNumber;
    private Boolean connectAllowed;
    private Integer participantsCount;
    private Boolean finished;
    private String roomType;
    private Integer stepsCount;
    // deprecated
    private Integer usersNumber;

    public void calcResult(List<User> users) {
        users.sort(Comparator.comparingInt(User::getOrder));
        final Optional<Double> resultOptional = users.stream()
                .map(user -> user.getResults().get(user.getResults().size() - 1))
                .reduce(Double::sum);
        if (resultOptional.isPresent()) {
            this.centerResults.add(resultOptional.get());
        } else {
            throw new RuntimeException("calcResult error. resultOptional is not present");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPriorityMethod() {
        return priorityMethod;
    }

    public void setPriorityMethod(String priorityMethod) {
        this.priorityMethod = priorityMethod;
    }

    public Double getPenalty() {
        return penalty;
    }

    public void setPenalty(Double penalty) {
        this.penalty = penalty;
    }

    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getStarted() {
        return started;
    }

    public void setStarted(Boolean started) {
        this.started = started;
    }

    public List<Double> getCenterResults() {
        return centerResults;
    }

    public void setCenterResults(List<Double> centerResults) {
        this.centerResults = centerResults;
    }

    public Double getResource() {
        return resource;
    }

    public void setResource(Double resource) {
        this.resource = resource;
    }

    public Long getNextTickAfter() {
        return nextTickAfter;
    }

    public void setNextTickAfter(Long nextTickAfter) {
        this.nextTickAfter = nextTickAfter;
    }

    public Long getNextTickAt() {
        return nextTickAt;
    }

    public void setNextTickAt(Long nextTickAt) {
        this.nextTickAt = nextTickAt;
    }

    public Integer getCurrentStepNumber() {
        return currentStepNumber;
    }

    public void setCurrentStepNumber(Integer currentStepNumber) {
        this.currentStepNumber = currentStepNumber;
    }

    public Boolean getConnectAllowed() {
        return connectAllowed;
    }

    public void setConnectAllowed(Boolean connectAllowed) {
        this.connectAllowed = connectAllowed;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getStepsCount() {
        return stepsCount;
    }

    public void setStepsCount(Integer stepsCount) {
        this.stepsCount = stepsCount;
    }

    public Integer getUsersNumber() {
        return usersNumber;
    }

    public void setUsersNumber(Integer usersNumber) {
        this.usersNumber = usersNumber;
    }
}
