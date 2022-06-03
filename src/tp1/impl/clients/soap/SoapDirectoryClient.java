package tp1.impl.clients.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Service;
import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.api.service.soap.SoapDirectory;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import util.Url;

public class SoapDirectoryClient extends SoapClient<SoapDirectory> implements Directory {
	
	public SoapDirectoryClient(URI serverURI) {
		super(serverURI, () -> {
			QName QNAME = new QName(SoapDirectory.NAMESPACE, SoapDirectory.NAME);
			Service service = Service.create(Url.from(serverURI + WSDL), QNAME);
			return service.getPort(SoapDirectory.class);
		});
	}
 
	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password, Long version) {
		return super.toJavaResult(() -> impl.writeFile(filename, data, userId, password));
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password, Long version) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId, password));
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password, Long version) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId, password));
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password, Long version) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId, password));
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password, Long version) {
		return super.toJavaResult(() -> impl.getFile(filename, userId, accUserId, password));
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password, Long version) {
		return super.toJavaResult(() -> impl.lsFile(userId, password));
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
		return super.toJavaResult(() -> impl.deleteUserFiles(userId, password, token));
	}

	@Override
	public Result<Void> writeFileSec(String filename, String userId, ExtendedFileInfo file, Long version) {
		return super.toJavaResult(() -> impl.writeFileSec(filename, userId, file));
	}

	@Override
	public Result<Void> deleteFileSec(String filename, String userId, Long version) {
		return super.toJavaResult(() -> impl.deleteFileSec(filename, userId));
	}

	@Override
	public Result<Void> shareFileSec(String filename, String userId, String userIdShare, Long version) {
		return super.toJavaResult(() -> impl.shareFileSec(filename, userId, userIdShare));
	}

	@Override
	public Result<Void> unshareFileSec(String filename, String userId, String userIdShare, Long version) {
		return super.toJavaResult(() -> impl.unshareFileSec(filename, userId, userIdShare));
	}
}
