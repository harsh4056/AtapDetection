package com.example.moon.atapdetection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


/**
 * Created by moon on 24/6/17.
 */

public  class TapDetection {

    private Context context;
    private double previousValue;
    private double doubleTapHOlder;
    private int counter=0;
    boolean singleTapHolding=false;


    int limiter=250;
    double lowerLimit =2.7d;
       double   upperLimit=7d;
    double range=4.3d;
    private OnTapDetectionListener onTapDetectionListener;
   public TapDetection(Context context){
        this.context=context;

    }
    void setOnTapDetectionListener(OnTapDetectionListener onTapDetectionListener){
        this.onTapDetectionListener=onTapDetectionListener;
        tapSensing();


    }
    void tapSensing() {

        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                double total = Math.sqrt(x * x + y * y + z * z);

                double currentValue = total;


                double differentiator = Math.abs(currentValue - previousValue);

                if(differentiator>lowerLimit && differentiator<upperLimit) {

                    if(!singleTapHolding){

                        singleTapHolding=true;

                        doubleTapHOlder=differentiator;

                    }

                  else   if (counter < limiter  ) {

                        if(Math.abs(differentiator-doubleTapHOlder)<=range) {
                            onTapDetectionListener.onDoubleTapDetected();
                            Log.d("Counter :",differentiator-doubleTapHOlder+"");
                        }

                    }
                    else if( counter>=limiter){
                        counter=0;
                        Log.d("Differentiator :",differentiator+"");
                        onTapDetectionListener.onTapDetected();
                        singleTapHolding=false;
                    }


                }

                Log.d("Counter :",counter+"");
                counter++;
                previousValue = Math.round(total);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }


        }, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }


public interface OnTapDetectionListener{
    void onTapDetected();
    void onDoubleTapDetected();

}

}
