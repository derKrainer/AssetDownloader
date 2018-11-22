package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;
import util.URLUtils;

public class SegmentBase extends DashComponent
{
  public SegmentBase(Node xmlContent)
  {
    super(xmlContent);
  }

  @Override
  protected void fillMissingValues() {
    // nah
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList) {
    // nah
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) {
    // nah
  }

  public List<DownloadTarget> getTargetFiles(String manifestLocation, String targetFolder, DashRepresentation rep)
  {
    List<DownloadTarget> singleFile = new ArrayList<>();
    BaseUrl baseUrl = rep.getBaseUrl();
    if (baseUrl == null)
    {
      throw new RuntimeException("Missing base URL in SegmentBase");
    }
    String serverUrl = URLUtils.makeAbsoulte(baseUrl.baseUrl, manifestLocation);
    String localPath = rep.generateDirectoryPath(targetFolder) + baseUrl.baseUrl.substring(baseUrl.baseUrl.lastIndexOf('/'));
    singleFile.add(new DownloadTarget(serverUrl, localPath));

    return singleFile;
  }

  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    // nothing to do here as this handled in the BaseURL (soooo far)
  }
}