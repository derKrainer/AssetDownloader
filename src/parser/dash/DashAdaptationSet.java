package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import parser.dash.subcomponents.BaseUrl;
import parser.dash.subcomponents.SegmentTemplate;

public class DashAdaptationSet extends DashComponent
{
  private static int uniqueAdaptationSetID = 0;

  public ContentType dataType = ContentType.Other;

  public SegmentTemplate segmentTemplate = null;

  public List<DashRepresentation> representations = new ArrayList<>();

  public DashPeriod parent;

  public String mimeType;
  public String codec;
  public String language;

  public DashAdaptationSet(Node xmlNode, DashPeriod parent)
  {
    super(xmlNode);
    this.parent = parent;
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {

    for (Node child : specialNodes)
    {
      if (child.getNodeName().equals("Representation"))
      {
        DashRepresentation newChild = new DashRepresentation(child, this);
        newChild.parent = this;
        newChild.parse();
        this.representations.add(newChild);
      }
      else if (child.getNodeName().equals("SegmentTemplate"))
      {
        this.segmentTemplate = new SegmentTemplate(child);
        this.segmentTemplate.parse();
      }
      else
      {
        // TODO: parse AdaptationSet Child != representation
        System.out.println("Unknown Adaptation set node: " + child);
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (int i = 0; i < specialAttributesList.size(); i++)
    {
      Node att = specialAttributesList.get(i);
      if (att.getNodeName().equals("codecs"))
      {
        this.codec = att.getNodeValue();
      }
      else if (att.getNodeName().equals("mimeType"))
      {
        this.mimeType = att.getNodeValue();
      }
      else if (att.getNodeName().equals("contentType"))
      {
        String stringContentType = att.getNodeValue();

        switch (stringContentType) {
        case "video":
          this.dataType = ContentType.Video;
          break;
        case "audio":
          this.dataType = ContentType.Audio;
          break;
        case "text":
          this.dataType = ContentType.Subtitles;
          break;
        case "application":
          this.dataType = ContentType.Application;
          break;
        default:
          System.out.println("unknown content type: " + stringContentType);
          break;
        }
      }
    }

    if (this.dataType == ContentType.Other && this.mimeType != null)
    {
      if (this.mimeType.startsWith("video"))
      {
        this.dataType = ContentType.Video;
      }
      else if (this.mimeType.startsWith("audio"))
      {
        this.dataType = ContentType.Audio;
      }
      else if (this.mimeType.startsWith("application"))
      {
        this.dataType = ContentType.Application;
      }
    }
  }

  public SegmentTemplate getSegmentTemplate()
  {
    return this.segmentTemplate;
  }

  @Override
  protected void fillMissingValues()
  {
    if (this.id == null)
    {
      this.id = Integer.toString(uniqueAdaptationSetID++);
    }
  }

  @Override
  public boolean removeChild(DashComponent toRemove)
  {
    boolean success = super.removeChild(toRemove);
    this.representations.remove(toRemove);
    return success;
  }

  @Override
  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    super.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);

    this.representations.forEach((rep) -> rep.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation));
  }

  public BaseUrl getBaseUrl()
  {
    if (this.baseUrl != null)
    {
      return this.baseUrl;
    }
    else {
      return this.parent.baseUrl;
    }
  }
}

enum ContentType
{
  Video, Audio, Subtitles, Application, Other
}
