package parser.dash.subcomponents.segmentlistcomponents;

import java.util.List;

import org.w3c.dom.Node;

public class Initialization extends AbstractSegmentListComponent
{
  public String url;
  public Node sourceUrlNode;

  public Initialization(Node initNode)
  {
    super(initNode);
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("sourceURL"))
      {
        this.url = attr.getNodeValue();
        this.sourceUrlNode = attr;
      }
      else
      {
        System.out.println("Unknown Initialization attribute: " + attr.getNodeName());
      }
    }  
  }

  @Override
  protected String getUrl() {
    return this.url;
  }

  @Override
  protected Node getUrlNode() {
    return this.sourceUrlNode;
  }
}