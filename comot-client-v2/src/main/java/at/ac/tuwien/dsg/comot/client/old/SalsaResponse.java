package at.ac.tuwien.dsg.comot.client.old;

/**
 * @author omoser
 */
public class SalsaResponse {

    int code;

    String message;

    int expectedCode;

    public SalsaResponse() {
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getExpectedCode() {
        return expectedCode;
    }

    public SalsaResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public SalsaResponse withExpectedCode(final int expectedCode) {
        this.expectedCode = expectedCode;
        return this;
    }

    public SalsaResponse withCode(final int code) {
        this.code = code;
        return this;
    }

    public SalsaResponse withMessage(final String message) {
        this.message = message;
        return this;
    }

    public boolean isExpected() {
        return expectedCode == code;
    }

    @Override
    public String toString() {
        return "SalsaResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", expectedCode=" + expectedCode +
                '}';
    }
}
