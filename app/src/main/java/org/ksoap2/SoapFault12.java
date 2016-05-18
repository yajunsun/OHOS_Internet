//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ksoap2;

import java.io.IOException;
import org.ksoap2.SoapFault;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SoapFault12 extends SoapFault {
    private static final long serialVersionUID = 1012001L;
    public Node Code;
    public Node Reason;
    public Node Node;
    public Node Role;
    public Node Detail;

    public SoapFault12() {
        this.version = 120;
    }

    public SoapFault12(int version) {
        this.version = version;
    }

    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        this.parseSelf(parser);
        this.faultcode = this.Code.getElement("http://www.w3.org/2003/05/soap-envelope", "Value").getText(0);
        this.faultstring = this.Reason.getElement("http://www.w3.org/2003/05/soap-envelope", "Text").getText(0);
        this.detail = this.Detail;
        this.faultactor = null;
    }

    private void parseSelf(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, "http://www.w3.org/2003/05/soap-envelope", "Fault");

        String name;
        String namespace;
        for(; parser.nextTag() == 2; parser.require(3, namespace, name)) {
            name = parser.getName();
            namespace = parser.getNamespace();
            parser.nextTag();
            if(name.toLowerCase().equals("Code".toLowerCase())) {
                this.Code = new Node();
                this.Code.parse(parser);
            } else if(name.toLowerCase().equals("Reason".toLowerCase())) {
                this.Reason = new Node();
                this.Reason.parse(parser);
            } else if(name.toLowerCase().equals("Node".toLowerCase())) {
                this.Node = new Node();
                this.Node.parse(parser);
            } else if(name.toLowerCase().equals("Role".toLowerCase())) {
                this.Role = new Node();
                this.Role.parse(parser);
            } else {
                if(!name.toLowerCase().equals("Detail".toLowerCase())) {
                    throw new RuntimeException("unexpected tag:" + name);
                }

                this.Detail = new Node();
                this.Detail.parse(parser);
            }
        }

        parser.require(3, "http://www.w3.org/2003/05/soap-envelope", "Fault");
        parser.nextTag();
    }

    public void write(XmlSerializer xw) throws IOException {
        xw.startTag("http://www.w3.org/2003/05/soap-envelope", "Fault");
        xw.startTag("http://www.w3.org/2003/05/soap-envelope", "Code");
        this.Code.write(xw);
        xw.endTag("http://www.w3.org/2003/05/soap-envelope", "Code");
        xw.startTag("http://www.w3.org/2003/05/soap-envelope", "Reason");
        this.Reason.write(xw);
        xw.endTag("http://www.w3.org/2003/05/soap-envelope", "Reason");
        if(this.Node != null) {
            xw.startTag("http://www.w3.org/2003/05/soap-envelope", "Node");
            this.Node.write(xw);
            xw.endTag("http://www.w3.org/2003/05/soap-envelope", "Node");
        }

        if(this.Role != null) {
            xw.startTag("http://www.w3.org/2003/05/soap-envelope", "Role");
            this.Role.write(xw);
            xw.endTag("http://www.w3.org/2003/05/soap-envelope", "Role");
        }

        if(this.Detail != null) {
            xw.startTag("http://www.w3.org/2003/05/soap-envelope", "Detail");
            this.Detail.write(xw);
            xw.endTag("http://www.w3.org/2003/05/soap-envelope", "Detail");
        }

        xw.endTag("http://www.w3.org/2003/05/soap-envelope", "Fault");
    }

    public String getMessage() {
        return this.Reason.getElement("http://www.w3.org/2003/05/soap-envelope", "Text").getText(0);
    }

    public String toString() {
        String reason = this.Reason.getElement("http://www.w3.org/2003/05/soap-envelope", "Text").getText(0);
        String code = this.Code.getElement("http://www.w3.org/2003/05/soap-envelope", "Value").getText(0);
        return "Code: " + code + ", Reason: " + reason;
    }
}
