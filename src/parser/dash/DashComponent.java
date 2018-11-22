package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import parser.dash.subcomponents.BaseUrl;

public abstract class DashComponent
{
  public Node xmlContent;

  public String id = null;

  public BaseUrl baseUrl;

  public String textContent;
  public Node textContentNode;

  public DashComponent(Node xmlNode)
  {
    this.xmlContent = xmlNode;
  }

  public final void parse()
  {
    List<Node> specialAttributes = this.parseGeneralAttributes();

    this.parseAttributes(specialAttributes);

    List<Node> specialNodes = this.parseGeneralNodes();
    this.parseSpecialNodes(specialNodes);

    this.fillMissingValues();
  }

  /**
   * parses all sub-nodes
   */
  protected abstract void parseSpecialNodes(List<Node> specialNodes);

  /**
   * parse all non-general attributes
   */
  protected abstract void parseAttributes(List<Node> specialAttributesList);

  /**
   * assign default values to everything that is missing (eg.: ID)
   */
  protected abstract void fillMissingValues();

  protected List<Node> parseGeneralAttributes()
  {
    List<Node> specialNodes = new ArrayList<>();

    NamedNodeMap attributes = this.xmlContent.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++)
    {
      Node attr = attributes.item(i);

      if (attr.getNodeName().equals("id"))
      {
        this.id = attr.getNodeValue();
      }
      else
      {
        specialNodes.add(attr);
      }
    }

    return specialNodes;
  }

  protected List<Node> parseGeneralNodes()
  {
    List<Node> unprocessed = new ArrayList<>();
    NodeList children = this.xmlContent.getChildNodes();

    for (int i = 0; i < children.getLength(); i++)
    {
      Node child = children.item(i);

      if (child.getNodeName().equals("BaseURL"))
      {
        this.baseUrl = new BaseUrl(child);
        this.baseUrl.parse();
      }
      else if (child.getNodeName().equals("#text"))
      {
        this.textContent = child.getNodeValue();
        if (this.textContent != null)
        {
          this.textContent = this.textContent.trim();
        }
        this.textContentNode = child;
      }
      else
      {
        unprocessed.add(child);
      }
    }

    return unprocessed;
  }

  public boolean removeChild(DashComponent toRemove)
  {
    return this.xmlContent.removeChild(toRemove.xmlContent) != null;
  }

  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    if (this.baseUrl != null)
    {
      this.baseUrl.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    }
  }
}
