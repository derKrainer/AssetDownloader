package parser.dash;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.AdaptationSet;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;
import parser.DashParser;

public class DashManifest extends DashComponent
{
  public List<DashPeriod> periods = new ArrayList<>();

  public double duration = -1;

  public Instant availabilityStartTime = null;
  public Instant downloadInstant = null;

  public double minimumUpdatePeriodInSeconds = -1;

  public double timeShiftBufferDepthInSec = -1;

  public boolean isLive = false;

  public DashManifest(Node xmlNode)
  {
    super(xmlNode.getFirstChild());
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes)
  {

    for (Node periodNode : specialNodes)
    {
      if (periodNode.getNodeName().equals("Period"))
      {
        DashPeriod currentPeriod = new DashPeriod(periodNode, this);

        System.out.println("processing Period: " + periodNode);
        currentPeriod.parse();

        this.periods.add(currentPeriod);
      }
      else
      {
        System.out.println("Unexpected MPD Child: " + periodNode.getNodeName());
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList)
  {
    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("mediaPresentationDuration"))
      {
        this.duration = DashParser.parseDuration(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("timeShiftBufferDepth"))
      {
        this.timeShiftBufferDepthInSec = DashParser.parseDuration(attr.getNodeValue());
      }
      else if (attr.getNodeName().equals("availabilityStartTime"))
      {
        this.availabilityStartTime = Instant.parse(attr.getNodeValue());
        this.downloadInstant = Instant.now();
      }
      else if (attr.getNodeName().equals("type"))
      {
        String streamType = attr.getNodeValue();
        if (streamType.equals("dynamic") || streamType.equals("event"))
        {
          this.isLive = true;
        }
        else if (streamType.equals("static"))
        {
          this.isLive = false;
        }
        else
        {
          this.isLive = false;
          System.err.println("Unknown MPD.type: " + streamType + ". Treating stream as VOD");
        }
      }
      else if (attr.getNodeName().equals("minimumUpdatePeriod"))
      {
        this.minimumUpdatePeriodInSeconds = DashParser.parseDuration(attr.getNodeValue());
      }
      else
      {
        System.out.println("Unhandled MPD attribute: " + attr.getNodeName());
      }
    }
  }

  public ManifestDownloadnfo generateDownloadInfo(String baseUrl, String targetFolder)
  {
    ManifestDownloadnfo retVal = new ManifestDownloadnfo();

    for (DashPeriod p : this.periods)
    {
      Period currentPeriod = new Period(p.id);
      retVal.periods.add(currentPeriod);

      for (DashAdaptationSet adSet : p.adaptationSets)
      {
        AdaptationSet dlSet = new AdaptationSet(adSet.id);
        currentPeriod.addAdaptationSet(dlSet);

        for (DashRepresentation rep : adSet.representations)
        {
          Representation dlRep = new Representation(rep.id, rep.bandwidth);
          dlSet.addRepresentation(dlRep);

          List<DownloadTarget> targets = rep.getTargetFiles(baseUrl, targetFolder);
          dlRep.filesToDownload = targets;
        }
      }
    }

    retVal.isLive = this.isLive;

    return retVal;
  }

  @Override
  protected void fillMissingValues()
  {
    // nothing as of yet
  }

  @Override
  public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl, DashRepresentation targetRepresentation)
  {
    super.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation);

    this.periods.forEach((period) -> period.adjustUrlsToTarget(targetFolder, manifestBaseUrl, targetRepresentation));
  }

}
