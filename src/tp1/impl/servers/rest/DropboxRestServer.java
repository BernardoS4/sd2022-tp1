package tp1.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tp1.api.service.java.Files;
import tp1.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import util.FlagState;

public class DropboxRestServer extends AbstractRestServer {

public static final int PORT = 5678;
	
	private static Logger Log = Logger.getLogger(DropboxRestServer.class.getName());
	private String apiKey;
	private String apiSecret;

	
	DropboxRestServer(String apiKey, String apiSecret) {
		super(Log, Files.SERVICE_NAME, PORT);
		this.apiKey = apiKey;
		this.apiSecret = apiSecret;
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( new DropboxFilesResources(apiKey, apiSecret) ); 
		config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
	}
	
	public static void main(String[] args) {

		Debug.setLogLevel( Level.INFO, Debug.TP1);
		
		FlagState.set(Boolean.parseBoolean(args[0]));
		Log.info("Flag: " + args[0] + " apiKey " + args[1] + " apiSecret " + args[2] + " accessTokenStr " + args[3]);

		new DropboxRestServer(args[1], args[2]).start();
	}	
}
