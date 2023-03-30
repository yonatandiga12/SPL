package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;

public class PublishConferenceBroadcast implements Broadcast {


    private ArrayList<Model> modelsToPublish;

    public PublishConferenceBroadcast(){
        modelsToPublish = new ArrayList<>();
    }

    public PublishConferenceBroadcast(ArrayList<Model> models){
        modelsToPublish = models;
    }


    public ArrayList<Model> getModels() {
        return modelsToPublish;
    }

    public void addPublish(Model model){
        modelsToPublish.add(model);
    }


}
