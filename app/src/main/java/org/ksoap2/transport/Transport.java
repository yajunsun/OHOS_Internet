//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2.transport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.transport.ServiceConnection;
import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParserException;

public abstract class Transport {
    protected Proxy proxy;
    protected String url;
    protected int timeout;
    public boolean debug;
    public String requestDump;
    public String responseDump;
    private String xmlVersionTag;
    protected static final String CONTENT_TYPE_XML_CHARSET_UTF_8 = "text/xml;charset=utf-8";
    protected static final String CONTENT_TYPE_SOAP_XML_CHARSET_UTF_8 = "application/soap+xml;charset=utf-8";
    protected static final String USER_AGENT = "ksoap2-android/2.6.0+";
    private int bufferLength;
    private HashMap prefixes;

    public HashMap getPrefixes() {
        return this.prefixes;
    }

    public Transport() {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.prefixes = new HashMap();
    }

    public Transport(String url) {
        this((Proxy)null, url);
    }

    public Transport(String url, int timeout) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.prefixes = new HashMap();
        this.url = url;
        this.timeout = timeout;
    }

    public Transport(String url, int timeout, int bufferLength) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.prefixes = new HashMap();
        this.url = url;
        this.timeout = timeout;
        this.bufferLength = bufferLength;
    }

    public Transport(Proxy proxy, String url) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.prefixes = new HashMap();
        this.proxy = proxy;
        this.url = url;
    }

    public Transport(Proxy proxy, String url, int timeout) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.prefixes = new HashMap();
        this.proxy = proxy;
        this.url = url;
        this.timeout = timeout;
    }

    public Transport(Proxy proxy, String url, int timeout, int bufferLength) {
        this.timeout = 20000;
        this.xmlVersionTag = "";
        this.bufferLength = 262144;
        this.prefixes = new HashMap();
        this.proxy = proxy;
        this.url = url;
        this.timeout = timeout;
        this.bufferLength = bufferLength;
    }

    protected void parseResponse(SoapEnvelope envelope, InputStream is) throws XmlPullParserException, IOException {
        KXmlParser xp = new KXmlParser();
        xp.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
        xp.setInput(is, (String)null);
        envelope.parse(xp);
        is.close();
    }

    protected byte[] createRequestData(SoapEnvelope envelope, String encoding) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(this.bufferLength);
        Object result = null;
        bos.write(this.xmlVersionTag.getBytes());
        KXmlSerializer xw = new KXmlSerializer();
        Iterator keysIter = this.prefixes.keySet().iterator();
        xw.setOutput(bos, encoding);

        while(keysIter.hasNext()) {
            String key = (String)keysIter.next();
            xw.setPrefix(key, (String)this.prefixes.get(key));
        }

        envelope.write(xw);
        xw.flush();
        bos.write(13);
        bos.write(10);
        bos.flush();
        byte[] result1 = bos.toByteArray();
        xw = null;
        bos = null;
        return result1;
    }

    protected byte[] createRequestData(SoapEnvelope envelope) throws IOException {
        return this.createRequestData(envelope, (String)null);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setXmlVersionTag(String tag) {
        this.xmlVersionTag = tag;
    }

    public void reset() {
    }

    public abstract List call(String var1, SoapEnvelope var2, List var3) throws IOException, XmlPullParserException;

    public abstract List call(String var1, SoapEnvelope var2, List var3, File var4) throws IOException, XmlPullParserException;

    public void call(String soapAction, SoapEnvelope envelope) throws IOException, XmlPullParserException {
        this.call(soapAction, envelope, (List)null);
    }

    public String getHost() throws MalformedURLException {
        return (new URL(this.url)).getHost();
    }

    public int getPort() throws MalformedURLException {
        return (new URL(this.url)).getPort();
    }

    public String getPath() throws MalformedURLException {
        return (new URL(this.url)).getPath();
    }

    public abstract ServiceConnection getServiceConnection() throws IOException;
}
