package com.example.wifiscanuhk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebInterface {

	private Context c;
	private int x,y;
	private float accX, accY, accZ, gyroX, gyroY, gyroZ, magX, magY, magZ;
	
	private WifiManager wifiManager;
	private List<ScanResult> scanResults;
	
	private SensorManager sensorManager;
	private SensorEventListener sensorListener;
	
	private FileWriter pw;
	
	public WebInterface(Context c) {
		this.c = c;
	}
	
	// TODO Pridat scan mobilni site (operator, BTS...)
	
	
	@JavascriptInterface
	public void setXY(int x, int y)
	{
		System.out.println(x + " " + y);
		this.x = x;
		this.y = y;
		
		wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
		sensorManager = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
		
		initListeners();
		
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
		sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
		
		if (!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);
		
		c.registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				scanResults = wifiManager.getScanResults();
				try {
					goNext();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}, new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
	}

	private void initListeners() {
		sensorListener = new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				Sensor sensor = event.sensor;
				switch (sensor.getType()) {
				case Sensor.TYPE_ACCELEROMETER:
					accX = event.values[0];
					accY = event.values[1];
					accZ = event.values[2];
					break;
				case Sensor.TYPE_GYROSCOPE:
					gyroX = event.values[0];
					gyroY = event.values[1];
					gyroZ = event.values[2];
					break;
				case Sensor.TYPE_MAGNETIC_FIELD:
					magX = event.values[0];
					magY = event.values[1];
					magZ = event.values[2];
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	private void goNext() throws IOException {
		// TODO Ukladani do CSV
		File file = new File(Environment.getExternalStorageDirectory(), "data.csv");
		if (!file.exists())
		{
			try {
				file.createNewFile();
				pw = new FileWriter(file, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			try {
				pw = new FileWriter(file, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Toast.makeText(c, "Size of ScanResults: " + scanResults.size(), Toast.LENGTH_SHORT).show();
		for (int i = 0 ; i < scanResults.size(); i++)
		{
			ScanResult sr = scanResults.get(i);
			String text = x + ";" + y + ";" +sr.SSID + 
						";" + sr.BSSID + ";" + sr.frequency + ";" + sr.level + 
						";" + sr.capabilities + 
						";" + accX + ";" + accY + ";" + accZ +
						";" + gyroX + ";" + gyroY + ";" + gyroZ +
						";" + magX + ";" + magY + ";" + magZ;			
			for (char c : text.toCharArray())
			{
				pw.append(c);
			}
			pw.append('\n');
			Toast.makeText(c, "Writed "+ sr.SSID + " Wifi ", Toast.LENGTH_SHORT).show();
		}
		scanResults = null;
		pw.close();
		
	}
	
}
