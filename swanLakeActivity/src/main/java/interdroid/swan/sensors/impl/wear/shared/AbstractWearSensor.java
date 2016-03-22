package interdroid.swan.sensors.impl.wear.shared;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import interdroid.swan.sensors.AbstractSwanSensor;
import interdroid.swan.sensors.impl.wear.shared.data.SensorDataPoint;
import interdroid.swan.sensors.impl.wear.shared.data.WearSensor;

/**
 * Created by slavik on 3/14/16.
 */
public abstract class AbstractWearSensor  extends AbstractSwanSensor{

    final String ABSTRACT_SENSOR = "Abstract Sensor";
    protected HashMap<String, Integer> valuePathMappings = new HashMap<>();
    protected String sensor_name = ABSTRACT_SENSOR;
    ArrayList<String> ids = new ArrayList<>();
    ReentrantLock lock = new ReentrantLock();

    private SensorUpdate updateReceiver = new SensorUpdate();

    private class SensorUpdate extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(RemoteSensorManager.UPDATE_MESSAGE)){
                Bundle extra = intent.getExtras();
                //String who = extra.getString("sensor");
                WearSensor s = (WearSensor)extra.getSerializable("sensor");
                SensorDataPoint d = (SensorDataPoint)extra.getSerializable("data");
                if(s.getName().equals(sensor_name)) {
                    Log.d(TAG, "Got update+++++++++++++++++++++" + s.getName());
                    float[] dataz = d.getValues();
                    for(Map.Entry<String, Integer> data : valuePathMappings.entrySet()) {
                            for(String id : ids) {
                                putValueTrimSize(data.getKey(), id, System.currentTimeMillis(), dataz[data.getValue()]);
                            }
                    }
                }

            }

        }
    }

    @Override
    public void initDefaultConfiguration(Bundle defaults) {

    }

    @Override
    public void register(String id, String valuePath, Bundle configuration) throws IOException {

        if(sensor_name.equals(ABSTRACT_SENSOR)){
            Log.w(ABSTRACT_SENSOR, "You need to set the sensor name");
            return;
        }

        if(valuePathMappings.isEmpty()) {
            Log.w(ABSTRACT_SENSOR, "You need to set the path mappings");
            return;
        }

        lock.lock();
        if(ids.isEmpty()) {
            RemoteSensorManager.getInstance(this).startMeasurement();
        }
        ids.add(id);
        lock.unlock();

        registerReceiver(updateReceiver, new IntentFilter(RemoteSensorManager.UPDATE_MESSAGE));

    }

    @Override
    public void unregister(String id) {
        lock.lock();
        ids.remove(id);

        if(ids.isEmpty())
            RemoteSensorManager.getInstance(this).stopMeasurement();
        lock.unlock();

        unregisterReceiver(updateReceiver);
    }

    @Override
    public void onConnected() {

    }
}
