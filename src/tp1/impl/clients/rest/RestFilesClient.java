package tp1.impl.clients.rest;

import java.net.URI;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.service.java.Files;
import tp1.api.service.java.Result;
import static util.SystemConstants.FILES_PATH;
import static util.SystemConstants.TOKEN;


public class RestFilesClient extends RestClient implements Files {

	private static final String USER = "user";

	public RestFilesClient(URI serverUri) {
		super(serverUri, FILES_PATH);
	}
	
	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		Response r = target.path(fileId)
				.queryParam(TOKEN, token)
				.request()
				.accept( MediaType.APPLICATION_OCTET_STREAM)
				.get();
		return super.toJavaResult(r, new GenericType<byte[]>() {});
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		Response r = target.path(fileId)
				.queryParam(TOKEN, token)
				.request()
				.delete();
		
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		Response r = target.path(fileId)
				.queryParam(TOKEN, token)
				.request()
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
		
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> deleteUserFiles(String userId) {
		Response r = target.path(USER)
				.path(userId)
				.request()
				.delete();
		
		return super.toJavaResult(r);
	}	
}
