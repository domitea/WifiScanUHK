/**
 * 
 */
package com.example.wifiscanuhk;

/**
 * @author Dominik
 * Trida repzentujici jednotlivy sken na urcitem miste s urcitou wifinou --> 
 * --> jeden sken - jedna Wifina - jedno misto
 */
public class Scan {

	private int x;
	private int y;
	private String MAC;
	private int strenght;
	
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public String getMAC() {
		return MAC;
	}
	public void setMAC(String mAC) {
		MAC = mAC;
	}
	public int getStrenght() {
		return strenght;
	}
	public void setStrenght(int strenght) {
		this.strenght = strenght;
	}
}
