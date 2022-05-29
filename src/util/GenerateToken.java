package util;

public class GenerateToken {
	
	//set to 30min
	private static final long EXPIRE_TIME = 1000*60*30;
	private long from;
	private long to;
	private String fileId;
	private int hash;
	

	public GenerateToken(String fileId) {
		
		this.fileId = fileId;
		//TokenSecret.get() -> mysecret
		from = System.currentTimeMillis(); 
		to = from + EXPIRE_TIME;
		//nao vai ser este hash mas e so para a logica
		hash = fileId.concat(String.valueOf(to)).concat(TokenSecret.get()).hashCode();
	}
	
	public GenerateToken() {
		//TokenSecret.get() -> mysecret
		from = System.currentTimeMillis(); 
		to = from + EXPIRE_TIME;
		//nao vai ser este hash mas e so para a logica
		hash = fileId.concat(String.valueOf(to)).concat(TokenSecret.get()).hashCode();
	}
	
	public String getFileId() {
		return fileId;
	}
	
	public void setTokenFileId(String fileId) {
		
		this.fileId = fileId;
	}

	public int getHash() {
		return hash;
	}
	
	public long getTo() {
		return to;
	}
}
