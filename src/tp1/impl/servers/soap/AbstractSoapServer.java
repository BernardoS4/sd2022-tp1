package tp1.impl.servers.soap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import jakarta.xml.ws.Endpoint;

import tp1.impl.discovery.Discovery;
import tp1.impl.servers.common.AbstractServer;
import util.IP;

public class AbstractSoapServer extends AbstractServer{
	private static String SERVER_BASE_URI = "http://%s:%s/soap";
	//private static String SERVER_BASE_URI = "https://%s:%s/soap";

	final Object implementor;
	
	protected AbstractSoapServer( boolean enableSoapDebug, Logger log, String service, int port, Object implementor) {
		super( log, service, port);
		this.implementor = implementor;
		
		if(enableSoapDebug ) {
			System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
			System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
			System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
		}
	}
	
	protected void start() {
		//TROCAR PARA TLS
		/*var ip = IP.hostAddress();
		var serverURI = String.format(SERVER_BASE_URI, ip, port);
		
		try {
			var server = HttpsServer.create(new InetSocketAddress(ip, port), 0);
			server.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
	
			var endpoint = Endpoint.create(implementor);      
			endpoint.publish(server.createContext("/soap"));
	
			server.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
		Discovery.getInstance().announce(service, serverURI);

		Log.info(String.format("%s Soap Server ready @ %s\n", service, serverURI));
		*/
		
		
		var ip = IP.hostAddress();
		var serverURI = String.format(SERVER_BASE_URI, ip, port);

		Endpoint.publish(serverURI.replace(ip, INETADDR_ANY), implementor );

		Discovery.getInstance().announce(service, serverURI);

		Log.info(String.format("%s Soap Server ready @ %s\n", service, serverURI));
	}
}
