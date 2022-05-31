package tp1.impl.servers.soap;

import static tp1.impl.clients.Clients.FilesClients;

import java.util.List;
import java.util.logging.Logger;

import jakarta.jws.WebService;
import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result.ErrorCode;
import tp1.api.service.soap.DirectoryException;
import tp1.api.service.soap.SoapDirectory;
import tp1.impl.servers.common.JavaDirectory;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import token.GenerateToken;

@WebService(serviceName = SoapDirectory.NAME, targetNamespace = SoapDirectory.NAMESPACE, endpointInterface = SoapDirectory.INTERFACE)
public class SoapDirectoryWebService extends SoapWebService implements SoapDirectory {

	static Logger Log = Logger.getLogger(SoapDirectoryWebService.class.getName());

	final Directory impl;

	public SoapDirectoryWebService() {
		impl = new JavaDirectory();
	}

	@Override
	public FileInfo writeFile(Long version, String filename, byte[] data, String userId, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP writeFile: filename = %s, data.length = %d, userId = %s, password = %s \n",
				filename, data.length, userId, password));

		return super.resultOrThrow(impl.writeFile(version, filename, data, userId, password), DirectoryException::new);
	}

	@Override
	public void deleteFile(Long version, String filename, String userId, String password) throws DirectoryException {
		Log.info(String.format("SOAP deleteFile: filename = %s, userId = %s, password =%s\n", filename, userId,
				password));

		super.resultOrThrow(impl.deleteFile(version, filename, userId, password), DirectoryException::new);
	}

	@Override
	public void shareFile(Long version, String filename, String userId, String userIdShare, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP shareFile: filename = %s, userId = %s, userIdShare = %s, password =%s\n", filename,
				userId, userIdShare, password));

		super.resultOrThrow(impl.shareFile(version, filename, userId, userIdShare, password), DirectoryException::new);
	}

	@Override
	public void unshareFile(Long version, String filename, String userId, String userIdShare, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP unshareFile: filename = %s, userId = %s, userIdShare = %s, password =%s\n",
				filename, userId, userIdShare, password));

		super.resultOrThrow(impl.unshareFile(version, filename, userId, userIdShare, password),
				DirectoryException::new);
	}

	@Override
	public byte[] getFile(Long version, String filename, String userId, String accUserId, String password)
			throws DirectoryException {
		Log.info(String.format("SOAP getFile: version = %d, filename = %s, userId = %s, accUserId = %s, password =%s\n",
				version, filename, userId, accUserId, password));

		var res = impl.getFile(version, filename, userId, accUserId, password);
		if (res.error() == ErrorCode.REDIRECT) {
			String location = res.errorValue();
			String fileId = JavaDirectory.fileId(filename, userId);
			res = FilesClients.get(location).getFile(fileId, new GenerateToken(fileId));
		}
		return super.resultOrThrow(res, DirectoryException::new);
	}

	@Override
	public List<FileInfo> lsFile(Long version, String userId, String password) throws DirectoryException {
		Log.info(String.format("SOAP lsFile: userId = %s, password = %s\n", userId, password));

		return super.resultOrThrow(impl.lsFile(version, userId, password), DirectoryException::new);
	}

	@Override
	public void deleteUserFiles(String userId, String password, GenerateToken token) throws DirectoryException {
		Log.info(
				String.format("SOAP deleteUserFiles: user = %s, password = %s, token = %s\n", userId, password, token));

		super.resultOrThrow(impl.deleteUserFiles(userId, password, token), DirectoryException::new);
	}

	@Override
	public void writeFile(long version, String filename, String userId, ExtendedFileInfo file) {
		Log.info(String.format("SOAP writeFile: filename = %s, userId = %s\n", filename, userId));
		try {
			super.resultOrThrow(impl.writeFile(version, filename, userId, file), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void deleteFile(long version, String filename, String userId) {
		Log.info(String.format("SOAP deleteFile: filename = %s, userId = %s\n", filename, userId));
		try {
			super.resultOrThrow(impl.deleteFile(version, filename, userId), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void shareFile(long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("SOAP shareFile: filename = %s, userId = %s, userIdShare = %s\n", filename, userId,
				userIdShare));
		try {
			super.resultOrThrow(impl.shareFile(version, filename, userId, userIdShare), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unshareFile(long version, String filename, String userId, String userIdShare) {
		Log.info(String.format("SOAP unshareFile: filename = %s, userId = %s, userIdShare = %s\n", filename, userId,
				userIdShare));
		try {
			super.resultOrThrow(impl.unshareFile(version, filename, userId, userIdShare), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lsFile(long version, String userId) {
		Log.info(String.format("SOAP lsFile: userId = %s\n", userId));
		try {
			super.resultOrThrow(impl.lsFile(version, userId), DirectoryException::new);
		} catch (DirectoryException e) {
			e.printStackTrace();
		}

	}
}
