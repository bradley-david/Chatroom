/**
 * Created by s641173 on 9/21/2016.
 */


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;


public class PortEntryWindow extends JFrame {
    private JTextField entryField;
    private JButton entryButton;
    private JTextArea entryLabel;


    public PortEntryWindow(){

        setTitle("Port Information");
        entryLabel = new JTextArea("Please enter the port you want to initialize the server on.\n" +
                "Please avoid ports from 1-10,000 as those are used by other apps.");
        entryLabel.setEditable(false);
        entryButton = new JButton("ENTER");
        entryField = new JTextField();
        this.setLayout(new GridLayout(3,1));
        this.add(entryLabel);
        this.add(entryField);
        this.add(entryButton);

        enterListener listener = new enterListener(this);
        entryButton.addActionListener(listener);
        entryButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
        entryButton.getActionMap().put("ENTER", listener);

    }
    class enterListener extends AbstractAction{
        private PortEntryWindow window;
        public enterListener(PortEntryWindow w){
            window = w;
        }
        public void actionPerformed(ActionEvent e){
            if(entryField.getText()!=null && !entryField.getText().equals("")){
                window.setVisible(false);
                Thread thread = new Thread() {
                    public void run() {
                        ServerRunner runner = new ServerRunner(Integer.parseInt(entryField.getText()));
                        runner.init();


                    }
                };
                thread.start();
            }
        }
    }

}
