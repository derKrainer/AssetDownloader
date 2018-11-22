package parser.dash.subcomponents.segmentlistcomponents;

import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;
import util.URLUtils;

public class Initialization extends DashComponent
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
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    for (Node n : specialNodes)
    {
      System.out.println("Unexpeted child of Initialization: " + n.getNodeName());
    }
  }

  @Override
  protected void fillMissingValues() {
    // nothing
  }

  public DownloadTarget toDownloadTarget(String manifestLocation, String targetFolder, DashRepresentation rep)
  {
    String serverURL = URLUtils.makeAbsoulte(this.url, manifestLocation);
    String localPath = rep.generateDirectoryPath(targetFolder);

    localPath += this.url.substring(this.url.lastIndexOf('/'));

    return new DownloadTarget(serverURL, localPath);
  }
}