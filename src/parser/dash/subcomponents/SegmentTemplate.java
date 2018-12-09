package parser.dash.subcomponents;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashPeriod;
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
  public SegementTimeline segmentTimeline;

  public SegmentTemplate(Node xmlNode)
  {
    super(xmlNode);
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    for (Node childNode : specialNodes)
    {
      if (childNode.getNodeName().equals("SegmentTimeline"))
      {
        this.segmentTimeline = new SegementTimeline(childNode, this);
        this.segmentTimeline.parse();
      }
      else
      {
        System.out.println("Unhandled SegmentTemplateNode: " + childNode);
      }
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
      }
      else if (attr.getNodeName().equals("initialization"))
      {
        this.initUrl = attr.getNodeValue();
        this.initUrlNode = attr;
      }
      else if (attr.getNodeName().equals("startNumber"))
      {
        this.startNumber = Integer.parseInt(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("duration"))
      {
        this.duration = Integer.parseInt(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("timescale"))
      {
        this.timescale = Integer.parseInt(attr.getNodeValue());
      }
      else
      {
        System.out.println("Unknown SegmentTemplate attribute: " + attr.getNodeName());
      }
    }
  }

  @Override
  protected void fillMissingValues()
  {
  }

  public List<DownloadTarget> getTargetFiles(DashRepresentation rep, String baseUrl, String targetFolder)
  {
    if (this.segmentTimeline != null)
    {
      return this.segmentTimeline.getTargetFiles(rep, baseUrl, targetFolder);
    }

    List<DownloadTarget> allFiles = new ArrayList<>();
    allFiles.add(new DownloadTarget(convertToDownloadUrl(initUrl, -1, rep, baseUrl),
        getTargetFileForUrl(this.replacePlaceholders(initUrl, -1, rep), rep, targetFolder, initUrl)));

    int numberOfSegments = this.getNumberOfAvailableSegments(rep);
    for (int i = this.startNumber; i < this.startNumber + numberOfSegments; i++)
    {
      String dlUrl = convertToDownloadUrl(this.mediaUrl, i, rep, baseUrl);
      allFiles.add(new DownloadTarget(dlUrl, getTargetFileForUrl(dlUrl, rep, targetFolder, mediaUrl)));
    }

    return allFiles;
  }

  protected int getNumberOfAvailableSegments(DashRepresentation rep)
  {
    DashPeriod containingPeriod = rep.parent.parent;
    double segmentDuration = (double) this.duration / this.timescale;
    if (containingPeriod.parent.isLive)
    {
      // according to my understanding the time until now is split in the following way:
      // |###########################| - now (0 -> now)
      // |#########| ----------------- - availabilityStartTime (0 -> startOfStream)
      // ----------|#########|-------- - periodStart (startOfStream -> startOfAvailablity)
      // --------------------|#######| - availableSegments (startOfAvailabilty -> now)
      // -----------------------------------------------------
      // numSegmentsAvailable = toSeconds(now - periodStart) / segmentDurationInSec
      Instant availabilityStartTime = containingPeriod.parent.availabilityStartTime;
      Instant now = containingPeriod.parent.downloadInstant;
      Instant firstAvailableSegmentTime = availabilityStartTime.plusSeconds((long) containingPeriod.start);
      // check if the period actually has an end time, if so use that, if not use now() as end
      long availabilityDuration;
      if (containingPeriod.duration > 0)
      {
        availabilityDuration = (long) containingPeriod.duration;
      }
      else
      {
        availabilityDuration = Duration.between(firstAvailableSegmentTime, now).getSeconds();
      }

      double segmentsAvailable = availabilityDuration / segmentDuration;
      int availableSegments = (int) Math.ceil(segmentsAvailable * 1000) / 1000;
      return availableSegments;
    }
    else
    {
      double streamDuration = containingPeriod.getDuration();

      int numberOfSegments = (int) Math.ceil(streamDuration / segmentDuration);
      return numberOfSegments;
    }
  }

  protected String convertToDownloadUrl(String url, int index, DashRepresentation rep, String baseUrl)
  {
    return URLUtils.makeAbsoulte(replacePlaceholders(url, index, rep), baseUrl);
  }

  protected String replacePlaceholders(String url, int index, DashRepresentation rep)
  {
    return url // keep
        .replace("$Number$", Integer.toString(index)) // this
        .replace("$RepresentationID$", rep.id) // formatting
        .replace("$Bandwidth$", Integer.toString(rep.bandwidth));
  }

  protected String getTargetFileForUrl(String url, DashRepresentation rep, String targetFolder, String templateUrl)
  {
    StringBuilder sb = new StringBuilder(targetFolder);
    if (sb.charAt(sb.length() - 1) != '/' || sb.charAt(sb.length() - 1) != '\\')
    {
      sb.append('/');
    }
    sb.append(rep.parent.parent.id).append('/');
    sb.append(rep.parent.id).append('/');
    sb.append(rep.id).append('/');
    if (templateUrl.contains("$Bandwidth$"))
    {
      sb.append(rep.bandwidth).append('/');
    }

    sb.append(url.substring(url.lastIndexOf('/') + 1));

    return sb.toString();
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

  protected String adjustUrl(String url, String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    StringBuffer sb = new StringBuffer();

    String[] parts = url.split("/");

    if (targetRepresentation.parent.baseUrl != null)
    {
      // nothing
    }
    else if (targetRepresentation.parent.parent.baseUrl != null)
    {
      sb.append(targetRepresentation.parent.id).append('/');
    }
    else
    {
      sb.append(targetRepresentation.parent.generateRelativeLocalPath(targetFolder)).append('/');
    }

    if (findPlaceHolderIndex(parts, "$RepresentationID$") > -1)
    {
      sb.append("$RepresentationID$").append('/');
    }
    else
    {
      sb.append(targetRepresentation.id).append('/');
    }

    if (findPlaceHolderIndex(parts, "$Bandwidth$") > -1)
    {
      sb.append("$Bandwidth$").append('/');
    }

    int segmentPlaceholderIndex = findPlaceHolderIndex(parts, "$Number$");
    if (segmentPlaceholderIndex == -1)
    {
      segmentPlaceholderIndex = findPlaceHolderIndex(parts, "$Time$");
    }

    // init segment won't have a placeholder
    if (segmentPlaceholderIndex > 0)
    {
      sb.append(parts[segmentPlaceholderIndex]);
    }
    else
    {
      sb.append(parts[parts.length - 1]);
    }

    return sb.toString();
  }

  private static int findPlaceHolderIndex(String[] heystack, String needle)
  {
    for (int i = 0; i < heystack.length; i++)
    {
      if (heystack[i].contains(needle))
      {
        return i;
      }
    }
    return -1;
  }
}
