//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ServiceConnection {
    int DEFAULT_TIMEOUT = 20000;
    int DEFAULT_BUFFER_SIZE = 262144;

    void connect() throws IOException;

    void disconnect() throws IOException;

    List getResponseProperties() throws IOException;

    int getResponseCode() throws IOException;

    void setRequestProperty(String var1, String var2) throws IOException;

    void setRequestMethod(String var1) throws IOException;

    void setFixedLengthStreamingMode(int var1);

    void setChunkedStreamingMode();

    OutputStream openOutputStream() throws IOException;

    InputStream openInputStream() throws IOException;

    InputStream getErrorStream();

    String getHost();

    int getPort();

    String getPath();
}
