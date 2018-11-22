package parser.dash.subcomponents.segmentlistcomponents;

import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;
import util.URLUtils;

public abstract class AbstractSegmentListComponent extends DashComponent
{
  public AbstractSegmentListComponent(Node xmlNode)
  {
    super(xmlNode);
  }

  @Override
  protected void fillMissingValues() {
    // nothing
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    for (Node n : specialNodes)
    {
      System.out.println("Unexpeted child of " + this.getClass().getName() +  ": " + n.getNodeName());
    }
  }

  public DownloadTarget toDownloadTarget(String manifestLocation, String targetFolder, DashRepresentation rep)
  {
    String serverURL = URLUtils.makeAbsoulte(this.getUrl(), manifestLocation);
    String localPath = rep.generateDirectoryPath(targetFolder);

    localPath += this.getUrl().substring(this.getUrl().lastIndexOf('/'));

    return new DownloadTarget(serverURL, localPath);
  }

  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation rep)
  {
    String localPath = rep.generateDirectoryPath(targetFolder);
    String serverPath = this.getUrl();
    localPath += serverPath.substring(serverPath.lastIndexOf('/'));
    this.getUrlNode().setNodeValue(localPath);
  }

  protected abstract String getUrl();

  protected abstract Node getUrlNode();
}