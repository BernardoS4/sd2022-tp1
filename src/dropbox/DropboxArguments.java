package dropbox;

public class DropboxArguments {

	private static String apiKey;
	private static String apiSecret;
	private static String accessTokenStr;
	
	private DropboxArguments() {}

	
	public static void setApiKey(String apiKey) {
		DropboxArguments.apiKey = apiKey;
	}


	public static void setApiSecret(String apiSecret) {
		DropboxArguments.apiSecret = apiSecret;
	}


	public static void setAccessTokenStr(String accessTokenStr) {
		DropboxArguments.accessTokenStr = accessTokenStr;
	}


	public static String getApiKey() {
		return apiKey;
	}

	public static String getApiSecret() {
		return apiSecret;
	}

	public static String getAccessTokenStr() {
		return accessTokenStr;
	}
}
