package com.example.lezh1k.sensordatacollector.Loggers;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.elvishew.xlog.XLog;
import com.example.lezh1k.sensordatacollector.Commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lezh1k on 12/26/17.
 */

public class AccelerationLogger implements SensorEventListener {
    private List<Sensor> m_lstSensors = new ArrayList<Sensor>();
    private SensorManager m_sensorManager;

    private static int[] sensorTypes = {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
    };

    public AccelerationLogger(SensorManager sensorManager) {
        m_sensorManager = sensorManager;
        for (Integer st : sensorTypes) {
            Sensor sensor = m_sensorManager.getDefaultSensor(st);
            if (sensor == null) {
                Log.d(Commons.AppName, String.format("Couldn't get sensor %d", st));
                continue;
            }
            m_lstSensors.add(sensor);
        }
    }

    public boolean start() {
        for (Sensor sensor : m_lstSensors) {
            if (!m_sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME)) {
                XLog.e("Couldn't register listener : %d", sensor.getType());
                return false;
            }
        }
        return true;
    }

    public void stop() {
        for (Sensor sensor : m_lstSensors) {
            m_sensorManager.unregisterListener(this, sensor);
        }
    }

    private float[] R = new float[16];
    private float[] RI = new float[16];
    private float[] accAxis = new float[4];
    private float[] linAcc = new float[4];
    private String lastLoggedString = "";

    public String getLastLoggedString() {
        return lastLoggedString;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                System.arraycopy(event.values, 0, linAcc, 0, event.values.length);
                android.opengl.Matrix.multiplyMV(accAxis, 0, RI,
                        0, linAcc, 0);
                lastLoggedString = String.format(" %d Linear abs acc : %f %f %f",
                        System.currentTimeMillis(), event.values[0],
                        event.values[1], event.values[2]);
                XLog.i(lastLoggedString);
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                android.opengl.Matrix.invertM(RI, 0, R, 0);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
