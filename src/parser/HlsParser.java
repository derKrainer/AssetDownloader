package parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.hls.AbstractPlaylist;
import parser.hls.AttributeLine;
import parser.hls.MasterPlaylist;
import parser.hls.MediaPlaylist;

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
    master.parse();
    if (master.childLists.isEmpty())
    {
      // single variant playlist
      List<AttributeLine> attributesList = new ArrayList<>();
      AttributeLine defaultLine = new AttributeLine("DEFAULT-INFO: GROUP-ID=default");
      attributesList.add(defaultLine);
      this.masterPlaylist = new MediaPlaylist(manifestContent, attributesList, this);
    }
    else
    {
      this.masterPlaylist = master;
    }

    return masterPlaylist.toDownloadInfo(null);
  }

  @Override
  public String getUpdatedManifest(Representation[] selectedRepresentations)
  {
    // TODO: actually filter out unwanted reps
    return this.masterPlaylist.getUpdatedManifest();
  }

  @Override
  public void writeUpdatedManfiest(Representation[] selectedRepresentations, int numberOfUpdates) 
  {
    this.masterPlaylist.writeUpdatedManifest(numberOfUpdates);  
  }

  @Override
  public int getLiveUpdateFrequency()
  {
    return ((int)(this.masterPlaylist.getTargetDuration() * 1000)) / 1000;
  }
}
