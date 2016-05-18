//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2.transport;

import java.io.IOException;
import java.util.List;

public class HttpResponseException extends IOException {
    private int statusCode;
    private List responseHeaders;

    public HttpResponseException(int statusCode) {
        this.statusCode = statusCode;
    }

    public HttpResponseException(String detailMessage, int statusCode) {
        super(detailMessage);
        this.statusCode = statusCode;
    }

    public HttpResponseException(String detailMessage, int statusCode, List responseHeaders) {
        super(detailMessage);
        this.statusCode = statusCode;
        this.responseHeaders = responseHeaders;
    }

    public HttpResponseException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpResponseException(Throwable cause, int statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public List getResponseHeaders() {
        return this.responseHeaders;
    }
}
