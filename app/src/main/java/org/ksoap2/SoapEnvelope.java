//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2;

import java.io.IOException;
import org.ksoap2.SoapFault;
import org.ksoap2.SoapFault12;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapEnvelope {
    public static final int VER10 = 100;
    public static final int VER11 = 110;
    public static final int VER12 = 120;
    public static final String ENV2003 = "http://www.w3.org/2003/05/soap-envelope";
    public static final String ENC2003 = "http://www.w3.org/2003/05/soap-encoding";
    public static final String ENV = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String ENC = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String XSD = "http://www.w3.org/2001/XMLSchema";
    public static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSD1999 = "http://www.w3.org/1999/XMLSchema";
    public static final String XSI1999 = "http://www.w3.org/1999/XMLSchema-instance";
    public Object bodyIn;
    public Object bodyOut;
    public Element[] headerIn;
    public Element[] headerOut;
    public String encodingStyle;
    public int version;
    public String env;
    public String enc;
    public String xsi;
    public String xsd;

    public static boolean stringToBoolean(String booleanAsString) {
        if(booleanAsString == null) {
            return false;
        } else {
            booleanAsString = booleanAsString.trim().toLowerCase();
            return booleanAsString.equals("1") || booleanAsString.equals("true");
        }
    }

    public SoapEnvelope(int version) {
        this.version = version;
        if(version == 100) {
            this.xsi = "http://www.w3.org/1999/XMLSchema-instance";
            this.xsd = "http://www.w3.org/1999/XMLSchema";
        } else {
            this.xsi = "http://www.w3.org/2001/XMLSchema-instance";
            this.xsd = "http://www.w3.org/2001/XMLSchema";
        }

        if(version < 120) {
            this.enc = "http://schemas.xmlsoap.org/soap/encoding/";
            this.env = "http://schemas.xmlsoap.org/soap/envelope/";
        } else {
            this.enc = "http://www.w3.org/2003/05/soap-encoding";
            this.env = "http://www.w3.org/2003/05/soap-envelope";
        }

    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.nextTag();
        parser.require(2, this.env, "Envelope");
        this.encodingStyle = parser.getAttributeValue(this.env, "encodingStyle");
        parser.nextTag();
        if(parser.getEventType() == 2 && parser.getNamespace().equals(this.env) && parser.getName().equals("Header")) {
            this.parseHeader(parser);
            parser.require(3, this.env, "Header");
            parser.nextTag();
        }

        parser.require(2, this.env, "Body");
        this.encodingStyle = parser.getAttributeValue(this.env, "encodingStyle");
        this.parseBody(parser);
        parser.require(3, this.env, "Body");
        parser.nextTag();
        parser.require(3, this.env, "Envelope");
    }

    public void parseHeader(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.nextTag();
        Node headers = new Node();
        headers.parse(parser);
        int count = 0;

        int i;
        Element child;
        for(i = 0; i < headers.getChildCount(); ++i) {
            child = headers.getElement(i);
            if(child != null) {
                ++count;
            }
        }

        this.headerIn = new Element[count];
        count = 0;

        for(i = 0; i < headers.getChildCount(); ++i) {
            child = headers.getElement(i);
            if(child != null) {
                this.headerIn[count++] = child;
            }
        }

    }

    public void parseBody(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.nextTag();
        if(parser.getEventType() == 2 && parser.getNamespace().equals(this.env) && parser.getName().equals("Fault")) {
            Object node1;
            if(this.version < 120) {
                node1 = new SoapFault(this.version);
            } else {
                node1 = new SoapFault12(this.version);
            }

            ((SoapFault)node1).parse(parser);
            this.bodyIn = node1;
        } else {
            Node node = this.bodyIn instanceof Node?(Node)this.bodyIn:new Node();
            node.parse(parser);
            this.bodyIn = node;
        }

    }

    public void write(XmlSerializer writer) throws IOException {
        writer.setPrefix("i", this.xsi);
        writer.setPrefix("d", this.xsd);
        writer.setPrefix("c", this.enc);
        writer.setPrefix("v", this.env);
        writer.startTag(this.env, "Envelope");
        writer.startTag(this.env, "Header");
        this.writeHeader(writer);
        writer.endTag(this.env, "Header");
        writer.startTag(this.env, "Body");
        this.writeBody(writer);
        writer.endTag(this.env, "Body");
        writer.endTag(this.env, "Envelope");
    }

    public void writeHeader(XmlSerializer writer) throws IOException {
        if(this.headerOut != null) {
            for(int i = 0; i < this.headerOut.length; ++i) {
                this.headerOut[i].write(writer);
            }
        }

    }

    public void writeBody(XmlSerializer writer) throws IOException {
        if(this.encodingStyle != null) {
            writer.attribute(this.env, "encodingStyle", this.encodingStyle);
        }

        ((Node)this.bodyOut).write(writer);
    }

    public void setOutputSoapObject(Object soapObject) {
        this.bodyOut = soapObject;
    }
}
