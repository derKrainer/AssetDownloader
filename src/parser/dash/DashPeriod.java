package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import parser.DashParser;

public class DashPeriod extends DashComponent
{
  public double duration = -1;
  public double start = -1;
  public DashManifest parent;
  public List<DashAdaptationSet> adaptationSets = new ArrayList<>();

  public DashPeriod(Node xmlNode, DashManifest parent)
  {
    super(xmlNode);
    this.parent = parent;
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    for (Node childNode : specialNodes)
    {
      if (childNode.getNodeName().equals("AdaptationSet"))
      {
        System.out.println("processing Adaptation Set: " + childNode);
        DashAdaptationSet currentSet = new DashAdaptationSet(childNode, this);
        currentSet.parse();
        this.adaptationSets.add(currentSet);
      }
      else
      {
        System.out.println("Unexpected child of Period: " + childNode);
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("duration"))
      {
        this.duration = DashParser.parseDuration(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("start"))
      {
        this.start = DashParser.parseDuration(attr.getNodeValue());
      }
      else
      {
        System.out.println("Unhandled Period attribute: " + attr.getNodeName());
      }
    }
  }

  @Override
  protected void fillMissingValues()
  {
    if (this.id == null)
    {
      this.id = Integer.toString(FallbackCounters.PeriodId++);
    }
  }

  public double getDuration()
  {
    if (this.duration > -1)
    {
      return this.duration;
    }
    else
    {
      return this.parent.duration;
    }
  }

  @Override
  public boolean removeChild(DashComponent toRemove)
  {
    boolean success = super.removeChild(toRemove);
    this.adaptationSets.remove(toRemove);
    return success;
  }

  @Override
  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    super.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);

    // it's easier to do the adjustment bottom up, so don't propagate to children
  }

  public String generateDirectoryPath(String downloadFolder)
  {
    return downloadFolder + this.id;
  }

  public String generateRelativeLocalPath(String downloadFolder)
  {
    if (this.baseUrl != null)
    {
      return this.baseUrl.baseUrl;
    }
    return this.generateDirectoryPath(downloadFolder);
  }
}
