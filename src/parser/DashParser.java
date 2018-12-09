package parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.dash.DashAdaptationSet;
import parser.dash.DashManifest;
import parser.dash.DashPeriod;
import parser.dash.DashRepresentation;
import util.FileHelper;
import util.XMLUtils;

public class DashParser extends AbstractParser
{
  private Document manifestDocument;
  private DashManifest dashManifest;
  public String baseUrl;

  public DashParser(String targetFolder)
  {
    super(targetFolder);
  }

  @Override
  public ManifestDownloadnfo internalParse(String manifestContent, String manifestUrl)
      throws MalformedURLException, IOException
  {
    this.manifestDocument = XMLUtils.parseXml(manifestContent);

    this.dashManifest = this.parseMPDInformation();

    this.baseUrl = manifestUrl.substring(0, manifestUrl.lastIndexOf('/'));

    ManifestDownloadnfo dlInfo = dashManifest.generateDownloadInfo(this.baseUrl, this.targetFolder);
    // System.out.println(dlInfo.toDebugString());

    return dlInfo;
  }

  @Override
  public String getUpdatedManifest(Representation[] selectedRepresentations)
  {
    if (this.dashManifest == null)
    {
      throw new RuntimeException("Unable to generate updated manifest, because there is not parsed one yet");
    }

    String[] selectedIds = new String[selectedRepresentations.length];
    for (int i = 0; i < selectedRepresentations.length; i++)
    {
      selectedIds[i] = selectedRepresentations[i].generateId(true);
    }

    List<DashRepresentation> selectedManifesRepresentations = new ArrayList<>();
    List<DashRepresentation> representationsToRemove = new ArrayList<>();
    // find matching representations
    for (DashPeriod dashPeriod : this.dashManifest.periods)
    {
      for (DashAdaptationSet dashAdSet : dashPeriod.adaptationSets)
      {
        for (DashRepresentation dashRep : dashAdSet.representations)
        {
          if (isSelectedRepresentation(selectedRepresentations, dashRep))
          {
            selectedManifesRepresentations.add(dashRep);
          }
          else
          {
            representationsToRemove.add(dashRep);
          }
        }
      }
    }

    // remove all unwanted representations from the manifest
    this.removeUnwantedRepresentations(representationsToRemove);

    for (DashRepresentation keptRep : selectedManifesRepresentations)
    {
      keptRep.adjustUrlsToTarget("", this.baseUrl, keptRep);
    }

    // replace / add possible BaseURL
    String updatedManifest = XMLUtils.writeXmlToString(this.dashManifest.xmlContent);
    System.out.println(updatedManifest);
    String targetFileName = this.targetFolder + "manifest.mpd";
    int index = 1;
    while (new File(targetFileName).exists())
    {
      targetFileName = this.targetFolder + Integer.toString(index) + ".mpd";
      index++;
    }

    FileHelper.writeContentToFile(targetFileName, updatedManifest);

    return updatedManifest;
  }

  private void removeUnwantedRepresentations(List<DashRepresentation> toRemove)
  {
    for (DashRepresentation rep : toRemove)
    {
      DashAdaptationSet parent = rep.parent;
      if (!parent.removeChild(rep))
      {
        System.out.println("Unable to remove " + rep.id + " from parent");
      }
      // if the adaptation set does not contain any representations anymore, remove it
      // too
      if (parent.representations.size() == 0)
      {
        parent.parent.removeChild(parent);
      }
    }
  }

  private boolean isSelectedRepresentation(Representation[] selectRepresentations, DashRepresentation dashRep)
  {
    String currentId = dashRep.generateId();

    for (Representation selected : selectRepresentations)
    {
      if (selected.generateId(true).equals(currentId))
      {
        return true;
      }
    }

    return false;
  }

  private DashManifest parseMPDInformation()
  {
    DashManifest manifest = new DashManifest(manifestDocument);

    manifest.parse();

    return manifest;
  }

  public static double parseDuration(String date)
  {
    Duration d = Duration.parse(date);
    return d.toMillis() / 1000.0;
  }

  @Override
  public int getLiveUpdateFrequency()
  {
    return (int) this.dashManifest.minimumUpdatePeriodInSeconds * 1000;
  }
}
