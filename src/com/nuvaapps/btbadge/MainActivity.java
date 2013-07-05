package com.nuvaapps.btbadge;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final int REQUEST_ENABLE_BT = 313131;
	BluetoothAdapter mBluetoothAdapter = null;
	BluetoothDevice device = null;
	BluetoothSocket socket = null;
	boolean on = false;
	Intent serviceIntent = null;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            BluetoothDevice tmpDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            Log.w("DEVICE::::", tmpDevice.getName());
	            if(tmpDevice.getName().equals(getString(R.string.device_name))){
	            	device = tmpDevice;
	            	mBluetoothAdapter.cancelDiscovery();
	            	Toast.makeText(context, R.string.deviceFound, Toast.LENGTH_SHORT).show();
	            	if(connectBluetoothDevice(device))
	            		setAlarm();
	            }
	        }
	    }
	};
	
	private void refreshUI(){
		Button button = (Button)findViewById(R.id.button1);
		Button testButton = (Button)findViewById(R.id.buttonTest);
		TextView textView = (TextView)findViewById(R.id.TextView01);
		final ProgressBar bar = (ProgressBar)findViewById(R.id.progressBar1);
		bar.setVisibility(View.INVISIBLE);
		button.setVisibility(View.VISIBLE);
		Log.w("Service is running:", String.valueOf(isServiceRunning()));
		if(isServiceRunning()){
			testButton.setVisibility(View.VISIBLE);
			textView.setText(R.string.serviceOn);
			button.setText(R.string.button_serviceOn);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					stopService();
				}
			});
		}else{
			testButton.setVisibility(View.INVISIBLE);
			textView.setText(R.string.serviceOff);
			button.setText(R.string.button_serviceOff);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					v.setVisibility(View.INVISIBLE);
					bar.setVisibility(View.VISIBLE);
					startService();
				}
			});
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		serviceIntent = new Intent(getApplicationContext(), BTBadgeService.class);
		Button testButton = (Button)findViewById(R.id.buttonTest);
		testButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BluetoothFunctions func = new BluetoothFunctions(device);
				byte[] bytesTest = {0x00, -0x76, 0x0A, -0x76, 0x64, -1};
				func.send(bytesTest);
			}
		});
		refreshUI();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == RESULT_OK){
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				if (pairedDevices.size() > 0) {
				    for (BluetoothDevice device : pairedDevices) {
				        if(device.getName().equals(getString(R.string.device_name))){
				        	this.device = device;
				    		Log.w("", "NachNuch device found in paired devices list");
				        	if(connectBluetoothDevice(device))
				        		setAlarm();
				        }
				    }
				}
				if(device == null){
					IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
					registerReceiver(mReceiver, filter); 
					mBluetoothAdapter.startDiscovery();
				}
			}else{
				Toast.makeText(this, R.string.bluetoothDisabled, Toast.LENGTH_LONG).show();
				refreshUI();
			}
		}
	}
	
	private boolean connectBluetoothDevice(BluetoothDevice device){
		//This method is called when the device is not yet paired, so
		//starting an initial connection for pairing
        try {
            //WTF is this?
        	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            socket = (BluetoothSocket) m.invoke(device, 1);
            Log.w("", "SOCKET found");
            socket.connect();
            Log.w("", "SOCKET connected");
            socket.close();
            return true;
        } catch (IOException e) { 
        	Log.e("", "Cannot connect to device");
        } catch (NoSuchMethodException e) {
			Log.e("", "", e);
		} catch (IllegalArgumentException e) {
			Log.e("", "", e);
		} catch (IllegalAccessException e) {
			Log.e("", "", e);
		} catch (InvocationTargetException e) {
			Log.e("", "", e);
		}
		Toast.makeText(this, R.string.noConnectionToDevice, Toast.LENGTH_LONG).show();
        return false;
	}
	
	private void startService(){
		//First check if bluetooth adapter is present
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.noBluetooth, Toast.LENGTH_LONG).show();
			return;
		}
		//Now check if bluetooth is enabled, try to enable if not
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}else{ // if already enabled
			onActivityResult(REQUEST_ENABLE_BT, RESULT_OK, null);
		}
	}
	
	private void stopService(){
		PendingIntent pii = PendingIntent.getService(getApplicationContext(), 2222, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.cancel(pii);
		setServiceRunning(false);
		refreshUI();
	}
	
	private void setAlarm(){
		serviceIntent.putExtra("device", device);
		PendingIntent pii = PendingIntent.getService(getApplicationContext(), 2222, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, 0, 15000, pii);
		setServiceRunning(true);
		refreshUI();
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try{
			unregisterReceiver(mReceiver);
		}catch(IllegalArgumentException e){
			Log.e("", "Illegal Argument Exception on unregisterReceiver...");
		}
	}

	public boolean isServiceRunning(){
		return getSharedPreferences(getResources().getString(R.string.app_name), 1).getBoolean("serviceRunning", false);
	}
	
	public void setServiceRunning(boolean serviceRunning) {
		getSharedPreferences(getResources().getString(R.string.app_name), 1).edit().putBoolean("serviceRunning", serviceRunning).commit();
	}
}
