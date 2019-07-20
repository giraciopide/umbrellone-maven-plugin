package dev.dimlight.umbrellone.shade;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Helpers to render shade plugin configuration xml fragment.
 *
 * @author Marco Nicolini
 */
public class Xml {

    private Xml() {}

    public static String render(ShadePluginConfiguration conf) {
        final String configuration = "<configuration>" +
                renderArtifactSet(conf.getIncludedArtifacts()) +
                renderRules(conf.getRelocationRules()) +
                "</configuration>";

        return prettyPrint(configuration, 4);
    }

    private static String renderConfiguration(Iterable<Artifact> artifacts, Iterable<RelocationRule> relocationRules) {
        final String configuration = "<configuration>" +
                    renderArtifactSet(artifacts) +
                    renderRules(relocationRules) +
                "</configuration>";

        return prettyPrint(configuration, 4);
    }

    private static String renderArtifactSet(Iterable<Artifact> artifacts) {
        final StringBuilder out = new StringBuilder("<artifactSet>\n").append("<includes>\n");

        for (Artifact artifact : artifacts) {
            out.append("<include>").append(artifact.getGroupId()).append(":").append(artifact.getArtifactId()).append("</include>").append("\n");
        }

        return out.append("</includes>\n")
                .append("</artifactSet>").toString();
    }

    private static String render(DependencyNode dependency) {
        return "<include>" + dependency.getArtifact().getGroupId() + ":" + dependency.getArtifact().getArtifactId() + "</include>";
    }

    private static String render(RelocationRule rule) {
        return "<relocation> <pattern>" + rule.pattern + "</pattern> <shadedPattern>" + rule.shadedPattern + "</shadedPattern> </relocation>";
    }

    private static String renderRules(Iterable<RelocationRule> rules) {
        final StringBuilder out = new StringBuilder("<relocations>\n");

        for (RelocationRule rule : rules) {
            out.append(render(rule)).append("\n");
        }

        return out.append("</relocations>").toString();
    }

    private static String prettyPrint(String xml, int indent) {
        // stolen from https://stackoverflow.com/questions/25864316/pretty-print-xml-in-java-8
        try {
            // Turn xml string into a document
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));

            // Remove whitespaces outside tags
            document.normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",
                    document,
                    XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            // Setup pretty print options
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Return pretty print xml string
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
