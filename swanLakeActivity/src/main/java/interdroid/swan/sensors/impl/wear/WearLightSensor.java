package interdroid.swan.sensors.impl.wear;

import android.os.Bundle;

import java.io.IOException;

import interdroid.swan.R;
import interdroid.swan.sensors.AbstractConfigurationActivity;
import interdroid.swan.sensors.impl.wear.shared.AbstractWearSensor;

/**
 * Created by Veaceslav Munteanu on 14-March-16.
 * @email veaceslav.munteanu90@gmail.com
 */
public class WearLightSensor extends AbstractWearSensor {



    public static final String LUX_FIELD = "lux";

    /**
     * The configuration activity for this sensor.
     *
     * @author nick &lt;palmer@cs.vu.nl&gt;
     *
     */
    public static class ConfigurationActivity extends
            AbstractConfigurationActivity {

        @Override
        public final int getPreferencesXML() {
            return R.xml.light_preferences;
        }

    }

    @Override
    public String[] getValuePaths() {
        return new String[] { LUX_FIELD };
    }



    @Override
    public void register(String id, String valuePath, Bundle configuration) throws IOException {
        SENSOR_NAME = "Wear Light Sensor";
        sensor_name = "Light";
        valuePathMappings.put(LUX_FIELD, 0);

        super.register(id, valuePath, configuration);
    }

}
