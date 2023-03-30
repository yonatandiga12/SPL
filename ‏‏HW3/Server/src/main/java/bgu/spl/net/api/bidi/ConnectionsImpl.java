package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T>{

    private static ConcurrentHashMap<Integer, ConnectionHandler> map;
    private static ConcurrentHashMap<Integer, String> usersId;


    private static ConnectionsImpl instance = null;
    private static boolean isDone = false;


    //Singleton
    private ConnectionsImpl(){
        map = new ConcurrentHashMap<>();
        usersId = new ConcurrentHashMap<>();
    }

    public static ConnectionsImpl getInstance(){
        if(!isDone){
            synchronized (ConnectionsImpl.class){
                if(!isDone){
                    instance = new ConnectionsImpl();
                    isDone = true;
                }
            }
        }
        return instance;
    }


    @Override
    public boolean send(int connectionId, Object msg) {
        if(map.containsKey(connectionId)) {
            map.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(Object msg){}

    @Override
    public void disconnect(int connectionId) {
        map.remove(connectionId);
    }


    public void addConnection(ConnectionHandler connectionHandler, int Id){
        map.put(Id, connectionHandler);
    }


    public int getUserId(String userName){
        for(Map.Entry<Integer, String> entry : usersId.entrySet()){
            if(entry.getValue().equals(userName))
                return entry.getKey();
        }
        return -1;
    }

    public String getUserById(int id){
        if(usersId.containsKey(id))
            return usersId.get(id);
        return "";
    }

    public void addUser(int idCounter ,String userName){
        usersId.putIfAbsent(idCounter, userName);
    }


    public void updateId(String userName, int connectionId) {
        if(usersId.containsValue(userName)){
            int key = -1;
            for(Map.Entry<Integer, String> entry : usersId.entrySet()){
                if(entry.getValue().equals(userName))
                    key = entry.getKey();
            }
            if(key != -1){
                usersId.remove(key);
                usersId.put(connectionId, userName);
            }
        }
    }
}
