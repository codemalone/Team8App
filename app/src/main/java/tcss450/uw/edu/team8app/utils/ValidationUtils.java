package tcss450.uw.edu.team8app.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    public static final Pattern USERNAME = Pattern.compile("[a-zA-Z0-9]{3,20}");
    public static final Pattern PASSWORD = Pattern.compile("[a-zA-Z0-9._'\\-]{6,20}");

}
