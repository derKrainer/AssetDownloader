package parser.hls;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import download.types.DownloadTarget;
import parser.HlsParser;
import util.URLUtils;

public class MediaPlaylist extends AbstractPlaylist
{
  public List<Map<String, String>> attributes;

  public List<SegmentInfo> segentInfos = new ArrayList<>();
  private SegmentInfo lastLinesOfManifest;

  public String id;

  public MediaPlaylist(String manfiestContent, List<Map<String, String>> attributes, HlsParser parser)
  {
    super(manfiestContent, parser);
    this.attributes = attributes;
    this.init();
  }

  public MediaPlaylist(URL manfiestLocation, List<Map<String, String>> attributes, HlsParser parser)
  {
    super(manfiestLocation, parser);
    this.attributes = attributes;
    this.init();
  }

  private void init() 
  {
    for (Map<String, String> attributeLine : this.attributes)
    {
      if (attributeLine.get("GROUP-ID") != null)
      {
        this.id = attributeLine.get("GROUP-ID");
      }
      else if (attributeLine.get("BANDWIDTH") != null)
      {
        this.id = attributeLine.get("BANDWIDTH");
      }
    }
  }

  @Override
  public List<DownloadTarget> getAllSegments() {
    List<DownloadTarget> allTargets = new ArrayList<>(this.segentInfos.size());

    for (SegmentInfo info : this.segentInfos)
    {
      allTargets.add(info.toDownloadTarget());
    }

    return allTargets;
  }

  @Override
  public String getUpdatedManifest() {
    StringBuffer newManifest = new StringBuffer();

    for (SegmentInfo info : this.segentInfos)
    {
      newManifest.append(info.toUpdatedManiest());
    }

    for (String endLines : this.lastLinesOfManifest.preceedingLines)
    {
      newManifest.append(endLines).append('\n');
    }

    return newManifest.toString();
  }

  @Override
  public void parse()
  {
    String[] lines = this.getManifestLines();
    int discontinuityNumber = 0;

    SegmentInfo segmentInfo = new SegmentInfo();
    for (int i = 0; i < lines.length; i++)
    {
      String currentLine = lines[i];

      segmentInfo.preceedingLines.add(currentLine);

      if (currentLine.startsWith("#"))
      {
        segmentInfo.preceedingAttributes.add(this.parseKeyValuePairs(currentLine));
      }

      if (currentLine.startsWith("#EXT-X-DISCONTINUITY-SEQUENCE"))
      {
        Map<String, String> attributes = segmentInfo.preceedingAttributes.get(segmentInfo.preceedingAttributes.size() -1);
        discontinuityNumber = Integer.parseInt(attributes.get("#EXT-X-DISCONTINUITY-SEQUENCE"));
      }
      else if (currentLine.startsWith("#EXT-X-DISCONTINUITY"))
      {
        discontinuityNumber++;
      }

      
      if (currentLine.startsWith("#EXTINF"))
      {
        // next line must be a url
        String urlLine = lines[++i];
        segmentInfo.segmentUrl = URLUtils.makeAbsoulte(urlLine, this.parser.getBaseUrl());        

        segmentInfo.finish(discontinuityNumber);
        this.segentInfos.add(segmentInfo);
        segmentInfo = new SegmentInfo();
      }
      
      if (currentLine.startsWith("#EXT-X-ENDLIST"))
      {
        this.isLive = false;
      }
    }
    this.lastLinesOfManifest = segmentInfo;
  }
}