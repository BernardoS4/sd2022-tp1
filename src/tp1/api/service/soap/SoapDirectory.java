package tp1.api.service.soap;

import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import tp1.api.FileInfo;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import token.GenerateToken;

@WebService(serviceName=SoapDirectory.NAME, targetNamespace=SoapDirectory.NAMESPACE, endpointInterface=SoapDirectory.INTERFACE)
public interface SoapDirectory {
	
	static final String NAME = "dir";
	static final String NAMESPACE = "http://sd2122";
	static final String INTERFACE = "tp1.api.service.soap.SoapDirectory";


	@WebMethod
	FileInfo writeFile(Long version, String filename, byte []data, String userId, String password) throws DirectoryException;

	@WebMethod
	void deleteFile(Long version, String filename, String userId, String password) throws DirectoryException;

	@WebMethod
	void shareFile(Long version, String filename, String userId, String userIdShare, String password) throws DirectoryException;

	@WebMethod
	void unshareFile(Long version, String filename, String userId, String userIdShare, String password) throws DirectoryException;

	@WebMethod
	byte[] getFile(Long version, String filename,  String userId, String accUserId, String password) throws DirectoryException;

	@WebMethod
	List<FileInfo> lsFile(Long version, String userId, String password) throws DirectoryException;

	@WebMethod
	void deleteUserFiles(String userId, String password, GenerateToken token) throws DirectoryException;

	@WebMethod
	void writeFile(long dEFAULT_VERSION, String filename, String userId, ExtendedFileInfo file);

	@WebMethod
	void deleteFile(long dEFAULT_VERSION, String filename, String userId);

	@WebMethod
	void shareFile(long dEFAULT_VERSION, String filename, String userId, String userIdShare);

	@WebMethod
	void unshareFile(long dEFAULT_VERSION, String filename, String userId, String userIdShare);

	@WebMethod
	void lsFile(long dEFAULT_VERSION, String userId);
}
