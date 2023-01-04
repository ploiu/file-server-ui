package ploiu.exception;

public class ServerUnavailableException extends RuntimeException {
    public ServerUnavailableException() {
        super("The server is not up");
    }
}
