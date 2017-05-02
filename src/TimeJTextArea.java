import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by David Bradley on 9/29/2016.
 *
 * This class is just an extension of JTextArea that
 * appends the time if a string starts with a newline character
 */
public class TimeJTextArea extends JTextArea {
    @Override
    public void append(String arg){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        if(arg.charAt(0)=='\n'){
            super.append("\n"+sdf.format(cal.getTime())+": "+arg.substring(1));
        }
        else{
            super.append(arg);
        }


    }


}
