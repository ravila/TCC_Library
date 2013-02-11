package usp.ime.gclib.net.communication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Used as a auxiliary class to get information about IP address.
 * 
 * @author Renato Avila e Tonny Cordeiro
 * @version 1.0
 *
 */
public class IP {

	/**
	 * Get my the current IP address
	 * 
	 * @return my IP address, or null whether no network is available.
	 */
	public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { 
                    	return inetAddress.getHostAddress(); 
                    }
                }
            }
        } catch (SocketException ex) {
        	ex.printStackTrace();
        }
   
        return null;
    }

	/**
	 * Get broadcast address of the network.
	 * 
	 * @param ipBase used to obtain broadcast address.
	 * @return the broadcast address of the network.
	 */
	public static String getBroadcastAddress(String ipBase) {
		String[] s = ipBase.split("\\.");
		if(s.length != 4)
			return "255.255.255.255";
		return s[0] + "." + s[1] + "." + s[2] + ".255";
	}

	/**
	 * Get gateway address of the network
	 * 
	 * @param ipBase used to obtain gateway address
	 * @return gateway address of the network.
	 */
	public static String getGatewayAddress(String ipBase) {
		String[] s = ipBase.split("\\.");
		if(s.length != 4)
			return "192.168.43.1";
		return s[0] + "." + s[1] + "." + s[2] + ".1";
	}

	//TODO(Tonny): (baixo n�vel de import�ncia) setar a m�scara apenas se conseguir setar o ip do dispositivo
}