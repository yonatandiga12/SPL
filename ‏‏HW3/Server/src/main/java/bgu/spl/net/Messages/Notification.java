package bgu.spl.net.Messages;

public class Notification {

    private int type;   //0 for post 1 for pm
    private String postingUser;
    private String content;
    private String sendToUser;
    private boolean error = false;

    public Notification(String type, String postingUser, String content, String sendToUser){
        setType(type);
        this.postingUser = postingUser;
        this.content = content;
        this.sendToUser = sendToUser;
    }

    public Notification(String sendToUser){
        error = true;
        this.sendToUser = sendToUser;
    }

    private void setType(String type) {
        if(type.equals("pm"))
            this.type = 0;
        else
            this.type = 1;
    }

    public String getPostingUser() {
        return postingUser;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public String getSendToUser() {
        return sendToUser;
    }

    public boolean isError() {
        return error;
    }

    public String getOpCode() {
        return "09";
    }
}
