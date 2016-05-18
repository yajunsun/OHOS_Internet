//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2.transport;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.ServiceConnection;
import org.ksoap2.transport.ServiceConnectionSE;
import org.ksoap2.transport.Transport;
import org.xmlpull.v1.XmlPullParserException;

import zgan.ohos.utils.DataCacheHelper;

public class HttpTransportSE extends Transport {
    public HttpTransportSE(String url) {
        super((Proxy)null, url);
    }

    public HttpTransportSE(Proxy proxy, String url) {
        super(proxy, url);
    }

    public HttpTransportSE(String url, int timeout) {
        super(url, timeout);
    }

    public HttpTransportSE(Proxy proxy, String url, int timeout) {
        super(proxy, url, timeout);
    }

    public HttpTransportSE(String url, int timeout, int contentLength) {
        super(url, timeout);
    }

    public HttpTransportSE(Proxy proxy, String url, int timeout, int contentLength) {
        super(proxy, url, timeout);
    }

    public void call(String soapAction, SoapEnvelope envelope) throws HttpResponseException, IOException, XmlPullParserException {
        this.call(soapAction, envelope, (List)null);
    }

    public List call(String soapAction, SoapEnvelope envelope, List headers) throws HttpResponseException, IOException, XmlPullParserException {
        return this.call(soapAction, envelope, headers, (File)null);
    }

    public List call(String soapAction, SoapEnvelope envelope, List headers, File outputFile) throws HttpResponseException, IOException, XmlPullParserException {
        if(soapAction == null) {
            soapAction = "\"\"";
        }

        byte[] requestData = this.createRequestData(envelope, "UTF-8");
        this.requestDump = this.debug?new String(requestData):null;
        this.responseDump = null;
        ServiceConnection connection = this.getServiceConnection();
        connection.setRequestProperty("User-Agent", "ksoap2-android/2.6.0+");
        if(envelope.version != 120) {
            connection.setRequestProperty("SOAPAction", soapAction);
        }

        if(envelope.version == 120) {
            connection.setRequestProperty("Content-Type", "application/soap+xml;charset=utf-8");
        } else {
            connection.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
        }

        connection.setRequestProperty("Accept-Encoding", "gzip");
        if(headers != null) {
            for(int is = 0; is < headers.size(); ++is) {
                HeaderProperty retHeaders = (HeaderProperty)headers.get(is);
                connection.setRequestProperty(retHeaders.getKey(), retHeaders.getValue());
            }
        }

        connection.setRequestMethod("POST");
        this.sendData(requestData, connection, envelope);
        Object var19 = null;
        Object var20 = null;
        List var21 = null;
        Object buf = null;
        int contentLength = 8192;
        boolean gZippedContent = false;
        boolean xmlContent = false;
        int status = connection.getResponseCode();

        try {
            var21 = connection.getResponseProperties();

            for(int e = 0; e < var21.size(); ++e) {
                HeaderProperty hp = (HeaderProperty)var21.get(e);
                if(null != hp.getKey()) {
                    if(hp.getKey().equalsIgnoreCase("content-length") && hp.getValue() != null) {
                        try {
                            contentLength = Integer.parseInt(hp.getValue());
                        } catch (NumberFormatException var17) {
                            contentLength = 8192;
                        }
                    }

                    if(hp.getKey().equalsIgnoreCase("Content-Type") && hp.getValue().contains("xml")) {
                        xmlContent = true;
                    }

                    if(hp.getKey().equalsIgnoreCase("Content-Encoding") && hp.getValue().equalsIgnoreCase("gzip")) {
                        gZippedContent = true;
                    }
                }
            }

            if(status != 200) {
                throw new HttpResponseException("HTTP request failed, HTTP status: " + status, status, var21);
            }

            if(contentLength > 0) {
                if(gZippedContent) {
                    var20 = this.getUnZippedInputStream(new BufferedInputStream(connection.openInputStream(), contentLength));
                } else {
                    var20 = new BufferedInputStream(connection.openInputStream(), contentLength);
                }
            }
        } catch (IOException var18) {
            if(contentLength > 0) {
                if(gZippedContent) {
                    var20 = this.getUnZippedInputStream(new BufferedInputStream(connection.getErrorStream(), contentLength));
                } else {
                    var20 = new BufferedInputStream(connection.getErrorStream(), contentLength);
                }
            }

            if(var18 instanceof HttpResponseException && !xmlContent) {
                if(this.debug && var20 != null) {
                    this.readDebug((InputStream)var20, contentLength, outputFile);
                }

                connection.disconnect();
                throw var18;
            }
        }

        if(this.debug) {
            var20 = this.readDebug((InputStream)var20, contentLength, outputFile);
        }

        //InputStream copyis=new BufferedInputStream ((InputStream) var20);
        ByteArrayOutputStream byteout=new ByteArrayOutputStream();
        byte[]buffer=new byte[contentLength];
        int len=0;
        while ((len = ((InputStream)var20).read(buffer)) > -1 ) {
            byteout.write(buffer, 0, len);
        }
        byteout.flush();

        InputStream stream1 = new ByteArrayInputStream(byteout.toByteArray());

//TODO:显示到前台

        InputStream stream2 = new ByteArrayInputStream(byteout.toByteArray());
        //this.parseResponse(envelope, (InputStream)var20, var21);
        this.parseResponse(envelope, stream1, var21);

        DataCacheHelper.add2DiskCache(soapAction, stream2);
        byteout.close();
        buffer=null;
        var20 = null;
        buf = null;
        connection.disconnect();
        connection = null;
        return var21;
    }

    protected void sendData(byte[] requestData, ServiceConnection connection, SoapEnvelope envelope) throws IOException {
        connection.setRequestProperty("Content-Length", "" + requestData.length);
        connection.setFixedLengthStreamingMode(requestData.length);
        OutputStream os = connection.openOutputStream();
        os.write(requestData, 0, requestData.length);
        os.flush();
        os.close();
    }

    protected void parseResponse(SoapEnvelope envelope, InputStream is, List returnedHeaders) throws XmlPullParserException, IOException {
        this.parseResponse(envelope, is);
    }

    private InputStream readDebug(InputStream is, int contentLength, File outputFile) throws IOException {
        Object bos;
        if(outputFile != null) {
            bos = new FileOutputStream(outputFile);
        } else {
            bos = new ByteArrayOutputStream(contentLength > 0?contentLength:262144);
        }

        byte[] buf = new byte[256];

        while(true) {
            int rd = is.read(buf, 0, 256);
            if(rd == -1) {
                ((OutputStream)bos).flush();
                if(bos instanceof ByteArrayOutputStream) {
                    buf = ((ByteArrayOutputStream)bos).toByteArray();
                }

                bos = null;
                this.responseDump = new String(buf);
                is.close();
                return (InputStream)(outputFile != null?new FileInputStream(outputFile):new ByteArrayInputStream(buf));
            }

            ((OutputStream)bos).write(buf, 0, rd);
        }
    }

    private InputStream getUnZippedInputStream(InputStream inputStream) throws IOException {
        try {
            return (GZIPInputStream)inputStream;
        } catch (ClassCastException var3) {
            return new GZIPInputStream(inputStream);
        }
    }

    public ServiceConnection getServiceConnection() throws IOException {
        return new ServiceConnectionSE(this.proxy, this.url, this.timeout);
    }
}
