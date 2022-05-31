package tp1.impl.clients.rest;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.FileInfo;
import tp1.api.service.java.Directory;
import tp1.api.service.java.Result;
import tp1.api.service.rest.RestDirectory;
import tp1.impl.servers.common.JavaDirectory.ExtendedFileInfo;
import util.Operation;
import token.GenerateToken;

public class RestDirectoryClient extends RestClient implements Directory {

	private static final String SHARE = "share";

	public RestDirectoryClient(URI serverUri) {
		super(serverUri, RestDirectory.PATH);
	}

	@Override
	public Result<FileInfo> writeFile(Long version, String filename, byte[] data, String userId, String password) {
		Response r = target.path(userId).path(filename).queryParam(RestDirectory.PASSWORD, password).request()
				.header(RestDirectory.HEADER_VERSION, version).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
		return super.toJavaResult(r, new GenericType<FileInfo>() {
		});
	}

	@Override
	public Result<Void> writeFile(Long version, String filename, String userId, ExtendedFileInfo file) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).request()
				.header(RestDirectory.HEADER_VERSION, version).accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(file, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> deleteFile(Long version, String filename, String userId, String password) {
		Response r = target.path(userId).path(filename).queryParam(RestDirectory.PASSWORD, password).request()
				.header(RestDirectory.HEADER_VERSION, version).delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> deleteFile(Long version, String filename, String userId) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).request()
				.header(RestDirectory.HEADER_VERSION, version).delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> shareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Response r = target.path(userId).path(filename).path(SHARE).path(userIdShare)
				.queryParam(RestDirectory.PASSWORD, password).request().header(RestDirectory.HEADER_VERSION, version)
				.post(Entity.json(null));
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> shareFile(Long version, String filename, String userId, String userIdShare) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).path(SHARE).path(userIdShare)
				.request().header(RestDirectory.HEADER_VERSION, version).post(Entity.json(null));
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
		Response r = target.path(userId).path(filename).path(SHARE).path(userIdShare)
				.queryParam(RestDirectory.PASSWORD, password).request().header(RestDirectory.HEADER_VERSION, version)
				.delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> unshareFile(Long version, String filename, String userId, String userIdShare) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).path(SHARE).path(userIdShare)
				.request().header(RestDirectory.HEADER_VERSION, version).delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<byte[]> getFile(Long version, String filename, String userId, String accUserId, String password) {
		Response r = target.path(userId).path(filename).queryParam(RestDirectory.ACC_USER_ID, accUserId)
				.queryParam(RestDirectory.PASSWORD, password).request().header(RestDirectory.HEADER_VERSION, version)
				.accept(MediaType.APPLICATION_OCTET_STREAM).get();
		return super.toJavaResult(r, new GenericType<byte[]>() {
		});
	}

	@Override
	public Result<List<FileInfo>> lsFile(Long version, String userId, String password) {
		Response r = target.path(userId).queryParam(RestDirectory.PASSWORD, password).request()
				.header(RestDirectory.HEADER_VERSION, version).accept(MediaType.APPLICATION_JSON).get();
		return super.toJavaResult(r, new GenericType<List<FileInfo>>() {
		});
	}

	@Override
	public Result<Void> lsFile(Long version, String userId) {
		Response r = target.path(userId).request().header(RestDirectory.HEADER_VERSION, version).get();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String password, GenerateToken token) {
		Response r = target.path(userId).queryParam(RestDirectory.PASSWORD, password)
				.queryParam(RestDirectory.TOKEN, token).request().delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Operation> getOperation(Long version) {
		Response r = target.path(RestDirectory.PREFIX).path(version.toString()).request().get();
		return super.toJavaResult(r, new GenericType<Operation>() {
		});
	}
}
