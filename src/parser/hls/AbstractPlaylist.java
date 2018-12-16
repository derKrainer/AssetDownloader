package parser.hls;

import java.io.File;
import java.net.URL;
import java.util.List;

import download.DownloadHelper;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import parser.FallbackCounters;
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
    this.parser = parser;
  }

  public AbstractPlaylist(URL manifestUrl, HlsParser parser)
  {
    this.manifestUrl = manifestUrl;
    this.parser = parser;
  }

  public abstract String getUpdatedManifest();

  public abstract void writeUpdatedManifest(int numUpdate);

  public abstract List<DownloadTarget> getAllSegments();

  public abstract ManifestDownloadnfo toDownloadInfo(ManifestDownloadnfo previousResult);

  public abstract void parse();
  
  public abstract double getTargetDuration();

  public String getManifestContent()
  {
    if (this.manifestContent != null)
    {
      return this.manifestContent;
    }
    else if (this.manifestUrl != null)
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

  
  protected String getFileNameForUpdatedManifest(String playListName, int numUpdate)
  {
    if (numUpdate == 0)
    {
      return this.parser.getTargetFolderName() + playListName + ".m3u8";
    }
    else
    {
      String fileName = this.parser.getTargetFolderName() + FallbackCounters.manifestFilePrefix + playListName + "_" + numUpdate + ".m3u8";
      while (new File(fileName).exists())
      {
        FallbackCounters.manifestFilePrefix = "_" + FallbackCounters.manifestFilePrefix;
        fileName = this.parser.getTargetFolderName() + FallbackCounters.manifestFilePrefix + playListName + "_" + numUpdate + ".m3u8";
      }
      return fileName;
    }
  }

  public String[] getManifestLines()
  {
    return this.getManifestContent().split("\n");
  }

  protected AttributeLine parseKeyValuePairs(String hlsLine)
  {
    return new AttributeLine(hlsLine);
  }
}