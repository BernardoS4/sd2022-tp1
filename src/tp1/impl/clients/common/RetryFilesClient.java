package tp1.impl.clients.common;

import tp1.api.service.java.Files;
import tp1.api.service.java.Result;
import util.GenerateToken;

public class RetryFilesClient extends RetryClient implements Files {

	final Files impl;

	public RetryFilesClient( Files impl ) {
		this.impl = impl;	
	}

	@Override
	public Result<byte[]> getFile(String fileId, GenerateToken token) {
		return reTry( () -> impl.getFile(fileId, token));
	}

	@Override
	public Result<Void> deleteFile(String fileId, GenerateToken token) {
		return reTry( () -> impl.deleteFile(fileId, token));
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, GenerateToken token) {
		// We do not retry this operation more than once, here...
		// In case of timeout, directory needs to try another server instead.
		return reTry( () -> impl.writeFile(fileId, data, token), 1);
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, GenerateToken token) {
		return reTry( () -> impl.deleteUserFiles(userId, token));
	}	
}
