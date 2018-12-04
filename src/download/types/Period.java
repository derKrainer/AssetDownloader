package download.types;

import java.util.ArrayList;
import java.util.List;

public class Period
{
  public List<AdaptationSet> adaptationSets = new ArrayList<>();

  public final String periodId;

  public Period(String id)
  {
    this.periodId = id;
  }

  public void addAdaptationSet(AdaptationSet toAdd)
  {
    this.adaptationSets.add(toAdd);
    toAdd.containingPeriod = this;
  }

  public String toDebugString()
  {
    String adString = "\n";
    for (AdaptationSet target : this.adaptationSets)
    {
      adString += "  " + target.toDebugString() + "\n";
    }

    return this.periodId + adString;
  }

  public String toString()
  {
    return "Period:" + this.periodId;
  }

  public void compareToOldPeriod(Period oldInfo, ComparisonResult container)
  {
    ListComparison<AdaptationSet> changes = new ListComparison<>(oldInfo.adaptationSets, this.adaptationSets);
    container.adaptationSetChangesInPeriod.put(this.periodId, changes);

    for (AdaptationSet same : changes.sameItems)
    {
      AdaptationSet oldSet = oldInfo.getAdaptationSetForID(same.id);
      AdaptationSet newSet = this.getAdaptationSetForID(same.id);
      newSet.compareToOldAdaptationSet(oldSet, container);
    }
  }

  public AdaptationSet getAdaptationSetForID(String adaptationSetID)
  {
    for (AdaptationSet adSet : this.adaptationSets)
    {
      if (adSet.id.equals(adaptationSetID))
      {
        return adSet;
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object other)
  {
    return (other instanceof Period) && this.periodId.equals(((Period)other).periodId);
  }
}
