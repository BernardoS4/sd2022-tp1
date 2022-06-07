package tp1.impl.clients.rest;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tp1.api.User;
import tp1.api.service.java.Result;
import tp1.api.service.java.Users;
import static util.SystemConstants.PASSWORD;
import static util.SystemConstants.USERS_PATH;
import static util.SystemConstants.QUERY;

public class RestUsersClient extends RestClient implements Users {

	public RestUsersClient(URI serverUri) {
		super(serverUri, USERS_PATH);
	}

	@Override
	public Result<String> createUser(User user) {
		Response r = target
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.post( Entity.entity(user, MediaType.APPLICATION_JSON));
		
		return super.toJavaResult(r, new GenericType<String>() {});
	}

	@Override
	public Result<User> updateUser(String userId, String password, User user) {
		Response r = target.path( userId)
				.queryParam(PASSWORD, password)
				.request()
				.accept(  MediaType.APPLICATION_JSON)
				.put(Entity.entity(user, MediaType.APPLICATION_JSON));
		
		return super.toJavaResult(r, new GenericType<User>() {});
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		Response r = target.path(userId)
				.queryParam(PASSWORD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return super.toJavaResult(r, new GenericType<User>() {});
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		Response r = target.path(userId)
				.queryParam(PASSWORD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		
		return super.toJavaResult(r, new GenericType<User>() {});
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		Response r = target
				.queryParam(QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		
		return super.toJavaResult(r, new GenericType<List<User>>() {});
	}
}
