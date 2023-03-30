package bgu.spl.mics.application.objects;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Data {
    public Data(String type, int size) {
        setType(type);
        this.size = size;
    }

    private void setType(String type) {
        type = type.toLowerCase();
        if(type.equals("images"))
            this.type = Type.Images;
        else if(type.equals("text"))
            this.type = Type.Text;
        else if(type.equals("tabular"))
            this.type = Type.Tabular;

    }

    /**
     * Enum representing the Data type.
     */
    enum Type {
        Images, Text, Tabular
    }

    private Type type;
    private int processed;
    private int size;

    public String getType(){
        if(type.equals(Type.Images)){
            return "Images";
        }
        else if(type.equals(Type.Text))
            return "Text";
        else if(type.equals(Type.Tabular))
            return "Tabular";
        return "";
    }

    public int getSize(){
        return size;
    }


}