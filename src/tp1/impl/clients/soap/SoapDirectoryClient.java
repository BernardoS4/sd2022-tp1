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
import token.GenerateToken;
import util.Url;

public class SoapDirectoryClient extends SoapClient<SoapDirectory> implements Directory {

	public SoapDirectoryClient(URI serverURI) {
		super(serverURI, () -> {
			QName QNAME = new QName(SoapDirectory.NAMESPACE, SoapDirectory.NAME);
			Service service = Service.create(Url.from(serverURI + WSDL), QNAME);
			return service.getPort(tp1.api.service.soap.SoapDirectory.class);
		});
	}

	@Override
	public Result<FileInfo> writeFile(Long version, String filename, byte[] data, String userId, String password) {
		return super.toJavaResult(() -> impl.writeFile(filename, data, userId, password));
	}

	@Override
	public Result<Void> deleteFile(Long version, String filename, String userId, String password) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId, password));
	}

	@Override
	public Result<Void> shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId, password));
	}

	@Override
	public Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId, password));
	}

	@Override
	public Result<byte[]> getFile(Long version, String filename, String userId, String accUserId, String password) {
		return super.toJavaResult(() -> impl.getFile(filename, userId, accUserId, password));
	}

	@Override
	public Result<List<FileInfo>> lsFile(Long version, String userId, String password) {
		return super.toJavaResult(() -> impl.lsFile(userId, password));
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
		return super.toJavaResult(() -> impl.deleteUserFiles(userId, password, token));
	}

	@Override
	public Result<Void> writeFile(Long version, String filename, String userId, ExtendedFileInfo file) {
		return super.toJavaResult(() -> impl.writeFile(filename, userId, file));
	}

	@Override
	public Result<Void> deleteFile(Long version, String filename, String userId) {
		return super.toJavaResult(() -> impl.deleteFile(filename, userId));
	}

	@Override
	public Result<Void> shareFile(Long version, String filename, String userId, String userIdShare) {
		return super.toJavaResult(() -> impl.shareFile(filename, userId, userIdShare));
	}

	@Override
	public Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare) {
		return super.toJavaResult(() -> impl.unshareFile(filename, userId, userIdShare));
	}

	@Override
	public Result<Void> lsFile(Long version, String userId) {
		return super.toJavaResult(() -> impl.lsFile(userId));
	}
}
