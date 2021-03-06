package usp.ime.gclib.net.wifi;

/**
 * This class contains only the basic configuration of a network, name (ssid) and password.
 * It's concatenate a prefix in ssid, "AND_GCLIB_", for internal use. 
 * 
 * @author Renato Avila e Tonny Cordeiro
 * @version 1.0
 *
 */
public class NetworkConfiguration {

	private static final String SSID_PREFIX = "AND_GCLIB_";
	private String sSID;
	private String password;
	private boolean isOpen;
	
	
	public NetworkConfiguration(){
		this.sSID = null;
		this.password = null;
		this.isOpen = false;		
	}
	
	public String getSSID() {
		return this.sSID;
	}

	public void setSSID(String sSID) {
		this.sSID = sSID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isOpen() {
		return this.isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}

	protected static String removePrefix(String ssid){
		if(ssid == null)
			return null;
		int mid = SSID_PREFIX.length();
		String s = ssid.substring(mid);
		return s;
	}
	protected static String getSsidPrefix() {
		return SSID_PREFIX;
	}
	
}
