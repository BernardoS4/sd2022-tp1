package token;


public class GenerateToken {

	// set to 10sec
	private static final long EXPIRE_TIME = 1000 * 10;


	public static String buildToken(String fileId) {
		Long to = System.currentTimeMillis() + EXPIRE_TIME;
		String hash = Hash.of(fileId, to, TokenSecret.get());
		return fileId.concat("-").concat(to.toString()).concat("-").concat(hash);
	}

	private static boolean checkConfidentiality(String hash, String fileId) {

		String newToken = buildToken(fileId);
		return hash.equalsIgnoreCase(splitToken(newToken)[2]);
	}
	
	public static boolean isTokenValid(String token, String fileId) {
		
		Long expireData = Long.parseLong(splitToken(token)[1]);
		String hash = splitToken(token)[2];
		System.out.println("sadfffffffffffffffffffffffffffff " + expireData);
		System.out.println("asfffffffffffffffffffffffffffffffffffffffffffffffffffffff " +hash);
		
		return expireData >= System.currentTimeMillis() && checkConfidentiality(hash, fileId);
	}
	
	private static String[] splitToken(String token) {
		
		return token.split("-");
	}
}
