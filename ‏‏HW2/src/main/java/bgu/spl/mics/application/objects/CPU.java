package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */



public class CPU {

    public int cores;
    public ArrayList<DataBatch> data;
    private Cluster cluster = Cluster.getInstance();
    private int id;
    public AtomicInteger tickCount= new AtomicInteger();
    public AtomicInteger timeUsed = new AtomicInteger();
    public int ticksDifference;
    private boolean proccessingNow = false;


    public CPU(int core, int id) {
        this.cores = core;
        data = new ArrayList<>();
        this.id = id;
    }

    public int getCores ()
    {
        return cores;
    }

    public ArrayList<DataBatch> getData() {
        return data;
    }

    public void addTick(){
        tickCount.incrementAndGet();
        ticksDifference++ ;
    }

    public int processingData()
    {
        return 0;
    }


    public String getTypeOfData() {
        return data.get(0).getType();
    }

    public int getTimeToProcess(DataBatch dataBatch) {
        int timeToProcess=0 ;
        if(dataBatch == null){
            return 0;
        }
        String k = dataBatch.getType();
        if (k.equals("Images")) {
            timeToProcess  = (32 / cores) * 4;
        }
        else if (k.equals("Text")) {
            timeToProcess  = (32 / cores) * 2;
        }
        else if (k.equals("Tabular")) {
            timeToProcess  = (32 / cores);
        }
        return timeToProcess;
    }

    public void ProcessingAllData() {
        proccessingNow = true;
        if(data!= null && !data.isEmpty()) {
            //ticksDifference++ ;
            processingOneData(data.get(0));
        }
        else {
            ArrayList<DataBatch> recieved = cluster.CPURequestMoreBatches(id);
            data.addAll(recieved);
        }
    }


    public void processingOneData(DataBatch dataBatch) {
        int timeToProcess = getTimeToProcess(dataBatch);
        //addTimeUsed();
        if(ticksDifference >= timeToProcess) {
            addTimeUsed();
            ticksDifference = 0;
            dataBatch.setProcessed();
            data.remove(dataBatch);
            sendBack(dataBatch);
        }
    }

    private void addTimeUsed() {
        timeUsed.incrementAndGet();
    }


    public void sendBack (DataBatch d) {
        cluster.acceptReadyBatchesFromCPU(d);
    }



    public void addBatches(ArrayList<DataBatch> unReadyDataBatchesToSet){
        data.addAll(unReadyDataBatchesToSet);
        //ProcessingAllData();
    }

    public boolean isProcessingNow() {
        return proccessingNow;
    }

    public int getId() {
        return id;
    }

    public int getTimeUsed() {
        return timeUsed.get();
    }

    public boolean canISendMoreBatchesToCPU() {
        return data.size() < (8 * cores);
    }
}