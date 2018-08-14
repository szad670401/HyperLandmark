package com.sample.tracking;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class Accelerometer {

    public enum CLOCKWISE_ANGLE {
        Deg0(0), Deg90(1), Deg180(2), Deg270(3);
        private int value;

        private CLOCKWISE_ANGLE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private SensorManager mSensorManager = null;

    private boolean mHasStarted = false;

    private static CLOCKWISE_ANGLE sRotation;

    public Accelerometer(Context ctx) {
        mSensorManager = (SensorManager) ctx
                .getSystemService(Context.SENSOR_SERVICE);
        sRotation = CLOCKWISE_ANGLE.Deg0;
    }


    public void start() {
        if (mHasStarted) return;
        mHasStarted = true;
        sRotation = CLOCKWISE_ANGLE.Deg0;
        mSensorManager.registerListener(accListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void stop() {
        if (!mHasStarted) return;
        mHasStarted = false;
        mSensorManager.unregisterListener(accListener);
    }

    static public int getDirection() {
        return sRotation.getValue();
    }


    private SensorEventListener accListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = arg0.values[0];
                float y = arg0.values[1];
                float z = arg0.values[2];
                if (Math.abs(x) > 3 || Math.abs(y) > 3) {
                    if (Math.abs(x) > Math.abs(y)) {
                        if (x > 0) {
                            sRotation = CLOCKWISE_ANGLE.Deg0;
                        } else {
                            sRotation = CLOCKWISE_ANGLE.Deg180;
                        }
                    } else {
                        if (y > 0) {
                            sRotation = CLOCKWISE_ANGLE.Deg90;
                        } else {
                            sRotation = CLOCKWISE_ANGLE.Deg270;
                        }
                    }
                }
            }
        }
    };
}
