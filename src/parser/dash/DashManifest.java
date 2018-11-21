package parser.dash;

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

  public DashManifest(Node xmlNode)
  {
    super(xmlNode.getFirstChild());
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) {

    for (Node periodNode: specialNodes)
    {
      if (periodNode.getNodeName().equals("Period"))
      {
        DashPeriod currentPeriod = new DashPeriod(periodNode, this);

        System.out.println("processing Period: " + periodNode);
        currentPeriod.parse();

        this.periods.add(currentPeriod);
      } else
      {
        System.out.println("Unexpected MPD Child: " + periodNode.getNodeName());
      }
    }
  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList) {
    // TODO: optional MPD values?
    for (Node attr : specialAttributesList)
    {
      if (attr.getNodeName().equals("mediaPresentationDuration"))
      {
        try
        {
          this.duration = DashParser.parseDuration(attr.getNodeValue());
        } catch (Exception ex)
        {
          throw new RuntimeException("Error parsing mpd duration", ex);
        }
      } else
      {
        System.out.println("Unhandled MPD attribute: " + attr.getNodeName());
      }
    }
  }

  public ManifestDownloadnfo generateDownloadInfo(String baseUrl, String targetFolder) {
    ManifestDownloadnfo retVal = new ManifestDownloadnfo(baseUrl);

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
          for (DownloadTarget dlTarget : targets)
          {
            dlRep.filesToDownload.add(dlTarget);
          }
        }
      }
    }

    return retVal;
  }

  @Override
  protected void fillMissingValues() {
    // nothing as of yet
	}
	
	@Override
	public void adjustUrlsToTarget(String targetFolder, String manifestBaseUrl)
	{
		super.adjustUrlsToTarget(targetFolder, manifestBaseUrl);

		this.periods.forEach((period) -> period.adjustUrlsToTarget(targetFolder, manifestBaseUrl));
	}

}
