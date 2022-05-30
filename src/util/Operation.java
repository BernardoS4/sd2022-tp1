package util;

public class Operation {

	private static final String DELIMITER = "\t";
	public final String id;
	public final Operation type;
	private final String jsonArgs;
		
	public Operation(Operation type, Object args) {
		this.type = type;
		this.id = Random.key128();
		this.jsonArgs = JSON.encode( args);
	}

	public Operation( String encoding ) {
		String[] tokens = encoding.split(DELIMITER);
		this.id = tokens[0];
		this.type = Operation.valueOf(tokens[1]);
		this.jsonArgs = tokens[2];
	}

	public String encode() {
		return new StringBuilder(id)
				.append(DELIMITER)
				.append(type.name())
				.append(DELIMITER)
				.append(jsonArgs).toString();
	}

	public <T> T arg(Class<T> classOf) {
		return JSON.decode( jsonArgs, classOf);
	}
	
	public <T> T args(Class<T> classOf) {
		return JSON.decode( jsonArgs, classOf);
	}
}
