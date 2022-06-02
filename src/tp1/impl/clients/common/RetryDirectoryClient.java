package tp1.impl.clients.common;

import java.util.List;

import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import token.GenerateToken;

public class RetryDirectoryClient extends RetryClient implements Directory {

	final Directory impl;

	public RetryDirectoryClient( Directory impl ) {
		this.impl = impl;	
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password, Long version) {
		return super.reTry( ()-> impl.writeFile(filename, data, userId, password, version));
	}
	
	@Override
	public Result<Void> writeFile(String userId, String fileId, ExtendedFileInfo file, Long version) {
		return super.reTry( ()-> impl.writeFile(userId, fileId, file, version));
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password, Long version) {
		return super.reTry( ()-> impl.deleteFile(filename, userId, password, version));
		
	}
	
	@Override
	public Result<Void> deleteFile(String filename, String userId, Long version) {
		return super.reTry( ()-> impl.deleteFile(filename, userId, version));
		
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password, Long version) {
		return super.reTry( ()-> impl.shareFile(filename, userId, userIdShare, password, version));
	}
	
	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, Long version) {
		return super.reTry( ()-> impl.shareFile(filename, userId, userIdShare, version));
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password, Long version) {
		return super.reTry( ()-> impl.unshareFile(filename, userId, userIdShare, password, version));
	}
	
	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, Long version) {
		return super.reTry( ()-> impl.unshareFile(filename, userId, userIdShare, userIdShare, version));
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password, Long version) {
		return super.reTry( ()-> impl.getFile(filename, userId, accUserId, password, version));
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password, Long version) {
		return super.reTry( ()-> impl.lsFile(userId, password, version));
	}
	

	@Override
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
		return super.reTry( ()-> impl.deleteUserFiles(userId, password, token));
	}
	
}
