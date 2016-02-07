/**
 * Created by Rakesh on 11/20/2015.
 */

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class queryPeerXML {
    public HashMap<String, ArrayList<String>> getPeerDetails(String fileName) {
        HashMap<String, ArrayList<String>> peerDetails = null;
        try {
            String[] peerIds = new String[100];
            String[] ips = new String[100];
            String[] ports = new String[100];
            String[] pieceLengths = new String[100];
            peerDetails = new HashMap<String, ArrayList<String>>();
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            InputSource inputSource = new InputSource("C:\\Users\\Rakesh\\IdeaProjects\\Tracker\\PeerXML\\" + fileName + ".xml");
            String name = "//file//name/text()";
            String peerId = "//peers//id/text()";
            String ip = "//peers//peer//ip/text()";
            String port = "//peers//peer//port/text()";
            String pieceLength = "//peers//peer//pieceLength/text()";
            NodeList peerIdResult = (NodeList) xpath.evaluate(peerId, inputSource, XPathConstants.NODESET);
            NodeList nameResult = (NodeList) xpath.evaluate(name, inputSource, XPathConstants.NODESET);
            NodeList ipresult = (NodeList) xpath.evaluate(ip, inputSource, XPathConstants.NODESET);
            NodeList portresult = (NodeList) xpath.evaluate(port, inputSource, XPathConstants.NODESET);
            NodeList pieceLengthresult = (NodeList) xpath.evaluate(pieceLength, inputSource, XPathConstants.NODESET);
            for (int i = 0; i < peerIdResult.getLength(); i++) {

                Node e = peerIdResult.item(i);
                if (e instanceof Text) {
                    peerIds[i] = ((Text) e).getData();
                }
            }

            for (int i = 0; i < ipresult.getLength(); i++) {
                Node e = ipresult.item(i);
                if (e instanceof Text) {
                    ips[i] = ((Text) e).getData();
                }
            }
            for (int i = 0; i < portresult.getLength(); i++) {
                Node e = portresult.item(i);
                if (e instanceof Text) {
                    ports[i] = ((Text) e).getData();
                }
            }
            for (int i = 0; i < pieceLengthresult.getLength(); i++) {
                Node e = pieceLengthresult.item(i);
                if (e instanceof Text) {
                    pieceLengths[i] = ((Text) e).getData();
                }
            }

            for (int i = 0; i < peerIdResult.getLength(); i++) {
                ArrayList<String> results = new ArrayList<String>();
                results.add(ips[i]);
                results.add(ports[i]);
                results.add(pieceLengths[i]);
                peerDetails.put(peerIds[i], results);
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return peerDetails;
    }
}
