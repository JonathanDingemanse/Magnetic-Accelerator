package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

import gnu.io.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;


public class Communicator implements SerialPortEventListener, ActionListener {
    // Identification constants
    final String NAME = "MIBArduino";
    final String CONFIRM = "Connected!";
    final byte ID = 123; // Identification Number
    final byte CN = 34;  // Control Number

    static Communicator communicator;

    SerialPort serialPort;

    public boolean isConnected = false;
    public boolean hasRightName = false;
    public boolean isCommunicating = false;

    private Enumeration ports = null;
    private HashMap portMap = new HashMap();
    private ArrayList<CommPortIdentifier> triedPorts = new ArrayList<>();

    public String portName;
    /** The port we're normally going to use. */
    //private static final String BASIC_PORT_NAME = "COM";
    /*private static final String PORT_NAMES[] = {
            //"COM2", // Mac OS X
            "COM3", // Raspberry Pi
            "COM4", // Linux
            "COM5", // Windows
    };*/
    /**
     * A BufferedReader which will be fed by a InputStreamReader
     * converting the bytes into characters
     * making the displayed results codepage independent
     */
    private BufferedReader reader;
    /** Basic input*/
    private InputStream input;
    /** Secondary input*/
    private DataInputStream data;
    /** The output stream to the port */
    private OutputStream output;
    /** Milliseconds to block while waiting for port open */
    private static final int TIME_OUT = 2000;
    /** Default bits per second for COM port. */
    private static final int DATA_RATE = 9600;

    private Timer timer;

    public void initialize() {
        timer = new Timer(1000, this);
        timer.start();

    }

    public void tryNextSerialDevice(){
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.

        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();

            if(currPortId.getPortType() == CommPortIdentifier.PORT_SERIAL){
                if(!triedPorts.contains(currPortId)){
                    portId = currPortId;
                    portMap.put(currPortId.getName(), currPortId);
                    triedPorts.add(currPortId);
                    break;
                }
            }

            /*for (String portName : PORT_NAMES) {

                if (currPortId.getName().equals("COM4")) {
                    portId = currPortId;
                    portMap.put(currPortId.getName(), currPortId);
                    break;
                }
            }*/
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            triedPorts.clear();
            isConnected = false;
            return;
        }
        else {
            portName = portId.getName();
            System.out.println("Connected to port: " + portName);
            isConnected = true;
        }

        try {
            // open serial port, and use class name for the appName.

            CommPort commPort = portId.open(this.getClass().getName(),TIME_OUT);
            serialPort = (SerialPort) commPort;

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = serialPort.getInputStream();
            //reader = new BufferedReader(new InputStreamReader(input));
            data = new DataInputStream(input);
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (Exception e) {
            System.err.println(e.toString());
            isConnected = false;
        }
        if(isConnected){
            Main.setSearchText("Connected to port: " + portName);
        }
    }

    public void send(byte b){
        try {
            output.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This should be called when you stop using the port.
     * This will prevent port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
            isConnected = false;
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                //System.out.println("Test 2");
                //input.close();
                //input = serialPort.getInputStream();



                int datacount = input.available();
                //System.out.println(datacount + " bytes available");

                if(!hasRightName){
                    byte[] bytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    if(datacount >= 10 ){
                        input.read(bytes);
                        String text = new String(bytes, "UTF-8");
                        System.out.println(text);
                        if(text.equals(NAME)){
                            hasRightName = true;
                            output.write(ID);
                            output.write(CN);
                        }
                    }
                }
                else if(!isCommunicating){
                    byte[] bytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                    if(datacount >= 14 ){
                        input.read(bytes);

                        String text = new String(bytes);
                        System.out.println(text);
                        if(text.equals(CONFIRM)){
                            isCommunicating = true;
                        }

                    }

                }
                else{
                    if(datacount >= 4 ){
                        DataPoint dp;
                        if(Main.lapLength == 0){
                            Main.lapLength = data.readFloat();
                        }
                        else {
                            float speed = data.readFloat();
                            //int l = data.readInt();
                            Main.speed = speed;
                            System.out.println(speed);
                            if(Main.velocityChart.dataSet.size() == 0){
                                Main.beginTime = System.currentTimeMillis();
                                Main.initialSpeed = speed;
                                dp = new DataPoint(speed, 0);
                            }
                            else {
                                dp = new DataPoint(speed, System.currentTimeMillis() - Main.beginTime);
                            }
                            Main.newInput(dp);
                        }

                    }
                    //input.read(bytes);
                    //String text = new String(bytes);
                    //System.out.println(text);
                }


            }
            catch (IOException e){
                e.printStackTrace();
            }



            /*try {
                String inputLine=input.readLine();
                System.out.println(inputLine);
            } catch (Exception e) {
                System.err.println(e.toString());
            }*/
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public static void main(String[] args) throws Exception {
        communicator = new Communicator();
        Main.begin();
        communicator.initialize();
        Thread t= new Thread() {
            public void run() {
                //the following line will keep this app alive for 1000 seconds,
                //waiting for events to occur and responding to them (printing incoming messages to console).
                try {Thread.sleep(1000000);} catch (InterruptedException ie) {}
            }
        };
        t.start();
        //System.out.println("Started");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!isCommunicating){
            close();
            int i = 0;
            while(!isConnected && i < 8){
                tryNextSerialDevice();
                i++;
            }
        }
        else {
            Main.showContent();
            System.out.println("Communication has started! :)");
            timer.stop();

        }
    }
}
