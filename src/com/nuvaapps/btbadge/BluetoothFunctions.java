package com.nuvaapps.btbadge;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothFunctions {
	private BluetoothDevice device;
	
	public BluetoothFunctions(BluetoothDevice device){
		this.device = device;
	}
	
	public boolean send(byte[] bytes){
		//WTF is this?
		try {
			Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			BluetoothSocket socket = (BluetoothSocket) m.invoke(device, 1);
			if(socket!=null){
				try {
					socket.connect();
					Log.w("", "SOCKET connected");
					socket.getOutputStream().write(bytes);
					socket.close();
					Log.w("", "OUTPUT written");
					return true;
				} catch (IOException e) {
					Log.e("", "", e);
				}
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}
        return false;
	}
}
