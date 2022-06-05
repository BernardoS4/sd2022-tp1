package tp1.impl.servers.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

import tp1.api.service.java.Directory;
import tp1.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import token.TokenSecret;

public class DirectoryRepRestServer extends AbstractRestServer {

	public static final int PORT = 4567;

	private static Logger Log = Logger.getLogger(DirectoryRepRestServer.class.getName());

	DirectoryRepRestServer() {
		super(Log, Directory.SERVICE_NAME, PORT);
	}

	@Override
	void registerResources(ResourceConfig config) {
		config.register(new DirectoryRepResources());
		config.register(GenericExceptionMapper.class);
//		config.register( CustomLoggingFilter.class);
	}

	public static void main(String[] args) throws Exception {

		Debug.setLogLevel(Level.INFO, Debug.TP1);
		TokenSecret.set(args.length > 0 ? args[0] : "");
		new DirectoryRepRestServer().start();
	}
}