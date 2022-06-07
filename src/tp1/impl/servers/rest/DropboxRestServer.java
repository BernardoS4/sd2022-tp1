package tp1.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import dropbox.DropboxArguments;
import tp1.api.service.java.Files;
import tp1.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import util.FlagState;

public class DropboxRestServer extends AbstractRestServer {

public static final int PORT = 5678;
	
	private static Logger Log = Logger.getLogger(DropboxRestServer.class.getName());
	
	DropboxRestServer() {
		super(Log, Files.SERVICE_NAME, PORT);
	}
	
	@Override
	void registerResources(ResourceConfig config) {
		config.register( DropboxFilesResources.class ); 
		config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
	}
	
	public static void main(String[] args) {

		Debug.setLogLevel( Level.INFO, Debug.TP1);
		
		FlagState.set(Boolean.parseBoolean(args[0]));
		DropboxArguments.setApiKey(args[1]);
		DropboxArguments.setApiSecret(args[2]);
		DropboxArguments.setAccessTokenStr(args[3]);

		new DropboxRestServer().start();
	}	
}
