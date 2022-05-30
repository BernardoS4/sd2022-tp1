package tp1.api.service.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import token.GenerateToken;

@WebService(serviceName=SoapFiles.NAME, targetNamespace=SoapFiles.NAMESPACE, endpointInterface=SoapFiles.INTERFACE)
public interface SoapFiles {

	static final String NAME = "files";
	static final String NAMESPACE = "http://sd2122";
	static final String INTERFACE = "tp1.api.service.soap.SoapFiles";

	@WebMethod
	byte[] getFile(String fileId, GenerateToken token) throws FilesException;

	@WebMethod
	void deleteFile(String fileId, GenerateToken token) throws FilesException;
	
	@WebMethod
	void writeFile(String fileId, byte[] data, GenerateToken token) throws FilesException;
	
	@WebMethod
	void deleteUserFiles(String userId, GenerateToken token) throws FilesException;
}
