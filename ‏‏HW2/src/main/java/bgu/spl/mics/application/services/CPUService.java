package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

/**
 * CPU service is responsible for handling the {@link //DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    int tickCount = 0;
    CPU cpu;
    private int id;

    public CPUService(int cores, int id) {
        super("CPU Service");
        cpu = new CPU(cores, id);
        this.id = id;
    }

    @Override
    protected void initialize() {


        subscribeBroadcast(TickBroadcast.class, broadcast ->
        {
            tickCount++;
            cpu.addTick();
            if(inMiddleOfProcessing() && tickCount > 5){
                cpu.ProcessingAllData();
            }
        });

    }

    private boolean inMiddleOfProcessing() {
        return cpu.isProcessingNow();
    }


    public int getId() {
        return id;
    }

    public CPU getCpu() {
        return cpu;
    }

    public int getCores(){
        return cpu.getCores();
    }

    public int getTimeUsed() {
        return cpu.getTimeUsed();
    }
}
