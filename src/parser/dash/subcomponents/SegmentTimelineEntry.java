package parser.dash.subcomponents;

import java.util.List;

import org.w3c.dom.Node;

import parser.dash.DashComponent;

public class SegmentTimelineEntry extends DashComponent
{
  public int startTime;
  public int duration;
  public int repeat = 1;
  public int endTime = -1;
  public SegmentTimelineEntry preceedingEntry;

  public SegmentTimelineEntry(Node xmlContent, SegmentTimelineEntry previousEntry)
  {
    super(xmlContent);
    this.preceedingEntry = previousEntry;
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) {
    // should always be emtpy
    for (Node n : specialNodes)
    {
      System.out.println("unexpected child of segment timeline entry: " + n.getNodeName());
    }
  }

  @Override
  protected void fillMissingValues()
  {
    if (this.startTime == -1)
    {
      if (this.preceedingEntry != null)
      {
        this.startTime = this.preceedingEntry.startTime + this.preceedingEntry.duration;
      }
      else {
        this.startTime = 0;
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node attr : specialAttributesList)
    {
      switch (attr.getNodeName()) 
      {
        case "D":
        case "d":
          this.duration = Integer.parseInt(attr.getNodeValue());
          break;
        case "R":
        case "r":
          this.repeat = Integer.parseInt(attr.getNodeValue());
          break;

        case "T":
        case "t":
          this.startTime = Integer.parseInt(attr.getNodeValue());
        
        default: {
          System.out.println("Unknown SegmentTimelineEntry attribute: " + attr.getNodeName());
        }
      }
    }  
  }
}