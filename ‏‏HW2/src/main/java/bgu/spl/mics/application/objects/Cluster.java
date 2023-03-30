package bgu.spl.mics.application.objects;


import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private static ArrayList<CPU> CPUS;
	private static ArrayList<GPU> GPUS;
	private static ConcurrentHashMap<Integer , ConcurrentLinkedQueue<DataBatch>> readyDataBatches;
	private static ConcurrentHashMap<Integer , ConcurrentLinkedQueue<DataBatch>> unReadyDataBatches;
	private AtomicInteger roundRobinCpu = new AtomicInteger(0);

	private AtomicInteger batchesProcessed= new AtomicInteger();

	private static Cluster instance = null;

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		if(instance == null){
			instance = new Cluster();
			CPUS = new ArrayList<>();
			GPUS = new ArrayList<>();
			readyDataBatches = new ConcurrentHashMap<>();
			unReadyDataBatches = new ConcurrentHashMap<>();
		}
		return instance;
	}


	public void addCPU(CPU cpu){
		CPUS.add(cpu);
	}

	public void addGPU(GPU gpu){
		GPUS.add(gpu);
	}

	public void setLists(){
		for (GPU gpus : GPUS) {
			readyDataBatches.put(gpus.getId(), new ConcurrentLinkedQueue<>());
			unReadyDataBatches.put(gpus.getId(), new ConcurrentLinkedQueue<>());
		}
	}



	public boolean canISendFiveBatches(int id) {
		if(unReadyDataBatches == null || unReadyDataBatches.isEmpty())
			return false;
		return unReadyDataBatches.get(id).size() < 100;
	}


	public  void acceptBatches(ArrayList<DataBatch> send, int id) {
		unReadyDataBatches.get(id).addAll(send);
		for(CPU cpu : CPUS){
			cpu.ProcessingAllData();
		}
	}

	private boolean cpusAreBusy() {
		for(CPU cpu : CPUS){
			if(!cpu.isProcessingNow()){
				return false;
			}
		}
		return true;
	}

	public synchronized ArrayList<DataBatch> GPURequestMoreBatches(int id, int memory) {
		ArrayList<DataBatch> send = new ArrayList<>();
		for(int i = 0; i < memory; i++) {
			if(!readyDataBatches.isEmpty() &&  readyDataBatches.get(id).size() > 0){
				send.add(readyDataBatches.get(id).remove());
			}
		}
		return send;
	}

	public ArrayList<DataBatch> CPURequestMoreBatches(int id) {
		int howMuchToSend = ChooseCPU(id);
		int gpuId = ChooseGPUToTakebatches();
		if(gpuId == -1)
			return new ArrayList<>();
		ArrayList<DataBatch> send = AddDataToSend(howMuchToSend, gpuId);
		return send;
    }

	private ArrayList<DataBatch> AddDataToSend(int howMuchToSend, int gpuId) {
		ArrayList<DataBatch> send = new ArrayList<>();
		synchronized (this){
			for(int i = 0; i < howMuchToSend ; i++) {
				if(!unReadyDataBatches.isEmpty() && unReadyDataBatches.get(gpuId).size() > 0) {
					send.add(unReadyDataBatches.get(gpuId).remove());
				}
			}
			return send;
		}
	}

	private int ChooseGPUToTakebatches() {
		Random rand = new Random();
		int chosen = rand.nextInt(50);
		int out = 0;

		if(chosen >= 25){
		//This is randomaly choosing
			ArrayList<Integer> temp = new ArrayList<>();
			for(GPU gpu : GPUS){
				if(gpu.getModel() == null){
					continue;
				}
				if(gpu.getModel().getStatus().equals("Training") && !unReadyDataBatches.get(gpu.getId()).isEmpty())
					temp.add(gpu.getId());
			}
			if(temp.isEmpty())
				return -1;
			int random = rand.nextInt(temp.size());
			return temp.get(random);
		}
		else{
		//This is taking the one closest to being done
			int min = 0;
			for(GPU g : GPUS){
				if(g.getDataBatchSize() < GPUS.get(min).getDataBatchSize())
					min = g.getId();
			}
			out = min;
		}
		return out;
	}

	private int ChooseCPU(int id) {
		for(CPU c : CPUS){
			if(c.getId() == id)
				return c.getCores() * 2;
		}
		return 1;
	}

	public void acceptReadyBatchesFromCPU(DataBatch d) {
		int gpuId = d.getGPUId();
		batchesProcessed.incrementAndGet();
		readyDataBatches.get(gpuId).add(d);
	}


	public int getBatchesProccessed() {
		return batchesProcessed.get();
	}

	public synchronized void acceptBatchesFromGPU(ArrayList<DataBatch> send, int gpuId) {
		for(CPU cpu : CPUS){
			if(cpu.canISendMoreBatchesToCPU()){
				ArrayList<DataBatch> sendToCPU = takeSomeOfTheSendBatches(send, cpu.getId());
				cpu.addBatches(sendToCPU);
			}
		}
		unReadyDataBatches.get(gpuId).addAll(send);
	}

	private ArrayList<DataBatch> takeSomeOfTheSendBatches(ArrayList<DataBatch> send, int cpuId) {
		ArrayList<DataBatch> sendToCPU = new ArrayList<>();
		int howMuchToSend = ChooseCPU(cpuId);
		for(int i = 0; i < howMuchToSend ; i++) {
			if(!send.isEmpty() ) {
				sendToCPU.add(send.remove(0));
			}
		}
		return sendToCPU;
	}

}
