package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.*;

public class Survey {

    private static int counterId = 1000;

    private final User surveyCreator;

    private final ArrayList<SurveyQuestion> surveyQuestions;

    private final String name;

    private boolean anonymousSurvey;
    private boolean hasResults;
    private boolean published;
    private boolean distributed;

    private double timeToAdd;
    private double durationOfActivity;

    private int lastEditedQuestion;
    private final int id;


    public Survey(User userCreator ,String name){
        surveyQuestions = new ArrayList<>();
        this.name = name;
        this.id = counterId++;
        this.hasResults = false;
        this.surveyCreator = userCreator;
        applyDefaultSettings();
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isDistributed() {
        return distributed;
    }

    public void setDistributed(boolean distributed) {
        this.distributed = distributed;
    }

    public String getName() {
        return name;
    }

    public boolean isHasResults() {
        return hasResults;
    }

    public void setHasResults(boolean hasResults) {
        this.hasResults = hasResults;
    }

    public boolean isAnonymousSurvey() {
        return anonymousSurvey;
    }

    public void setAnonymousSurvey(boolean anonymousSurvey) {
        this.anonymousSurvey = anonymousSurvey;
    }

    public double getDurationOfActivity() {
        return durationOfActivity;
    }

    public void setDurationOfActivity(double durationOfActivity) {
        this.durationOfActivity = durationOfActivity;
    }

    public double getTimeToAdd(){
        return timeToAdd;
    }

    public void setTimeToAdd(double newTime){
        this.timeToAdd = newTime;
    }

    public int getLastEditedQuestion(){
        return lastEditedQuestion;
    }

    public void setLastEditedQuestion(int index){
        this.lastEditedQuestion = index;
    }

    public User getSurveyCreator(){
        return surveyCreator;
    }

    public List<SurveyQuestion> getSurveyQuestions(){
        return surveyQuestions;
    }

    public int getId(){
        return id;
    }

    public List<SendMessage> getQuestionsAsMessage(){
        return surveyQuestions.stream().map(SurveyQuestion::getMessage).toList();
    }

    public void applyDefaultSettings(){
        setTimeToAdd(surveyCreator.getDefaultDistributionTime());
        setDurationOfActivity(surveyCreator.getDefaultDurationOfActivity());
        setAnonymousSurvey(surveyCreator.isDefaultAnonymousSurvey());

    }

    public SendMessage getResultsAsMessage(){
        SendMessage message = new SendMessage();
        message.setChatId(surveyCreator.getChatId());
        message.setText("📬 התקבלו תוצאות על הסקר שפרסמת :");
        for (SurveyQuestion question : surveyQuestions){
            message.setText(message.getText() + "\n\n"+question.getResults());
        }
        message.setParseMode("Markdown");
        return message;
    }

    public List<User> getUsersThatParticipateInAll() {
        if (surveyQuestions.isEmpty()) {
            return new ArrayList<>();
        }

        Set<User> commonSet = new HashSet<>();
        Set<User> userList = new HashSet<>();
        for (SurveyQuestion question : surveyQuestions){
            commonSet.addAll(question.getUsers());
        }
        boolean exists;
        for (User user : commonSet){
            exists = true;
            for (SurveyQuestion question : surveyQuestions){
                if (!question.getUsers().contains(user)){
                    exists = false;
                    break;
                }
            }
            if (exists){
                userList.add(user);
            }
        }

        return new ArrayList<>(userList);
    }

    public List<User> getAllVoting(){
        Set<User> commonSet = new HashSet<>();
        for (SurveyQuestion surveyQuestion : surveyQuestions) {
            commonSet.addAll(surveyQuestion.getUsers());
        }
        return new ArrayList<>(commonSet);
    }

    public String voterInformation(User user){
        StringBuilder text = new StringBuilder("""
                *🗂 נתונים על המצביע :*

                """);
        text.append("*🗣 שם :* ").append(user.getFullName());
        text.append("\n").append("🆔 *ID* : [").append(user.getChatId()).append("](tg://user?id=").append(user.getChatId()).append(")");

        for (SurveyQuestion surveyQuestion : surveyQuestions){
            text.append("\n\n").append(surveyQuestion.voterInformation(user));
        }
        return text.toString();
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Survey survey = (Survey) object;
        return this.id == survey.id;
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString(){
        return surveyQuestions.toString();
    }
}
