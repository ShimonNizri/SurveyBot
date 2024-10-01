package org.example;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.Collectors;

public class SurveyQuestion {

    private List<String> answerOptions;

    private Map<Integer,List<User>> votes = new HashMap<>();

    private String questionText;

    private int questionNumber;
    private Survey survey;
    private int lastAnswerAdded;

    public SurveyQuestion(String questionText,int questionNumber){
        this.questionText = questionText;
        this.answerOptions = new ArrayList<>();
        this.questionNumber = questionNumber;
    }

    public String getQuestionText(){
        return questionText;
    }

    public void setQuestionNumber(int newNum){
        this.questionNumber = newNum;
    }

    public int getQuestionNumber(){
        return questionNumber;
    }

    public void setQuestionText(String newT){
        this.questionText = newT;
    }

    public int getLastAnswerAdded(){
        return lastAnswerAdded;
    }

    public void setLastAnswerAdded(int newLast){
        this.lastAnswerAdded = newLast;
    }

    public List<String> getAnswerOptions(){
        return answerOptions;
    }

    public int getSumOfAnswer(){
        return answerOptions.size();
    }

    public void setSurvey(Survey newS){
        this.survey = newS;
    }

    public String getSurveyID(){
        return survey.getId()+"";
    }


    public void addAnswer(String answer){
        answerOptions.add(answer);
        List<User> users = new ArrayList<>();
        this.votes.put(votes.size(), users);
    }

    public void removeAnswerByIndex(int removedIndex) {
        this.answerOptions.remove(removedIndex);
        this.votes.remove(removedIndex);

        Map<Integer, List<User>> updatedVotes = new HashMap<>();

        for (int i = 0; i < answerOptions.size(); i++) {
            if (i >= removedIndex) {
                if (votes.containsKey(i + 1)) {
                    updatedVotes.put(i, votes.get(i + 1));
                }
            } else {
                if (votes.containsKey(i)) {
                    updatedVotes.put(i, votes.get(i));
                }
            }
        }

        this.votes = updatedVotes;
    }

    public SendMessage getMessage() {
        return createMessage();
    }

    private SendMessage createMessage(){
        SendMessage message = new SendMessage();
        message.setText( "📄 *השאלה :* " + questionText + " (" + (questionNumber+1)+"/"+survey.getSurveyQuestions().size()+")" + "\n*📝 התשובות : *");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < answerOptions.size(); i++) {
            List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(answerOptions.get(i));
            button.setCallbackData(getSurveyID()+":"+questionNumber+":"+i);
            keyboardButtonsRow1.add(button);
            rowList.add(keyboardButtonsRow1);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    public void addVote(int numAnswer,User user){
        if (votes.containsKey(numAnswer)){
            if (!votes.get(numAnswer).contains(user)){
                votes.get(numAnswer).add(user);
                for (int i = 0; i < votes.size(); i++){
                    if (i != numAnswer){
                        removeVote(i,user);
                    }
                }
            }
        }
    }

    public void removeVote(int numAnswer,User user){
        if (votes.containsKey(numAnswer)){
            votes.get(numAnswer).remove(user);
        }
    }

    public String getResults() {
        Map<String, Integer> answerVotesMap = new LinkedHashMap<>();
        for (int i = 0; i < answerOptions.size(); i++) {
            answerVotesMap.put(answerOptions.get(i), votes.get(i).size());
        }

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(answerVotesMap.entrySet());
        sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        int totalVotes = votes.values().stream().mapToInt(List::size).sum();

        StringBuilder results = new StringBuilder("➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖\n" +
                "📄 *השאלה* : \n" + questionText + "\n📂 *תשובות* : \n");
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            int count = entry.getValue();
            double percentage = totalVotes > 0 ? (count * 100.0 / totalVotes) : 0;
            results.append(entry.getKey())
                    .append(" -> ")
                    .append(String.format("%.2f", percentage))
                    .append("% (")
                    .append(count)
                    .append(" קולות)\n");
        }

        results.append("\n\uD83C\uDF10 סה\"כ משתתפים שהצביעו בשאלה : ")
                .append(totalVotes);

        return results.toString();
    }

    public List<User> getUsers() {
        return votes.values()
                .stream()
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public String getQuestionAndAnswerAsText(){
        String t = "💢 השאלה שנבחרה : \n➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖\n" +  "📄 *השאלה* : \n➖ " + questionText  +"\n📂 *תשובות* :\n";
        StringBuilder results = new StringBuilder(t);
        for (String answer : answerOptions) {
            results.append("➖ ").append(answer).append("\n");
        }
        return results.toString();
    }

    public String voterInformation(User user){
        String text = "📄 *עבור השאלה : *" + questionText;
        text = text + "\n" +  "📪 *הצביע :* ";
        for (int i = 0 ; i < getSumOfAnswer() ; i++){
            if (votes.get(i).contains(user)){
                text = text + answerOptions.get(i);
                break;
            }
            if (i == getSumOfAnswer()-1){
                text = text + "*- לא הצביע -*";
            }
        }

        return text;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        SurveyQuestion question = (SurveyQuestion) object;
        return Objects.equals(getSurveyID(),question.getSurveyID()) &&  Objects.equals(questionText, question.questionText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionText, getSurveyID());
    }

    public String toString(){
        return questionText + " - " + answerOptions;
    }

}