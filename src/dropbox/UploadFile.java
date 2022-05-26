package dropbox;


import org.pac4j.scribe.builder.api.DropboxApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import dropbox.msgs.UploadFileArgs;
import util.DropboxArguments;

public class UploadFile {

	//private static final String apiKey = "nyekq63jvq28jsq";
	//private static final String apiSecret = "cbsndqm28jogmbp";
	//private static final String accessTokenStr = "sl.BIUwns933jo6Urn9ISbENp_2qgCYta9CRWbXH0q4dTFFXwjI8cdfVcEX5BJlMQ5JdZI3_Ps89DdPmXO3sPbr-4k4JJMQ9ucQoFvu_Z0TMfj9rErKi-MEKG4VbKY5wYJs3Sif1jtGJ8jA";

	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";

	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String DROPBOX_API_ARG_HDR = "Dropbox-API-Arg";
	private static final String JSON_CONTENT_TYPE = "application/octet-stream";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public UploadFile() {
		json = new Gson();
		accessToken = new OAuth2AccessToken(DropboxArguments.getAccessTokenStr());
		service = new ServiceBuilder(DropboxArguments.getApiKey()).apiSecret(DropboxArguments.getApiSecret()).build(DropboxApi20.INSTANCE);
	}

	public void execute(String path, byte[] data) throws Exception {

		var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_URL);
		uploadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
		uploadFile.addHeader(DROPBOX_API_ARG_HDR, json.toJson(new UploadFileArgs(false, "overwrite", false, path, false)));

		uploadFile.setPayload(data);

		service.signRequest(accessToken, uploadFile);

		Response r = service.execute(uploadFile);
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(
					String.format("Failed to create directory Status: %d, \nReason: %s\n", r.getCode(), r.getBody()));
	}
}
