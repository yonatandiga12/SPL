package bgu.spl.net.api.bidi;

import bgu.spl.net.DataBase;
import bgu.spl.net.Messages.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BGUProtocol<T> implements BidiMessagingProtocol<T>{

    private boolean shouldTerminate = false;

    private ConnectionsImpl<T> connectionImpl;
    private DataBase dataBase;
    private int connectionId;
    private String userName = "";

    public BGUProtocol(DataBase dataBase) {
        this.dataBase = dataBase;
        connectionImpl = ConnectionsImpl.getInstance();
    }

    @Override
    public void start(int connectionId, ConnectionsImpl<T> connections) {
        connectionImpl = connections;
        this.connectionId = connectionId;
    }

    @Override
    public void process(T msg) {
        String message = (String) msg;
        short opCode = Short.parseShort(message.substring(0, 2));
        String restOfMsg = message.substring(2);
        switch (opCode) {
            case 1:
                RegisterUser(restOfMsg);
                break;
            case 2:
                LoginUser(restOfMsg);
                break;
            case 3:
                Logout();
                break;
            case 4:
                follow(restOfMsg);
                break;
            case 5:
                postMsg(restOfMsg);
                break;
            case 6:
                pmMsg(restOfMsg);
                break;
            case 7:
                logStat();
                break;
            case 8:
                stat(restOfMsg);
                break;
            case 12:
                block(restOfMsg);
            default:
        }
    }


    private void RegisterUser(String msg) {
        boolean t = msg.contains("\0");
        //System.out.println(t);
        String userName = msg.substring(0, msg.indexOf("\0"));
        msg = msg.substring(msg.indexOf("\0") + 1);
        String password = msg.substring(0, msg.indexOf("\0"));
        msg = msg.substring(msg.indexOf("\0") + 1);
        String birthday = msg.substring(0, 10);

        String result = dataBase.registerUser(userName, password, birthday);
        if(result.contains("ack")){
            connectionImpl.addUser(connectionId, userName);
        }
        handleMsg( "01", result);
    }



    private void LoginUser(String msg) {
        String userName = msg.substring(0, msg.indexOf("\0"));
        msg = msg.substring(msg.indexOf("\0") + 1);
        String password = msg.substring(0, msg.indexOf("\0"));
        msg = msg.substring(msg.indexOf("\0") + 1);
        short captcha = Short.parseShort(msg.substring(0, 1));
        String result = dataBase.loginUser(userName, password, captcha);
        connectionImpl.updateId(userName, connectionId);
        handleMsg("02", result);
        if(result.contains("ack")){
            ArrayList<String> sendTo = dataBase.checkIfIHaveMsgWaiting(userName);
            if(!sendTo.isEmpty())
                sendNotificationToMe(sendTo);
        }
    }

    private void Logout() {

        shouldTerminate = true;
        String result = dataBase.logout(getName());
        handleMsg("03", result);
        connectionImpl.disconnect(connectionId);
    }



    private void follow(String msg) {
        short captcha = Short.parseShort(msg.substring(0, 1));
        String username = msg.substring( 1, msg.indexOf("\0"));

        String result = dataBase.follow(getName() ,username, captcha );
        handleMsg("04", result);
    }



    private void postMsg(String msg) {
        String content = msg.substring(0, msg.indexOf("\0"));
        ArrayList<Notification> result = dataBase.postMessage(getName() , content);
        CreateMsg("05" ,result);
    }



    private void pmMsg(String msg) {
        String sendTo = msg.substring(0, msg.indexOf("\0"));
        msg = msg.substring(msg.indexOf("\0") + 1);
        String content = msg.substring(0, msg.indexOf("\0"));
        msg = msg.substring(msg.indexOf("\0") + 1);
        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        content += "  " + date;
        ArrayList<Notification> result = dataBase.pmMsg(getName() , sendTo, content);
        CreateMsg("06" ,result);
    }


    //preapres the msg to be ready for the encoder with ","
    private void CreateMsg(String opCode, ArrayList<Notification> result) {
        ConcurrentHashMap<String, String> output = new ConcurrentHashMap<>();
        if(result.isEmpty()) {                                        // do nothing, user is blocked
        }
        else if(result.get(0).isError())                            // need to send error
            sendError(opCode);
        else{                                                        //notifications are ok
            for(Notification notif : result){
                String msg = notif.getOpCode() + "," + notif.getType() + "," + notif.getPostingUser() + "," + notif.getContent();
                output.put(notif.getSendToUser(), msg);
            }
            sendNotificationToOther(output);
        }
    }

    private void sendNotificationToOther(ConcurrentHashMap<String, String> output) {
        for(Map.Entry<String, String> entry : output.entrySet()){
            int id = connectionImpl.getUserId(entry.getKey());
            if(id != -1)
                connectionImpl.send(id, entry.getValue());
        }
    }


    private void sendNotificationToMe(ArrayList<String> sendTo) {
        for(String curr : sendTo){
            connectionImpl.send(connectionId, curr);
        }
    }



    private void handleMsg(String msgOpCode ,String result) {
        if(result.contains("error")){
            sendError(msgOpCode);
        }
        else if(result.contains("ack")){
            if(result.length() > 4)
                msgOpCode = msgOpCode + result.substring(3);       //if the ack msg need more information
            connectionImpl.send(connectionId, "10" + msgOpCode + ",");
        }
    }


    private void handleMsg(String msgOpCode , ArrayList<String> result) {
        ArrayList<String> output = new ArrayList<>();
        if(result.size() == 0){
            sendError(msgOpCode);
        }
        else{
            for(String curr : result){
                output.add(msgOpCode + "," + curr);
            }
            sendAck(output);
        }
    }

    private void sendAck(ArrayList<String> output) {
        for(String curr : output){
            connectionImpl.send(connectionId, "10" + curr + ",");
        }
    }


    private void sendError(String msgOpCode) {
        String msg = "11" + msgOpCode;
        connectionImpl.send(connectionId, msg);
    }


    private void logStat() {
        ArrayList<String> result = dataBase.logStat(getName());
        handleMsg("07" ,result);
    }


    private void stat(String msg) {
        //String list = msg.substring(0, msg.indexOf("\0"));
        ArrayList<String> users = new ArrayList<>();
        while(msg.indexOf('|') != -1){
            users.add(msg.substring(0, msg.indexOf("|")));
            msg = msg.substring(msg.indexOf("|") + 1);
        }
        if(!msg.equals("\0")){
            users.add(msg.substring(0, msg.indexOf("\0")));
        }
        ArrayList<String> result = dataBase.stat(getName(), users);
        handleMsg("08" ,result);
    }


    private void block(String msg) {
        String userName = msg.substring(0, msg.indexOf("\0"));

        String result = dataBase.block(getName() ,userName);
        handleMsg("12", result);
    }

    public String getName(){
        if(userName.length() != 0)
            return userName;
        String res = connectionImpl.getUserById(connectionId);
        if(res.length() == 0)
            return "";
        return res;
    }



    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
