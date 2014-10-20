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
		 * V�po�et euklidovsk� vzd�lenosti - jak je zn�mo, je to v�po�et al� pythagorova v�ta, jen ve v�ce rozm�rech
		 * �ili to je sqrt(a*a+b*b+c*c+d*d....n*n)
		 * 
		 * Proto�e budu po��tat vzd�lenost a� z rozd�lu, tak to bude: sqrt((a1-a2)^2 + (b1-b2)^2....), 
		 * kde hodnoty s indexem 1 budou aktu�ln� nam��en� hodnoty a hodnoty s indexem 2 budou br�ny z "fondu" sken�
		 * je tak� pot�eba db�t na spr�vn� ode��t�n� hodnot, tzn. od stejn�ch MACovek!
		 * 
		 * Co se t��e v�po�tu, je prov�d�n tak, �e se cyklicky projdou v�echny z�znamy a z nich se vypo��t� rozd�l podle MAC.
		 * Pokud se rozd�l nenajde, po��t� se od nuly.
		 * 
		 * Po��t� se tak, �e se postupn� p�i��taj� rozd�ly^2 k sob�, nakonec se odmocn�.
		 * 
		 * NOTE: Zat�m je k=1. Pro ov��en� spr�vn� funk�nosti....
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
