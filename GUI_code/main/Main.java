package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class Main implements ComponentListener{
    static int paneWidth;
    static int paneHeight;
    static JFrame frame;
    static Container pane;
    static JLabel searching;
    static JLabel text1;
    static JLabel text2;
    public static Communicator communicator;

    static Button accButton;
    static Button dccButton;
    static Button stopButton;
    static Button resetButton;

    static Chart velocityChart;
    static Chart accelerationChart;

    static JLabel atX1;
    static JLabel atY1;
    static JLabel atX2;
    static JLabel atY2;

    static Font tinyFont;
    static Font smallFont;
    static Font midFont;
    static Font bigFont;


    // Command constants
    static final byte ACC = 100;  // Accelerate
    static final byte DCC = 101; // Decelerate
    static final byte STOP = 102; // Stop accelerating or de-accelerating

    static float speed = 0;
    static float acceleration = 0;
    static float initialSpeed = 0;
    static float lapLength = 0;
    static float lapTime = 0;

    static long beginTime;
    static boolean isAccelerating;
    static boolean isDecelerating;
    //static ArrayList<DataPoint> speedSet;
    static ComponentListener componentListener;

    static boolean isContentReady = false;

    public Main(){

    }

    public static void begin(){
        communicator = Communicator.communicator;
        frame = new JFrame("Magnatic Accelerator Software");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(null);
        frame.setVisible(true);

        Dimension frameSize = frame.getSize();
        Insets insets = frame.getInsets();
        double fakePaneWidth = frameSize.getWidth();
        double fakePaneHeight = frameSize.getHeight();
        paneWidth = (int) (fakePaneWidth - (insets.left + insets.right));
        paneHeight = (int) (fakePaneHeight - (insets.top + insets.bottom));

        frame.setMinimumSize(new Dimension(2*(int)fakePaneWidth/3, 2*(int)fakePaneHeight/3));
        //frame.setPreferredSize(new Dimension(2*(int)fakePaneWidth/3, 2*(int)fakePaneHeight/3));

        pane = frame.getContentPane();
        pane.setLayout(null);
        searching = new JLabel("Searching...", JLabel.CENTER);

        tinyFont = searching.getFont().deriveFont(Font.PLAIN, (float)(3*paneWidth/1500*Math.sqrt(paneWidth*paneHeight)/200));
        smallFont = searching.getFont().deriveFont(Font.PLAIN, (float)(4*paneWidth/1500*Math.sqrt(paneWidth*paneHeight)/200));
        midFont = searching.getFont().deriveFont(Font.PLAIN, (float)(6*paneWidth/1500*Math.sqrt(paneWidth*paneHeight)/200));
        bigFont = searching.getFont().deriveFont(Font.PLAIN, (float)(8*paneWidth/1500*Math.sqrt(paneWidth*paneHeight)/200));

        searching.setLayout(null);
        searching.setForeground(Color.BLACK);
        searching.setFont(bigFont);
        searching.setSize(paneWidth,paneHeight);

        accButton = new Button("Accelerate", "ACC");
        accButton.setSize(2*paneWidth/10, paneHeight/10);
        accButton.setLocation(paneWidth/15, 11*paneHeight/20);

        dccButton = new Button("Decelerate", "DACC");
        dccButton.setSize(2*paneWidth/10, paneHeight/10);
        dccButton.setLocation(paneWidth/15, 14*paneHeight/20);

        stopButton = new Button("Stop", "STOP");
        stopButton.setSize(2*paneWidth/10, paneHeight/10);
        stopButton.setLocation(paneWidth/15, 14*paneHeight/20);

        resetButton = new Button("Reset data", "RESET");
        resetButton.setSize(2*paneWidth/10, paneHeight/10);
        resetButton.setLocation(paneWidth/15, 17*paneHeight/20);

        velocityChart = new Chart(new Point(paneWidth/20, paneHeight/20), 2*paneWidth/5, 2*paneHeight/5, 0, 1f, Color.RED);
        velocityChart.repaint();

        accelerationChart = new Chart(new Point(11*paneWidth/20, paneHeight/20), 2*paneWidth/5, 2*paneHeight/5, -0.5f, 0.5f, Color.BLUE);
        accelerationChart.repaint();

        atX1 = new JLabel("<html>time [s]<html>", SwingConstants.CENTER);
        atY1 = new JLabel("<html>speed [m/s]<html>", SwingConstants.LEFT);
        atX2 = new JLabel("<html>time [s]<html>", SwingConstants.CENTER);
        atY2 = new JLabel("<html>acceleration [m/s<sup><font size=\"4\">2 </font></sup>]<html>", SwingConstants.LEFT);

        atX1.setFont(tinyFont);
        atY1.setFont(tinyFont);
        atX2.setFont(tinyFont);
        atY2.setFont(tinyFont);

        atX1.setSize(Main.paneWidth/8, Main.paneHeight/30);
        atY1.setSize(Main.paneWidth/7, Main.paneHeight/30);
        atX2.setSize(Main.paneWidth/8, Main.paneHeight/30);
        atY2.setSize(Main.paneWidth/4, Main.paneHeight/30);

        atX1.setLocation(3*Main.paneWidth/8, 19*Main.paneHeight/40);
        atY1.setLocation(Main.paneWidth/64, 0);
        atX2.setLocation(7*Main.paneWidth/8, 19*Main.paneHeight/40);
        atY2.setLocation(4*Main.paneWidth/8, 0);

        text1 = new JLabel(" ", JLabel.LEFT);
        text2 = new JLabel(" ", JLabel.LEFT);

        text1.setVerticalAlignment(JLabel.TOP);
        text2.setVerticalAlignment(JLabel.TOP);

        text1.setSize(Main.paneWidth/3, 9*Main.paneHeight/20);
        text2.setSize(Main.paneWidth/3, 9*Main.paneHeight/20);

        text1.setLocation(Main.paneWidth/3, 11*Main.paneHeight/20);
        text2.setLocation(2*Main.paneWidth/3, 11*Main.paneHeight/20);

        text1.setFont(midFont);
        text2.setFont(midFont);

        if(velocityChart.dataSet.size() > 0){
            pane.add(resetButton);
        }

        componentListener = new Main();
        frame.addComponentListener(componentListener);

        pane.add(searching);
        pane.repaint();
        frame.repaint();

        Button.resetLayout();
    }

    public static void setSearchText(String text){
        searching.setText(text);
        searching.repaint();
    }

    public static void showContent(){
        pane.removeAll();

        setRightText();

        addFixedContent();
        pane.add(accButton);
        if(speed > 0){
            dccButton.setLocation(paneWidth/15, 14*paneHeight/20);
            pane.add(dccButton);
        }
        pane.repaint();
        frame.repaint();
        isContentReady = true;
    }

    public static void buttonAction(String com){
        switch (com){
            case "ACC":
                communicator.send(ACC);
                //pane.removeAll();
                //addFixedContent();
                pane.remove(accButton);
                dccButton.setLocation(paneWidth/15, 11*paneHeight/20);
                pane.add(dccButton);
                pane.add(stopButton);
                isAccelerating = true;
                isDecelerating = false;
                break;

            case "DACC":
                communicator.send(DCC);
                pane.removeAll();
                addFixedContent();
                pane.add(accButton);
                pane.add(stopButton);
                isAccelerating = false;
                isDecelerating = true;
                break;

            case "STOP":
                communicator.send(STOP);
                pane.removeAll();
                addFixedContent();
                dccButton.setLocation(paneWidth/15, 14*paneHeight/20);
                pane.add(dccButton);
                pane.add(accButton);
                isAccelerating = false;
                isDecelerating = false;
                break;

            case "RESET":
                velocityChart.dataSet.clear();
                velocityChart.timeScale = 10000;
                accelerationChart.dataSet.clear();
                accelerationChart.timeScale = 10000;
                velocityChart.repaint();
                accelerationChart.repaint();
                pane.remove(resetButton);
        }
        setRightText();
        pane.repaint();

    }

    public static void addFixedContent(){
        pane.add(atX1);
        pane.add(atY1);
        pane.add(atX2);
        pane.add(atY2);
        pane.add(velocityChart);
        pane.add(accelerationChart);
        pane.add(text1);
        pane.add(text2);
        if(velocityChart.dataSet.size() > 0){
            pane.add(resetButton);
        }
    }

    public static void newInput(DataPoint dp){
        //System.out.println("new Data!!");
        velocityChart.add(dp);
        if(velocityChart.dataSet.size() > 1){
            DataPoint pdp =velocityChart.dataSet.get(velocityChart.dataSet.size() - 2);
            acceleration = (dp.value - pdp.value)/(dp.time - pdp.time)*1000;
            accelerationChart.add(new DataPoint(acceleration, (dp.time + pdp.time)/2));
            //accelerationChart.repaint();

            if(velocityChart.dataSet.size() > 2){
                lapTime = (dp.time - velocityChart.dataSet.get(velocityChart.dataSet.size() - 3).time)/1000.0f;
            }
        }
        else if(isContentReady){
            pane.add(resetButton);
        }
        //
        //velocityChart.repaint();
        setRightText();
        //velocityChart.repaint();
        /*pane.removeAll();
        addFixedContent();
        if(isAccelerating){
            dccButton.setLocation(paneWidth/15, 11*paneHeight/20);
            pane.add(dccButton);
            pane.add(stopButton);
        }
        else if(isDecelerating){
            pane.add(accButton);
            pane.add(stopButton);
        }
        else {
            dccButton.setLocation(paneWidth/15, 14*paneHeight/20);
            pane.add(dccButton);
            pane.add(accButton);
        }*/
        pane.repaint();
        //frame.repaint();

    }

    public static void setRightText(){
        String speedT = String.valueOf(speed);
        String accelerationT = String.valueOf(acceleration);
        String iniSpeedT = String.valueOf(initialSpeed);
        if(speedT.length() > 4){
            speedT = speedT.substring(0,5);
        }
        if(accelerationT.contains("-") && accelerationT.length() > 5){
            accelerationT = accelerationT.substring(0,6);
        }
        else if(accelerationT.length() > 4){
            accelerationT = accelerationT.substring(0,5);
        }
        if(iniSpeedT.length() > 4){
            iniSpeedT = iniSpeedT.substring(0,5);
        }

        String status;
        if(isAccelerating){
            status = "Accelerating...";
        }
        else if(isDecelerating){
            status = "Decelerating...";
        }
        else {
            status = "Doing nothing...";
        }


        text1.setText("<html>Status:\t" + status + "<br>" +
                "Speed:\t" + speedT + " m/s<sup> </sup><br>" +
                "Acceleration:\t" + accelerationT + " m/s<sup><font size=\"6\">2</font> </sup><br>" +
                "Initial speed:\t" + iniSpeedT + " m/s<sup> </sup><html>");
        text2.setText("<html>Lap length:\t" + lapLength + "<br>" +
                "Lap time:\t" + lapTime + "s<sup> </sup><br>" +
                "Port:\t" + communicator.portName + "<sup> </sup><br><html>");
    }

    @Override
    public void componentResized(ComponentEvent e) {
        Dimension frameSize = frame.getSize();
        Insets insets = frame.getInsets();
        double fakePaneWidth = frameSize.getWidth();
        double fakePaneHeight = frameSize.getHeight();
        paneWidth = (int) (fakePaneWidth - (insets.left + insets.right));
        paneHeight = (int) (fakePaneHeight - (insets.top + insets.bottom));
        pane.repaint();
        System.out.println("Troll screen resized");

        text1.setSize(Main.paneWidth/3, 9*Main.paneHeight/20);
        text2.setSize(Main.paneWidth/3, 9*Main.paneHeight/20);

        text1.setLocation(Main.paneWidth/3, 11*Main.paneHeight/20);
        text2.setLocation(2*Main.paneWidth/3, 11*Main.paneHeight/20);

        atX1.setSize(Main.paneWidth/8, Main.paneHeight/30);
        atY1.setSize(Main.paneWidth/7, Main.paneHeight/30);
        atX2.setSize(Main.paneWidth/8, Main.paneHeight/30);
        atY2.setSize(Main.paneWidth/4, Main.paneHeight/30);

        atX1.setLocation(3*Main.paneWidth/8, 19*Main.paneHeight/40);
        atY1.setLocation(Main.paneWidth/64, 0);
        atX2.setLocation(7*Main.paneWidth/8, 19*Main.paneHeight/40);
        atY2.setLocation(4*Main.paneWidth/8, 0);

        accButton.setSize(2*paneWidth/10, paneHeight/10);
        accButton.setLocation(paneWidth/15, 11*paneHeight/20);

        dccButton.setSize(2*paneWidth/10, paneHeight/10);
        if(isAccelerating){
            dccButton.setLocation(paneWidth/15, 11*paneHeight/20);
        }
        else {
            dccButton.setLocation(paneWidth/15, 14*paneHeight/20);
        }


        stopButton.setSize(2*paneWidth/10, paneHeight/10);
        stopButton.setLocation(paneWidth/15, 14*paneHeight/20);

        resetButton.setSize(2*paneWidth/10, paneHeight/10);
        resetButton.setLocation(paneWidth/15, 17*paneHeight/20);

        velocityChart.resetSize( 2*paneWidth/5, 2*paneHeight/5);
        velocityChart.resetLocation(paneWidth/20, paneHeight/20);

        accelerationChart.resetSize( 2*paneWidth/5, 2*paneHeight/5);
        accelerationChart.resetLocation(11*paneWidth/20, paneHeight/20);

        pane.repaint();
        frame.repaint();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
