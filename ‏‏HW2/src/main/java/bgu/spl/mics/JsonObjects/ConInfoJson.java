package bgu.spl.mics.JsonObjects;

import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;

public class ConInfoJson {

    private String name;
    private int date;
    private ArrayList<ModelJson> Publications;

    public ConInfoJson(ConfrenceInformation conf){

        name = conf.getName();
        date = conf.getDate();
        Publications = new ArrayList<>();
        addToPublish(conf.getPublications());
    }

    private void addToPublish(ArrayList<Model> publications) {
        for(Model m : publications){
            ModelJson modelJson = new ModelJson(m);
            Publications.add(modelJson);
        }
    }
}
