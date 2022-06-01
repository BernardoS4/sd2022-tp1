package token;

public class GenerateToken {
	
	//set to 30min
	private static final long EXPIRE_TIME = 1000*60*30;
	private long from;
	private long to;
	private String fileId;
	private String hash;
	
	
	public GenerateToken() {
		//TokenSecret.get() -> mysecret
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
}
