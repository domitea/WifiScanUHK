package com.example.wifiscanuhk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ArrayList<Scan> scans;
	
	HashMap<String, ArrayList<Scan>> navigation;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		scans = new ArrayList<Scan>();
		navigation = new HashMap<String, ArrayList<Scan>>();
		
		Config.context = this;
		WebView view = (WebView) findViewById(R.id.web);
        view.getSettings().setJavaScriptEnabled(true);
        view.setWebChromeClient(new WebChromeClient());
        view.addJavascriptInterface(new WebInterface(this), "Android");
        view.loadData(readTextFromResource(R.drawable.uhk_2_patro), "application/svg+xml", "UTF-8");
        
        // nacitani dat pro urceni pozice
        prepareDataForNav();
        
        //view.loadUrl("javascript:alert(" + " \" " + "Nazdarek!" + " \" " + ")");
        
        Finder finder = new Finder(navigation, view);
        finder.getActualScan();
	}

	private void prepareDataForNav() {
		
		
		String data = readTextFromResource(R.drawable.data);
        //Toast.makeText(getBaseContext(),String.valueOf(data.indexOf('\n')), Toast.LENGTH_SHORT).show();
        String[] dataScanInRawRows = data.split("\n");
        //Toast.makeText(getBaseContext(), String.valueOf(dataScanInRawRows.length), Toast.LENGTH_SHORT).show();
        for (String s : dataScanInRawRows)
        {
        	String[] row = s.split(";");
        	//0; 1; 2;    3;          4;       5
        	// x;y;SSID;BSSID(MAC);Frekvence;sila; other stuff.... priklad nize
        	// 135;192;eduroam;00:1a:e3:d2:e7:20;2462;-73;	[WPA2-EAP-CCMP][ESS][P2P];	-0.785;	4.711;	9.557;	0;	0;	0;	-9.12;	-16.74;	-48.12
        	Scan scan = new Scan(Integer.parseInt(row[0]), Integer.parseInt(row[1]), row[3], Integer.parseInt(row[5]));
        	scans.add(scan);
        }
        //Toast.makeText(getBaseContext(), String.valueOf(scans.size()), Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), String.valueOf(macs.size()), Toast.LENGTH_SHORT).show(); // pocet vysilacich zarizeni (urcene podle MAC)
        
       for (Scan s : scans)
       {
    	   String hashCoord = String.valueOf(s.getX()) + String.valueOf(s.getY());
    	   if (navigation.containsKey(hashCoord))
    	   {
    		   ArrayList<Scan> scanCoords = navigation.get(hashCoord);
    		   scanCoords.add(s);
    	   }
    	   else
    	   {
    		   ArrayList<Scan> scan = new ArrayList<Scan>();
    		   scan.add(s);
    		   navigation.put(hashCoord,scan);
    	   }
       }
       
     //Toast.makeText(getBaseContext(), String.valueOf(navigation.size()), Toast.LENGTH_SHORT).show();
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private String readTextFromResource(int resourceID)
	{
		InputStream raw = getResources().openRawResource(resourceID);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int i;
		try
		{
			i = raw.read();
			while (i != -1)
			{
				stream.write(i);
				i = raw.read();
			}
			raw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return stream.toString();
	}

}
