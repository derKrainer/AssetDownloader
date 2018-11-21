package download.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Representation
{
  public List<DownloadTarget> filesToDownload = new ArrayList<>();
  public Map<String, String> attributes = new HashMap<>();

  public final String name;
  public final int bandwidth;
  public String manifestContent = null;
  public AdaptationSet containingAdaptationSet = null;

  public Representation(String name, int bandwidth)
  {
    this.name = name;
    this.bandwidth = bandwidth;
  }

  public String toDebugString() {
    String dlTargetString = "\n";
    for (DownloadTarget target : this.filesToDownload)
    {
      dlTargetString += "      " + target.toString() + "\n";
    }

    return this.name + " @" + this.bandwidth + dlTargetString;
  }

  @Override
  public String toString() {
    return this.name + " @" + this.bandwidth;
  }

  public String generateId() {
    return this.name + '_' + containingAdaptationSet.name + '_' + containingAdaptationSet.containingPeriod.periodId;
  }

}