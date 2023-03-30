package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    enum Status {
        PreTrained, Training, Trained, Tested
    }
    enum Result {
        None, Good, Bad
    }

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Result result;

    public Model(String name, Data data, Student student, String status , String result ){
        this.name = name;
        this.data = data;
        this.student = student;
        setStatus(status);
        setresult(result);
    }



    public void setresult(String r){
        switch(r){
            case "None":
                result = Result.None;
                break;
            case "Bad":
                result = Result.Bad;
                break;
            case "Good":
                result = Result.Good;
                break;
        }
    }

    public boolean resultIsGood() {
        return result.equals(Result.Good);
    }

    public void setStatus(String s) {
        switch (s) {
            case "Training":
                status = Status.Training;
                break;
            case "Trained":
                status = Status.Trained;
                break;
            case "Tested":
                status = Status.Tested;
                break;
            case "PreTrained":
                status = Status.PreTrained;
                break;
        }
    }

    public Result getResult() {
        return result;
    }

    public Data getData() {
        return data;
    }

    public String getName(){
        return name;
    }

    public String getStudentName(){
        return student.getName();
    }

    public String getStatus() {
        if(status.equals(Status.Tested))
            return "Tested";
        else if(status.equals(Status.PreTrained))
            return "PreTrained";
        else if(status.equals(Status.Trained))
            return "Trained";
        else
            return "Training";

    }

    public String getStudentDegree() {
        //return student.getDegree();
        return "MSc";
    }




}
