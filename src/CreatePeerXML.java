/**
 * Created by Rakesh on 11/18/2015.
 */

import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FilenameUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreatePeerXML {

    //    public static void main(String argv[]) {
    public void createXmlTree(ArrayList<String> peerDetails) {
        try {
            String filename = peerDetails.get(0);
            String PeerID = peerDetails.get(1);
            String IP = peerDetails.get(2);
            String PORT = peerDetails.get(3);
            String PieceLength = peerDetails.get(4);


            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("FileList");
            doc.appendChild(rootElement);

            // staff elements
            Element staff = doc.createElement("file");
            rootElement.appendChild(staff);

            // set attribute to staff element
            Attr attr = doc.createAttribute("name");
            attr.setValue(filename);
            staff.setAttributeNode(attr);

            // shorten way
            // staff.setAttribute("id", "1");

            // firstname elements
            Element peers = doc.createElement("peers");
            Element peer = doc.createElement("peer");
            peers.appendChild(peer);

            Attr peerId = doc.createAttribute("id");
            peerId.setValue(PeerID);
            peer.setAttributeNode(peerId);

            Element peerID = doc.createElement("id");
            Element ip = doc.createElement("ip");
            Element port = doc.createElement("port");
            Element pieceLength = doc.createElement("pieceLength");

            peerID.appendChild(doc.createTextNode(PeerID));
            ip.appendChild(doc.createTextNode(IP));
            port.appendChild(doc.createTextNode(PORT));
            pieceLength.appendChild(doc.createTextNode(PieceLength));

            peer.appendChild(peerID);
            peer.appendChild(ip);
            peer.appendChild(port);
            peer.appendChild(pieceLength);
            rootElement.appendChild(peers);
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:\\Users\\Rakesh\\IdeaProjects\\Tracker\\PeerXML\\"+FilenameUtils.getBaseName(filename)+".xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            System.out.println("Torrent created!");

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
    }
}
