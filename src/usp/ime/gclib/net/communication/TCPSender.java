package usp.ime.gclib.net.communication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import usp.ime.gclib.net.protocol.AppProtocol;
import usp.ime.gclib.net.protocol.ESendTo;
import usp.ime.gclib.net.protocol.ProtocolInformation;

/**
 * This class is for internal use, it must not called.
 * It must use {@link CommunicationSocket} for send messages.
 * 
 * @author Renato Avila e Tonny Cordeiro
 * @version 1.0
 *
 */
public class TCPSender {
	
	protected TCPSender() {
		
	}

	protected int send(AppProtocol protocol, ProtocolInformation appInfo) {
		int sendReturn = 0;
		
		if(protocol.getSendTo() == ESendTo.ALL)
			return 2;
		
		if(protocol.getSendTo() == ESendTo.GATEWAY)
			sendReturn = send(appInfo, IP.getGatewayAddress(appInfo.getDeviceSrc().getIp()));
		else {
			for (String ip : protocol.getListIpDst()) {
				sendReturn += send(appInfo, ip);
			}
			if(sendReturn > 1)
				sendReturn = 1;
		}
		
		return sendReturn;
	}
	
	private int send(ProtocolInformation appInfo, String ipDst) {
		try {
			InetAddress address = InetAddress.getByName(ipDst);
			
			Socket socket = new Socket(address, TCPReceiver.SERVERPORT);
			ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
			
			outToServer.writeObject(appInfo);
			
			socket.close();
			outToServer.flush();
			outToServer.close();
		} 
		catch (UnknownHostException e) {
			e.printStackTrace();
			return 2;
		} 
		catch(IOException e) {
			e.printStackTrace();
			return 1;
		}
		
		return 0;
	}
}