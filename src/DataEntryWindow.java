import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static java.lang.System.out;

/**
 * Created by s641173 on 9/9/2016.
 */


public class DataEntryWindow extends JFrame {
    private JTextField nameField;
    private JTextField ipField;
    private JTextField portField;
    private JLabel nameLabel,ipLabel,portLabel;
    private JButton enterButton;
    private ClientRunner clientRunner;

    public DataEntryWindow(ClientRunner clientRunner){
        setTitle("Connection Information");
        nameField = new JTextField();
        ipField = new JTextField();
        portField = new JTextField();
        nameLabel = new JLabel("Your name: ");
        ipLabel = new JLabel("IP of server to connect to: ");
        portLabel = new JLabel("Port to connect to: ");
        enterButton = new JButton("OK");
        this.clientRunner = clientRunner;
        init();
    }
    public void init(){
        this.setLayout(new GridLayout(4,2));
        this.add(nameLabel);
        this.add(nameField);
        this.add(ipLabel);
        this.add(ipField);
        this.add(portLabel);
        this.add(portField);
        this.add(enterButton);
        enterListener listener = new enterListener(this);
        enterButton.addActionListener(listener);
        enterButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
        enterButton.getActionMap().put("ENTER", listener);
    }
    class enterListener extends AbstractAction{
        private DataEntryWindow window;
        public enterListener(DataEntryWindow w){
            window = w;
        }
        public void actionPerformed(ActionEvent e){


            Thread thread = new Thread() {
                public void run() {
                    clientRunner.userName = nameField.getText();
                    clientRunner.hostName = ipField.getText();
                    clientRunner.port = Integer.parseInt(portField.getText());
                    window.setVisible(false);
                    clientRunner.init();
                    out.println("Initializing");
                }
            };
            thread.start();


        }
    }


}
