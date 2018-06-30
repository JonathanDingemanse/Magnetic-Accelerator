package main;

import javax.swing.*;
import javax.swing.text.Segment;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class Chart extends JPanel{
    Graphics2D g;
    Point p;
    int width;
    int height;

    long timeScale = 1000;
    int upperScale = 1;
    int lowerScale = 1;

    long timeScaleRed = 1;
    long timeScaleMag = 1;

    int upperScaleRed = 1;
    int upperScaleMag = 1;

    int  YTerm = 1;
    int  XTerm = 100;

    int  YTermMag = 1;
    int  XTermMag = 100;

    int  YTermRed = 1;
    int  XTermRed = 1;

    int dotRadius;

    Color color;

    static final int EQUALISE_FACTOR = 100;

    public ArrayList<DataPoint> dataSet = new ArrayList<>();

    public Chart(Point p, int width, int height, float lowerScale, float upperScale, Color color){
        setSize(Main.paneWidth, Main.paneHeight/2);

        this.p = p;
        this.width = width;
        this.height = height;
        this.upperScale = (int)(upperScale*EQUALISE_FACTOR);
        this.lowerScale = (int)(lowerScale*EQUALISE_FACTOR);
        this.color = color;
        timeScale = 10000;
        this.setOpaque(false);
        setLayout(null);

        dotRadius = Main.paneHeight/128;
        setFocusable(false);
        //dataSet.add(new DataPoint(0.12345f, 0));
        //dataSet.add(new DataPoint(-0.12345f, 1000));
        //dataSet.add(new DataPoint(32*0.12345f, 2000));
        //.add(new DataPoint(-0.12345f, 3000));
        //dataSet.add(new DataPoint(3*0.12345f, 8000));
    }

    public void resetSize(int width, int height){
        this.width = width;
        this.height = height;
        setSize(Main.paneWidth, Main.paneHeight/2);
    }

    public void resetLocation(int x, int y){
        p = new Point(x, y);
        this.repaint();
    }

    public void add(DataPoint dp){
        dataSet.add(dp);
        if(lowerScale == 0 && dp.value*EQUALISE_FACTOR > upperScale){
            while(dp.value*EQUALISE_FACTOR > upperScale){
                upperScaleRed = upperScale;
                upperScaleMag = 1;

                while (upperScaleRed > 9){
                    upperScaleRed = upperScaleRed/10;
                    upperScaleMag = upperScaleMag*10;
                }
                upperScale = (upperScaleRed + 1)*upperScaleMag;
            }

        }
        else if(lowerScale == -upperScale && (dp.value*EQUALISE_FACTOR > upperScale || dp.value*EQUALISE_FACTOR < lowerScale)){
            while(dp.value*EQUALISE_FACTOR > upperScale || dp.value*EQUALISE_FACTOR < lowerScale){
                upperScaleRed = upperScale;
                upperScaleMag = 1;

                while (upperScaleRed > 9){
                    upperScaleRed = upperScaleRed/10;
                    upperScaleMag = upperScaleMag*10;
                }

                upperScaleRed = (upperScaleRed + 1);
                if (upperScaleRed == 7) {
                    upperScaleRed = 8;
                }
                else if(upperScaleRed == 9){
                    upperScaleRed = 10;
                }
                upperScale = upperScaleRed*upperScaleMag;
                lowerScale = -upperScale;
            }
        }
    }

    public void paintComponent(Graphics z){
        g = (Graphics2D) z;
        Font font = g.getFont();
        FontMetrics fm = g.getFontMetrics();
        FontRenderContext context = g.getFontRenderContext();

        timeScaleRed = timeScale;
        timeScaleMag = 1;

        while (timeScaleRed > 9){
            timeScaleRed = timeScaleRed/10;
            timeScaleMag = timeScaleMag*10;
        }

        //System.out.println(timeScale + "    " + timeScaleRed + "    " + timeScaleMag);

        if(dataSet.size() > 1){
            while(timeScale < dataSet.get(dataSet.size() - 1).time){
                timeScale = (timeScaleRed + 1)*timeScaleMag;

                timeScaleRed = timeScale;
                timeScaleMag = 1;

                while (timeScaleRed > 9){
                    timeScaleRed = timeScaleRed/10;
                    timeScaleMag = timeScaleMag*10;
                }
                //System.out.println(timeScale + "    " + timeScaleRed + "    " + timeScaleMag);
            }
        }

        // TODO onderstaande stuk code beter maken
        /*for(int i = 0; i < dataSet.size(); i++){
            if(dataSet.get(i).value > upperScale){
                upperScale = upperScale + YTermMag;
            }
            else if(dataSet.get(i).value < lowerScale){
                lowerScale = lowerScale - YTermMag;
            }
        }

        for(int i = 0; i < dataSet.size(); i++){
            if(dataSet.get(i).time > timeScale){
                timeScale = timeScale + XTermMag;
            }
        }*/

        int Oy = height*upperScale/(upperScale - lowerScale);

        Point origin = new Point(p.x, Oy + p.y);

        YTerm = 1;
        YTermRed = 1;
        YTermMag = 1;
        while((upperScale - lowerScale)/YTerm > 12){
            if((upperScale - lowerScale)/(2*YTerm) < 12){
                YTerm = YTerm*2;
                YTermRed = 2;
            }
            else if((upperScale - lowerScale)/(5*YTerm) < 12){
                YTerm = YTerm*5;
                YTermRed = 5;
            }
            else{
                YTerm = YTerm*10;
                YTermMag = YTerm;
            }
        }


        XTerm = 1;
        while(timeScale/XTerm > 12){
            if(timeScale/(2*XTerm) < 12){
                XTerm = XTerm*2;
                XTermRed = 2;
            }
            else if(timeScale/(5*XTerm) < 12){
                XTerm = XTerm*5;
                XTermRed = 5;
            }
            else{
                XTerm = XTerm*10;
                XTermMag = XTerm;
            }
        }
        //System.out.println("XTerm: " + XTerm);




        //g.drawString("0", origin.x, origin.y);

        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(1*Main.paneHeight/800));
        int Ly;
        int n = 0;
        //for(int i = lowerScale/YTerm; i <= upperScale/YTerm; i++){
        for(int i = upperScale/YTerm; i >= lowerScale/YTerm; i--){
            g.setColor(Color.GRAY);
            Ly = height*n/((upperScale - lowerScale)/YTerm) + p.y;
            g.drawLine(p.x, Ly, p.x + width, Ly);
            //System.out.println(" " + i + "   " + n + "  " + (1000*n/((upperScale - lowerScale)/YTerm)));

            g.setColor(Color.BLACK);
            float axisNumber = ((float)(i))*YTerm/EQUALISE_FACTOR;
            String axisNumberText;
            if(axisNumber == (float)((int)(axisNumber))){
                axisNumberText = "" + (int)axisNumber;
            }
            else{
                axisNumberText = "" + axisNumber;
            }
            int antWidth = fm.stringWidth(axisNumberText);
            int antHeight = (int)font.getStringBounds(axisNumberText, context).getHeight();
            g.drawString(axisNumberText, p.x - antWidth - 3*Main.paneHeight/800, Ly + antHeight/3);
            n++;
        }

        int Lx;
        for(int j = 0; j <= timeScale/XTerm; j++){
            g.setColor(Color.GRAY);
            Lx = width*j/((int)timeScale/XTerm) + p.x;
            g.drawLine(Lx, p.y, Lx, p.y + height);
            //System.out.println(" " + j + "  " + height*j/((int)timeScale/XTerm));

            g.setColor(Color.BLACK);
            float axisNumber = ((float)(j))*XTerm/1000;
            String axisNumberText;
            if(axisNumber == (float)((int)(axisNumber))){
                axisNumberText = "" + (int)axisNumber;
            }
            else{
                axisNumberText = "" + axisNumber;
            }

            //int antWidth = fm.stringWidth(axisNumberText);
            int antHeight = (int)font.getStringBounds(axisNumberText, context).getHeight();
            if(lowerScale == 0){
                int antWidth = fm.stringWidth(axisNumberText);
                g.drawString(axisNumberText, Lx - antWidth/2, origin.y + antHeight);
            }
            else {
                g.drawString(axisNumberText, Lx + 3*Main.paneHeight/800, origin.y + antHeight);
            }

            n++;
        }

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2*Main.paneHeight/800));
        g.drawLine(origin.x, origin.y, origin.x + width, origin.y);
        g.drawLine(p.x, p.y, p.x, p.y + height);

        if(dataSet.size() == 0){
            return;
        }
        if(dataSet.size() > 1){
            g.setColor(color);
            g.setStroke(new BasicStroke(1*Main.paneHeight/600));

            Point firstPoint = new Point((int)(width*dataSet.get(0).time/timeScale) + p.x, -(int)(EQUALISE_FACTOR*height*dataSet.get(0).value/(upperScale - lowerScale)) + origin.y);
            Point secondPoint;
            for(int i = 1; i < dataSet.size(); i++){
                secondPoint = new Point((int)(width*dataSet.get(i).time/timeScale) + p.x, -(int)(EQUALISE_FACTOR*height*dataSet.get(i).value/(upperScale - lowerScale)) + origin.y);
                g.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y);
                firstPoint = secondPoint;
            }
        }

        //g.setColor(Color.RED);
        g.setStroke(new BasicStroke(100*Main.paneHeight/800));

        DataPoint lastPoint = dataSet.get(dataSet.size() - 1);
        Ellipse2D.Double dot = new Ellipse2D.Double((int)(width*lastPoint.time/timeScale) - dotRadius/2 + p.x, -(int)(EQUALISE_FACTOR*height*lastPoint.value/(upperScale - lowerScale)) - dotRadius/2 + origin.y, dotRadius, dotRadius);
        g.fill(dot);

    }

}
