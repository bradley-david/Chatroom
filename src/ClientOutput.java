/**
 * responsible for reading input from user and outputting it to server
 */

import javax.swing.*;
import java.lang.Thread;
import java.net.SocketException;
import java.util.Scanner;
import java.io.*;

import static java.lang.System.err;
import static java.lang.System.out;

public class ClientOutput implements Runnable {
    private Thread t;
    private String threadName;
    private String userName;
    private String userInput = "";
    private ObjectOutputStream stream;
    private boolean running;
    private boolean newUserInput = false;
    private JTextArea textArea;
    private JTextField textField;
    private ClientRunner clientRunner;

    public ClientOutput(String threadName, String userName, ObjectOutputStream stream, JTextField textField, JTextArea textArea, ClientRunner clientRunner) {
        this.threadName = threadName;
        this.userName = userName;
        this.stream = stream;
        this.textArea = textArea;
        this.textField = textField;
        this.clientRunner = clientRunner;
    }

    public void run() {
        //start reading keyboard input
        textArea.append("\nClientOutput running.");

        running = true;

        while (running) {
            if ((userInput.equals("") || userInput == null) || !newUserInput) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();

                }

            } else {
                newUserInput = false;
                Message message = null;
                if (userInput.charAt(0) == '!') {
                    message = new Message(userInput, userName, true);
                } else {
                    message = new Message(userInput, userName, false);
                }
                try {
                    stream.writeObject(message);
                } catch (IOException ex) {
                    textArea.append("\nIO error sending message to server.");
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();

                }
                if (userInput.equals("!exit")) {
                    break;
                }

                if (t.isInterrupted()) {
                    textArea.append("\nThread " + threadName + "interrupted.");
                    break;
                }
            }

        }
        try {
            stream.writeObject(new Message(userName + " has left the server.", "Server", false));
        } catch (IOException ex) {
            textArea.append("\nIO error sending message to server.");
        }
        textArea.append("\nShutting down client.");

        clientRunner.shutdownClient();


    }

    public void closeStream() {
        try {
            if (stream != null) {
                stream.close();
            }

        } catch (SocketException ex) {
            if (ex.getMessage().equals("Socket closed")) {
                textArea.append("\nSocket already closed, probably due to unexpected server shutdown.");
            } else {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
        }
        t.start();
    }

    public void stop() {
        running = false;
    }

    public void read() {
        userInput = textField.getText();
        newUserInput = true;
    }


}
