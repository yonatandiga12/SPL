package bgu.spl.mics.application;
import bgu.spl.mics.JsonObjects.ConInfoJson;
import bgu.spl.mics.JsonObjects.ModelJson;
import bgu.spl.mics.JsonObjects.StudentJson;
import bgu.spl.mics.Waiter;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) {



        if(args.length == 0){
            return;
        }
        HashMap<String, String> json = parseJson(args[0]);
        String outputPath = args[1];
        System.out.println("Running program");

        if (json != null) {
            createObjects(json, outputPath);
        }



    }


    private static void createObjects(HashMap<String, String> json, String outputPath) {
        Object[] jsonArr = json.values().toArray();
        Object[] keysArr = json.keySet().toArray();
        HashMap<StudentService, List<Model>>  studentsAndModels = new HashMap<>();
        ArrayList<CPUService> cpuServices = new ArrayList<>();
        ArrayList<GPUService> gpuServices = new ArrayList<>();
        ArrayList<ConfrenceInformation> confrenceInformations = new ArrayList<>();

        Double[] ticksInfo = new Double[2];
        for (int i = 0; i < keysArr.length; i++) {
            switch ((String) keysArr[i]) {
                case "Students":
                    studentsAndModels = createStudents((ArrayList) jsonArr[i]);
                    break;
                case "GPUS":
                    gpuServices = createGpuServices((ArrayList) jsonArr[i]);
                    break;
                case "Conferences":
                    confrenceInformations = createConferences((ArrayList) jsonArr[i]);
                    break;
                case "CPUS":
                    cpuServices = createCpuServices((ArrayList) jsonArr[i]);
                    break;
                case "TickTime":
                    ticksInfo[0] = (Double) jsonArr[i];
                    break;
                case "Duration":
                    ticksInfo[1] = (Double) jsonArr[i];
                    break;
                default:
            }
        }
        Waiter waiter = new Waiter();
        waiter.justWait();
        createCluster(cpuServices, gpuServices);
        waiter.justWait();
        createStudentsThreads(studentsAndModels);
        waiter.justWait();
        setTimeService(ticksInfo[0], ticksInfo[1]);

        Cluster cluster = Cluster.getInstance();
        int proccessed = cluster.getBatchesProccessed();
        createOutputJson(outputPath , studentsAndModels, confrenceInformations , cpuServices, gpuServices , proccessed );

    }

    private static void createStudentsThreads(HashMap<StudentService, List<Model>> studentsAndModels) {
        int j = 0;
        for(StudentService s : studentsAndModels.keySet()){
            List<Model> currModels = studentsAndModels.get(s);
            for(int i = 0; i < currModels.size(); i++){
                s.addUnTrainedModel(currModels.get(i));
            }
            Thread studentThread = new Thread(s);
            studentThread.setName("Student Service");
            j++;
            studentThread.start();
        }

    }

    private static void createCluster(ArrayList<CPUService> cpuServices, ArrayList<GPUService> gpuServices) {

        Cluster cluster = Cluster.getInstance();
        for(GPUService gpuService:  gpuServices ){
            cluster.addGPU(gpuService.getGpu());
            Thread gpuThread = new Thread( gpuService);
            gpuThread.setName("Gpu Service " + gpuService.getId());
            gpuThread.start();
        }

        cpuServices.sort(Comparator.comparingInt(CPUService::getCores).reversed());
        for(CPUService cpuService : cpuServices){
            cluster.addCPU(cpuService.getCpu());
            Thread cpuThread = new Thread( cpuService);
            cpuThread.setName("Cpu Service " + cpuService.getId());
            cpuThread.start();
        }
        cluster.setLists();

    }

    private static HashMap<String, String> parseJson(String arg) {
        try(Reader reader = new FileReader(arg)){
            Gson gson = new Gson();
            return gson.fromJson(reader, HashMap.class);
            } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        return null;
    }

    private static void setTimeService(Double tickTime, Double duration) {
        int tick = (int) Math.round(tickTime);
        int dur = (int) Math.round(duration);
        Thread timeThread = new Thread( new TimeService(tick, dur));
        timeThread.start();

        try {
            Thread.sleep((long) (tickTime * duration * 1.05));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<ConfrenceInformation> createConferences(ArrayList arr) {
        int i = 0;
        ArrayList<ConfrenceInformation> confrenceInformations = new ArrayList<>();
        for( Object conf : arr){
            String conference = String.valueOf(conf);
            conference = conference.substring(1, conference.length()-1);
            ArrayList<String> list = new ArrayList<>(Arrays.asList(conference.split(",")));
            String name = list.get(0).substring(conference.indexOf('=') + 1);
            String dateString = list.get(1).substring(conference.indexOf('=') + 1).replace('=',' ');
            int date = Double.valueOf(dateString).intValue();
            ConfrenceInformation confrenceInformation = new ConfrenceInformation(name, date);
            confrenceInformations.add(confrenceInformation);
            Thread confernceThread = new Thread(new ConferenceService(confrenceInformation));
            confernceThread.setName("Conference Service " + i);
            i++;
            confernceThread.start();
        }
        return confrenceInformations;
    }

    private static ArrayList<CPUService> createCpuServices(ArrayList arr) {
        ArrayList<CPUService> cpuServices = new ArrayList<>();
        int i = 0;
        for( Object cpu : arr){
            int cpuCores = Double.valueOf(cpu.toString()).intValue();
            cpuServices.add(new CPUService(cpuCores, i));
            i++;
        }
        return cpuServices;
    }

    private static ArrayList<GPUService> createGpuServices(ArrayList arr) {
        ArrayList<GPUService> gpuServices = new ArrayList<>();
        int i = 0;
        for( Object gpu : arr){
            gpuServices.add( new GPUService(gpu.toString(), null, i));
            i++;
        }
        return gpuServices;
    }

    private static HashMap<StudentService, List<Model>> createStudents( ArrayList arr) {
        HashMap<StudentService,List<Model>> studentsAndModels = new HashMap<>();
        for( Object student : arr){
            LinkedTreeMap<String, String> obj = (LinkedTreeMap) student;
            String name = obj.get("name");
            String department = obj.get("department");
            String status = obj.get("status");
            String models = String.valueOf(obj.get("models"));
            ArrayList<String> list = new ArrayList<>(Arrays.asList(models.split("}")));
            StudentService studentService = new StudentService( name, department, status);
            Student student1 = studentService.getStudent();
            studentsAndModels.put(studentService, new ArrayList<>());
            for(String i : list){
                if(i.contains("{")){
                    i = i.substring(i.indexOf('{'));
                    String nameOfModel = i.substring(i.indexOf('=') + 1, i.indexOf(',')).trim();
                    i = i.substring(i.indexOf(',') +1 );
                    String type = i.substring(i.indexOf('=') + 1, i.indexOf(',')).trim();
                    i = i.substring(i.indexOf(',') +1 );
                    Double sizeDouble = Double.valueOf(i.substring(i.indexOf('=') + 1).trim());
                    int size = sizeDouble.intValue();
                    Data data = new Data(type, size);
                    Model m = new Model( nameOfModel, data, student1,"PreTrained" , "None");
                    studentsAndModels.get(studentService).add(m);
                }
            }
        }
        return studentsAndModels;
    }


    public static String outputJson(Student student){
        String output = "";

        return output;
    }


    private static void createOutputJson(String outputPath, HashMap<StudentService, List<Model>> studentsAndModels, ArrayList<ConfrenceInformation> confrenceInformations, ArrayList<CPUService> cpuArray, ArrayList<GPUService> gpuArray, int batchesProcessed) {

        // pretty print
        Gson gsonOutput = new GsonBuilder().setPrettyPrinting().create();

        //NEED TO GET ALL THE DONE OBJECTS NAD SEND THEM TO THIS
        Map<String, Object> map = new LinkedTreeMap<>();

        //Create the students array for the json
        ArrayList<StudentJson> studentArrayList = new ArrayList<>();
        for(Map.Entry<StudentService, List<Model>> entry : studentsAndModels.entrySet()){
            studentArrayList.add(new StudentJson(entry.getKey().getStudent()));
        }

        ArrayList<ConInfoJson> conInfoJsonArrayList = new ArrayList<>();
        for(ConfrenceInformation confrenceInformation : confrenceInformations){
            conInfoJsonArrayList.add(new ConInfoJson(confrenceInformation));
        }

        int cpuTime = 0;
        int gpuTime = 0;

        for(CPUService cpuService : cpuArray){
            cpuTime = cpuTime + cpuService.getTimeUsed();
        }
        for(GPUService gpuService : gpuArray){
            gpuTime = gpuTime + gpuService.getTimeUsed();
        }

        map.put("Students", studentArrayList);
        map.put("conferences", conInfoJsonArrayList);
        map.put("cpuTimeUsed", cpuTime);
        map.put("gpuTimeUsed", gpuTime);
        map.put("batchesProcessed", batchesProcessed);
        String jsonOutput = gsonOutput.toJson(map);

        //System.out.println(jsonOutput);
        // Java objects to File
        try (FileWriter writer = new FileWriter(outputPath)) {
            gsonOutput.toJson(map, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
