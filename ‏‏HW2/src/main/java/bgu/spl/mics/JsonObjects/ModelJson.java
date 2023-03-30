package bgu.spl.mics.JsonObjects;


import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;
import java.util.HashMap;

public class ModelJson {

    private String name;
    private HashMap<String, Object> data;
    private String status;
    private String result;

    public ModelJson(Model m){
        name = m.getName();
        data = new HashMap<>();
        status = m.getStatus();
        result = String.valueOf(m.getResult());
        data.put( "size" , m.getData().getSize());
        data.put("type" , m.getData().getType());
    }


    public String getName() {
        return name;
    }

    public String getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }
}
