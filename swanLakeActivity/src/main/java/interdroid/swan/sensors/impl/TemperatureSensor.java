package interdroid.swan.sensors.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import interdroid.swan.R;
import interdroid.swan.sensors.AbstractConfigurationActivity;
import interdroid.swan.sensors.AbstractSwanSensor;

public class TemperatureSensor extends AbstractSwanSensor {
    public static final String TAG = "TemperatureSensor";

    /**
     * The configuration activity for this sensor.
     *
     * @author nick &lt;palmer@cs.vu.nl&gt;
     */
    public static class ConfigurationActivity extends AbstractConfigurationActivity {

        @Override
        public final int getPreferencesXML() {
            return R.xml.temperature_preferences;
        }
    }

    /**
     * Value of ACCURACY must be one of SensorManager.SENSOR_STATUS_ACCURACY_*
     */
    public static final String ACCURACY = "accuracy";

    public static final String TEMPERATURE_FIELD = "temperature";

    private Sensor temperatureSensor;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                currentConfiguration.putInt(ACCURACY, accuracy);
            }
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                long now = acceptSensorReading();
                if (now >= 0) {
                    Log.d(TAG, "onSensorChanged: " + now + " val " + event.values[0]);
                    putValueTrimSize(VALUE_PATHS[0], null, now, event.values[0]);
                }
            }
        }
    };

    @Override
    public String[] getValuePaths() {
        return new String[]{TEMPERATURE_FIELD};
    }

    @Override
    public void initDefaultConfiguration(Bundle DEFAULT_CONFIGURATION) {
        DEFAULT_CONFIGURATION.putInt(DELAY, normalizeSensorDelay(SensorManager.SENSOR_DELAY_NORMAL));
        DEFAULT_CONFIGURATION.putInt(ACCURACY, SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    @Override
    public void onConnected() {
        SENSOR_NAME = "Temperature Sensor";
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sensorList.size() > 0) {
            temperatureSensor = sensorList.get(0);
        } else {
            Toast.makeText(getApplicationContext(), "No temperatureSensor found on device", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No temperatureSensor found on device!");
        }
    }

    @Override
    public final void register(String id, String valuePath, Bundle configuration, final Bundle httpConfiguration, Bundle extraConfiguration) {
        super.register(id, valuePath, configuration, httpConfiguration, extraConfiguration);
        updateDelay();
    }

    private void updateDelay() {
        sensorManager.unregisterListener(sensorEventListener);
        int delay = getSensorDelay();
        if (delay >= 0) {
            sensorManager.registerListener(sensorEventListener, temperatureSensor, delay);
            Log.d(TAG, "delay set to " + delay);
        }
    }

    @Override
    public final void unregister(String id) {
        updateDelay();
    }

    @Override
    public final void onDestroySensor() {
        sensorManager.unregisterListener(sensorEventListener);
        super.onDestroySensor();
    }

    @Override
    public float getCurrentMilliAmpere() {
        return temperatureSensor.getPower();
    }
}
