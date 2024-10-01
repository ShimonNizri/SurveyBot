package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DataLoader {
    private final File usersFile;
    private final File informationFile;
    private final Map<String,User> users;
    private int sumOfSurvey;

    public DataLoader() throws IOException{
        this.users = new HashMap<>();
        this.usersFile = new File("src/main/java/org/example/users.txt");
        this.informationFile = new File("src/main/java/org/example/information.txt");
        if (!usersFile.exists()) usersFile.createNewFile();
        if (!informationFile.exists()) informationFile.createNewFile();
        initializeData();
    }

    public int getSumOfSurvey(){
        return this.sumOfSurvey;
    }

    public Map<String,User> getUsersAsMap(){
        return users;
    }

    public List<User> getUsersAsList(){
        return users.values().stream().toList();
    }

    public void addNewUser(User newUser) throws IOException{
        FileWriter fw = new FileWriter(usersFile, true);
        fw.write(newUser.getChatId() +","+ newUser.getDateOfJoining()+ System.lineSeparator());
        fw.close();

        users.put(newUser.getChatId(), newUser);
        updateInformationFile();

    }

    public void initializeData() throws IOException {
        Scanner s = new Scanner(usersFile);
        while (s.hasNextLine()){
            String[] details = s.nextLine().split(",");
            User u = new User(details[0],details[1]);
            users.put(u.getChatId(),u);
        }
        s = new Scanner(informationFile);
        if (s.hasNextLine()){
            String[] data = s.nextLine().split(":");
            sumOfSurvey = Integer.parseInt(data[1]);
        }else {
            sumOfSurvey = 0;
        }
        updateInformationFile();
    }

    public void updateInformationFile() throws IOException{
        FileWriter fw = new FileWriter(informationFile, false);
        fw.write(users.size() + ":" + sumOfSurvey);
        fw.close();
    }

    public void addOneToSumOfSurvey() throws IOException{
        this.sumOfSurvey++;
        updateInformationFile();
    }
}