package parser.hls;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import download.types.AdaptationSet;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;
import parser.FallbackCounters;
import parser.HlsParser;
import util.FileHelper;
import util.URLUtils;

public class MediaPlaylist extends AbstractPlaylist
{
  public List<AttributeLine> attributes;

  public List<SegmentInfo> segentInfos = new ArrayList<>();
  private SegmentInfo lastLinesOfManifest;

  public String id;
  public String groupID;
  public int bandwidth = -1;

  public MediaPlaylist(String manfiestContent, List<AttributeLine> attributes, HlsParser parser)
  {
    super(manfiestContent, parser);
    this.attributes = attributes;
    this.init();
  }

  public MediaPlaylist(URL manfiestLocation, List<AttributeLine> attributes, HlsParser parser)
  {
    super(manfiestLocation, parser);
    this.attributes = attributes;
    this.init();
  }

  private void init()
  {
    for (AttributeLine attributeLine : this.attributes)
    {
      if (attributeLine.get("GROUP-ID") != null)
      {
        this.groupID = attributeLine.get("GROUP-ID");
      }
      else if (attributeLine.get("BANDWIDTH") != null)
      {
        this.bandwidth = Integer.parseInt(attributeLine.get("BANDWIDTH"));
      }
      else if (attributeLine.get("ID") != null)
      {
        this.id = attributeLine.get("ID");
      }
    }

    if (this.id == null)
    {
      this.id = Integer.toString(FallbackCounters.RepresentationId++);
    }
    if (this.bandwidth == -1)
    {
      this.bandwidth = FallbackCounters.Bandwidth++;
    }
  }

  @Override
  public List<DownloadTarget> getAllSegments()
  {
    List<DownloadTarget> allTargets = new ArrayList<>(this.segentInfos.size());

    for (SegmentInfo info : this.segentInfos)
    {
      allTargets.add(info.toDownloadTarget());
    }

    return allTargets;
  }

  @Override
  public String getUpdatedManifest(Representation[] selectedRepresentations)
  {
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
  public void writeUpdatedManifest(Representation[] selectedRepresentations, int numUpdate)
  {
    String fileName = this.getFileNameForUpdatedManifest(this.id + '@' + this.bandwidth, numUpdate);
    FileHelper.writeContentToFile(fileName, this.getUpdatedManifest(selectedRepresentations));
  }

  @Override
  public void parse()
  {
    String[] lines = this.getManifestLines();
    int discontinuityNumber = 0;

    SegmentInfo segmentInfo = new SegmentInfo(this);
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
        AttributeLine attributes = segmentInfo.preceedingAttributes.get(segmentInfo.preceedingAttributes.size() - 1);
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
        segmentInfo = new SegmentInfo(this);
      }

      if (currentLine.startsWith("#EXT-X-ENDLIST"))
      {
        this.isLive = false;
      }
    }
    this.lastLinesOfManifest = segmentInfo;
  }

  @Override
  public ManifestDownloadnfo toDownloadInfo(ManifestDownloadnfo previousResult)
  {
    ManifestDownloadnfo retVal = previousResult;
    if (retVal == null)
    {
      // create a new one and add basic structure
      retVal = new ManifestDownloadnfo();
      retVal.isLive = this.isLive;
      this.addDefaultPeriod(retVal, this.segentInfos.get(0).discontinuityNumber);
    }

    List<SegmentInfo> allSegments = this.segentInfos;
    for (SegmentInfo segInfo : allSegments)
    {
      this.addSegmentInfo(segInfo, retVal);
    }

    return retVal;
  }

  private Representation addDefaultPeriod(ManifestDownloadnfo dlInfo, int discontinuityNumber)
  {
    String firstPeriodID = Integer.toString(discontinuityNumber);
    Period firstPeriod = new Period(firstPeriodID);
    dlInfo.periods.add(firstPeriod);

    AdaptationSet defaultAdSet = new AdaptationSet(this.groupID == null ? "0" : this.groupID);
    firstPeriod.addAdaptationSet(defaultAdSet);

    Representation rep = new Representation(this.id, this.bandwidth);
    defaultAdSet.addRepresentation(rep);

    return rep;
  }

  private void addSegmentInfo(SegmentInfo segInfo, ManifestDownloadnfo downloadInfo)
  {
    int discontinuityNumber = segInfo.discontinuityNumber;
    Period p = downloadInfo.getPeriodForId(Integer.toString(discontinuityNumber));
    Representation rep;
    if (p == null)
    {
      rep = this.addDefaultPeriod(downloadInfo, discontinuityNumber);
    }
    else
    {
      AdaptationSet containingSet = p.getAdaptationSetForID(this.groupID == null ? "0" : this.groupID);
      rep = containingSet.getRepresentationForId(this.id);

      if (rep == null)
      {
        rep = new Representation(segInfo.parent.id, segInfo.parent.bandwidth);
        containingSet.addRepresentation(rep);
      }
    }

    rep.filesToDownload.add(segInfo.toDownloadTarget());
  }
  
  @Override
  public double getTargetDuration()
  {
    for (AttributeLine attr : this.segentInfos.get(0).preceedingAttributes)
    {
      if ("#EXT-X-TARGETDURATION".equals(attr.attributeName))
      {
        return Double.parseDouble(attr.get(""));
      }
    }
    return 0;
  }
}