package bgu.spl.mics.JsonObjects;

import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.ArrayList;

public class StudentJson {

    private String name;
    private String department;
    private String status;
    private int publications;
    private int papersRead;
    private ArrayList<ModelJson> trainedModels;

    public StudentJson(Student s){

        name = s.getName();
        department = s.getDepartment();
        status = s.getDegree();
        publications = s.getPublications();
        papersRead = s.getPapersRead();
        trainedModels = new ArrayList<>();

        for(Model m : s.getTrainedModels()){
            trainedModels.add(new ModelJson(m));
        }

    }
}
