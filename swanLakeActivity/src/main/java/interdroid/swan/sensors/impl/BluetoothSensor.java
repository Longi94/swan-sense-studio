package interdroid.swan.sensors.impl;

import interdroid.swan.R;
import interdroid.swan.sensors.AbstractConfigurationActivity;
import interdroid.swan.sensors.AbstractSwanSensor;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * A sensor for discovered bluetooth devices.
 * 
 * @author nick &lt;palmer@cs.vu.nl&gt;
 * 
 */
public class BluetoothSensor extends AbstractSwanSensor {

	private static final String TAG = "Bluetooth Sensor";
	
	/**
	 * This configuration activity for this sensor.
	 * 
	 * @author nick &lt;palmer@cs.vu.nl&gt;
	 * 
	 */
	public static class ConfigurationActivity extends
			AbstractConfigurationActivity {

		@Override
		public final int getPreferencesXML() {
			return R.xml.bluetooth_preferences;
		}
	}

	/**
	 * The device name field.
	 */
	public static final String DEVICE_NAME_FIELD = "name";
	/**
	 * The device address field.
	 */
	public static final String DEVICE_ADDRESS_FIELD = "address";

	/**
	 * The bond state for the device.
	 */
	public static final String DEVICE_BOND_STATE = "bond_state";

	/**
	 * The class of the device.
	 */
	private static final String DEVICE_CLASS = "class";

	/**
	 * The major class of the device.
	 */
	private static final String DEVICE_MAJOR_CLASS = "major_class";

	/**
	 * The discovery interval.
	 */
	public static final String DISCOVERY_INTERVAL = "discovery_interval";

	/**
	 * The default discovery interval.
	 */
	public static final long DEFAULT_DISCOVERY_INTERVAL = 5 * 60 * 1000; // ms

	
	/**
	 * A flag to indicate we should stop polling.
	 */
	private boolean stopPolling = false;

	/**
	 * The bluetooth adapter we use to do discovery.
	 */
	private BluetoothAdapter mBluetoothAdapter;

	/**
	 * The broadcast receiver which gets information on bluetooth discovery.
	 */
	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			long now = System.currentTimeMillis();
			BluetoothDevice device = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(TAG, "Bluetooth discovered: " + device.getName() + " "
					+ device.getAddress());

			putValueTrimSize(DEVICE_NAME_FIELD, null, now, device.getName());
			putValueTrimSize(DEVICE_ADDRESS_FIELD, null, now, device.getAddress());
		}

	};

	/**
	 * The thread which polls for bluetooth devices.
	 */
	private Thread bluetoothPoller = new Thread() {
		public void run() {
			while (!stopPolling) {
				long start = System.currentTimeMillis();
				if (registeredConfigurations.size() > 0) {
					if (mBluetoothAdapter != null
							&& !mBluetoothAdapter.isDiscovering()) {
						mBluetoothAdapter.startDiscovery();
						Log.d(TAG, "bt started discovery");
					}
				}
				try {
					Thread.sleep(currentConfiguration
							.getLong(DISCOVERY_INTERVAL)
							+ start
							- System.currentTimeMillis());
				} catch (InterruptedException e) {
					Log.w(TAG, "Interrupted while sleeping.");
				}
			}
		}
	};

	@Override
	public final String[] getValuePaths() {
		return new String[] { DEVICE_ADDRESS_FIELD, DEVICE_NAME_FIELD,
				DEVICE_BOND_STATE, DEVICE_CLASS, DEVICE_MAJOR_CLASS };
	}

	@Override
	public final void initDefaultConfiguration(final Bundle defaults) {
		defaults.putLong(DISCOVERY_INTERVAL, DEFAULT_DISCOVERY_INTERVAL);
	}

	@Override
	public final void onConnected() {
		SENSOR_NAME = "Bluetooth Sensor";
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		Log.d(TAG, "bluetooth connected");
	}

	@Override
	public final void register(final String id, final String valuePath,
							   final Bundle configuration, final Bundle httpConfiguration, Bundle extraConfiguration) {

		super.register(id,valuePath,configuration,httpConfiguration, extraConfiguration);

		if (registeredConfigurations.size() == 1) {
			registerReceiver(bluetoothReceiver, new IntentFilter(
					BluetoothDevice.ACTION_FOUND));
			if (!bluetoothPoller.isAlive()) {
				bluetoothPoller.start();
			}
		}
		updatePollRate();
	}

	/**
	 * Update the polling rate.
	 */
	private void updatePollRate() {
		boolean keepDefault = true;
		long updatedPollRate = Long.MAX_VALUE;
		for (Bundle configuration : registeredConfigurations.values()) {
			if (configuration.containsKey(DISCOVERY_INTERVAL)) {
				keepDefault = false;
				updatedPollRate = Math.min(updatedPollRate,
						configuration.getLong(DISCOVERY_INTERVAL));
			}
		}
		if (keepDefault) {
			currentConfiguration.putLong(DISCOVERY_INTERVAL,
					DEFAULT_DISCOVERY_INTERVAL);
		} else {
			currentConfiguration.putLong(DISCOVERY_INTERVAL, updatedPollRate);
		}
	}

	@Override
	public final void unregister(final String id) {
		if (registeredConfigurations.size() == 0) {
			unregisterReceiver(bluetoothReceiver);
		}
		updatePollRate();
	}

	@Override
	public final void onDestroySensor() {		
		if (registeredConfigurations.size() > 0) {
			unregisterReceiver(bluetoothReceiver);
		}
		mBluetoothAdapter.cancelDiscovery();
		stopPolling = true;
		bluetoothPoller.interrupt();
		
		super.onDestroySensor();
	}

}
