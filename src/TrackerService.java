import com.sun.org.apache.xpath.internal.SourceTree;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by Rakesh on 11/14/2015.
 */
public class TrackerService implements Runnable {
    private static Socket trackerServiceSocket;
    static String xmlPath = "C:\\Users\\Rakesh\\IdeaProjects\\Tracker\\PeerXML\\";

    public TrackerService(Socket socket) {
        this.trackerServiceSocket = socket;
    }

    @Override
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    private static synchronized void processRequest() throws IOException {
        /*
        read the tracker request from peers which is typically of this format
        http://some.tracker.com:999/announce
        ?info_hash=12345678901234567890
        &peer_id=ABCDEFGHIJKLMNOPQRST
        &ip=255.255.255.255
        &port=6881
        &downloaded=1234
        &left=98765
        &event=stopped
        */
        CreatePeerXML createPeerXML = new CreatePeerXML();
        queryPeerXML queryPeerXML = new queryPeerXML();
        BufferedReader br = new BufferedReader(new InputStreamReader(trackerServiceSocket.getInputStream()));
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(trackerServiceSocket.getOutputStream());
        String requestLine = br.readLine();
        System.out.println("request from peer----" + requestLine);
        String[] requestLineParts = requestLine.split(",");
//        for (String requestLinePart : requestLineParts) {
//            System.out.println(requestLinePart);
//        }

        if (requestLineParts[0].equalsIgnoreCase("register")) {
            System.out.println("Tracker request received from peer to register itself to the swamp");
            System.out.println(requestLine);
            ArrayList<String> peerDetails = new ArrayList<String>();
            peerDetails.add(0, requestLineParts[1]);
            peerDetails.add(1, requestLineParts[2]);
            peerDetails.add(2, requestLineParts[3]);
            peerDetails.add(3, requestLineParts[4]);
            peerDetails.add(4, requestLineParts[5]);
            createPeerXML.createXmlTree(peerDetails);
        }
        //request for peer details from another peer
        else if (requestLineParts[0].equalsIgnoreCase("requesting")) {
            String fileName[] = requestLineParts[1].split(":");
            String peerId = requestLineParts[2];
            System.out.println("Peer request received from peer:" + peerId + "for file" + fileName[1]);
            HashMap<String, ArrayList<String>> peerDetails = queryPeerXML.getPeerDetails(FilenameUtils.getBaseName(fileName[1]));
            //construct a tracker response
            objectOutputStream.writeObject(peerDetails);
            objectOutputStream.flush();
            System.out.println("Tracker response sent to peer");
            System.out.println("peer details");
            Iterator it = peerDetails.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
        } else if (requestLineParts[0].equalsIgnoreCase("updating")) {
            System.out.println("Tracker request received from peer to update xml");
            try {
                String Path = xmlPath + FilenameUtils.getBaseName(requestLineParts[1]) + ".xml";
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder;
                docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new File(Path));
                //get the peerid
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xpath = xpathFactory.newXPath();
                InputSource inputSource = new InputSource(Path);
                String peersId = requestLineParts[2];
//                String[] peerIds = new String[1];
                String[] piecesLength = new String[1];
                String xpathQueryForPeerId = "/FileList/peers/peer/id[text()=\'%s\']";
                String xpathQueryForPeerIdResult = String.format(xpathQueryForPeerId, peersId);
//                String tempPeerId = "/FileList/peers/peer/id[text()='Peer2']";
                System.out.println(xpathQueryForPeerIdResult);
                NodeList peerIdResult = (NodeList) xpath.evaluate(xpathQueryForPeerIdResult, inputSource, XPathConstants.NODESET);
                /*for (int i = 0; i < peerIdResult.getLength(); i++) {
                    Node e = peerIdResult.item(i);
                    if (e instanceof Text) {
                        peerIds[i] = ((Text) e).getData();
                    }
                }*/
                System.out.println(peerIdResult.getLength());
                if (peerIdResult.getLength() >= 1) {
                    System.out.println("inside if");
                    //peers//peer[id="Peer2"]/pieceLength
                    String xpathQueryForPeerLength = "//peers//peer[id=\"%s\"]/pieceLength/text()";
                    String xpathQueryForPeerLengthResult = String.format(xpathQueryForPeerLength, peersId);
//                    String tempPieceLength = "//peers//peer[id=\"Peer2\"]/pieceLength/text()";
                    System.out.println(xpathQueryForPeerLengthResult);
                    NodeList pieceLengthResult = (NodeList) xpath.evaluate(xpathQueryForPeerLengthResult, inputSource, XPathConstants.NODESET);
                    for (int i = 0; i < pieceLengthResult.getLength(); i++) {
                        Node e = pieceLengthResult.item(i);
                        if (e instanceof Text) {
                            piecesLength[i] = ((Text) e).getData();
                        }
                    }
                    String temp = piecesLength[0];
                    System.out.println(temp);
                    int s = Integer.parseInt(temp) + 1;
                    System.out.println("new length" + s);
                    Node node = (Node) xpath.evaluate(xpathQueryForPeerLengthResult, doc, XPathConstants.NODE);
                    // Set the node content
                    node.setTextContent(String.valueOf(s));

                    // Write changes to a file
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.transform(new DOMSource(doc), new StreamResult(new File(Path)));
//                    queryPeerXML.update(doc, "//peers//peer[id=\"Peer2\"]/pieceLength/",Integer.toString(s));

                } else {
                    System.out.println("inside else");
                    Node peers = doc.getFirstChild().getLastChild().getPreviousSibling();
                    Element peer = doc.createElement("peer");
                    peers.appendChild(peer);

                    Attr peerId = doc.createAttribute("id");
                    peerId.setValue(requestLineParts[2]);
                    peer.setAttributeNode(peerId);

                    Element peerID = doc.createElement("id");
                    Element ip = doc.createElement("ip");
                    Element port = doc.createElement("port");
                    Element pieceLength = doc.createElement("pieceLength");

                    peerID.appendChild(doc.createTextNode(requestLineParts[2]));
                    ip.appendChild(doc.createTextNode(requestLineParts[3]));
                    port.appendChild(doc.createTextNode(requestLineParts[4]));
                    pieceLength.appendChild(doc.createTextNode(requestLineParts[5]));

                    peer.appendChild(peerID);
                    peer.appendChild(ip);
                    peer.appendChild(port);
                    peer.appendChild(pieceLength);
                    peers.appendChild(peer);
                    // write the content into xml file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File("C:\\Users\\Rakesh\\IdeaProjects\\Tracker\\PeerXML\\" + FilenameUtils.getBaseName(requestLineParts[1]) + ".xml"));
                    transformer.transform(source, result);

                    System.out.println("XML updated with the new peer details");
                }
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
        }
    }
}

