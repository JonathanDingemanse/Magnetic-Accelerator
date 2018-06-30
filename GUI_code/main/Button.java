package main;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Button extends JLabel
        implements MouseListener {

    public static Border border = BorderFactory.createLineBorder(Color.BLACK,2);
    public static Border mouseBorder = BorderFactory.createLineBorder(Color.BLACK,4);
    protected boolean actionOnRelease = false;
    public String command;

    public Button(String name, String com) {
        command = com;
        setOpaque(true);
        setText(name);

        setHorizontalAlignment(JLabel.CENTER);
        setLayout(null);
        setFocusable(true);
        setBorder(border);
        addMouseListener(this);
        setSize(300,185);
        setFont(getFont().deriveFont(Font.BOLD, (float)(5*Math.sqrt(Main.paneWidth*Main.paneHeight)/200)));
        setRightLayout();
        setIgnoreRepaint(false);
        return;
    }

    public static void resetLayout(){
        border = BorderFactory.createLineBorder(Color.BLACK, 3*Main.paneHeight/1080);
        mouseBorder = BorderFactory.createLineBorder(Color.BLACK, 5*Main.paneHeight/1080);
    }

    public void setRightLayout(){
        setBackground(Color.lightGray);
        setForeground(Color.BLACK);

        if(this.getMousePosition() != null){
            setBorder(mouseBorder);
        }
        else{
            setBorder(border);
        }
        //repaint();
        Main.pane.repaint();

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        setRightLayout();
        if(actionOnRelease){
            setBackground(Color.CYAN.darker());
        }
    }

    public void mouseExited(MouseEvent e) {
        setRightLayout();
        setBorder(border);
    }

    public void mousePressed(MouseEvent e) {
        setBackground(Color.CYAN.darker());
        Main.pane.repaint();
        actionOnRelease = true;
    }

    public void mouseReleased(MouseEvent e) {
        Main.buttonAction(command);
        setRightLayout();
        actionOnRelease = false;
    }



}

