package parser.dash.subcomponents;

import java.util.List;

import org.w3c.dom.Node;

import parser.dash.DashAdaptationSet;
import parser.dash.DashComponent;
import parser.dash.DashPeriod;
import parser.dash.DashRepresentation;

public class BaseUrl extends DashComponent
{
  public String baseUrl;

  public DashComponent parent;

  public BaseUrl(Node xmlContent, DashComponent parent)
  {
    super(xmlContent);
    this.parent = parent;
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

    BaseUrl possibleParentBaseUrl = null;
    if (this.parent instanceof DashRepresentation)
    {
      DashRepresentation repParent = (DashRepresentation)this.parent;
      possibleParentBaseUrl = repParent.parent.getBaseUrl();
    }
    else if (this.parent instanceof DashAdaptationSet)
    {
      DashAdaptationSet adSetParent = (DashAdaptationSet)parent;
      newBaseUrl = adSetParent.generateDirectoryPath(targetFolder);
      possibleParentBaseUrl = adSetParent.parent.baseUrl;
    }
    else if (this.parent instanceof DashPeriod)
    {
      DashPeriod periodParent = (DashPeriod)parent;
      newBaseUrl = targetFolder + periodParent.id;
    }

    if (possibleParentBaseUrl != null)
    {
      possibleParentBaseUrl.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
      // ensure we stay relaative to the parent BaseURL
      String parentPath = possibleParentBaseUrl.baseUrl;
      if (newBaseUrl.startsWith(parentPath))
      {
        newBaseUrl = '.' + newBaseUrl.substring(parentPath.length());
      }
    }

    this.baseUrl = newBaseUrl;
    this.textContentNode.setNodeValue(newBaseUrl);
  }

}
