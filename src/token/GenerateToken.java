package token;

public class GenerateToken {

	// set to 10sec
	private static final long EXPIRE_TIME = 1000 * 10;

	public static String buildToken(String fileId) {
		Long to = System.currentTimeMillis() + EXPIRE_TIME;
		String hash = Hash.of(fileId, to, TokenSecret.get());
		return fileId.concat("-").concat(to.toString()).concat("-").concat(hash);
	}
	
	public static boolean isTokenValid(String token, String fileId) {
		String[] content = splitToken(token);
		Long expireData = Long.parseLong(content[1]);
		String hash = content[2];
		return expireData >= System.currentTimeMillis() && checkConfidentiality(hash, expireData, fileId);
	}

	private static boolean checkConfidentiality(String hash, Long expireData, String fileId) {
		String newHash = Hash.of(fileId, expireData, TokenSecret.get());
		return newHash.equals(hash);
	}

	private static String[] splitToken(String token) {
		return token.split("-");
	}
}
