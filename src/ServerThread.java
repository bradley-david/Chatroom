/**
 * Created by s641173 on 9/2/2016.
 */

import sun.security.x509.IPAddressName;

import java.util.*;
import java.io.*;
import java.net.*;
import java.io.*;
import javax.swing.*;

import static java.lang.System.out;

public class ServerThread implements Runnable {
    private Thread t = null;
    private String threadName;
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private boolean running;
    private JTextArea textArea;
    private ServerRunner serverRunner;
    public final int id;
    public String name = null;


    public ServerThread(String threadName, Socket clientSocket, JTextArea textArea, ServerRunner serverRunner, int uuid) {
        this.threadName = threadName;
        this.clientSocket = clientSocket;
        this.textArea = textArea;
        this.serverRunner = serverRunner;
        id = uuid;

    }

    public void run() {
        running = true;
        textArea.append("\nThread " + threadName + " running.");
        try {

            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            textArea.append("\noutputStream established");
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            textArea.append("\ninputStream established");
            Message message;
            while (running) {
                if (t.isInterrupted()) {
                    out.println(threadName + "is interrupted. Exiting thread and closing connection now.");
                    break;
                }
                try {
                    message = (Message) inputStream.readObject();
                    textArea.append("\nMessage received on server.");
                    if (!message.getIsCommand()) {
                        name = message.getName();
                        serverRunner.updateName(id,name);
                        serverRunner.sendOutMessage(message);
                        textArea.append("\nMessage Text: "+message.getMessage());
                    } else if (message.getMessage().equals("!exit")) {
                        running = false;
                    }
                } catch (SocketException ex) {
                    if (ex.getMessage().equals("Connection reset")) {
                        textArea.append("\nClient disconnected.");
                        break;
                    } else {
                        textArea.append("\nUnknown socket error. Closing " + threadName);
                        break;
                    }
                } catch (EOFException ex) {
                    textArea.append("\nEOFException, closing thread");
                    break;
                    //this is expected when the client closes the stream. No other way around this, unfortunately.
                } catch (IOException ex) {
                    ex.printStackTrace();
                    textArea.append("\nIOException in " + threadName + ". Exiting thread and closing connection now.");
                    break;
                } catch (ClassNotFoundException ex) {
                    textArea.append("\nClassNotFoundException in " + threadName + ". Exiting thread and closing connection now.");
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    out.println(threadName + "is interrupted. Exiting thread and closing connection now.");
                    break;
                }
            }
        } catch (IOException ex) {
            textArea.append("\nError establishing server socket data streams. Exiting " + threadName);
        } finally {

            try {
                textArea.append("\nConnection Closing...");
                if (inputStream != null) {
                    inputStream.close();
                    textArea.append("\nSocket Input Stream Closed");
                }

                if (outputStream != null) {
                    outputStream.close();
                    textArea.append("\nSocket Output Stream Closed");
                }
                if (clientSocket != null) {
                    clientSocket.close();
                    textArea.append("\nSocket Closed");
                }
                stop();

            } catch (IOException ie) {
                textArea.append("\nSocket Close Error");
            }
        }


    }

    public void sendMessage(Message m) {
        try {
            outputStream.writeObject(m);
        } catch (IOException ex) {
            textArea.append("\nError sending message in " + threadName);
        }

    }

    public void start() {
        if (t == null) {
            t = new Thread(this, threadName);
        }
        t.start();
    }

    public String getThreadName() {
        return threadName;
    }

    public void stop() {
        running = false;
        serverRunner.removeThread(this);
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }
}
