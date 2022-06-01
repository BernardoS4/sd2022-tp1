package token;

import java.util.UUID;

public class GenerateToken {

	// set to 10sec
	private static final long EXPIRE_TIME = 1000 * 10;
	private long from;
	private long to;
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

	public long getTo() {
		return to;
	}

	public void buildToken(String fileId) {
		this.fileId = fileId;
		hash = Hash.of(fileId, to, TokenSecret.get());
	}
	
	//na chamada do metodo
	//parametro e o (String) gt.getTo()
	public boolean isTokenExpired(long timeStamp) {
	
		return timeStamp <= System.currentTimeMillis();
	}

	//na chamada do metodo
	//1º parametro e -> new String(...)
	/*private boolean checkConfidentiality(String gt, String fileId) {

		String newToken = new String();
		newToken.buildToken(fileId);
		return gt.getHash().equalsIgnoreCase(newToken.getHash());
	}
	
	private boolean isTokenValid(long timeStamp, String gt, String fileId) {
		
		return isTokenExpired(timeStamp) && checkConfidentiality(gt, fileId);
	}*/
}
