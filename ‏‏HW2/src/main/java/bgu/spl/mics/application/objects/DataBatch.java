package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {

    private String dataType;
    private boolean isProcessed;
    private  int start_index;
    private Data data;
    private int GPUId;

    public DataBatch(int start_index, Data data, int gpuId, String dataType){
        this.start_index = start_index;
        this.data = data;
        this.GPUId = gpuId;
        this.dataType = dataType;
    }



    public String getType() {
        return dataType;
    }

    public int getStart_index() {
        return start_index;
    }

    public void setStart_index(int start_index) {
        this.start_index = start_index;
    }

    public int getGPUId() {
        return GPUId;
    }

    public void setGPUId(int GPUId) {
        this.GPUId = GPUId;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed() {
        isProcessed = true;
    }
}
