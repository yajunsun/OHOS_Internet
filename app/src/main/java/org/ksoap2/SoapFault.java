//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2;

import java.io.IOException;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapFault extends IOException {
    private static final long serialVersionUID = 1011001L;
    public String faultcode;
    public String faultstring;
    public String faultactor;
    public Node detail;
    public int version;

    public SoapFault() {
        this.version = 110;
    }

    public SoapFault(int version) {
        this.version = version;
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, "http://schemas.xmlsoap.org/soap/envelope/", "Fault");

        while(parser.nextTag() == 2) {
            String name = parser.getName();
            if(name.equals("detail")) {
                this.detail = new Node();
                this.detail.parse(parser);
                if(parser.getNamespace().equals("http://schemas.xmlsoap.org/soap/envelope/") && parser.getName().equals("Fault")) {
                    break;
                }
            } else {
                if(name.equals("faultcode")) {
                    this.faultcode = parser.nextText();
                } else if(name.equals("faultstring")) {
                    this.faultstring = parser.nextText();
                } else {
                    if(!name.equals("faultactor")) {
                        throw new RuntimeException("unexpected tag:" + name);
                    }

                    this.faultactor = parser.nextText();
                }

                parser.require(3, (String)null, name);
            }
        }

        parser.require(3, "http://schemas.xmlsoap.org/soap/envelope/", "Fault");
        parser.nextTag();
    }

    public void write(XmlSerializer xw) throws IOException {
        xw.startTag("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
        xw.startTag((String)null, "faultcode");
        xw.text("" + this.faultcode);
        xw.endTag((String)null, "faultcode");
        xw.startTag((String)null, "faultstring");
        xw.text("" + this.faultstring);
        xw.endTag((String)null, "faultstring");
        xw.startTag((String)null, "detail");
        if(this.detail != null) {
            this.detail.write(xw);
        }

        xw.endTag((String)null, "detail");
        xw.endTag("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
    }

    public String getMessage() {
        return this.faultstring;
    }

    public String toString() {
        return "SoapFault - faultcode: \'" + this.faultcode + "\' faultstring: \'" + this.faultstring + "\' faultactor: \'" + this.faultactor + "\' detail: " + this.detail;
    }
}
