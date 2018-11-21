package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class DashPeriod extends DashComponent
{
  private static int uniqueIdCounter = 0;

  public double duration = -1;
  public DashManifest parent;

  public DashPeriod(Node xmlNode, DashManifest parent)
  {
    super(xmlNode);
    this.parent = parent;
  }

  public List<DashAdaptationSet> adaptationSets = new ArrayList<>();

  public String id;

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) {
    for (int i = 0; i < specialNodes.size(); i++)
    {
      Node childNode = specialNodes.get(i);
      if (childNode.getNodeName().equals("AdaptationSet"))
      {
        System.out.println("processing Adaptation Set: " + childNode);
        DashAdaptationSet currentSet = new DashAdaptationSet(childNode, this);
        currentSet.parse();
        this.adaptationSets.add(currentSet);
      } else
      {
        System.out.println("Unexpected child of Period: " + childNode);
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList) {
    // TODO: start + duration
  }

  @Override
  protected void fillMissingValues() {
    if (this.id == null)
    {
      this.id = Integer.toString(uniqueIdCounter++);
    }
  }

  public double getDuration() {
    if (this.duration > -1)
    {
      return this.duration;
    } else
    {
      return this.parent.duration;
    }
  }

  @Override
  public boolean removeChild(DashComponent toRemove) {
    boolean success = super.removeChild(toRemove);
    this.adaptationSets.remove(toRemove);
    return success;
  }
}
