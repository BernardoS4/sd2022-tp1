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

public class RestDirectoryClient extends RestClient implements Directory {

	private static final String SHARE = "share";

	public RestDirectoryClient(URI serverUri) {
		super(serverUri, RestDirectory.PATH);
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password, Long version) {
		Response r = target.path(userId).path(filename)
				.queryParam(RestDirectory.PASSWORD, password)
				.request()
				.header(RestDirectory.HEADER_VERSION, version)
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));
		return super.toJavaResult(r, new GenericType<FileInfo>() {
		});
	}

	@Override
	public Result<Void> writeFileSec(String filename, String userId, ExtendedFileInfo file) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename)
				.request()
				.post(Entity.entity(file, MediaType.APPLICATION_JSON));
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> deleteFile(String filename, String userId, String password, Long version) {
		Response r = target.path(userId).path(filename).queryParam(RestDirectory.PASSWORD, password).request()
				.header(RestDirectory.HEADER_VERSION, version).delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> deleteFileSec(String filename, String userId) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).request()
				.delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password, Long version) {
		Response r = target.path(userId).path(filename).path(SHARE).path(userIdShare)
				.queryParam(RestDirectory.PASSWORD, password).request().header(RestDirectory.HEADER_VERSION, version)
				.post(Entity.json(null));
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> shareFileSec(String filename, String userId, String userIdShare) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).path(SHARE).path(userIdShare)
				.request().post(Entity.json(null));
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password, Long version) {
		Response r = target.path(userId).path(filename).path(SHARE).path(userIdShare)
				.queryParam(RestDirectory.PASSWORD, password).request().header(RestDirectory.HEADER_VERSION, version)
				.delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<Void> unshareFileSec(String filename, String userId, String userIdShare) {
		Response r = target.path(RestDirectory.PREFIX).path(userId).path(filename).path(SHARE).path(userIdShare)
				.request().delete();
		return super.toJavaResult(r);
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password, Long version) {
		Response r = target.path(userId).path(filename)
				.queryParam(RestDirectory.ACC_USER_ID, accUserId)
				.queryParam(RestDirectory.PASSWORD, password)
				.request()
				.header(RestDirectory.HEADER_VERSION, version)
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();
		return super.toJavaResult(r, new GenericType<byte[]>() {
		});
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password, Long version) {
		Response r = target.path(userId).queryParam(RestDirectory.PASSWORD, password).request()
				.header(RestDirectory.HEADER_VERSION, version).accept(MediaType.APPLICATION_JSON).get();
		return super.toJavaResult(r, new GenericType<List<FileInfo>>() {
		});
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
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
