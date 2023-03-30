package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.sql.SQLOutput;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {

    private String state = "PreTraining";
    private Student student;

    private TrainModelEvent trainModelEvent;
    private Future<Model> resultTrainModel;
    private  Future<Model> resultTestModel;
    private Future<Model> resultPublishModel;

    private Model currModel;

    public StudentService(String name, String department, String degree) {
        super("Student Service");

        student = new Student(name, department, degree);

    }

    public Student getStudent(){
        return student;
    }
    public Model getUntrainedModel(int i){ return student.getUnTrainedModel(i); }
    public Model getTrainedModel(int i){ return student.getTrainedModel(i); }

    public void addTrainedModel(Model m){
        student.addTrainedModel(m);
    }

    @Override
    protected void initialize() {

        arrangeModelsbySize();

        subscribeBroadcast(TickBroadcast.class, broadcast ->
        {
            if(state.equals("PreTraining") && student.unTrainedModelsLeft() != 0){
                iAmBusy = true;
                trainModelEvent = new TrainModelEvent( getUntrainedModel(0));
                resultTrainModel = sendEvent(trainModelEvent);
                state = "TrainModel";
            }

            else if( state.equals("TrainModel") && resultTrainModel.isDone()){
                try {
                    currModel = trainModel(resultTrainModel, resultTrainModel.get());
                    //System.out.println("In student service done training the model " + currModel.getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            else if(state.equals("TestModel")  ){
                if(resultTestModel == null){
                    resultTestModel = sendEvent(new TestModelEvent(currModel));
                }
                if( resultTestModel.isDone()){
                    currModel = testModel(resultTestModel, currModel);
                }
            }
            else if(state.equals("PublishModel")){
                if(resultPublishModel == null){
                    resultPublishModel = sendEvent(new PublishResultsEvent<>(currModel));
                }
                if( resultPublishModel.isDone())
                    currModel = publishModel(resultPublishModel, currModel);
            }
            else if(state.equals("AfterPublish")){
                state = "PreTraining";
                currModel = null;
                resultPublishModel =null;
                resultTestModel = null;
                resultTrainModel = null;
                iAmBusy = false;
            }
        });


        subscribeBroadcast(PublishConferenceBroadcast.class, conferenceBroadcast ->{
            iAmBusy = true;
            for(Model m : conferenceBroadcast.getModels()){
                if( student.hasModel(m)){
                    student.addPublication();
                }
                else{
                    student.addPapaerRead();
                }
            }
            iAmBusy = false;
        });
    }

    private void arrangeModelsbySize() {
        student.arrangeModelListBySize();
    }


    private Model trainModel(Future<Model> resultTrainModel, Model currModel) {
        Model newModel = currModel;
        try {
            currModel = resultTrainModel.get();
            student.moveToTrainedModels(currModel);
            currModel = newModel;
            state = "TestModel";

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return student.getTrainedModel(currModel.getName());
    }

    private Model testModel(Future<Model> resultTestModel, Model currModel) {
        Model newModel = currModel;
        try {
            currModel = resultTestModel.get();
            student.updateModelStatus(currModel,"Tested");
            currModel = newModel;
            state = "PublishModel";
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return student.getTrainedModel(currModel.getName());
    }

    private Model publishModel(Future<Model> resultPublishModel, Model currModel) {
        Model newModel = currModel;
        try {
            currModel = resultPublishModel.get();
            currModel = newModel;
            state = "AfterPublish";

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return student.getTrainedModel(currModel.getName());
    }




    public void addUnTrainedModel(Model model) {
        student.addunTrainedModel(model);
    }
}
