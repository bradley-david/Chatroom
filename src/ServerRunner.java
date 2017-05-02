import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

import static java.lang.System.out;

import java.awt.*;
import java.awt.event.*;

public class ServerRunner {
    private JFrame frame;
    private JTextArea textArea = new JTextArea();
    private JTextField textField;
    private JScrollPane scrollPane;
    private JButton enterButton;
    private ArrayList<ServerThread> threadList = new ArrayList<>();
    private HashMap<Integer, String> hash = new HashMap<>();
    private PrintWriter printWriter;
    private FileWriter fileWriter;
    private int currentId = 1;

    private final int PORT;


    public ServerRunner(int port) {
        PORT = port;
    }

    public static void main(String[] args) {

        PortEntryWindow window = new PortEntryWindow();
        window.setSize(400, 300);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public void init() {


        textArea = new TimeJTextArea();
        textArea.setEditable(false);
        textField = new JTextField(25);
        enterButton = new JButton("ENTER");

        frame = new JFrame("Chatbox Server");
        frame.setSize(450, 450);
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        GridBagConstraints textAreaConstraints = new GridBagConstraints();
        textAreaConstraints.gridx = 0;
        textAreaConstraints.gridy = 0;
        textAreaConstraints.gridwidth = 2;
        textAreaConstraints.weightx = 0.5;
        textAreaConstraints.weighty = 0.8;
        textAreaConstraints.fill = GridBagConstraints.BOTH;

        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

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

        frame.add(textField, textFieldConstraints);

        GridBagConstraints enterButtonConstraints = new GridBagConstraints();
        enterButtonConstraints.gridx = 1;
        enterButtonConstraints.gridy = 1;
        enterButtonConstraints.weightx = 0.2;
        enterButtonConstraints.weighty = 0.2;
        enterButtonConstraints.fill = GridBagConstraints.BOTH;

        EnterListener enterListener = new EnterListener("ENTER");
        enterButton.addActionListener(enterListener);
        enterButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER");
        enterButton.getActionMap().put("ENTER", enterListener);
        frame.add(enterButton, enterButtonConstraints);
        frame.setVisible(true);


        int numThreads = 1;
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(PORT);
            textArea.append("\nServer initialized on local IP " + InetAddress.getLocalHost() + " with port " + PORT);

        } catch (IOException ex) {
            ex.printStackTrace();
            textArea.append("\nServer socket failed to initialize on port " + PORT);
        }

        while (true) {
            try {
                int uid = createUniqueUserId();
                Socket s = null;
                textArea.append("\nServer listening for connections.");
                s = ss.accept();
                textArea.append("\nSocket Accepted");
                ServerThread st = new ServerThread("Server Thread " + numThreads, s, textArea, this, uid);
                st.start();
                textArea.append("\nClient socket initialized.");
                threadList.add(st);
                numThreads++;

            } catch (IOException ex) {
                textArea.append("\nIOException when connecting to client.");
            }
        }
    }

    public void sendOutMessage(Message m) {
        textArea.append("\nMessage sent.");
        for (ServerThread t : threadList) {
            t.sendMessage(m);
        }
    }

    public void removeThread(ServerThread t) {//uses iterator to prevent concurrentModificationException.
        Iterator<ServerThread> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            ServerThread r = iterator.next();
            if (r.equals(t)) {
                iterator.remove();
                textArea.append("\nThread removed from threadList.");
            }
        }
    }

    public void shutdown() {    //uses iterator to prevent concurrentModificationException.
        Iterator<ServerThread> iterator = threadList.iterator();
        while (iterator.hasNext()) {
            ServerThread r = iterator.next();
            r.stop();
        }
        System.exit(0);
    }

    public int createUniqueUserId() {
        int x = currentId;
        currentId++;
        return x;
    }

    public void updateName(int key, String name) {
        hash.put(key, name);
    }

    public void kick(String[] args) {    //expects integer argument for person to kick
        try {
            if (args.length != 2) {
                throw new NumberFormatException();
            } else {
                int id = Integer.parseInt(args[1]);
                for (ServerThread t : threadList) {
                    if (t.getId() == id) {
                        textArea.append("\n" + "User with name " + hash.get(id) + " and id " + id + " kicked from server.");
                        sendOutMessage(new Message("User with name " + hash.get(id) + " and id " + id + " kicked from server.", "Server", false));
                        t.stop();
                        removeThread(t);
                        break;
                    }
                }

            }
        } catch (NumberFormatException ex) {
            textArea.append("\nError in your arguments, please try again.");
        }
    }

    public void printHash() {
        Iterator i = hash.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            textArea.append("\nUser with name " + entry.getValue() + " has id " + entry.getKey());
        }

    }

    public void log() {
        try {
            Date date = new Date();
            String logTitle = "Logged "+ date.toString();
            FileWriter filewriter = new FileWriter("log.txt", true);
            PrintWriter pr = new PrintWriter(filewriter);
            pr.println(logTitle+"\n"+textArea.getText());
            pr.close();
            filewriter.close();

        } catch (IOException ex) {
            textArea.append("\nLog file writer initialization failed.\n" +
                    "You will be unable to write log files.");
        }


    }

    public void clearLog() {
        try {
            PrintWriter pw = new PrintWriter("log.txt");
            pw.close();
        } catch (FileNotFoundException ex) {
            textArea.append("\nLog file writer initialization failed.\n" +
                    "You will be unable to write log files.");
        }

    }


    class EnterListener extends AbstractAction {

        public EnterListener(String actionCommandKey) {

            putValue(ACTION_COMMAND_KEY, actionCommandKey);
        }

        public void actionPerformed(ActionEvent e) {
            if (textField.getText() != null && !textField.getText().equals("")) {
                String s = textField.getText();
                String split[] = s.split(" "); //split args on spaces
                switch (split[0]) {
                    case "!exit":
                        shutdown();
                        break;
                    case "!kick":
                        kick(split);
                        break;
                    case "!printHash":
                        printHash();
                        break;
                    case "!log":
                        log();
                        break;
                    case "!clearLog":
                        clearLog();
                        break;
                }
                textField.setText("");
                textField.requestFocus();
            }

        }


    }


}
