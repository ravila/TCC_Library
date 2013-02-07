package usp.ime.gclib.net.communication;

import android.util.Log;
import usp.ime.gclib.Device;
import usp.ime.gclib.hit.HitCalculations;
import usp.ime.gclib.hit.ShootingRestrictions;
import usp.ime.gclib.hit.TargetRestrictions;
import usp.ime.gclib.net.protocol.ProtocolGEOACKInformation;
import usp.ime.gclib.net.protocol.ProtocolGEOMSGInformation;
import usp.ime.gclib.net.protocol.ProtocolInformation;
import usp.ime.gclib.net.protocol.ProtocolLIBCONFIGInformation;

public class Receiver{
	protected IReceiveListener listener;
	private TargetRestrictions targetRestrictions;
	private Device receiverDevice;

	protected Receiver(IReceiveListener listener, Device receiverDevice) {
		this.listener = listener;
		this.receiverDevice = receiverDevice;
		targetRestrictions = new TargetRestrictions();
	}
	
	protected Receiver(IReceiveListener listener, Device receiverDevice, TargetRestrictions targetRestrictions) {
		this.listener = listener;
		this.receiverDevice = receiverDevice;
		this.targetRestrictions = targetRestrictions;
	}

	protected Receiver(IReceiveListener listener, TargetRestrictions targetRestrictions, Device receiverDevice) {
		this.listener = listener;
		this.targetRestrictions = targetRestrictions;
		this.receiverDevice = receiverDevice;
	}
	
	protected TargetRestrictions getTargetRestrictions() {
		return targetRestrictions;
	}

	protected void setTargetRestrictions(TargetRestrictions targetRestrictions) {
		this.targetRestrictions = targetRestrictions;
	}	
	
	protected void activateEvents(ProtocolInformation appInfo){
		switch(appInfo.getTypeMsg()){
			case GEOMSG:
				listener.onReceiveGEOMSG((ProtocolGEOMSGInformation)appInfo);
				break;
			case GEOACK:
				listener.onReceiveGEOACK((ProtocolGEOACKInformation)appInfo);
				break;
			case APPDATA:
				listener.onReceiveAPPDATA(appInfo);
				break;
			case ONLINE:
				listener.onReceiveONLINE(appInfo);
				break;
			case ONLINEANSWER:
				listener.onReceiveONLINEANSWER(appInfo);
				break;
			case LIBCONFIG:
				listener.onReceiveLIBCONFIG((ProtocolLIBCONFIGInformation)appInfo);
				break;
			default:
				break;
		}
	}
	
	protected boolean thisMessageIsForMe(ProtocolInformation appInfo) {
		if(receiverDevice.getIp().equals(appInfo.getDeviceSrc().getIp()))
			return false;
		switch(appInfo.getTypeMsg()){
			case GEOMSG:
				Log.d("Receiver", "lat_src:" + appInfo.getDeviceSrc().getDeviceLocation().getLatitude() +
						" long_src:" + appInfo.getDeviceSrc().getDeviceLocation().getLongitude() +
						" azi_src:" + Math.toDegrees(appInfo.getDeviceSrc().getDeviceOrientation().getAzimuth()) +
						" lat_dst:" + receiverDevice.getDeviceLocation().getLatitude() +
						" long_dst:" + receiverDevice.getDeviceLocation().getLongitude());
				ProtocolGEOMSGInformation geoData;
				HitCalculations hitCalculations;
				
				geoData = (ProtocolGEOMSGInformation) appInfo;
				hitCalculations = new HitCalculations();
				
				if(geoData.getShootRestrictions() == null)
					hitCalculations.setShootingRestrictions(new ShootingRestrictions());
				else
					hitCalculations.setShootingRestrictions(geoData.getShootRestrictions());
				hitCalculations.setTargetRestrictions(getTargetRestrictions());
				return hitCalculations.hitTheDestination(Math.toDegrees(appInfo.getDeviceSrc().getDeviceOrientation().getAzimuth()), 
														  appInfo.getDeviceSrc(),
														  receiverDevice);
				
			case GEOACK:
			case APPDATA:
			case ONLINE:
			case ONLINEANSWER:
			case LIBCONFIG:
				return true;
			default:
				break;
		}

		return true;
	}
}
