package bgu.spl.mics.application.services;

import bgu.spl.mics.Message;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import org.junit.Test;

import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private int tickCount = 0;
    private int id;
    private boolean isTraining = false;
    private TrainModelEvent currTrainModelEvent;
    private Model currModel;

    public GPUService(String type, Model m, int id) {
        super("GPU Service");
        gpu = new GPU(type, m, id);
        this.id = id;
    }

    public GPUService(){
        super("GPU Service");
    }

    @Override
    protected void initialize() {

        subscribeEvent(TrainModelEvent.class, trainModelEvent->
        {
            iAmBusy = true;
            currTrainModelEvent = trainModelEvent;
            currModel = trainModelEvent.getModel();
            gpu.setModel(currModel);
            gpu.splitModel(trainModelEvent.getModel());
            gpu.sendToClusterBatchesFromGPU();

        } );


        subscribeEvent(TestModelEvent.class, testModelEvent -> {
            testModel(testModelEvent);
        } );


        subscribeBroadcast(TickBroadcast.class, broadcast ->
        {
            boolean doneProccessing = false;
            boolean doneTesting = false;
            tickCount++;
            gpu.addTick();
            if(isTraining()){
                doneProccessing = gpu.trainModels();
                if(doneProccessing){
                    Model curr = gpu.getModel();
                    curr.setStatus("Trained");
                    currTrainModelEvent.setModel(curr);
                    gpu.getModel().setStatus("Trained");
                    complete(currTrainModelEvent, currTrainModelEvent.getModel());
                    //iAmBusy = false;
                }
            }
            else if(isTested()){
                for(Message m : otherMsgQueue){
                    if(m instanceof PublishResultsEvent){
                        callbacksQueue.get(m.getClass()).call(m);
                        otherMsgQueue.remove(m);
                    }
                }
                doneProccessing = false;
                iAmBusy = false;
                currModel = null;
            }
            else if( isTrained()){
                for(Message m : otherMsgQueue){
                    if(m instanceof TestModelEvent){
                        callbacksQueue.get(m.getClass()).call(m);
                        otherMsgQueue.remove(m);
                    }
                }
            }
        });
    }



    private boolean isTested() {
        if(gpu.getModel() != null)
            return getModelStatus().equals("Tested");
        return false;

    }

    private boolean isTrained() {
        if(gpu.getModel() != null)
            return getModelStatus().equals("Trained");
        return false;

    }

    private boolean isTraining() {
        if(gpu.getModel() != null)
            return getModelStatus().equals("Training");
        return false;
    }

    private void testModel(TestModelEvent testModelEvent) {
        Random rand = new Random();
        int result = rand.nextInt(100);
        String degree = testModelEvent.getDegree();
        if(degree.equals("MSc")){
            if(result >= 60)
                testModelEvent.setResult("Good");
            else
                testModelEvent.setResult("Bad");
        }
        else if(degree.equals("PhD")){
            if(result >= 80)
                testModelEvent.setResult("Good");
            else
                testModelEvent.setResult("Bad");
        }
        complete(testModelEvent, testModelEvent.getModel());
    }


    private String getModelStatus() {
        return gpu.getModel().getStatus();
    }

    public int getId() {
        return id;
    }

    public GPU getGpu() {
        return gpu;
    }

    public int getTimeUsed() {
        return gpu.getTimeUsed();
    }

}
