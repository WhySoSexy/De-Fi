package bc.group.caspian.recon.exception;

public class CepClientRequestException extends RuntimeException {

    private String endpoint;
    private Object request;

    public CepClientRequestException(String message, Throwable cause, String endpoint, Object request) {
        super(message, cause);
        this.endpoint = endpoint;
        this.request = request;
    }

    @Override
    public String getMessage() {
        return String.format("%s, request: %s [endpoint:%s]", super.getMessage(), request, endpoint);
    }

    @Override
    public String getLocalizedMessage() {
        return String.format("%s, request: %s [endpoint:%s]", super.getLocalizedMessage(), request, endpoint);
    }
}
