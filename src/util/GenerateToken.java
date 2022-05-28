package util;

import java.text.SimpleDateFormat;

public class GenerateToken {
	
	//set to 30min
	private static final int EXPIRE_TIME = 3600*500;
	
	public GenerateToken(String fileId) {
		
		//mysecret
		TokenSecret.get();
		String ts = new SimpleDateFormat("2022.05..HH.mm.ss").format(new java.util.Date());
		fileId.concat(EXPIRE_TIME);
	}
}
