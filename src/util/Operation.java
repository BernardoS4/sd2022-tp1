package util;

import java.util.Map;


public class Operation {

	public static final String FILE = "file";
	public static final String USERID = "userId";
	public static final String FILENAME = "filename";
	public static final String USERID_SHARE = "userIdShare";
	
	private OperationType type;
	private Map<String, Object> opParams;
/**
 * @param type
 * @param opParams
 * 
 * <"filename", "yaya.txt">
	<"data", [0x21, 0x32, 0x54, ..... ]>
	<"userId", "amd">
	<"password", "123">
 */
	
	
	public Operation(OperationType type, Map<String, Object> opParams) {
		
		this.type = type;
		this.opParams = opParams;
	}
	
	
	public OperationType getType() {
		return type;
	}

	public Map<String, Object> getOpParams() {
		return opParams;
	}
	
	public void execute(OperationType operationType) {
		switch (operationType) {	
			case WRITE_FILE:
				break;
			case DELETE_FILE:
				break;
			case SHARE_FILE:
				break;
			case UNSHARE_FILE:
				break;
			case LIST_FILES:
				break;
			case DELETE_USER_FILES:
				break;
		}
	}
}
