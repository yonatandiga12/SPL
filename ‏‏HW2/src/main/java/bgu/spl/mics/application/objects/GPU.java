package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {


    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private  Cluster cluster = Cluster.getInstance();
    private  Model model;
    private Data currData;
    private boolean isTheDataProcessed;
    private ArrayList<DataBatch> dataBatches = new ArrayList<>();
    private ArrayList<DataBatch> readyDataBatches = new ArrayList<>();
    private int memory;
    public int dataLeft;
    private AtomicInteger tickCount= new AtomicInteger();
    private AtomicInteger timeUsed = new AtomicInteger();

    private int ticksDifference;
    private int id;
    private int sendingTime;


    public GPU(String type, Model m, int id) {
        setType(type);
        this.id = id;
        memory = getNumOfMemory();

    }

    public GPU(String type, Model m) {
        setType(type);
        this.model = m;
    }


    public int getMemory() {
        return memory;
    }
    public int getId(){
        return id;
    }
    public int getReadyDataBatchesSize(){
        return readyDataBatches.size();
    }

    public int getSendingTime() {
        return sendingTime;
    }

    public void addTick() {
        tickCount.incrementAndGet();
        ticksDifference++;

    }



    public int getdataBatchesSize(){
        return dataBatches.size();
    }

    public int getNumOfMemory()
    {
        if(type.equals(Type.RTX3090))
            return 32;
        if(type.equals(Type.GTX1080))
            return 8;
        if(type.equals(Type.RTX2080))
            return 16;
        return 0;
    }

    public int getDataSize() {
        return currData.getSize();
    }

    public String getType()
    {
        if(type.equals(Type.RTX3090))
            return "RTX3090";
        else if(type.equals(Type.GTX1080))
            return "GTX1080";
        else if(type.equals(Type.RTX2080))
            return "RTX2080";
        return "";
    }

    public void setType(String type)
    {
        if(type.equals("RTX3090"))
            this.type= Type.RTX3090;
        else if(type.equals("GTX1080"))
            this.type = Type.GTX1080;
        else if(type.equals( "RTX2080"))
            this.type = Type.RTX2080;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model m) {
        model = m;
    }

    public void splitModel(Model m) {
        setModel(m);
        getModel().setStatus("Training");
        prepareData();
    }

    public void trainModel(DataBatch d) {
        if(type.equals(Type.RTX3090))
        {
            if(ticksDifference >= 1) {
                readyDataBatches.remove(d);
                dataLeft--;
                ticksDifference = 0;
            }
        }
        else if(type.equals(Type.GTX1080))
        {
            if(ticksDifference >= 4) {

                readyDataBatches.remove(d);
                dataLeft--;
                ticksDifference=0;
            }
        }
        else if(type.equals(Type.RTX2080))
        {
            if(ticksDifference >= 2) {
                readyDataBatches.remove(d);
                dataLeft--;
                ticksDifference=0;
            }
        }
        addTimeUsed();
        //addTick();
    }

    private void addTimeUsed() {
        timeUsed.incrementAndGet();
    }


    public boolean trainModels() {

        if( dataLeft == 0){
            return true;
        }
        else if(readyDataBatches.isEmpty()) {
            requestMoreBatchesFromCluster();
        }
        else{
            //ticksDifference++;
            trainModel(readyDataBatches.get(0));
        }
        if(dataBatches.size() > 0 && canIsendToClusterFiveBatches()){
            sendToClusterBatchesFromGPU();
        }
        return false;
    }



    private void requestMoreBatchesFromCluster() {
        ArrayList<DataBatch> recieved = cluster.GPURequestMoreBatches(id, memory);
        readyDataBatches.addAll(recieved);
    }

    public void sendToClusterBatchesFromGPU() {
        int i = 0;
        ArrayList<DataBatch> send = new ArrayList<>();
        while(i < 20){
            if(dataBatches.size() > 0){
                send.add(dataBatches.remove(0));
            }
            i++;
        }
        if(tickCount.get() < 100)
            cluster.acceptBatches(send, id);
        else{
            cluster.acceptBatchesFromGPU(send, id);
        }
    }

    private boolean canIsendToClusterFiveBatches() {
        return cluster.canISendFiveBatches(id);
    }


    public int getDataBatchSize() {
        return dataBatches.size();

    }


    public void prepareData() {
        currData = model.getData();
        int index = 0;
        dataBatches= new ArrayList<>();
        for(int i = 0; i < currData.getSize() / 1000; i++) {
            DataBatch temp = new DataBatch(index, currData, id, currData.getType());
            dataBatches.add(temp);
            index+=1000;
        }
        dataLeft = dataBatches.size();
    }

    public int getTimeUsed() {
        return timeUsed.get();
    }


}