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
	
	public void execute() {
		// TODO Auto-generated method stub
		
	}
}
