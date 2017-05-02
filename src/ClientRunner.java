import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;

import static java.lang.System.err;
import static java.lang.System.lineSeparator;
import static java.lang.System.out;

public class ClientRunner {
    public String hostName;
    public String userName;
    public int port;
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private ClientOutput clientOutput;
    private ClientInput clientInput;
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField;
    private JScrollPane scrollPane;
    private JButton enterButton;


    public static void main(String[] args) {
        ClientRunner clientRunner = new ClientRunner();
        DataEntryWindow window = new DataEntryWindow(clientRunner);
        window.setSize(400, 300);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public void init() {
        frame = new JFrame("Chatbox Client");
        frame.setSize(400, 400);
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        GridBagConstraints textAreaConstraints = new GridBagConstraints();
        textAreaConstraints.gridx = 0;
        textAreaConstraints.gridy = 0;
        textAreaConstraints.gridwidth = 2;
        textAreaConstraints.weightx = 0.5;
        textAreaConstraints.weighty = 0.8;
        textAreaConstraints.fill = GridBagConstraints.BOTH;
        textArea = new TimeJTextArea();
        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, textAreaConstraints);

        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        textFieldConstraints.gridx = 0;
        textFieldConstraints.gridy = 1;
        textFieldConstraints.weightx = 0.8;
        textFieldConstraints.weighty = 0.2;
        textFieldConstraints.gridwidth = 1;
        textFieldConstraints.gridheight = 1;
        textFieldConstraints.fill = GridBagConstraints.BOTH;
        textField = new JTextField(25);
        frame.add(textField, textFieldConstraints);

        GridBagConstraints enterButtonConstraints = new GridBagConstraints();
        enterButtonConstraints.gridx = 1;
        enterButtonConstraints.gridy = 1;
        enterButtonConstraints.weightx = 0.2;
        enterButtonConstraints.weighty = 0.2;
        enterButtonConstraints.fill = GridBagConstraints.BOTH;
        enterButton = new JButton("ENTER");
        EnterListener enterListener = new EnterListener("ENTER");
        enterButton.addActionListener(enterListener);
        enterButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
        enterButton.getActionMap().put("ENTER", enterListener);
        frame.add(enterButton, enterButtonConstraints);

        try {
            socket = new Socket(hostName, port);
            textArea.append("\nSocket established");
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            textArea.append("\noutputStream established");
            inputStream = new ObjectInputStream(socket.getInputStream());
            textArea.append("\ninputStream established");
            try {
                outputStream.writeObject(new Message(userName + " has joined the server.", "Server", false));
            } catch (IOException ex) {
                ex.printStackTrace();
                textArea.append("\nIO error sending message to server.");
            }

        } catch (UnknownHostException e) {
            textArea.append("\nDon't know about target server " + hostName);
            System.exit(1);

        } catch (IOException e) {
            textArea.append("\nCouldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }

        textArea.append("\nStarting ClientInput");
        clientOutput = new ClientOutput("output", userName, outputStream, textField, textArea, this);
        clientOutput.start();
        clientInput = new ClientInput("input", userName, inputStream, socket, textArea, this);
        clientInput.start();
        frame.setVisible(true);
    }

    public void shutdownClient() {
        clientInput.stop();
        clientInput.closeStream();
        clientOutput.stop();
        clientOutput.closeStream();
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    class EnterListener extends AbstractAction {

        public EnterListener(String actionCommandKey) {

            putValue(ACTION_COMMAND_KEY, actionCommandKey);
        }

        public void actionPerformed(ActionEvent e) {

            if (textField.getText() != null && !textField.getText().equals("")) {
                clientOutput.read();
                textField.setText("");
                textField.requestFocus();
            }

        }


    }


}
