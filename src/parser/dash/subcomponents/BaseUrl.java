package parser.dash.subcomponents;

import java.util.List;

import org.w3c.dom.Node;

import parser.dash.DashComponent;
import parser.dash.DashRepresentation;

public class BaseUrl extends DashComponent
{
  public String baseUrl;

  public BaseUrl(Node xmlContent)
  {
    super(xmlContent);
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    this.baseUrl = this.textContent;
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
  }

  @Override
  protected void fillMissingValues()
  {
    // nothing to do here
  }

  @Override
  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    super.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);

    String newBaseUrl = targetRepresentation.generateDirectoryPath(targetFolder);
    if (this.baseUrl.charAt(this.baseUrl.length() - 1) != '/')
    {
      newBaseUrl += this.baseUrl.substring(this.baseUrl.lastIndexOf('/') - 1);
    }

    this.textContentNode.setNodeValue(newBaseUrl);
  }

}
