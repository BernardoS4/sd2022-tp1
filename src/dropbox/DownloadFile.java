package dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import dropbox.msgs.DownloadFileArgs;

public class DownloadFile {

	
	private static final String apiKey = "nyekq63jvq28jsq";
	private static final String apiSecret = "cbsndqm28jogmbp";
	private static final String accessTokenStr = "sl.BILQjYgY5W6XPDMpeDOduQgyJIeQ2UlIyM4aZKDDVFLJVUeXIw9AUg7AqsOlcBCmPmiZPJ3C49qeHSA7Dmm0ygw3F8vmTmkzcU54tKoqpw_Vb3dsLf3H7nqohDzm8Bhy0hWUfRgHYXOw";
	
	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	
	private static final int HTTP_SUCCESS = 200;
	private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg";
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/octet-stream";
	
	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;
		
	public DownloadFile() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}
	
	public byte[] execute( String filePath ) throws Exception {
		
		var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
		downloadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		downloadFile.addHeader(DROPBOX_API_ARG_HDR, json.toJson(new DownloadFileArgs(filePath)));

		service.signRequest(accessToken, downloadFile);
		
		Response r = service.execute(downloadFile);
		if (r.getCode() != HTTP_SUCCESS) 
			throw new RuntimeException(String.format("Failed to download file Status: %d, \nReason: %s\n",  r.getCode(), r.getBody()));
		
		return r.getStream().readAllBytes();
	}
}
