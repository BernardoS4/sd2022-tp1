package token;

import java.util.UUID;

public class GenerateToken {

	// set to 10sec
	private static final long EXPIRE_TIME = 1000 * 10;
	private Long from;
	private Long to;
	private String fileId;
	private String hash;
	private String tokenId;

	public GenerateToken() {
		// TokenSecret.get() -> mysecret
		from = System.currentTimeMillis();
		to = from + EXPIRE_TIME;
	}

	public void hashToken() {
		hash = Hash.of(fileId, to, TokenSecret.get());
	}

	public String getFileId() {
		return fileId;
	}

	public void setTokenFileId(String fileId) {

		this.fileId = fileId;
	}

	public void setTokenId() {

		this.tokenId = UUID.randomUUID().toString();
	}
	
	public String getTokenId() {
		return tokenId;
	}

	public String getHash() {
		return hash;
	}

	public Long getTo() {
		return to;
	}

	public String buildToken(String fileId) {
		this.fileId = fileId;
		hash = Hash.of(fileId, to, TokenSecret.get());
		return this.fileId.concat("-").concat(to.toString()).concat("-").concat(hash);
	}
	
	//na chamada do metodo
	//parametro e o (String) gt.getTo()
	private boolean isTokenExpired(String timeStamp) {
	
		//return timeStamp <= System.currentTimeMillis();
		Long now = System.currentTimeMillis();
		return timeStamp.compareTo(now.toString()) < 0;
	}

	//na chamada do metodo
	//1º parametro e ->
	private boolean checkConfidentiality(String hash, String fileId) {

		GenerateToken newToken = new GenerateToken();
		newToken.buildToken(fileId);
		return hash.equalsIgnoreCase(newToken.getHash());
	}
	
	public boolean isTokenValid(String token, String fileId) {
		
		String expireData = splitToken(token)[1];
		String hash = splitToken(token)[2];
		return !isTokenExpired(expireData) && checkConfidentiality(hash, fileId);
	}
	
	private String[] splitToken(String token) {
		
		return token.split("-");
	}
}
