package usp.ime.gclib.net.wifi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * This class is the main class in this package. From this, it is possible
 * connect in a network available, create a new access point, get information
 * about all networks and more.
 * 
 * @author Renato Avila e Tonny Cordeiro
 * @version 1.0
 *
 */
public class NetworkManager {

	private final WifiManager wifiManager;
	private WifiConfiguration wifiConfig;
	private boolean wasStartedNetwork;
	private Context context;
	private int networkId;
	
	public NetworkManager(Context context) {
		this.context = context;
		wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
		wasStartedNetwork = false;
	}
	
	/**
	 * This method returns a wifi access point state.
	 * 
	 * @return @see {@link NetworkState}
	 */
	public NetworkState getNetworkState(){
		try{
			Method method = wifiManager.getClass().getMethod("getWifiApState");
	        int tmp = ((Integer) method.invoke(wifiManager)) ;
	        if (tmp > 10)
	            tmp = tmp - 10; // Fix for Android 4
	        
	        return NetworkState.class.getEnumConstants()[tmp];
	    }
		catch (Exception e) {
	        return NetworkState.FAILED;
	    }
	}
	
	
	/**
	 * 
	 */
	public void searchNetworks() {
		wifiManager.startScan();
	}
	

	/**
	 * This method returns a list of all Networks available that was create
	 * by another device running the same application.
	 * 
	 * @return A list of {@link NetworkAvailable}. Return null if the wifi is disable.
	 * If the return was empty is because no networks are available.
	 */
	public List<NetworkAvailable> getAllNetworksAvailable(){
		if(!wifiManager.isWifiEnabled())
			return null;
		List<ScanResult> scans = wifiManager.getScanResults();
		List<NetworkAvailable> nets = new ArrayList<NetworkAvailable>();
		
		for (ScanResult scan : scans) {
			String ssid = scan.SSID;
			if(ssid.startsWith(NetworkConfiguration.getSsidPrefix())){
				boolean isopen;
				if(scan.capabilities.contains("WPA"))
					isopen = false;
				else
					isopen = true;
				NetworkAvailable net = new NetworkAvailable(NetworkConfiguration.removePrefix(ssid), isopen);
				nets.add(net);
			}
		}
		return nets;
	}
	
	public boolean isConnect() {
		ConnectivityManager connManager =  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
		
		return (netInfo != null && netInfo.isConnected());
	}
	
	/**
	 * Try connect to a network. 
	 * 
	 * @param net
	 * @param password
	 * @return
	 */
	public int connectNetwork(NetworkAvailable net, String password){
		if(net == null)
			return 1;
		if(!net.isOpen() && password == null)
			return 2;
		
		NetworkConfiguration netConfig = new NetworkConfiguration();
		netConfig.setOpen(net.isOpen());
		netConfig.setSSID("\"" + NetworkConfiguration.getSsidPrefix() + net.getSSID() + "\"");
		netConfig.setPassword("\"" + password + "\"");
		
		parseConfiguration(netConfig);
		
		networkId = wifiManager.addNetwork(wifiConfig);
		if(networkId < 0)
			return 3;
		boolean ret = wifiManager.enableNetwork(networkId, true);
		
		if(ret)
			return 0;
		else
			return 4;
	}
	
	/**
	 * Remove network that was connect before
	 * 
	 * @return true if successful operation, false otherwise. 
	 */
	public boolean removeNetwork() {
		if(networkId > 0) {
			return wifiManager.removeNetwork(networkId);
		}
		return false;
	}
	
	/**
	 * Try start a new Access Point. It will turn off wifi.
	 * 
	 * @param netConfig @see {@link NetworkConfiguration}
	 * 
	 * @return These are the return codes:
	 * <ul>
	 * 	<li>0: The network was started successfully.</li>
	 * 	<li>1: A exception occurred. Verify if your device can start a wifi Access Point.</li>
	 * 	<li>2: The network was started. Verify the {@see getNetworkState()} before calling this method.</li>
	 * 	<li>3: netConfig is null or has null parameters.
	 * </ul>
	 */
	public int startNetwork(NetworkConfiguration netConfig){
		
		if(wasStartedNetwork)
			return 2;
		if(netConfig == null || netConfig.getSSID() == null || (!netConfig.isOpen() && netConfig.getPassword() == null))
			return 3;
		netConfig.setSSID(NetworkConfiguration.getSsidPrefix() + netConfig.getSSID());
		
		if(wifiManager.isWifiEnabled())
			wifiManager.setWifiEnabled(false);
		
		int ret = parseConfiguration(netConfig);
		if(ret > 0)
			return ret;
		
		boolean res = setWifiApEnabled(wifiConfig, true);
		if(!res)
			return 1;
		
		wasStartedNetwork = true;
		return 0;
	}
	
	/**
	 * 
	 * @return These are the return codes:
	 * <ul>
	 * 	<li>0: Successful</li>
	 * 	<li>1: Occurred a exception during the stopping.</li>
	 * 	<li>2: Network was not started.</li>
	 * <ul>
	 */
	public int stopNetwork(){
		if(!wasStartedNetwork){
			return 2;
		}
		boolean res = setWifiApEnabled(wifiConfig, false);
		if(!res)
			return 1;
		wifiManager.removeNetwork(wifiConfig.networkId);
		wifiManager.saveConfiguration();
		
		wasStartedNetwork = false;
		return 0;
	}
	
	private int parseConfiguration(NetworkConfiguration netConfig) {
		wifiConfig = new WifiConfiguration();
		
		wifiConfig.SSID = netConfig.getSSID();
		wifiConfig.priority = 40;
		wifiConfig.status = WifiConfiguration.Status.DISABLED;
		if(netConfig.isOpen()){
			wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wifiConfig.allowedAuthAlgorithms.clear();
			wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

		}
		else {
			wifiConfig.preSharedKey = netConfig.getPassword();
	        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
	        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		}
		
		return 0;
	}

	private boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled) {
		try {
			Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
			return (Boolean) method.invoke(wifiManager, wifiConfig, enabled);
		} catch (Exception e) {
			Log.e(this.getClass().toString(), "", e);
			return false;
		}
	}
}
