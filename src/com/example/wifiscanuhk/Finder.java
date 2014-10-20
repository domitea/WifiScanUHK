/**
 * 
 */
package com.example.wifiscanuhk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * 
 * Trida repzentujici metody potrebne k nalezeni polohy + potrebna omacka
 *
 */
public class Finder {
	
	private HashMap<String, ArrayList<Scan>> navigationData;
	
	private WifiManager wifiManager;
	private List<ScanResult> scanResults;
	private Context c;
	private HashMap<Float, String> computedData;
	private WebView view;
	
	public Finder(HashMap<String, ArrayList<Scan>> navigationData, WebView wv) {
		this.navigationData = navigationData;
		c = Config.context;
		computedData = new HashMap<Float, String>();
		view = wv;
	}
	
	public void getActualScan()
	{
		
		wifiManager = (WifiManager) c.getSystemService(c.WIFI_SERVICE);
		
		if (!wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(true);
		
		c.registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				scanResults = wifiManager.getScanResults();
				computePosition();
				context.unregisterReceiver(this);
			}

			
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		
	}
	
	private void computePosition() {
		Toast.makeText(c, String.valueOf(scanResults.size()), Toast.LENGTH_SHORT).show();	
		
		/**
		 * Výpoèet euklidovské vzdálenosti - jak je známo, je to výpoèet alá pythagorova vìta, jen ve více rozmìrech
		 * èili to je sqrt(a*a+b*b+c*c+d*d....n*n)
		 * 
		 * Protože budu poèítat vzdálenost až z rozdílu, tak to bude: sqrt((a1-a2)^2 + (b1-b2)^2....), 
		 * kde hodnoty s indexem 1 budou aktuálnì namìøené hodnoty a hodnoty s indexem 2 budou brány z "fondu" skenù
		 * je také potøeba dbát na správné odeèítání hodnot, tzn. od stejných MACovek!
		 * 
		 * Co se týèe výpoètu, je provádìn tak, že se cyklicky projdou všechny záznamy a z nich se vypoèítá rozdíl podle MAC.
		 * Pokud se rozdíl nenajde, poèítá se od nuly.
		 * 
		 * Poèítá se tak, že se postupnì pøièítají rozdíly^2 k sobì, nakonec se odmocní.
		 * 
		 * NOTE: Zatím je k=1. Pro ovìøení správné funkènosti....
		 * 
		 */
		
		float distance = 0;
		
		for (ArrayList<Scan> listOfScans : navigationData.values()) // prochazime fond skenu
		{
			for (ScanResult s : scanResults) // prochazime jednotlive aktualne naskenovane site
			{
				for (Scan scan : listOfScans) // spolu se skeny mist
				{
					if (s.BSSID.equals(scan.getMAC())) // pokud mame shodu v MAC, odectete se od sebe a mocni se
					{
						distance += Math.pow((s.level - scan.getStrenght()),2);
					}
					else // pokud ne, vezmu nulu 
					{
						distance += Math.pow((0 - scan.getStrenght()),2);
					}
				}
			}
			computedData.put((float) Math.sqrt(distance), String.valueOf(listOfScans.get(1).getX()) + String.valueOf(listOfScans.get(1).getY()));
		}
		
		ArrayList<Float> sortedKeys = new ArrayList<Float>(computedData.keySet());
		Collections.sort(sortedKeys);
		
		int x = navigationData.get(computedData.get(sortedKeys.get(0))).get(0).getX();
		int y = navigationData.get(computedData.get(sortedKeys.get(0))).get(0).getY();
		
		//Toast.makeText(c, "Position is: " + String.valueOf(x) + " " + String.valueOf(y), Toast.LENGTH_SHORT).show();
		
		//view.loadUrl("javascript:alert(" + " \" " + String.valueOf(x) + " " + String.valueOf(y)  + " \" " + " )");
		view.loadUrl("javascript:  var svgns = \"http://www.w3.org/2000/svg\"; " +
				" 						function makeShape(x,y)	{" +
				"							var svg = document.getElementsByTagName(\"svg\")[0];" +
				"							var shape = document.createElementNS(svgns, \"circle\");" +
				"							shape.setAttributeNS(svgns, \"cx\", x);	" +
				"							shape.setAttributeNS(svgns, \"cy\", y);	" +
				"							shape.setAttributeNS(svgns, \"r\", 25);	" +
				"							shape.setAttributeNS(svgns, \"fill\", \"green\");	" +
				"							" +
				"							svg.appendChild(shape); }" +
				"" +
				"	makeShape(25,25);						" );
		
		
	}
}
