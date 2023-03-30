package bgu.spl.net;

import bgu.spl.net.Messages.Message;
import bgu.spl.net.Messages.Notification;
import bgu.spl.net.Messages.PmMsg;
import bgu.spl.net.Messages.PostMsg;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

public class DataBase {


    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> registerdUsers = new ConcurrentHashMap<>();   //First ij list is password, second is birthday
    private ConcurrentHashMap<String, String> loggedUsers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> following = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Message> postPmMsg = new ConcurrentLinkedQueue<>();
    private ArrayList<String> forbidden = new ArrayList<>();
    private ConcurrentHashMap<String, ArrayList<String>> blockingMap = new ConcurrentHashMap<>();


    //All this methods are boolean, if return false need to reconstruct error msg in the method that calls this methods
        
    public DataBase(ArrayList<String> forbiddenArray){
        forbidden.addAll(forbiddenArray);


    }


    public boolean isRegistered(String userName){
        return registerdUsers.containsKey(userName);
    }


    public boolean isLoggedIn(String userName){
        return loggedUsers.containsKey(userName);
    }

    public String registerUser(String username, String password, String birthday){
        if(isRegistered(username))
            return error();
        registerdUsers.put(username, new ConcurrentLinkedQueue<>());
        registerdUsers.get(username).add(password);
        registerdUsers.get(username).add(birthday);
        following.put(username, new ConcurrentLinkedQueue<>());
        return ack();
    }

    private String ack(String s) {
        return "ack," + s;
    }

    private String ack() {
        return "ack,";
    }

    private String error() {
        return "error,";
    }

    public String loginUser(String username, String password, short captcha)  {
        if(captcha == 0)
            return error();
        if(loggedUsers.containsKey(username))
            return error();
        if(!registerdUsers.containsKey(username))
            return error();
        if(!registerdUsers.get(username).contains(password))
            return error();
        loggedUsers.put(username, password);
        return ack();
    }

    public ArrayList<String> checkIfIHaveMsgWaiting(String username) {
        ArrayList<String> output = new ArrayList<>();
        for(Message msg : postPmMsg){
            if(msg.needToSendToUser(username)){
                //send to username the message  that I wanted to from msg
                msg.setRecieved(username);
                output.add( "09," + msg.getType() + "," +  msg.getPostingUser() + "," + msg.getContent());     //output all the people I need to send to
            }
        }
        return output;
    }



    public String logout(String username) {
        if(!registerdUsers.containsKey(username))
            return error();
        if(!loggedUsers.containsKey(username))
            return error();
        loggedUsers.remove(username);
        return ack();

    }

    public String follow(String follower, String followedUser, int act) {
        if(!isLoggedIn(follower) || follower.equals(followedUser))
            return error();
        if(!isRegistered(followedUser))
            return error();
        if(!following.containsKey(follower)){
            following.put(follower, new ConcurrentLinkedQueue<>());
        }
        if(isBlocked(followedUser, follower))
            return error();
        if(act == 0){
            if(following.get(follower).contains(followedUser)){
                return error();
            }
            else{
                following.get(follower).add(followedUser);
            }
        }
        else if(act == 1){
            if(!following.get(follower).contains(followedUser)){
                return error();
            }
            else{
                following.get(follower).remove(followedUser);
            }
        }
        return ack(String.valueOf(act) + "," + followedUser);
    }


    public ArrayList<Notification> postMessage(String userName, String content){
        ArrayList<Notification> output = new ArrayList<>();
        if(!isLoggedIn(userName)) {
            output.add(new Notification(userName));     //Error should be sent to userName
            return output;
        }
        ArrayList<String> sendTo = new ArrayList<>(chooseUsersToSendPost(userName, content));
        PostMsg temp = new PostMsg(userName, content, sendTo);
        postPmMsg.add(temp);
        ArrayList<String> deleteUsers = new ArrayList<>();
        for(String sendToUser : sendTo){
            if(isLoggedIn(sendToUser) && temp.needToSendToUser(sendToUser)  && !isBlocked(sendToUser, userName)) {
                deleteUsers.add(sendToUser);
                output.add(new Notification( "post", userName, content, sendToUser));
            }
            //All the people who are not logged in right now will stay on the list, check the login in notification.
        }
        for(String currUserToDelete : deleteUsers){
            temp.setRecieved(currUserToDelete);
        }
        return output;
    }


    public ArrayList<Notification> pmMsg(String username, String receiver, String content){
        ArrayList<Notification> output = new ArrayList<>();
        if(receiver.equals(username) || !isLoggedIn(username) || !isRegistered(receiver)){
            output.add(new Notification(username));     //Error should be sent to userName
        }
        else if(!following.containsKey(username)  || !following.get(username).contains(receiver))
            output.add(new Notification(username));
        else{
            if(isBlocked(receiver, username)){
                return output;         // if output is empty then nothing should happen
            }
            String filtered = filterContent(content);
            PmMsg temp = new PmMsg(username, receiver, filtered);
            postPmMsg.add(temp);
            if(isLoggedIn(receiver) && !temp.recivedAlready() ){
                temp.setRecieved(username);
                output.add(new Notification( "pm", username, filtered, receiver));
            }
        }
        return output;
    }



    //null means error
    public ArrayList<String> logStat(String SendingUser) {
        ArrayList<String> output = new ArrayList<>();
        if (!isLoggedIn(SendingUser)) {
            return null;
        }
        if (!isRegistered(SendingUser))
            return null;
        for(String username : registerdUsers.keySet()){
            if(!(isBlocked(username, SendingUser) || isBlocked( SendingUser, username))){
                ArrayList<Integer> results = statistics(username);
                output.add( results.get(0).toString() + "," + results.get(1).toString() + "," + results.get(2).toString() + "," + results.get(3).toString() );
            }
        }
        return output;
    }

    //null means error
    public ArrayList<String> stat(String sendingUser, ArrayList<String> users){
        ArrayList<String> output = new ArrayList<>();
        if (!isLoggedIn(sendingUser)) {
            return null;
        }
        if (!isRegistered(sendingUser))
            return null;
        for(String username : users){
            if(!isBlocked(username, sendingUser)) {
                ArrayList<Integer> results = statistics(username);
                output.add(results.get(0) + "," + results.get(1) + "," + results.get(2) + "," + results.get(3));
            }
        }
        return output;
    }



    public String block(String blockingUser, String blockedUser){
        if(!isRegistered(blockedUser))
            return error();
        if(following.containsKey(blockedUser))
            following.get(blockedUser).remove(blockingUser);
        if(following.containsKey(blockingUser))
            following.get(blockingUser).remove(blockedUser);
        if(!blockingMap.containsKey(blockingUser)){
            blockingMap.put(blockingUser, new ArrayList<>());
        }
        blockingMap.get(blockingUser).add(blockedUser);
        return ack();
    }


    private ArrayList<Integer> statistics(String username) {
        ArrayList<Integer> result = new ArrayList<>();
        int age = extractAge(username);
        int numOfPosts = numOfPosts(username);
        int numOfFollowers = numOfFollowers(username);
        int numOfFollowing = following.get(username).size();
        result.add(age);
        result.add(numOfPosts);
        result.add(numOfFollowers);
        result.add(numOfFollowing);
        return result;
    }


    private int numOfFollowers(String username) {
        int output = 0;
        for(Map.Entry<String, ConcurrentLinkedQueue<String>> entry : following.entrySet()){
            if(entry.getValue().contains(username))
                output += 1;
        }
        return output;
    }

    private int numOfPosts(String username) {
        int output = 0;
        for(Message message : postPmMsg){
            if(message.getPostingUser().equals(username) && message instanceof PostMsg){
                output++;
            }
        }
        return output;
    }


    private int extractAge(String username) {
        int age = 0;
        String birthdayString = (String) registerdUsers.get(username).toArray()[1];
        String timeToday = new SimpleDateFormat("dd-MM-yyyy").format(Calendar.getInstance().getTime());
        Date birthday = null;
        Date now = null;
        try {
            birthday = new SimpleDateFormat("dd-MM-yyyy").parse(birthdayString);
            now = new SimpleDateFormat("dd-MM-yyyy").parse(timeToday);
            long diff = now.getTime() - birthday.getTime();
            age = (int) (diff / (1000L * 60 * 60 * 24 * 365 ));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return age;
    }


    private String filterContent(String content) {
        for(String curr : forbidden){
            content = Pattern.compile(curr).matcher(content).replaceAll("<filtered>");

        }
        return content;
    }


    private ArrayList<String> chooseUsersToSendPost(String userName, String content) {
        ArrayList<String> sendTo = new ArrayList<>();
        for(Map.Entry<String, ConcurrentLinkedQueue<String>> entry : following.entrySet()){
            if(entry.getValue().contains(userName) && !sendTo.contains(userName) )
                sendTo.add(entry.getKey());
        }
        for(String user : registerdUsers.keySet()){
            int index = content.indexOf(user);
            if(index > 0 && !user.equals(userName)){
                char ch = content.charAt(index - 1);
                if(ch == '@'){
                    if(!sendTo.contains(user) & isRegistered(user))
                        sendTo.add(user);
                }
            }
        }
        return sendTo;
    }

    private String composeUser(String content, int firstIndex) {
        String temp = content.substring(firstIndex);
        int spaceIndex = temp.indexOf(" ");
        return temp.substring(0, spaceIndex);
    }


    public boolean isBlocked(String blockingUser, String blockedUser){
        if(blockingMap.containsKey(blockingUser))
            return blockingMap.get(blockingUser).contains(blockedUser);
        return false;
    }


    public void clear() {
        registerdUsers.clear();
        loggedUsers.clear();
    }

}
