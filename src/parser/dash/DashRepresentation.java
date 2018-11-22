package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.subcomponents.SegmentList;
import parser.dash.subcomponents.SegmentTemplate;

public class DashRepresentation extends DashAdaptationSet
{
  private static int uniqueBandwidthCounter = 1;

  public Integer bandwidth = null;
  public int width = -1;
  public int height = -1;

  public DashAdaptationSet parent;

  public SegmentList segmentList = null;

  public DashRepresentation(Node xmlNode, DashAdaptationSet parent)
  {
    super(xmlNode, parent.parent);
    this.parent = parent;
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    super.parseSpecialNodes(specialNodes);

    for(Node child : specialNodes)
    {
      if (child.getNodeName().equals("SegmentList"))
      {
        this.segmentList = new SegmentList(child);
        this.segmentList.parse();
      }
      else 
      {
        System.out.println("Unhandled Representation Child: " + child.getNodeName());
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    super.parseAttributes(specialAttributesList);

    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("bandwidth"))
      {
        this.bandwidth = Integer.parseInt(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("width"))
      {
        this.width = Integer.parseInt(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("height"))
      {
        this.height = Integer.parseInt(attr.getNodeValue());
      }
      else
      {
        System.out.println("Unknown DashRepresentation attribute: " + attr.getNodeName());
      }
    }
  }

  @Override
  protected void fillMissingValues()
  {
    super.fillMissingValues();

    if (this.bandwidth == null)
    {
      this.bandwidth = uniqueBandwidthCounter++;
    }
    if (this.getSegmentTemplate() != null)
    {
      SegmentTemplate template = this.getSegmentTemplate();

      if ((template.initUrl != null && template.initUrl.contains("$Bandwidth$"))
          || (template.mediaUrl != null && template.mediaUrl.contains("$Bandwidth$")))
      {
        this.id = Integer.toString(this.bandwidth);
      }
    }
  }

  public SegmentTemplate getSegmentTemplate()
  {
    SegmentTemplate retVal = null;
    if (this.segmentTemplate != null)
    {
      retVal = this.segmentTemplate;
    }
    else if (parent.getSegmentTemplate() != null)
    {
      retVal = parent.getSegmentTemplate();
    }

    return retVal;
  }

  public List<DownloadTarget> getTargetFiles(String manifestLocation, String targetFolder)
  {
    List<DownloadTarget> filesToDownload = new ArrayList<>();

    if (this.getSegmentTemplate() != null)
    {
      filesToDownload = this.getSegmentTemplate().getTargetFiles(this, manifestLocation, targetFolder);
    }
    else if (this.segmentList != null)
    {
      filesToDownload = this.segmentList.getTargetFiles(manifestLocation, targetFolder, this);
    }
    else
    {
      System.err.println("TODO: implement other representation types than segment template");
    }

    return filesToDownload;
  }

  public String generateId()
  {
    // generates an id matching the one from the download.Representation object
    return this.id + '_' + this.parent.id + '_' + this.parent.parent.id;
  }

  @Override
  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    super.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);

    if (this.getSegmentTemplate() != null)
    {
      this.getSegmentTemplate().adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    }
    else if (this.segmentList != null)
    {
      this.segmentList.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    }
    else
    {
      throw new RuntimeException("Implement adjustUrlsToTarget for the current manifest type");
    }
  }

  public String generateDirectoryPath(String downloadFolder)
  {
    StringBuffer sb = new StringBuffer(downloadFolder);

    sb.append(this.parent.parent.id).append('/');
    sb.append(this.parent.id).append('/');
    sb.append(this.id);

    return sb.toString();
  }

}
