package dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import dropbox.msgs.DeleteDirectoryV2Args;
import dropbox.msgs.DownloadFileArgs;
import dropbox.msgs.UploadFileArgs;

public class DropboxOperations {

	private static final String DELETE_DIR_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	
	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE_JSON = "application/json; charset=utf-8";
	private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg";
	private static final String JSON_CONTENT_TYPE_OCTET = "application/octet-stream";
	
	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;
	
	
	public DropboxOperations() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(DropboxArguments.getAccessTokenStr());
		service = new ServiceBuilder(DropboxArguments.getApiKey()).apiSecret(DropboxArguments.getApiSecret()).build(DropboxApi20.INSTANCE);
	}
	
	public void deleteDirectory( String directoryName ) throws Exception {
		
		var deleteDir = new OAuthRequest(Verb.POST, DELETE_DIR_V2_URL);
		deleteDir.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE_JSON);

		deleteDir.setPayload(json.toJson(new DeleteDirectoryV2Args(directoryName)));

		service.signRequest(accessToken, deleteDir);
		
		Response r = service.execute(deleteDir);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to delete directory/file: %s, Status: %d, \nReason: %s\n", directoryName, r.getCode(), r.getBody()));
		
	}
	
	public byte[] downloadFile( String filePath ) throws Exception {
		
		var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
		downloadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE_OCTET);
		downloadFile.addHeader(DROPBOX_API_ARG_HDR, json.toJson(new DownloadFileArgs(filePath)));

		service.signRequest(accessToken, downloadFile);
		
		Response r = service.execute(downloadFile);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to download file Status: %d, \nReason: %s\n",  r.getCode(), r.getBody()));
		
		return r.getStream().readAllBytes();
	}
	
	public void uploadFile(String path, byte[] data) throws Exception {

		var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_URL);
		uploadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE_OCTET);
		uploadFile.addHeader(DROPBOX_API_ARG_HDR, json.toJson(new UploadFileArgs(false, "overwrite", false, path, false)));

		uploadFile.setPayload(data);

		service.signRequest(accessToken, uploadFile);

		Response r = service.execute(uploadFile);
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(
					String.format("Failed to create directory Status: %d, \nReason: %s\n", r.getCode(), r.getBody()));
	}
}
