package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.*;

public class Survey {
    @JsonIgnore
    private User surveyCreator;

    private String name;

    private ArrayList<SurveyQuestion> surveyQuestions;
    private int id;

    @JsonIgnore
    private int lastEditedQuestion;

    private static int counterId = 1000;

    @JsonIgnore
    private double timeToAdd;
    private double durationOfActivity;
    private boolean anonymousSurvey;
    private boolean hasResults;
    private boolean published;
    private boolean distributed;

    public Survey(User userCreator ,String name){
        surveyQuestions = new ArrayList<>();
        this.name = name;
        this.id = counterId++;
        this.hasResults = false;
        this.surveyCreator = userCreator;
        applyDefaultSettings();
    }

    public void applyDefaultSettings(){
        setTimeToAdd(surveyCreator.getDefaultDistributionTime());
        setDurationOfActivity(surveyCreator.getDefaultDurationOfActivity());
        setAnonymousSurvey(surveyCreator.isDefaultAnonymousSurvey());

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

    @JsonIgnore
    public List<SendMessage> getQuestionsAsMessage(){
        return surveyQuestions.stream().map(SurveyQuestion::getMessage).toList();
    }

    @JsonIgnore
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

    public List<SurveyQuestion> getSurveyQuestions(){
        return surveyQuestions;
    }
    public int getId(){
        return id;
    }

    public String toString(){
        return surveyQuestions.toString();
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
        for (int i = 0; i < surveyQuestions.size(); i++) {
            commonSet.addAll(surveyQuestions.get(i).getUsers());
        }
        return new ArrayList<>(commonSet);
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

    public String voterInformation(User user){
        String text =  "*🗂 נתונים על המצביע :*" + "\n\n";
        text = text + "*🗣 שם :* " + user.getDetails().getFirstName() + (user.getDetails().getLastName() != null ? user.getDetails().getLastName():"");
        text = text + "\n" + "🔗 *ID* : [" + user.getChatId() + "](tg://user?id=" + user.getChatId() + ")";

        for (SurveyQuestion surveyQuestion : surveyQuestions){
            text = text +"\n\n" + surveyQuestion.voterInformation(user);
        }

        return text;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Survey survey = (Survey) object;
        return this.id == survey.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(surveyCreator, name, surveyQuestions, id, lastEditedQuestion, timeToAdd, durationOfActivity, anonymousSurvey, hasResults, published, distributed);
    }
}
