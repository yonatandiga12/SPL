package bgu.spl.mics.application.objects;

import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;

import java.util.*;
import java.util.concurrent.BlockingDeque;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {



    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private ArrayList<Model> untrainedModels;
    private ArrayList<Model> trainedModels;

    public Student(String name, String department, String degree) {
        this.name = name;
        this.department = department;
        setDergree(degree);
        publications = 0;
        papersRead  = 0;
        untrainedModels = new ArrayList<>();
        trainedModels = new ArrayList<>();
    }

    private void setDergree(String d) {
        String degree = d.toLowerCase();
        if(degree.equals("msc"))
            this.status = Degree.MSc;
        else if(degree.equals("phd") )
            this.status = Degree.PhD;
    }

    public void addPapaerRead(){
        papersRead++;
    }

    public void addPublication(){
        publications++;
    }

    public void addunTrainedModel(Model m){
        untrainedModels.add(m);
    }

    public int unTrainedModelsLeft() {
        return untrainedModels.size();
    }

    public void addTrainedModel(Model m){
        trainedModels.add(m);
    }

    public String getDepartment() {
        return department;
    }

    public int getPapersRead() {
        return papersRead;
    }


    public int getPublications() {
        return publications;
    }


    public String getName() {
        return name;
    }

    public Model getUnTrainedModel(int i){
        if(untrainedModels.size() > 0)
            return untrainedModels.get(i);
        return null;
    }

    public Model getTrainedModel(int i){
        if(trainedModels.size() > 0)
            return trainedModels.get(i);
        return null;
    }

    public Model getTrainedModel(String name) {
        for (Model m : trainedModels) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }


    public String getDegree() {
        if(status.equals(Degree.MSc))
            return "MSc";
        else
            return "PhD";
    }


    public boolean hasModel(Model m) {
        return trainedModels.contains(m) || untrainedModels.contains(m);
    }


    public void updateModelResult(Model currModel, String result) {
        if(trainedModels.contains(currModel)) {
            for (Model m : trainedModels) {
                if (m.getName().equals(currModel.getName())) {
                    m.setresult(result);
                }
            }
        }
        else if(untrainedModels.contains(currModel)){
            for(Model m : untrainedModels){
                if(m.getName().equals(currModel.getName())) {
                    m.setresult(result);
                }
            }
        }
    }

    public void updateModelStatus(Model currModel, String status) {
        if(trainedModels.contains(currModel)){
            for(Model m : trainedModels){
                if(m.getName().equals(currModel.getName())) {
                    m.setStatus(status);
                }
            }
        }
        else if(untrainedModels.contains(currModel)){
            for(Model m : untrainedModels){
                if(m.getName().equals(currModel.getName())) {
                    m.setStatus(status);
                }
            }
        }
    }

    public ArrayList<Model> getTrainedModels() {
        return trainedModels;
    }


    public void moveToTrainedModels(Model currModel) {
        trainedModels.add(currModel);
        untrainedModels.remove(currModel);
    }


    public void arrangeModelListBySize() {
        ArrayList<Model> temp = new ArrayList<>();
        TreeMap<Integer, Model> idsAndSum = new TreeMap<>();
        for(Model model : untrainedModels){
            String type = model.getData().getType();
            int size = model.getData().getSize() / 1000;
            if(type.equals("Images")){
                size = 4 * size;
            }
            else if(type.equals("Text")){
                size = 2 * size;
            }
            while(idsAndSum.containsKey(size)){
                size++;
            }
            idsAndSum.put(size, model);
        }
        for(Map.Entry<Integer, Model> entry : idsAndSum.entrySet()){
            temp.add(entry.getValue());
        }
        untrainedModels = temp;
    }



}
