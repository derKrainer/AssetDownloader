package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;
import parser.dash.subcomponents.segmentlistcomponents.Initialization;
import parser.dash.subcomponents.segmentlistcomponents.SegmentUrl;

public class SegmentList extends DashComponent
{
  public Initialization initNode;
  public List<SegmentUrl> segmentUrls = new ArrayList<>();

  public SegmentList(Node xmlNode)
  {
    super(xmlNode);
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList) 
  {
    for (Node attribute: specialAttributesList)
    {
      System.out.println("Unknown SegmentList attribute: " + attribute.getNodeName());
    }    
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) 
  {
    for (Node childNode : specialNodes)
    {
      if (childNode.getNodeName().equals("Initialization"))
      {
        this.initNode = new Initialization(childNode);
        this.initNode.parse();
      }
      else if (childNode.getNodeName().equals("SegmentURL"))
      {
        SegmentUrl newSegmentUrl = new SegmentUrl(childNode);
        newSegmentUrl.parse();;
        this.segmentUrls.add(newSegmentUrl);
      }
      else
      {
        System.out.println("Unknown SegmentList child: " + childNode.getNodeName());    
      }
    }  
  }

  @Override
  protected void fillMissingValues() {
    // nothing as of yet
  }

  public List<DownloadTarget> getTargetFiles(String manifestLocation, String targetFolder, DashRepresentation rep)
  {
    List<DownloadTarget> targets = new ArrayList<>();

    if (this.initNode != null)
    {
      targets.add(this.initNode.toDownloadTarget(manifestLocation, targetFolder, rep));
    }
    else
    {
      System.out.println("No init url in SegmentList");
    }

    for(SegmentUrl url : this.segmentUrls)
    {
      targets.add(url.toDownloadTarget(manifestLocation, targetFolder, rep));
    }

    return targets;
  }

  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    // TODO: adjust all segment list urls
  }
}
