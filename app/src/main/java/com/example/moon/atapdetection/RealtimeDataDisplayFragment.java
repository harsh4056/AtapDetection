package com.example.moon.atapdetection;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

/**
 * Created by moon on 24/6/17.
 */

public class RealtimeDataDisplayFragment extends Fragment {

    private LineGraphSeries<DataPoint> dataSignalLineGraphSeries;// add original signal to graph
    private LineGraphSeries<DataPoint> binarySignalLineGraphSeries;//add peaks detected to graphs
    private LineGraphSeries<DataPoint> stdeviationLineGraphSeries;// Standard deviation
    private double graph2LastXValue = 0d;// X value of the Graph

    private EditText etInfluence,etThreshold,etLag;// Edit texts
    private SeekBar seekBar;
     Sensor sensor;
     SensorManager sensorManager;
    private  String binarySignal="";//String for matching with ending with 0100
    private int samplingRate=5;//Current saples per second

    private int initialTap=0;// first tap detected at value
    private int tapGap=50;// interval after which second tap is to be detected

    ArrayList<Double> filteredY = new ArrayList<>();//initialised with values of original signal till lag th position
    ArrayList<Double> avgFilter = new ArrayList<>();// moving average storage arraylist
    ArrayList<Double> stdFilter = new ArrayList<>();// moving standard deviation storage arraylist
    ArrayList<Double> originalSignal = new ArrayList<>();// original signal storage arraylist

    int lag= 15;// window size to calculate mean

    private  int i=lag;// x value counter,cannot be same xlastvalue as to initialize other variables
    double threshold= 3.5;// Calculation of tap detection if signal is threshold values away
    double influence= 0.5;// influence percentage of new signal to mean and standard deviation

    double mean;// as the name suggests
    private boolean initialized=false;// for values stored up to lag point
    private TextView textViewData,textViewSTD;
    private EditText etTapGap;// edit text to mannual enter tap gap
    private LineGraphSeries<DataPoint> meanSeries;// mean sereis to add to graph

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context= container.getContext();

        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);



/*------------------------------View Initializations and associating series to graphs------------------------------------*/
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GraphView graph = (GraphView) rootView.findViewById(R.id.graphView);
        dataSignalLineGraphSeries = new LineGraphSeries<>();
        graph.addSeries(dataSignalLineGraphSeries);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);
        GraphView graph2 = rootView.findViewById(R.id.graphBinary);
        binarySignalLineGraphSeries = new LineGraphSeries<>();
        graph2.addSeries(binarySignalLineGraphSeries);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        etInfluence= rootView.findViewById(R.id.editTextInfluence);
        etLag=rootView.findViewById(R.id.editTextLag);
        etThreshold=rootView.findViewById(R.id.editThreshold);
        etTapGap=rootView.findViewById(R.id.editTextGap);


        etLag.setOnKeyListener(onKeyListener);
        etThreshold.setOnKeyListener(onKeyListener);
        etInfluence.setOnKeyListener(onKeyListener);
        etTapGap.setOnKeyListener(onKeyListener);
        seekBar= rootView.findViewById(R.id.seekBar);

        stdeviationLineGraphSeries = new LineGraphSeries<>();
        meanSeries = new LineGraphSeries<>();
        graph.addSeries(stdeviationLineGraphSeries);
        graph.addSeries(meanSeries);
/*-------------------------------------------------------------------------------------------------------------------------------------*/
        sensorManager.registerListener(sensorEventListener,sensor,samplingRate);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sensorManager.unregisterListener(sensorEventListener);// de reg
                samplingRate=i;
               sensorManager.registerListener(sensorEventListener,sensor,samplingRate) ;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });










        return rootView;
    }

    private int j=0;
    SensorEventListener sensorEventListener=  new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double total = Math.sqrt(x * x + y * y + z * z);
            //    double total=y;
            double currentValue=  total;



            graph2LastXValue += 1d;// increment x on graph

            if(graph2LastXValue<lag){//if initialized uptill lag

                originalSignal.add(currentValue);
                filteredY.add(currentValue);

            }else if(!initialized){//if lag = graph2lastvalue
                originalSignal.add(currentValue);
                filteredY.add(currentValue);
                setZeros();// setting zeros to required array lists
                mean = mean(originalSignal, lag, 0);// initial mean till lag window
                avgFilter.add(lag, mean);//add mean to arraylist
                stdFilter.add( lag,standardDeviation(originalSignal, mean, lag, 0));//add stdeviation to arraylist

                initialized=true;
            }
            else// always execute after initialization complete
            {
                originalSignal.add(currentValue);

                method();
            }


          /*---------------------------Adding Data To graph-------------------------------------------*/
            dataSignalLineGraphSeries.setColor(Color.GRAY);
            dataSignalLineGraphSeries.appendData(new DataPoint(graph2LastXValue, currentValue), true, 40);

            if(!stdFilter.isEmpty()) {
                stdeviationLineGraphSeries.setColor(Color.BLUE);
                stdeviationLineGraphSeries.appendData(new DataPoint(graph2LastXValue,threshold* -stdFilter.get(j)), true, 40);
            stdeviationLineGraphSeries.appendData(new DataPoint(graph2LastXValue,threshold* stdFilter.get(j)), true, 40);}
            if(!avgFilter.isEmpty()) {
                meanSeries.setColor(Color.GREEN);
                meanSeries.appendData(new DataPoint(graph2LastXValue, avgFilter.get(j)), true, 40);
            }


/*-------------------------------------------------------------------------------------------------------*/

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }};
/*---------------------------------------------Adding LIstener to  edit texts for manually changing of threshold,lag,influence ,and tap interval values----------------------------------*/
        View.OnKeyListener onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
                        (i == KeyEvent.KEYCODE_ENTER)) {
                    switch (view.getId()) {
                        case R.id.editTextInfluence:
                            influence= Double.parseDouble(etInfluence.getText().toString());
                            Log.d("TAG",influence+"");
                            break;


                        case R.id.editTextLag:
                            lag =Integer.parseInt(etLag.getText().toString());
                            Log.d("TAG",lag+"");
                            break;


                        case R.id.editThreshold:
                        threshold= Double.parseDouble(etThreshold.getText().toString());
                            Log.d("TAG",threshold+"");
                            break;

                        case R.id.editTextGap:



                    }


                }



                return false;
            }
        };
/*---------------------------------------------------------------------------------------------------------------------*/











/*-----------------------------------Zscore threshlding Algorithm ----------------------------------------------------------*/
    public  void method() {



        try {


                if (abs(originalSignal.get(i) - avgFilter.get(i - 1)) > threshold * stdFilter.get(i-1)) {// check if the diference from mean is greater than threshold times standard deviation of previous
                    if (originalSignal.get(i) > avgFilter.get(i - 1)) {

                        binarySignal=binarySignal+1;
                        binarySignalLineGraphSeries.appendData(new DataPoint(graph2LastXValue, 1.0), true, 40);// add to realtime graph
                    } else {

                        binarySignalLineGraphSeries.appendData(new DataPoint(graph2LastXValue, -1.0), true, 40);//add to realtime graph

                    }
                    filteredY.add(influence * originalSignal.get(i) + (1 - influence) * filteredY.get(i-1));// influence role: How much does the standard deviation and mean is affected by newer values of the the signal from accelerometer
                } else {

                    binarySignal=binarySignal+0;// this section for no change in values
                    binarySignalLineGraphSeries.appendData(new DataPoint(graph2LastXValue, 0.0), true, 40);//add to realtime graph

                    filteredY.add( originalSignal.get(i));
                }
                    Log.d("I :",i+"");
          //  Log.d("Binary :",binarySignal);
                mean = mean(filteredY, i, i - lag);
                avgFilter.add( mean);


            double std=standardDeviation(filteredY, mean, i, i - lag);
            Log.d("Data :","Mean: "+ mean+"STD : "+std *threshold);

                stdFilter.add( std);
            j++;

            i++;

            /*---------------------------Taps detection logic-------------------------------------*/
            if(binarySignal.endsWith("0100") ) {// Regex matching with ends with to detect tap

                if(initialTap==0){ // first tap detect and hold position
                    initialTap=i;
                    binarySignal="";



                }
                else if (i>initialTap+tapGap) {//Check if second tap detected in signals

                    initialTap=0;
                    binarySignal="";
                    OnDoubleTapDetection();
                }

                else {//  Second tap not detected then declare single tap
                    OnSingleTapDetection();
                    initialTap=0;
                    binarySignal="";
                }
                }

 /*------------------------------------------------------------------------------------------------------------------*/
/*--------------------------------------------------------------------------------------------------------------------------------*/





        } catch (Exception e) {

        Log.d("Exception",e.getMessage().toString());
        }


    }







 public void   setZeros(){

     for(int i=0;i<lag;i++){
         stdFilter.add(0.0);
         avgFilter.add(0.0);
     }


    }

/*-----------Calculating Standard deviation function-----------------------------------------*/
    private double standardDeviation(ArrayList<Double> signal,double mean,int upperLimit,int lowerLimit){



        double sd = 0;
        for (int i = lowerLimit; i < upperLimit; i++)
        {
            sd += ((signal.get(i) - mean)  / (lag));
        }
        double standardDeviation = Math.sqrt(sd);


        return standardDeviation;
    }


/*-----------Calculating Mean function-----------------------------------------*/

    private double mean(ArrayList<Double> signal ,int upperLimit,int lowerLimit){



        double sd = 0;
        for (int i = lowerLimit; i < upperLimit; i++)
        {
            sd += signal.get(i) ;
        }
        double mean = sd/lag;


        return mean;
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }
    void OnSingleTapDetection(){
        Toast.makeText(getContext(),"Single",Toast.LENGTH_SHORT).show();
        Intent i = new Intent("com.android.music.musicservicecommand.togglepause");
        i.putExtra("command", "next");
        getContext().sendBroadcast(i);
    }
    void OnDoubleTapDetection(){

        Intent i = new Intent("com.android.music.musicservicecommand.togglepause");
        i.putExtra("command", "togglepause");
        getContext().sendBroadcast(i);


    }




}