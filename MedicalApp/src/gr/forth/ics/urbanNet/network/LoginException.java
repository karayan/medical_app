package gr.forth.ics.urbanNet.network;

public class LoginException extends ServerException {

	public LoginException() {
	}

	public LoginException(String detailMessage) {
		super(detailMessage);
	}

	public LoginException(Throwable throwable) {
		super(throwable);
	}

	public LoginException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
