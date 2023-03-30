package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {

    Model model;

    public TrainModelEvent(){
    }

    public TrainModelEvent(Model m){
        model = m;
    }



    public void setModel(Model m){
        model = m;
    }


    public void setStatus(String s){
        model.setStatus(s);
    }

    public void setResult(String r){
        model.setresult(r);
    }

    public Model getModel() {
        return model;
    }
}
