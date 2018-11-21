package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import parser.dash.DashRepresentation;

public class SegementTimeline extends SegmentTemplate
{
  public SegmentTemplate parent;
  public List<SegmentTimelineEntry> entries = new ArrayList<>();

  public SegementTimeline(Node xmlNode, SegmentTemplate parent)
  {
    super(xmlNode);
    this.parent = parent;
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {
    SegmentTimelineEntry latestEntry = null;
    for(Node child : specialNodes) {
      if (child.getNodeName().equals("S")) 
      {
        latestEntry = new SegmentTimelineEntry(child, latestEntry);
        this.entries.add(latestEntry);
      }
      else {
        System.out.println("Unhandled SegmentTimeline Child: " + child.getNodeName());
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node n : specialAttributesList)
    {
      System.out.println("Unhandled SegmentTimeline attribute: " + n.getNodeName());
    }
  }

  @Override
  protected void fillMissingValues()
  {
    // assure that the start number is 0
    this.startNumber = 0;
  }

  @Override
  protected int getNumberOfSegments(DashRepresentation rep) {
    return this.entries.size();
  }

  @Override
  protected String replacePlaceholders(String url, int index, DashRepresentation rep) {
    String generalReplacements = super.replacePlaceholders(url, index, rep);

    return generalReplacements
      .replace("$Time$", Integer.toString(this.entries.get(index).startTime));
  }
}
