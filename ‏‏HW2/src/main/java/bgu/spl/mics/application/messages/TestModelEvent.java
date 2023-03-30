package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TestModelEvent implements Event<Model> {

    Model model;

    public TestModelEvent(){
    }

    public TestModelEvent(Model m){
        model = m;
    }



    public void setModel(Model m){
        model = m;
    }




    public Model getModel() {
        return model;
    }


    public void setResult(String result) {
        model.setresult(result);
    }

    public String getDegree() {
        return model.getStudentDegree();
    }
}
