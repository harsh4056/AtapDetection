package com.example.moon.atapdetection;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.net.InterfaceAddress;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by moon on 24/6/17.
 */

public class RealtimeDataDisplayFragment extends Fragment {

    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private double graph2LastXValue = 10d;
    private double previousDouble=0d;
    private double previousDifferentiator=0d;
    private double previousTap=0d;
    private TextView textViewData,textViewDiff;
    private TextView textViewDoubleDiff;

   double [] movingAverage= new double[25];

    int counter=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context= container.getContext();
        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);




        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GraphView graph = (GraphView) rootView.findViewById(R.id.graph1);
        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);



        for(int i=0;i<25;i++)
            movingAverage[i]=0.0d;






        GraphView graph2 = (GraphView) rootView.findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        textViewData= rootView.findViewById(R.id.textView);
        textViewDiff=rootView.findViewById(R.id.textViewDiff);
        textViewDoubleDiff=rootView.findViewById(R.id.tvDOubleDIfferentiator);
        GraphView graph3 = (GraphView) rootView.findViewById(R.id.graphView);
        mSeries3= new LineGraphSeries<>();
        graph3.addSeries(mSeries3);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinX(0);
        graph3.getViewport().setMaxX(40);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double total = Math.sqrt(x * x + y * y + z * z);
                graph2LastXValue += 1d;
                double currentValue=total;
                textViewData.setText( currentValue+"");






                mSeries1.appendData(new DataPoint(graph2LastXValue, currentValue), true, 40);
                double differentiator= Math.abs( currentValue-previousDouble);
                previousDouble=currentValue;


                if(differentiator>2)

                    textViewDiff.setText(differentiator+"");

                mSeries2.   appendData(new DataPoint(graph2LastXValue,differentiator ), true, 40);



                double doubleDifferentiator=Math.abs( differentiator-previousDifferentiator);
                mSeries3.appendData(new DataPoint(graph2LastXValue, Math.round(doubleDifferentiator)), true, 40);
                textViewDoubleDiff.setText(Math.round(doubleDifferentiator)+"");
                previousDifferentiator=doubleDifferentiator;

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

        }, sensor, SensorManager.SENSOR_DELAY_GAME);

        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }





}