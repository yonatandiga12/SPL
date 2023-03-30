package bgu.spl.net.Messages;

public abstract class Message {


    private String postingUser;
    private String content;

    public Message(String user, String _content){
        postingUser = user;
        content = _content;
    }

    public String getContent() {
        return content;
    }

    public String getPostingUser() {
        return postingUser;
    }


    public abstract  boolean needToSendToUser(String username);

    public abstract String getType();

    public abstract void setRecieved(String username);

}
