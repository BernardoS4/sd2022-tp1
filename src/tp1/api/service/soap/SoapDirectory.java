package tp1.api.service.soap;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import tp1.api.FileInfo;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;

@WebService(serviceName=SoapDirectory.NAME, targetNamespace=SoapDirectory.NAMESPACE, endpointInterface=SoapDirectory.INTERFACE)
public interface SoapDirectory {
	
	static final String NAME = "dir";
	static final String NAMESPACE = "http://sd2122";
	static final String INTERFACE = "tp1.api.service.soap.SoapDirectory";


	@WebMethod
	FileInfo writeFile(String filename, byte []data, String userId, String password) throws DirectoryException;

	@WebMethod
	void deleteFile(String filename, String userId, String password) throws DirectoryException;

	@WebMethod
	void shareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException;

	@WebMethod
	void unshareFile(String filename, String userId, String userIdShare, String password) throws DirectoryException;

	@WebMethod
	byte[] getFile(String filename,  String userId, String accUserId, String password) throws DirectoryException;

	@WebMethod
	List<FileInfo> lsFile(String userId, String password) throws DirectoryException;

	@WebMethod
	void deleteUserFiles(String userId, String password, String token) throws DirectoryException;

	@WebMethod
	void writeFile(String filename, String userId, ExtendedFileInfo file);

	@WebMethod
	void deleteFile(String filename, String userId);

	@WebMethod
	void shareFile(String filename, String userId, String userIdShare);

	@WebMethod
	void unshareFile(String filename, String userId, String userIdShare);
}
