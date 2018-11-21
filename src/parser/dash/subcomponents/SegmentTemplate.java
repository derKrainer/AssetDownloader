package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;
import util.URLUtils;

public class SegmentTemplate extends DashComponent
{
  public String mediaUrl;
  public Node mediaUrlNode;
  public String initUrl;
  public Node initUrlNode;
  public int startNumber;
  public int duration;
  public int timescale;

  public SegmentTemplate(Node xmlNode)
  {
    super(xmlNode);
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    for (Node childNode : specialNodes)
    {
      System.out.println("Unhandled SegmentTemplateNode: " + childNode);
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("media"))
      {
        this.mediaUrl = attr.getNodeValue();
        this.mediaUrlNode = attr;
      } else if (attr.getNodeName().equals("initialization"))
      {
        this.initUrl = attr.getNodeValue();
        this.initUrlNode = attr;
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

  public List<DownloadTarget> getTargetFiles(DashRepresentation rep, String baseUrl, String targetFolder)
  {
    List<DownloadTarget> allFiles = new ArrayList<>();
    allFiles.add(new DownloadTarget(convertToDownloadUrl(initUrl, 0, rep, baseUrl),
        getTargetFileForUrl(initUrl, rep, targetFolder)));
    double segmentDuration = (double) this.duration / this.timescale;
    double streamDuration = rep.parent.parent.getDuration();

    double numberOfSegments = Math.ceil(streamDuration / segmentDuration);

    for (int i = this.startNumber; i < this.startNumber + numberOfSegments; i++)
    {
      String dlUrl = convertToDownloadUrl(this.mediaUrl, i, rep, baseUrl);
      allFiles.add(new DownloadTarget(dlUrl, getTargetFileForUrl(dlUrl, rep, targetFolder)));
    }

    return allFiles;
  }

  private String convertToDownloadUrl(String url, int index, DashRepresentation rep, String baseUrl)
  {
    return URLUtils.makeAbsoulte(replacePlaceholders(url, index, rep), baseUrl);
  }

  // private String makeUrlAbsoute(String segmentUrl, String baseUrl) {
  // if (segmentUrl.startsWith("http") || segmentUrl.startsWith("//")) {
  // // already absolute
  // return segmentUrl;
  // }

  // StringBuilder sb = new StringBuilder(baseUrl);
  // if (sb.charAt(sb.length() -1) != '/') {
  // sb.append('/');
  // }
  // sb.append(segmentUrl);
  // return sb.toString();
  // }

  private String replacePlaceholders(String url, int index, DashRepresentation rep)
  {
    return url.replace("$Number$", Integer.toString(index)).replace("$RepresentationID$", rep.id);
  }

  private String getTargetFileForUrl(String url, DashRepresentation rep, String targetFolder)
  {
    StringBuilder sb = new StringBuilder(targetFolder);
    if (sb.charAt(sb.length() - 1) != '/' || sb.charAt(sb.length() - 1) != '\\')
    {
      sb.append('/');
    }
    sb.append(rep.parent.parent.id).append('/');
    sb.append(rep.parent.id).append('/');
    sb.append(rep.id).append('/');
    sb.append(url.substring(url.lastIndexOf('/') + 1));

    return sb.toString();
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

    String targetFile = adjustUrl(this.mediaUrl, targetFolder, manifestBaseUrl, targetRepresentation);
    this.mediaUrlNode.setNodeValue(targetFile);

    targetFile = adjustUrl(this.initUrl, targetFolder, manifestBaseUrl, targetRepresentation);
    this.initUrlNode.setNodeValue(targetFile);
  }

  private String adjustUrl(String url, String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation) 
  {
    StringBuffer sb = new StringBuffer("./");

    String[] parts = url.split("/");

    sb.append(targetRepresentation.parent.parent.id).append('/');
    sb.append(targetRepresentation.parent.id).append('/');
    if (findPlaceHolderIndex(parts, "$RepresentationID$") > -1) {
      sb.append("$RepresentationID$").append('/');
    } else {
      sb.append(targetRepresentation.id).append('/');
    }

    int segmentPlaceholderIndex = findPlaceHolderIndex(parts, "$Number$");
    if (segmentPlaceholderIndex == -1) {
      segmentPlaceholderIndex = findPlaceHolderIndex(parts, "$Time$");
    }

    // init segment won't have a placeholder
    if (segmentPlaceholderIndex > 0) {
      sb.append(parts[segmentPlaceholderIndex]);
    } else {
      sb.append(parts[parts.length - 1]);
    }

    return sb.toString();
  }

  private static int findPlaceHolderIndex(String[] heystack, String needle) {
    for(int i = 0; i < heystack.length; i++) {
      if (heystack[i].contains(needle)) {
        return i;
      }
    }
    return -1;
  }
}
