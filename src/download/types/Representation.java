package download.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import download.compare.ComparisonResult;
import download.compare.ListComparison;

public class Representation implements Comparable<Representation>
{
  public List<DownloadTarget> filesToDownload = new ArrayList<>();
  public Map<String, String> attributes = new HashMap<>();

  public final String id;
  public final int bandwidth;
  public String manifestContent = null;
  public AdaptationSet containingAdaptationSet = null;

  public Representation(String name, int bandwidth)
  {
    this.id = name;
    this.bandwidth = bandwidth;
  }

  public String toDebugString()
  {
    String dlTargetString = "\n";
    for (DownloadTarget target : this.filesToDownload)
    {
      dlTargetString += "      " + target.toString() + "\n";
    }

    return this.id + " @" + this.bandwidth + dlTargetString;
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    if (this.containingAdaptationSet != null)
    {
      sb.append(this.containingAdaptationSet.toString()).append("; ");
    }
    sb.append("Rep: ").append(this.id);
    sb.append(" @").append(this.bandwidth);
    return sb.toString();
  }

  public String generateId(boolean includePeriod)
  {
    StringBuffer sb = new StringBuffer();
    if (includePeriod)
    {
      sb.append(containingAdaptationSet.containingPeriod.periodId).append('_');
    }
    sb.append(containingAdaptationSet.id).append('_');
    sb.append(this.id);
    return sb.toString();
  }

  @Override
  public int compareTo(Representation o)
  {
    return this.toString().compareTo(o.toString());
  }

  public void compareToOldRepresentation(Representation oldRep, ComparisonResult container)
  {
    ListComparison<DownloadTarget> changes = new ListComparison<>(oldRep.filesToDownload, this.filesToDownload);
    container.downloadTargetChangesInRepresentations.put(this.id, changes);
  }

  public String generateId()
  {
    return this.containingAdaptationSet.generateId() + '_' + this.id;
  }

  @Override
  public boolean equals(Object other)
  {
    return (other instanceof Representation) && this.generateId().equals(((Representation) other).generateId());
  }
}