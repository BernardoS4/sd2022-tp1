package dropbox;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import dropbox.msgs.UpdateFileArgs;

public class UpdateFile {

	private static final String apiKey = "nyekq63jvq28jsq";
	private static final String apiSecret = "cbsndqm28jogmbp";
	private static final String accessTokenStr = "sl.BHsHk601YHsUps4uD2gPNQqI44OI40Sr2rMZXkO9swGHgB1dP92ku7wNP4i0fRqHdNt0JkroPtkxVoeiBPMh0ot7LoN9_FFG_CpKP2Dnw3gZKyUdRyhMUvme2o_5YlEUuLSHCJQJWETT";

	private static final String UPDATE_FILE_URL = "https://api.dropboxapi.com/2/file_requests/update";
	
	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

	public UpdateFile() {

		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
	}

	public void execute(String newFileName, String id, String destination) throws Exception {

		var updateFile = new OAuthRequest(Verb.POST, UPDATE_FILE_URL);
		updateFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		updateFile.setPayload(json.toJson(new UpdateFileArgs(newFileName, true, id, destination)));

		service.signRequest(accessToken, updateFile);

		Response r = service.execute(updateFile);
		if (r.getCode() != HTTP_SUCCESS)
			throw new RuntimeException(String.format("Failed to update file: %s, %s, %s, Status: %d, \nReason: %s\n",
					newFileName, id, destination, r.getCode(), r.getBody()));
	}
}
