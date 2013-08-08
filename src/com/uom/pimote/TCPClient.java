package com.uom.pimote;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    public static String SERVERIP = "";
    public static int SERVERPORT = 8080;
    PrintWriter out;
    BufferedReader in;
    Socket socket;
    private String serverMessage;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages
     * received from server
     */
    public TCPClient(OnMessageReceived listener, String ip, int port) {
        mMessageListener = listener;
        SERVERIP = ip;
        SERVERPORT = port;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient() {
        Log.e("TCP Client", "Stopping");
        mRun = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        mRun = true;

        try {
            // here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Log.e("TCP Client", "C: Connecting...");

            // create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVERPORT);

            try {
                Log.e("TCP Client", "Connected");
                // send the message to the server
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())), true);

                // receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // in this while the client listens for the messages sent by the
                // server
                while (mRun) {
                    try {
                        if (!mRun) throw new Exception();
                        serverMessage = in.readLine();
                        if (serverMessage == null)
                            throw new Exception();
                    } catch (Exception e) {
                        Log.e("TCP Client", "Disconnected");
                        break;
                    }

                    if (serverMessage != null && mMessageListener != null) {
                        // call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                }
                Log.e("TCP Client", "Stopped listening");
            } catch (Exception e) {

                Log.e("TCP Client", "S: Error", e);
                //mMessageListener.failActivity("Error connecting to host");

            } finally {
                // the socket must be closed. It is not possible to reconnect to
                // this socket
                // after it is closed, which means a new socket instance has to
                // be created.
                Log.e("TCP Client", "Socket closed");
                socket.close();
                if (mRun)
                    mMessageListener.failActivity("Connection ended by remote host");
            }

        } catch (Exception e) {
            //Log.e("TCP Client", "C: Error, unable to connect", e);
            mMessageListener.failActivity("Cannot connect to host");

        }

    }

    // Declare the interface. The method messageReceived(String message) will
    // must be implemented in the MyActivity
    // class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);

        public void failActivity(String msg);
    }
}
