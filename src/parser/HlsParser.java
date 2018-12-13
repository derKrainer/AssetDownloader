package parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import download.DownloadHelper;
import download.types.AdaptationSet;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;
import parser.hls.AbstractPlaylist;
import parser.hls.MasterPlaylist;
import parser.hls.MediaPlaylist;
import util.FileHelper;

public class HlsParser extends AbstractParser
{
  public AbstractPlaylist masterPlaylist;

  public static final String PERIOD_ID_PREFIX = "period_";

  public static final String DISCONTINUITY_TAG = "#EXT-X-DISCONTINUITY";

  public HlsParser(String folderName)
  {
    super(folderName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see parser.IParser#parseManifest(java.lang.String, java.lang.String)
   */
  @Override
  public ManifestDownloadnfo internalParse(String manifestContent, String manifestUrl)
      throws MalformedURLException, IOException
  {
    MasterPlaylist master = new MasterPlaylist(manifestContent, this);
    if (master.childLists.isEmpty())
    {
      // single variant playlist
      Map<String, String> defaultAttributes = new HashMap<>();
      defaultAttributes.put("GROUP-ID", "default");
      List<Map<String, String>> attributesList = new ArrayList<>();
      attributesList.add(defaultAttributes);
      this.masterPlaylist = new MediaPlaylist(manifestContent, attributesList, this);
    }
    else
    {
      this.masterPlaylist = master;
    }

    masterPlaylist.getAllSegments();

    return null;
  }

  @Override
  public String getUpdatedManifest(Representation[] selectedRepresentations)
  {
    return this.masterPlaylist.getUpdatedManifest();
  }

  @Override
  public int getLiveUpdateFrequency()
  {
    // TOOD: replace with #TARGET-DURATION or min(#EXT-INF)
    return 3;
  }
}
