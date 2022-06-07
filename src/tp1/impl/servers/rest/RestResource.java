package tp1.impl.servers.rest;

import java.net.URI;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp1.api.service.java.Result;
import static util.SystemConstants.HEADER_VERSION;

public class RestResource {

	/**
	 * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
	 * matching the error code...
	 */
	protected <T> T resultOrThrow(Result<T> result, Long version) {
		if (result.isOK()) {
			if(result.value() != null) {
				throw new WebApplicationException(Response.ok().header(HEADER_VERSION, version).entity(result.value()).build());
			}
			else 
				throw new WebApplicationException(Response.noContent().header(HEADER_VERSION, version).build());
		}
			
		else
			throw new WebApplicationException(statusCode(result, version));
	}

	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	static protected Status statusCode(Result<?> result, Long version) {
		switch (result.error()) {
		case CONFLICT:
			return Status.CONFLICT;
		case NOT_FOUND:
			return Status.NOT_FOUND;
		case FORBIDDEN:
			return Status.FORBIDDEN;
		case TIMEOUT:
		case BAD_REQUEST:
			return Status.BAD_REQUEST;
		case NOT_IMPLEMENTED:
			return Status.NOT_IMPLEMENTED;
		case INTERNAL_ERROR:
			return Status.INTERNAL_SERVER_ERROR;
		case OK:
			return result.value() == null ? Status.NO_CONTENT : Status.OK;
		case REDIRECT:
			doRedirect(result, version);

		default:
			return Status.INTERNAL_SERVER_ERROR;
		}
	}

	static private void doRedirect(Result<?> result, Long version) throws WebApplicationException {
		var location = URI.create(result.errorValue());
		throw new WebApplicationException(Response.temporaryRedirect(location).header(HEADER_VERSION, version).build());
	}
}