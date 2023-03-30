package bgu.spl.net.Messages;

public class PmMsg extends Message{

    private String sendingDate;
    private String reciever;
    private boolean recieved = false;
    private Notification notification;

    public PmMsg(String user, String r, String _content) {
        super(user, _content);
        reciever = r;
    }

    public String getSendingDate() {
        return sendingDate;
    }

    public void setSendingDate(String sendingDate) {
        this.sendingDate = sendingDate;
    }

    public boolean recivedAlready() {
        return recieved;
    }

    public void setRecieved(String username) {
        this.recieved = true;
    }

    @Override
    public boolean needToSendToUser(String username) {
        return (reciever.equals(username)) && !recieved;
    }

    @Override
    public String getType() {
        return "0";
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
