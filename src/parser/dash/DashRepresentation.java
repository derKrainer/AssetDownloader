package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.subcomponents.BaseUrl;
import parser.dash.subcomponents.SegmentBase;
import parser.dash.subcomponents.SegmentList;
import parser.dash.subcomponents.SegmentTemplate;
import util.URLUtils;

public class DashRepresentation extends DashAdaptationSet
{
  public Integer bandwidth = null;
  public int width = -1;
  public int height = -1;

  public DashAdaptationSet parent;

  public SegmentList segmentList = null;
  public SegmentBase segmentBase = null;

  public DashRepresentation(Node xmlNode, DashAdaptationSet parent)
  {
    super(xmlNode, parent.parent);
    this.parent = parent;
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    super.parseSpecialNodes(specialNodes);

    for (Node child : specialNodes)
    {
      if (child.getNodeName().equals("SegmentList"))
      {
        this.segmentList = new SegmentList(child);
        this.segmentList.parse();
      }
      else if (child.getNodeName().equals("SegmentBase"))
      {
        this.segmentBase = new SegmentBase(child);
        this.segmentBase.parse();
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
    if (this.bandwidth == null)
    {
      this.bandwidth = FallbackCounters.Bandwidth++;
    }
    if (this.id == null)
    {
      // this seems like a stream issue
      System.err.println("Warning, representation without ID found");
      this.id = Integer.toString(FallbackCounters.RepresentationId++);
    }

    super.fillMissingValues();
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

  public BaseUrl getBaseUrl()
  {
    if (this.baseUrl != null)
    {
      return this.baseUrl;
    }
    return parent.getBaseUrl();
  }

  public List<DownloadTarget> getTargetFiles(String manifestLocation, String targetFolder)
  {
    List<DownloadTarget> filesToDownload = new ArrayList<>();

    String baseUrl = manifestLocation;
    if (this.getBaseUrl() != null)
    {
      baseUrl = this.getBaseUrl().baseUrl;
    }

    if (this.getSegmentTemplate() != null)
    {
      filesToDownload = this.getSegmentTemplate().getTargetFiles(this, baseUrl, targetFolder);
    }
    else if (this.segmentList != null)
    {
      filesToDownload = this.segmentList.getTargetFiles(baseUrl, targetFolder, this);
    }
    else if (this.segmentBase != null)
    {
      filesToDownload = this.segmentBase.getTargetFiles(baseUrl, targetFolder, this);
    }
    else if (this.baseUrl != null)
    {
      // single files might be in the representation with a baseURL
      String possibleOtherBaseUrl = manifestLocation;
      if (parent.getBaseUrl() != null)
      {
        possibleOtherBaseUrl = parent.getBaseUrl().baseUrl;
      }
      String serverUrl = URLUtils.makeAbsoulte(this.baseUrl.baseUrl, possibleOtherBaseUrl);
      String localPath = this.generateDirectoryPath(targetFolder)
          + this.baseUrl.baseUrl.substring(this.baseUrl.baseUrl.lastIndexOf('/'));
      filesToDownload.add(new DownloadTarget(serverUrl, localPath));
    }
    else
    {
      System.err.println("implement current representation type");
    }

    return filesToDownload;
  }

  public String generateId()
  {
    // generates an id matching the one from the download.Representation object
    return this.parent.parent.id + '_' + this.parent.id + '_' + this.id;
  }

  @Override
  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    this.parent.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    super.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);

    if (this.getSegmentTemplate() != null)
    {
      this.getSegmentTemplate().adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    }
    else if (this.segmentList != null)
    {
      this.segmentList.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    }
    else if (this.segmentBase != null)
    {
      this.segmentBase.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);
    }
    else if (this.baseUrl != null)
    {
      // single subtitle file might be like this
      // BaseURL is already handled in super, so nothing to do here
    }
    else
    {
      throw new RuntimeException("Implement adjustUrlsToTarget for the current manifest type");
    }
  }

  public String generateDirectoryPath(String downloadFolder)
  {
    return this.parent.generateDirectoryPath(downloadFolder) + '/' + this.id;
  }

  public String generateRelativeLocalPath(String downloadFolder)
  {
    if (this.baseUrl != null)
    {
      return this.baseUrl.baseUrl;
    }
    return this.parent.generateRelativeLocalPath(downloadFolder) + '/' + this.id;
  }
}
