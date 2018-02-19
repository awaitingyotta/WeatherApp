package knowit.com.weatherapp.exceptions;

public class AppErrorException extends Exception {

    public AppErrorException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;

}
