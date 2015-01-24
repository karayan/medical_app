package gr.forth.ics.urbanNet.network;

/**
 * General urbanNet server exception.
 * <p>Describes server-side errors exceptions and error messages.</p>
 * @author michalis
 */
public class ServerException extends Exception {

	/**
	 * 
	 */
	public ServerException() {
	}

	/**
	 * @param detailMessage
	 */
	public ServerException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * @param throwable
	 */
	public ServerException(Throwable throwable) {
		super(throwable);
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public ServerException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
