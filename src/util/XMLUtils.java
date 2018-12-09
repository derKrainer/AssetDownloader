package util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLUtils
{

  /**
   * https://stackoverflow.com/questions/5456680/xml-document-to-string
   */
  public static String writeXmlToString(Node node)
  {
    try
    {
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(node), new StreamResult(writer));
      String output = writer.getBuffer().toString();
      return output;
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Error during writing xml file", ex);
    }
  }

  public static Document parseXml(String xmlContent)
  {
    try
    {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlContent)));
    }
    catch (SAXException e)
    {
      throw new RuntimeException("Invalid XML encountered", e);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Unexpected error during xml parsing", e);
    }
  }
}