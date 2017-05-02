/**
 * Created by s641173 on 8/29/2016.
 */
import java.io.*;
public class Message implements Serializable {
    private boolean isCommand;
    private String name;
    private String text;
    private String message;
    public Message(String m, String n, boolean c){
        name = n;
        text = m;
        message = n + ": " + m;
        isCommand = c;
    }
    public boolean getIsCommand(){
        return isCommand;
    }

    public String getMessage(){
        return message;
    }
    public String getText(){
        return text;
    }
    public String getName(){
        return name;
    }


}
