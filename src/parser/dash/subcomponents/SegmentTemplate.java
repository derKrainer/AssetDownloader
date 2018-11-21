package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;

public class SegmentTemplate extends DashComponent
{
  public String mediaUrl;
  public String initUrl;
  public int startNumber;
  public int duration;
  public int timescale;

  public SegmentTemplate(Node xmlNode)
  {
    super(xmlNode);
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) {
    for (Node childNode : specialNodes)
    {
      System.out.println("Unhandled SegmentTemplateNode: " + childNode);
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList) {
    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("media"))
      {
        this.mediaUrl = attr.getNodeValue();
      } else if (attr.getNodeName().equals("initialization"))
      {
        this.initUrl = attr.getNodeValue();
      } else if (attr.getNodeName().equals("startNumber"))
      {
        this.startNumber = Integer.parseInt(attr.getNodeValue());
      } else if (attr.getNodeName().equals("duration"))
      {
        this.duration = Integer.parseInt(attr.getNodeValue());
      } else if (attr.getNodeName().equals("timescale"))
      {
        this.timescale = Integer.parseInt(attr.getNodeValue());
      } else
      {
        System.out.println("Unknown SegmentTemplate attribute: " + attr.getNodeName());
      }
    }
  }

  public List<DownloadTarget> getTargetFiles(DashRepresentation rep, String baseUrl) {
    List<DownloadTarget> allFiles = new ArrayList<>();
    allFiles.add(new DownloadTarget(convertToDownloadUrl(initUrl, 0, rep, baseUrl), getTargetFileForUrl(initUrl, rep)));
    double segmentDuration = this.duration / this.timescale;
    double streamDuration = rep.parent.parent.getDuration();

    double numberOfSegments = Math.ceil(streamDuration / segmentDuration);

    for (int i = this.startNumber; i < this.startNumber + numberOfSegments; i++)
    {
      String dlUrl = convertToDownloadUrl(this.mediaUrl, i, rep, baseUrl);
      allFiles.add(new DownloadTarget(dlUrl, getTargetFileForUrl(dlUrl, rep)));
    }

    return allFiles;
  }

  private String convertToDownloadUrl(String url, int index, DashRepresentation rep, String baseUrl) {
    return makeUrlAbsoute(replacePlaceholders(url, index, rep), baseUrl);
  }

  private String makeUrlAbsoute(String segmentUrl, String baseUrl) {
    if (segmentUrl.startsWith("http") || segmentUrl.startsWith("//")) {
      // already absolute
      return segmentUrl;
    }
    
    StringBuilder sb = new StringBuilder(baseUrl);
    if (sb.charAt(sb.length() -1) != '/') {
      sb.append('/');
    }
    sb.append(segmentUrl);
    return sb.toString();
  }

  private String replacePlaceholders(String url, int index, DashRepresentation rep) {
    return url.replace("$Number$", Integer.toString(index)).replace("$RepresentationID$", rep.id);
  }

  private String getTargetFileForUrl(String url, DashRepresentation rep) {
    return rep.parent.parent.id + '/' + rep.parent.id + '/' + rep.id + '/' + url.substring(url.lastIndexOf('/') + 1);
  }

  @Override
  protected void fillMissingValues() {
    // nothing to do here
  }
}
