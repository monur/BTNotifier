package com.nuvaapps.btbadge;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;

public class BTBadgeService extends Service {
	private BluetoothSocket socket;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try{
			String[] projection = { CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE };
			String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1";         
			Cursor cur = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, projection,where, null, null);
			int cursorSize = 0;
			try{
		         if (cur.moveToFirst()) {
		           String name;
		           String number;
		           long date;
		           System.out.println("Reading Call Details: ");
		           do {
		               name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));
		               number = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));
		               date = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE));
		               cursorSize++;
		               Log.w("Call Log:",number + ":"+ new Date(date) +":"+name);
		          } while (cur.moveToNext());
		        }
		    }
		    finally{
		      cur.close();
		    }
			BluetoothDevice device = intent.getParcelableExtra("device");
			//WTF is this?
	    	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	        socket = (BluetoothSocket) m.invoke(device, 1);
			if(socket!=null){
				try {
					socket.connect();
					Log.w("", "SOCKET connected");
					if(cursorSize > 0)
						socket.getOutputStream().write('1');
					else
						socket.getOutputStream().write('0');
					socket.close();
					Log.w("", "OUTPUT written");
				} catch (IOException e) {
					Log.e("", "", e);
				}
			}
		}catch(Exception e){
			Log.e("", "", e);
		}
		return Service.START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	

}
