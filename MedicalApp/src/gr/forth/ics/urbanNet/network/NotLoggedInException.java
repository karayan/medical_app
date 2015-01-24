package gr.forth.ics.urbanNet.network;

public class NotLoggedInException extends ServerException {

	public NotLoggedInException() {
	}

	public NotLoggedInException(String detailMessage) {
		super(detailMessage);
	}

	public NotLoggedInException(Throwable throwable) {
		super(throwable);
	}

	public NotLoggedInException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
