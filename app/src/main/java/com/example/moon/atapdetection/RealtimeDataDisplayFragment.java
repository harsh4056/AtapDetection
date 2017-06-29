package com.example.moon.atapdetection;

import android.app.Fragment;
import android.content.Context;
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

import static java.lang.Math.abs;

/**
 * Created by moon on 24/6/17.
 */

public class RealtimeDataDisplayFragment extends Fragment {

    private LineGraphSeries<DataPoint> mSeries1;
    private LineGraphSeries<DataPoint> mSeries2;
    private LineGraphSeries<DataPoint> mSeries3;
    private double graph2LastXValue = 0d;
    private double previousDouble=0d;
    private double previousDifferentiator=0d;
    private double previousTap=0d;
    private EditText etInfluence,etThreshold,etLag;
    private SeekBar seekBar;
     Sensor sensor;
     SensorManager sensorManager;
    private  String binarySignal="";
    private int samplingRate=5;
    private  int sizeCounter=30;
    private int initialTap=0;
    private int tapGap=50;
    ArrayList<Double> ternarySignals = new ArrayList<>();
    ArrayList<Double> filteredY = new ArrayList<>();
    ArrayList<Double> avgFilter = new ArrayList<>();
    ArrayList<Double> stdFilter = new ArrayList<>();
    ArrayList<Double> originalSignal = new ArrayList<>();

    int lag= 15;
    int differentiatorSize=5;
    double differentiatorArray[];
    private  int i=lag;
    double threshold= 3.5;
    double influence= 0.0;

    double mean;
    private boolean initialized=false;
    private TextView textViewData,textViewSTD;
    private EditText etTapGap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Context context= container.getContext();

        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        differentiatorArray= new double[differentiatorSize];
        for(int i=0;i<differentiatorSize;i++)
            differentiatorArray[i]=0;

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GraphView graph = (GraphView) rootView.findViewById(R.id.graph1);
        mSeries1 = new LineGraphSeries<>();
        graph.addSeries(mSeries1);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);








        seekBar= rootView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sensorManager.unregisterListener(sensorEventListener);
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
        GraphView graph2 = rootView.findViewById(R.id.graph2);
        mSeries2 = new LineGraphSeries<>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);
        etInfluence= rootView.findViewById(R.id.editTextInfluence);
        etLag=rootView.findViewById(R.id.editTextLag);
        etThreshold=rootView.findViewById(R.id.editThreshold);
        etTapGap=rootView.findViewById(R.id.editTextGap);
        textViewData=rootView.findViewById(R.id.textView);
        textViewSTD=rootView.findViewById(R.id.textViewSTD);

        etLag.setOnKeyListener(onKeyListener);
        etThreshold.setOnKeyListener(onKeyListener);
        etInfluence.setOnKeyListener(onKeyListener);
        etTapGap.setOnKeyListener(onKeyListener);
        GraphView graph3 =  rootView.findViewById(R.id.graphView);



        mSeries3= new LineGraphSeries<>();
        graph3.addSeries(mSeries3);
        graph3.getViewport().setXAxisBoundsManual(true);
        graph3.getViewport().setMinX(0);
        graph3.getViewport().setMaxX(40);
        sensorManager.registerListener(sensorEventListener,sensor,samplingRate);




        return rootView;
    }

   SensorEventListener sensorEventListener=  new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double total = Math.sqrt(x * x + y * y + z * z);
            //    double total=y;
            double currentValue=  Math.round(total);
            textViewData.setText( currentValue+"");


            graph2LastXValue += 1d;

            if(graph2LastXValue<lag){

                originalSignal.add(currentValue);
                filteredY.add(currentValue);

            }else if(!initialized){
                originalSignal.add(currentValue);
                filteredY.add(currentValue);
                setZeros();
                mean = mean(originalSignal, lag, 0);
                avgFilter.add(lag, mean);
                stdFilter.add( lag,standardDeviation(originalSignal, mean, lag, 0));

                initialized=true;
            }
            else
            {
                originalSignal.add(currentValue);
                method();
            }

            mSeries1.appendData(new DataPoint(graph2LastXValue, currentValue), true, 40);










        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }};

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

    public  void method() {



        try {


                if (abs(originalSignal.get(i) - avgFilter.get(i - 1)) > threshold * stdFilter.get(i-1)) {
                    if (originalSignal.get(i) > avgFilter.get(i - 1)) {
                       ternarySignals.add( 1.0);
                        binarySignal=binarySignal+1;
                        mSeries2.appendData(new DataPoint(graph2LastXValue, 1.0), true, 40);
                    } else {
                        ternarySignals.add( -1.0);
                        mSeries2.appendData(new DataPoint(graph2LastXValue, -1.0), true, 40);

                    }
                    filteredY.add(influence * originalSignal.get(i) + (1 - influence) * filteredY.get(i-1));
                } else {
                    ternarySignals.add( 0.0);
                    binarySignal=binarySignal+0;
                    mSeries2.appendData(new DataPoint(graph2LastXValue, 0.0), true, 40);

                    filteredY.add( originalSignal.get(i));
                }
                    Log.d("I :",i+"");
            Log.d("Binary :",binarySignal);
                mean = mean(filteredY, i, i - lag);
                avgFilter.add( mean);
            double std=standardDeviation(filteredY, mean, i, i - lag);
                stdFilter.add( std);

            i++;
            if(binarySignal.endsWith("0100") ) {
                if( initialTap==0)
                initialTap=i;
                else if(i>=initialTap+tapGap) {
                    Toast.makeText(getContext(), "Double Tap ", Toast.LENGTH_SHORT).show();
                    initialTap=0;
                    binarySignal="";
                }
                else{
                    Toast.makeText(getContext(), "Single Tap ", Toast.LENGTH_SHORT).show();
                    initialTap=0;
                    binarySignal="";
                }


            }








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


    private double standardDeviation(ArrayList<Double> signal,double mean,int upperLimit,int lowerLimit){



        double sd = 0;
        for (int i = lowerLimit; i < upperLimit; i++)
        {
            sd += ((signal.get(i) - mean)  / (lag));
        }
        double standardDeviation = Math.sqrt(sd);


        return standardDeviation;
    }




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

public interface  TapDetection{

    void OnSingleTapDetection();
    void OnDoubleTapDetection();

}



}