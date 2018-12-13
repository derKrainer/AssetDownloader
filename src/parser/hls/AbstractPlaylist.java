package parser.hls;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import download.DownloadHelper;
import download.types.DownloadTarget;
import parser.HlsParser;

public abstract class AbstractPlaylist
{
  private String manifestContent;
  private URL manifestUrl;
  public HlsParser parser;
  public boolean isLive = true;

  public AbstractPlaylist(String manifestContent, HlsParser parser)
  {
    this.manifestContent = manifestContent;
  }

  public AbstractPlaylist(URL manifestUrl, HlsParser parser)
  {
    this.manifestUrl = manifestUrl;
  }

  public abstract String getUpdatedManifest();

  public abstract List<DownloadTarget> getAllSegments();

  public abstract void parse();

  public String getManifestContent()
  {
    if (this.manifestContent != null)
    {
      return this.manifestContent;
    }
    else  if (this.manifestUrl != null )
    {
      try
      {
        this.manifestContent = DownloadHelper.getContent(this.manifestUrl.toString());
        return this.manifestContent;
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Unable to retrieve playlist content for " + this.manifestUrl.toString(), ex);
      }
    }
    else
    {
      throw new RuntimeException("Playlist without URL or content cannot be processed");
    }
  }

  public String[] getManifestLines()
  {
    return this.getManifestContent().split("\n");
  }

  protected Map<String, String> parseKeyValuePairs(String hlsLine)
  {
    String attributeStr = hlsLine.substring(hlsLine.indexOf(":") + 1);
    String[] attributePairs = attributeStr.split(",");

    Map<String, String> keyValuePairs = new HashMap<>();
    for (String pair : attributePairs)
    {
      String[] keyValue = pair.split("=");
      if (keyValue.length >= 2)
      {
        // if there are = in the value, reduce them again to a single value
        if (keyValue.length > 2)
        {
          for (int i = 2; i < keyValue.length; i++)
          {
            keyValue[1] += '=' + keyValue[i];
          }
        }

        // ensure upper string for further checks and remoe "" around the value
        int start = 0, end = keyValue[1].length();
        if (keyValue[1].charAt(0) == '"')
        {
          start++;
          end--;
        }

        keyValuePairs.put(keyValue[0].toUpperCase(), keyValue[1].substring(start, end));
      }
    }
    return keyValuePairs;
  }
}