package dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import dropbox.msgs.CreateFileArgs;


public class CreateFileRequest {

	private static final String apiKey = "nyekq63jvq28jsq";
	private static final String apiSecret = "cbsndqm28jogmbp";
	private static final String accessTokenStr = "sl.BH_r9l3FdmYq5J5GPRNkRxQ6jLCc8VwjtKcdi5F7yX6vpdB7fpgDQE4rlKcxmmObB38rKE4W9ncltDGgL4kiXQSG0or9e3YiBPXevzjJsEEPlU880C1NrwanKjteYWxPRTdWPhBXcswU";

	private static final String CREATE_FILE_URL = "https://api.dropboxapi.com/2/file_requests/create";
	
	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public CreateFileRequest() {

		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}

	public void execute(String fileName, String destination) throws Exception {

		var createFile = new OAuthRequest(Verb.POST, CREATE_FILE_URL);
		createFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		createFile.setPayload(json.toJson(new CreateFileArgs(fileName, true, destination)));

		service.signRequest(accessToken, createFile);

		Response r = service.execute(createFile);
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(String.format("Failed to create file: %s, %s, Status: %d, \nReason: %s\n",
					fileName, destination, r.getCode(), r.getBody()));
	}
}
