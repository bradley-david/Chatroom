/**
 * Created by s641173 on 8/31/2016.
 */

import java.lang.Thread;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.io.*;

import javax.swing.*;

import static java.lang.System.err;
import static java.lang.System.out;

public class ClientInput implements Runnable {
    private Thread t;
    private String threadName;
    private String userName;
    private ObjectInputStream stream;
    private Socket s;
    private boolean running;
    private JTextArea textArea;
    private ClientRunner clientRunner;


    public ClientInput(String threadName, String userName, ObjectInputStream stream, Socket socket, JTextArea textArea, ClientRunner clientRunner) {
        this.threadName = threadName;
        this.userName = userName;
        this.stream = stream;
        s = socket;
        this.textArea = textArea;
        this.clientRunner = clientRunner;
    }

    public void run() {
        running = true;
        //start reading keyboard input
        textArea.append("\nClientInput running.");
        while (running) {
            Message message;
            if (t.isInterrupted()) {
                textArea.append("\nThread " + threadName + "interrupted.");
                break;
            }
            try {
                if (s.isConnected()) {

                    message = (Message) stream.readObject();
                    if (message != null) {
                        textArea.append("\n" + message.getMessage());

                    }
                }

            } catch (SocketException ex) {
                if (ex.getMessage().equals("Socket closed")) {
                    break;//this is normal and I can't find another way to fix it. Happens when the server has already disconnected the socket.
                }
            } catch (IOException ex) {   //TODO add better error traps
                ex.printStackTrace();
                textArea.append("\nIO Error in ClientInput");
                break;
            } catch (ClassNotFoundException ex) {
                textArea.append("\nClassNotFoundException in ClientInput.");
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                textArea.append("\nThread interrupted in ClientInput. Shutting down client.");
                break;

            }

        }
        clientRunner.shutdownClient();

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
        }
        t.start();
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

    public void stop() {
        running = false;
    }

}
