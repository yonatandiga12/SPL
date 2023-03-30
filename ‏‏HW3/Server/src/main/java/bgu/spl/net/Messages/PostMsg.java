package bgu.spl.net.Messages;

import java.util.ArrayList;

public class PostMsg extends Message{

    private ArrayList<String> sendTo;

    public PostMsg(String user, String _content, ArrayList<String> _sendTo){
        super(user, _content);
        sendTo = _sendTo;
    }


    public ArrayList<String> getSendToUsers() {
        return sendTo;
    }

    @Override
    public boolean needToSendToUser(String username) {
        return sendTo.contains(username);
    }

    @Override
    public String getType() {
        return "1";
    }

    @Override
    public void setRecieved(String username) {
        sendTo.remove(username);

    }


}
