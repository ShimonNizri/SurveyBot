package org.example;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class User {
    @JsonIgnore
    private org.telegram.telegrambots.meta.api.objects.User details;
    private String answer;
    private String chatID;
    private UserStatus status;
    @JsonIgnore
    private List<Survey> surveys;
    private String dateOfJoining;
    private boolean NewSurveyAlert;
    private boolean NewMemberAlert;
    private boolean anonymousAccount;
    private int participationInSurveys;
    @JsonIgnore
    private Instant accountStatusChangeTime;
    private double defaultDistributionTime;
    private double defaultDurationOfActivity;
    private boolean defaultAnonymousSurvey;


    public enum UserStatus {
        CS_CreateSurvey,CS_getName,CS_SurveySelection,CS_voter, CS_ViewingSurvey,CS_ViewingResults,CS_voting,CS_ANSWER_SUM_QUESTION, CS_ANSWER_GET_QUESTION, CS_ANSWER_GET_OPTIONS, CS_mainMenu
        ,CS_Select_question,CS_Edit_question_menu,CS_EditingQuestionItself, CS_Editing_answers,CS_surveySettings,CS_SendingTimeDistribution,
        CS_SendingDurationOfActivity,CS_MySurveys
        ,voting,FREE,
        EP_EditingProfile,EP_ProfileSettings,EP_Set_alerts, EP_Set_account,EP_Set_Defaults,EP_DefaultTimeDistribution,EP_DefaultDurationOfActivity,
        US_searchType,US_SearchSelection,US_UserList,US_UserInformation
    }

    public User(org.telegram.telegrambots.meta.api.objects.User from) {
        this.details = from;
        this.status = UserStatus.FREE;
        this.dateOfJoining = Instant.now().toString();
        this.NewSurveyAlert = true;
        this.NewMemberAlert = true;
        this.anonymousAccount = true;
        this.accountStatusChangeTime = Instant.now().minus(1, ChronoUnit.DAYS);
        this.defaultDistributionTime = 0;
        this.defaultDurationOfActivity = 5;
        this.defaultAnonymousSurvey = false;
        this.chatID = details.getId().toString();
        this.surveys = new ArrayList<>();
        this.participationInSurveys = 0;
    }
    public User(String chatId, String dateOfJoining){
        this.details = null;
        this.dateOfJoining = dateOfJoining;
        this.status = UserStatus.FREE;
        this.chatID = chatId;
        this.NewSurveyAlert = true;
        this.NewMemberAlert = true;
        this.anonymousAccount = true;
        this.accountStatusChangeTime = Instant.now().minus(1, ChronoUnit.DAYS);
        this.defaultDistributionTime = 0;
        this.defaultDurationOfActivity = 5;
        this.defaultAnonymousSurvey = false;
        this.surveys = new ArrayList<>();
        this.participationInSurveys = 0;

    }

    public org.telegram.telegrambots.meta.api.objects.User getDetails(){
        return this.details;
    }

    public void setDetails(org.telegram.telegrambots.meta.api.objects.User from){
        this.details = from;
    }


    public boolean isDefaultAnonymousSurvey() {
        return defaultAnonymousSurvey;
    }

    public void setDefaultAnonymousSurvey(boolean defaultAnonymousSurvey) {
        this.defaultAnonymousSurvey = defaultAnonymousSurvey;
    }

    public double getDefaultDistributionTime() {
        return defaultDistributionTime;
    }

    public void setDefaultDistributionTime(double defaultDistributionTime) {
        this.defaultDistributionTime = defaultDistributionTime;
    }

    public double getDefaultDurationOfActivity() {
        return defaultDurationOfActivity;
    }

    public void setDefaultDurationOfActivity(double defaultDurationOfActivity) {
        this.defaultDurationOfActivity = defaultDurationOfActivity;
    }

    public boolean isCanChangeAccountStatus(){
        Instant nowTime = Instant.now();
        Duration duration = Duration.between(accountStatusChangeTime, nowTime);
        return duration.toDays() >= 1;
    }

    public boolean isAnonymousAccount() {
        return anonymousAccount;
    }

    public Instant getAccountStatusChangeTime(){
        return accountStatusChangeTime;
    }

    public void setAnonymousAccount(boolean anonymousAccount) {
        this.accountStatusChangeTime = Instant.now();
        this.anonymousAccount = anonymousAccount;
    }

    public void setNewSurveyAlert(boolean newSurveyAlert){
        this.NewSurveyAlert = newSurveyAlert;
    }
    public boolean isNewSurveyAlert(){
        return this.NewSurveyAlert;
    }

    public boolean isNewMemberAlert() {
        return NewMemberAlert;
    }

    public void setNewMemberAlert(boolean newMemberAlert) {
        NewMemberAlert = newMemberAlert;
    }

    public void createNewSurvey(String name) {
        this.surveys.addLast(new Survey(this,name));
        this.surveys.getLast().setDurationOfActivity(this.defaultDurationOfActivity);
        this.surveys.getLast().setTimeToAdd(this.defaultDistributionTime);
    }


    public Survey getLastSurvey(){
        return this.surveys.getLast();
    }

    public List<Survey> getSurveys(){
        return this.surveys;
    }

    public Survey getSurveyByName(String name){
        Survey survey = null;
        for (Survey survey1 : surveys){
            if (survey1.getName().equals(name)){
                survey = survey1;
                break;
            }
        }
        return survey;
    }

    public int getParticipationInSurveys() {
        return participationInSurveys;
    }

    public void addOneToParticipationInSurveys() {
        this.participationInSurveys++;
    }

    public String getChatId(){
        if (details != null)
            return this.details.getId().toString();
        else
            return chatID;
    }
    public UserStatus getStatus(){
        return this.status;
    }
    public void setStatus(UserStatus newStates){
        this.status = newStates;
    }
    public String getAnswer(){
        return answer;
    }
    public void setAnswer(String newAnswer){
        this.answer = newAnswer;
    }
    public String getDateOfJoining() {
        return dateOfJoining;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getChatId().equals(user.getChatId());
    }
    @Override
    public int hashCode() {
        return getChatId().hashCode();
    }
    public String toString(){
        return getChatId();
    }
}
