package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;

public class SegementTimeline extends DashComponent
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
    for (Node child : specialNodes)
    {
      if (child.getNodeName().equals("S"))
      {
        latestEntry = new SegmentTimelineEntry(child, latestEntry);
        latestEntry.parse();
        List<SegmentTimelineEntry> expandedEntries = this.expandRepeat(latestEntry);
        this.entries.addAll(expandedEntries);
        latestEntry = expandedEntries.get(expandedEntries.size() - 1);
      }
      else
      {
        System.out.println("Unhandled SegmentTimeline Child: " + child.getNodeName());
      }
    }
  }

  @Override
  protected void fillMissingValues()
  {
    // nothing
  }

  protected List<SegmentTimelineEntry> expandRepeat(SegmentTimelineEntry entry)
  {
    List<SegmentTimelineEntry> collection = new ArrayList<>();

    if (entry.repeat > 0)
    {
      int expandTo = entry.repeat;
      SegmentTimelineEntry lastEntry = entry;
      SegmentTimelineEntry newEntry = null;
      lastEntry.repeat = 0;
      collection.add(lastEntry);
      for (int i = 0; i < expandTo; i++)
      {
        newEntry = new SegmentTimelineEntry(entry.xmlContent, lastEntry);
        newEntry.duration = entry.duration;
        newEntry.startTime = lastEntry.startTime + lastEntry.duration;
        newEntry.preceedingEntry = lastEntry;
        collection.add(newEntry);
        lastEntry = newEntry;
      }
    }
    else
    {
      collection.add(entry);
    }

    return collection;
  }

  public List<DownloadTarget> getTargetFiles(DashRepresentation rep, String baseUrl, String targetFolder)
  {
    List<DownloadTarget> allFiles = new ArrayList<>();
    allFiles.add(new DownloadTarget(parent.convertToDownloadUrl(this.parent.initUrl, -1, rep, baseUrl),
        parent.getTargetFileForUrl(parent.initUrl, rep, targetFolder, this.parent.initUrl)));

    int numberOfSegments = this.entries.size();
    for (int i = 0; i < numberOfSegments; i++)
    {
      String dlUrl = this.convertToDownloadUrl(parent.mediaUrl, i, rep, baseUrl);
      allFiles.add(new DownloadTarget(dlUrl, parent.getTargetFileForUrl(dlUrl, rep, targetFolder, this.parent.mediaUrl)));
    }

    return allFiles;
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node n : specialAttributesList)
    {
      System.out.println("Unhandled SegmentTimeline attribute: " + n.getNodeName());
    }
  }

  protected String convertToDownloadUrl(String url, int index, DashRepresentation rep, String baseUrl)
  {
    return this.replacePlaceholders(parent.convertToDownloadUrl(url, index, rep, baseUrl), index, rep);
  }

  protected String replacePlaceholders(String url, int index, DashRepresentation rep)
  {
    return url.replace("$Time$", Long.toString(this.entries.get(index).startTime));
  }
}
