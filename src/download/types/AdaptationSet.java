package download.types;

import java.util.ArrayList;
import java.util.List;

import download.compare.ComparisonResult;
import download.compare.ListComparison;

public class AdaptationSet
{
  public List<Representation> representations = new ArrayList<>();
  public final String id;

  public Period containingPeriod = null;

  public AdaptationSet(String name)
  {
    this.id = name;
  }

  public void addRepresentation(Representation toAdd)
  {
    this.representations.add(toAdd);
    toAdd.containingAdaptationSet = this;
  }

  public String toDebugString()
  {
    String repString = "\n";
    for (Representation target : this.representations)
    {
      repString += "    " + target.toDebugString() + "\n";
    }

    return this.id + repString;
  }

  @Override
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    if (this.containingPeriod != null)
    {
      sb.append(this.containingPeriod.toString()).append("; ");
    }
    sb.append("AdSet: ").append(this.id);
    return sb.toString();
  }

  public void compareToOldAdaptationSet(AdaptationSet oldSet, ComparisonResult container)
  {
    ListComparison<Representation> changes = new ListComparison<>(oldSet.representations, this.representations);
    container.representationChangesInAdaptationSets.put(this.id, changes);

    for (Representation same : changes.sameItems)
    {
      Representation oldRep = oldSet.getRepresentationForId(same.id);
      Representation newRep = this.getRepresentationForId(same.id);
      newRep.compareToOldRepresentation(oldRep, container);
    }
  }

  public Representation getRepresentationForId(String repID)
  {
    for (Representation rep : this.representations)
    {
      if (rep.id.equals(repID))
      {
        return rep;
      }
    }
    return null;
  }

  public String generateId()
  {
    return this.containingPeriod.periodId + '_' + this.id;
  }

  @Override
  public boolean equals(Object other)
  {
    return (other instanceof AdaptationSet) && this.generateId().equals(((AdaptationSet) other).generateId());
  }
}