package download.compare;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import download.types.AdaptationSet;
import download.types.DownloadTarget;
import download.types.Period;
import download.types.Representation;

/**
 * Information about how a manifest changed between an update
 */
public class ComparisonResult 
{
  public ListComparison<Period> periodChanges = null;

  public Map<String, ListComparison<AdaptationSet>> adaptationSetChangesInPeriod = new HashMap<>();

  public Map<String, ListComparison<Representation>> representationChangesInAdaptationSets = new HashMap<>();

  public Map<String, ListComparison<DownloadTarget>> downloadTargetChangesInRepresntation = new HashMap<>();

  public Set<Representation> getSameAndNewRepresentations()
  {
    Set<Representation> newTargets = new HashSet<>();
    for (ListComparison<Representation> changedTargets : representationChangesInAdaptationSets.values())
    {
      newTargets.addAll(changedTargets.sameItems);
      newTargets.addAll(changedTargets.newItems);
    }
    return newTargets;
  }

  public Set<DownloadTarget> getNewDownloadTargets() 
  {
    Set<DownloadTarget> newTargets = new HashSet<>();
    for (ListComparison<DownloadTarget> changedTargets : downloadTargetChangesInRepresntation.values())
    {
      newTargets.addAll(changedTargets.newItems);
    }
    return newTargets;
  }
}