package tp1.impl.servers.soap;

import static tp1.impl.clients.Clients.FilesClients;

import java.util.List;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import token.GenerateToken;
import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.api.service.java.Result.ErrorCode;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.impl.servers.common.JavaDirectory;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import tp1.impl.servers.common.JavaRepDirectory;

@WebService(serviceName = SoapDirectory.NAME, targetNamespace = SoapDirectory.NAMESPACE, endpointInterface = SoapDirectory.INTERFACE)
public class SoapDirectoryWebService extends SoapWebService implements SoapDirectory {

	static Logger Log = Logger.getLogger(SoapDirectoryWebService.class.getName());

	final Directory impl;
	private long DEFAULT_VERSION = 0L;

	public SoapDirectoryWebService() {
		impl = new JavaRepDirectory();
	}

	@Override
	public FileInfo writeFile(String filename, byte[] data, String userId, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP writeFile: filename = %s, data.length = %d, userId = %s, password = %s \n",
				filename, data.length, userId, password));

		Result<FileInfo> res = impl.writeFile(filename, data, userId, password, DEFAULT_VERSION);
		if(res.isOK())
			writeFileSec(filename, userId, null);
		return super.resultOrThrow(res, DirectoryException::new);
	}

	@Override
	public void deleteFile(String filename, String userId, String password) throws DirectoryException {
		Log.info(String.format("SOAP deleteFile: filename = %s, userId = %s, password =%s\n", filename, userId,
				password));

		Result<Void> res = impl.deleteFile(filename, userId, password, DEFAULT_VERSION);
		if(res.isOK())
			deleteFileSec(filename, userId);
		super.resultOrThrow(res, DirectoryException::new);
	}

	@Override
	public void shareFile(String filename, String userId, String userIdShare, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP shareFile: filename = %s, userId = %s, userIdShare = %s, password =%s\n", filename,
				userId, userIdShare, password));
		
		Result<Void> res = impl.shareFile(filename, userId, userIdShare, password, DEFAULT_VERSION);
		if(res.isOK())
			shareFileSec(filename, userId, userIdShare);
		super.resultOrThrow(res, DirectoryException::new);
	}

	@Override
	public void unshareFile(String filename, String userId, String userIdShare, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP unshareFile: filename = %s, userId = %s, userIdShare = %s, password =%s\n",
				filename, userId, userIdShare, password));

		Result<Void> res = impl.unshareFile(filename, userId, userIdShare, password, DEFAULT_VERSION);
		if(res.isOK())
			unshareFileSec(filename, userId, userIdShare);
		super.resultOrThrow(res, DirectoryException::new);
	}

	@Override
	public byte[] getFile(String filename, String userId, String accUserId, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP getFile: version = %d, filename = %s, userId = %s, accUserId = %s, password =%s\n",
				DEFAULT_VERSION, filename, userId, accUserId, password));

		var res = impl.getFile(filename, userId, accUserId, password, DEFAULT_VERSION);
		if (res.error() == ErrorCode.REDIRECT) {
			String location = res.errorValue();
			String fileId = JavaDirectory.fileId(filename, userId);
			String token = GenerateToken.buildToken(fileId);
			res = FilesClients.get(location).getFile(fileId, token);
		}
		return super.resultOrThrow(res, DirectoryException::new);
	}

	@Override
	public List<FileInfo> lsFile(String userId, String password) throws DirectoryException {
		Log.info(String.format("SOAP lsFile: userId = %s, password = %s\n", userId, password));

		return super.resultOrThrow(impl.lsFile(userId, password, DEFAULT_VERSION), DirectoryException::new);
	}

	@Override
	public void deleteUserFiles(String userId, String password) throws DirectoryException {
		Log.info(
				String.format("SOAP deleteUserFiles: user = %s, password = %s\n", userId, password));

		super.resultOrThrow(impl.deleteUserFiles(userId, password), DirectoryException::new);
	}

	@Override
	public void writeFileSec(String filename, String userId, ExtendedFileInfo file) {
		Log.info(String.format("SOAP writeFile: filename = %s, userId = %s\n", filename, userId));
		try {
			super.resultOrThrow(impl.writeFileSec(filename, userId, null), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void deleteFileSec(String filename, String userId) {
		Log.info(String.format("SOAP deleteFile: filename = %s, userId = %s\n", filename, userId));
		try {
			super.resultOrThrow(impl.deleteFileSec(filename, userId), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void shareFileSec(String filename, String userId, String userIdShare) {
		Log.info(String.format("SOAP shareFile: filename = %s, userId = %s, userIdShare = %s\n", filename, userId,
				userIdShare));
		try {
			super.resultOrThrow(impl.shareFileSec(filename, userId, userIdShare), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unshareFileSec(String filename, String userId, String userIdShare) {
		Log.info(String.format("SOAP unshareFile: filename = %s, userId = %s, userIdShare = %s\n", filename, userId,
				userIdShare));
		try {
			super.resultOrThrow(impl.unshareFileSec(filename, userId, userIdShare), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}
	}
}
