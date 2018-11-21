package download.types;

import java.util.ArrayList;
import java.util.List;

public class ManifestDownloadnfo
{
  public List<Period> periods = new ArrayList<>();

  public final String baseURL;

  public ManifestDownloadnfo(String baseUrl)
  {
    this.baseURL = baseUrl;
  }

  @Override
  public String toString() {
    String periodString = "\n";
    for (Period target : this.periods)
    {
      periodString += target.toString() + "\n";
    }

    return periodString;
  }

  public String toDebugString() {
    String periodString = "\n";
    for (Period target : this.periods)
    {
      periodString += target.toDebugString() + "\n";
    }

    return periodString;
  }
}
