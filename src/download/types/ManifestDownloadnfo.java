package download.types;

import java.util.ArrayList;
import java.util.List;

import download.compare.ComparisonResult;
import download.compare.ListComparison;

public class ManifestDownloadnfo
{
  public List<Period> periods = new ArrayList<>();

  public final String baseURL;

  public boolean isLive = false;

  public ManifestDownloadnfo(String baseUrl)
  {
    this.baseURL = baseUrl;
  }

  @Override
  public String toString()
  {
    String periodString = "\n";
    for (Period target : this.periods)
    {
      periodString += target.toString() + "\n";
    }

    return periodString;
  }

  public String toDebugString()
  {
    String periodString = "\n";
    for (Period target : this.periods)
    {
      periodString += target.toDebugString() + "\n";
    }

    return periodString;
  }

  public ComparisonResult compareToOldManifest(ManifestDownloadnfo oldInfo)
  {
    ComparisonResult result = new ComparisonResult();

    result.periodChanges = new ListComparison<>(oldInfo.periods, this.periods);

    for (Period stayingPeriod : result.periodChanges.sameItems)
    {
      Period newPeriod = this.getPeriodForId(stayingPeriod.periodId);
      Period oldPeriod = oldInfo.getPeriodForId(stayingPeriod.periodId);
      newPeriod.compareToOldPeriod(oldPeriod, result);
    }

    return result;
  }

  public Period getPeriodForId(String periodId)
  {
    for (Period p : this.periods)
    {
      if (p.periodId.equals(periodId))
      {
        return p;
      }
    }
    return null;
  }
}