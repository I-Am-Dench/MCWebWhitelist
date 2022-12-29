package theof.webwhitelist.webserver;

import java.util.HashMap;
import java.util.Map;

/*
* Response status codes pulled from https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
*
* Deprecated, experimental, WebDAV, and other status codes that are deemed unnecessary are omitted, but can be added later.
*
* These constants could have absolutely been inside an enum, but since HTTP requests and responses could technically return
* a response code other than the Standard's, these are left as final ints as to not have to cover every single case.
*
* Yes, there could have been a method such as Enum::getCode, but these constants are purely for convenience, not as an
* intrinsic part of the Context's functionality.
* */
public class Status {

    // INFORMATIONAL
    public static final int CONTINUE = 100;
    public static final int SWITCHING_PROTOCOLS = 101;
    public static final int EARLY_HINTS = 103;

    // SUCCESSFUL
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int ACCEPTED = 202;
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;
    public static final int NO_CONTENT = 204;
    public static final int RESET_CONTENT = 205;
    public static final int PARTIAL_CONTENT = 206;

    // REDIRECTION
    public static final int MULTIPLE_CHOICES = 300;
    public static final int MOVED_PERMANENTLY = 301;
    public static final int FOUND = 302;
    public static final int SEE_OTHER = 303;
    public static final int NOT_MODIFIED = 304;
    public static final int TEMPORARY_REDIRECT = 307;
    public static final int PERMANENT_REDIRECT = 308;

    // CLIENT ERROR
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
    public static final int REQUEST_TIMEOUT = 408;
    public static final int CONFLICT = 409;
    public static final int GONE = 410;
    public static final int LENGTH_REQUIRED = 411;
    public static final int PRECONDITION_FAILED = 412;
    public static final int PAYLOAD_TOO_LARGE = 413;
    public static final int URI_TOO_LONG = 414;
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int RANGE_NOT_SATISFIABLE = 416;
    public static final int EXPECTATION_FAILED = 417;
    public static final int IM_A_TEAPOT = 418;
    public static final int UPGRADE_REQUIRED = 426;
    public static final int PRECONDITION_REQUIRED = 428;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;

    // SERVER ERROR
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int NOT_IMPLEMENTED = 501;
    public static final int BAD_GATEWAY = 502;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
    public static final int VARIANT_ALSO_NEGOTIATES = 506;
    public static final int NOT_EXTENDED = 510;
    public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

    private static final Map<Integer, String> MESSAGES = new HashMap<>() {{
        put(CONTINUE, "Continue");
        put(SWITCHING_PROTOCOLS, "Switching Protocols");
        put(EARLY_HINTS, "Early Hints");

        put(OK, "OK");
        put(CREATED, "Created");
        put(ACCEPTED, "Accepted");
        put(NON_AUTHORITATIVE_INFORMATION, "Non-Authoritative Information");
        put(NO_CONTENT, "No Content");
        put(RESET_CONTENT, "Reset Content");
        put(PARTIAL_CONTENT, "Partial Content");

        put(MULTIPLE_CHOICES, "Multiple Choices");
        put(MOVED_PERMANENTLY, "Moved Permanently");
        put(FOUND, "Found");
        put(SEE_OTHER, "See Other");
        put(NOT_MODIFIED, "Not Modified");
        put(TEMPORARY_REDIRECT, "Temporary Redirect");
        put(PERMANENT_REDIRECT, "Permanent Redirect");

        put(BAD_REQUEST, "Bad Request");
        put(UNAUTHORIZED, "Unauthorized");
        put(FORBIDDEN, "Forbidden");
        put(NOT_FOUND, "Not Found");
        put(METHOD_NOT_ALLOWED, "Method Not Allowed");
        put(NOT_ACCEPTABLE, "Not Acceptable");
        put(PROXY_AUTHENTICATION_REQUIRED, "Proxy Authentication Required");
        put(REQUEST_TIMEOUT, "Request Timeout");
        put(CONFLICT, "Conflict");
        put(GONE, "Gone");
        put(LENGTH_REQUIRED, "Length Required");
        put(PRECONDITION_FAILED, "Precondition Failed");
        put(PAYLOAD_TOO_LARGE, "Payload Too Large");
        put(URI_TOO_LONG, "URI Too Long");
        put(UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
        put(RANGE_NOT_SATISFIABLE, "Range Not Satisfiable");
        put(EXPECTATION_FAILED, "Expectation Failed");
        put(IM_A_TEAPOT, "I'm a Teapot");
        put(UPGRADE_REQUIRED, "Upgrade Required");
        put(PRECONDITION_REQUIRED, "Precondition Required");
        put(TOO_MANY_REQUESTS, "Too Many Requests");
        put(REQUEST_HEADER_FIELDS_TOO_LARGE, "Request Header Fields Too Large");

        put(INTERNAL_SERVER_ERROR, "Internal Server Error");
        put(NOT_IMPLEMENTED, "Not Implemented");
        put(BAD_GATEWAY, "Bad Gateway");
        put(SERVICE_UNAVAILABLE, "Service Unavailable");
        put(GATEWAY_TIMEOUT, "Gateway Timeout");
        put(HTTP_VERSION_NOT_SUPPORTED, "HTTP Version Not Supported");
        put(VARIANT_ALSO_NEGOTIATES, "Variant Also Negotiates");
        put(NOT_EXTENDED, "Not Extended");
        put(NETWORK_AUTHENTICATION_REQUIRED, "Network Authentication Required");
    }};

    public static String toString(int status) {
        if (!MESSAGES.containsKey(status)) {
            return String.format("Status %d", status);
        }

        return MESSAGES.get(status);
    }
}
