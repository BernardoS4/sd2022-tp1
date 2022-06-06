package tp1.impl.clients.common;

import java.util.List;

import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;

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
	public Result<Void> writeFileSec(String userId, String fileId, ExtendedFileInfo file) {
		return super.reTry( ()-> impl.writeFileSec(userId, fileId, file));
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password, Long version) {
		return super.reTry( ()-> impl.deleteFile(filename, userId, password, version));
		
	}
	
	@Override
	public Result<Void> deleteFileSec(String filename, String userId) {
		return super.reTry( ()-> impl.deleteFileSec(filename, userId));
		
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password, Long version) {
		return super.reTry( ()-> impl.shareFile(filename, userId, userIdShare, password, version));
	}
	
	@Override
	public Result<Void> shareFileSec(String filename, String userId, String userIdShare) {
		return super.reTry( ()-> impl.shareFileSec(filename, userId, userIdShare));
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password, Long version) {
		return super.reTry( ()-> impl.unshareFile(filename, userId, userIdShare, password, version));
	}
	
	@Override
	public Result<Void> unshareFileSec(String filename, String userId, String userIdShare) {
		return super.reTry( ()-> impl.unshareFileSec(filename, userId, userIdShare));
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
