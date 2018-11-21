package parser;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.dash.DashAdaptationSet;
import parser.dash.DashManifest;
import parser.dash.DashPeriod;
import parser.dash.DashRepresentation;
import util.XMLUtils;

public class DashParser implements IParser
{
  private Document manifestDocument;
  private String targetFolder;
  private DashManifest dashManifest;

  public DashParser(String targetFolder)
  {
    this.targetFolder = targetFolder;
  }

  @Override
  public ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl) {
    this.manifestDocument = XMLUtils.parseXml(manifestContent);

    this.dashManifest = this.parseMPDInformation();

    ManifestDownloadnfo dlInfo = dashManifest.generateDownloadInfo(manifestUrl);
    System.out.println(dlInfo.toDebugString());

    return dlInfo;
  }

  @Override
  public String getUpdatedManifest(Representation[] selectedRepresentations) {
    if (this.dashManifest == null)
    {
      throw new RuntimeException("Unable to generate updated manifest, because there is not parsed one yet");
    }

    String[] selectedIds = new String[selectedRepresentations.length];
    for (int i = 0; i < selectedRepresentations.length; i++)
    {
      selectedIds[i] = selectedRepresentations[i].generateId();
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
          } else
          {
            representationsToRemove.add(dashRep);
          }
        }
      }
    }

    // remove all unwanted representations from the manifest
    this.removeUnwantedRepresentations(representationsToRemove);

    System.out.println(XMLUtils.writeXmlToString(this.dashManifest.xmlContent));
    // replace all urls with updated relative urls
    // replace / add possible BaseURL

    return null;
  }

  private void removeUnwantedRepresentations(List<DashRepresentation> toRemove) {
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

  private boolean isSelectedRepresentation(Representation[] selectRepresentations, DashRepresentation dashRep) {
    String currentId = dashRep.generateId();

    for (Representation selected : selectRepresentations)
    {
      if (selected.generateId().equals(currentId))
      {
        return true;
      }
    }

    return false;
  }

  private DashManifest parseMPDInformation() {
    DashManifest manifest = new DashManifest(manifestDocument);

    manifest.parse();

    return manifest;
  }

  public static double parseDuration(String date) throws Exception {
    // parse something like P0Y0M0DT0H3M30.000S
    double duration = 0;
    char lastChar = '0';
    StringBuffer currentPart = new StringBuffer();
    int multiplier = 0;
    for (int i = date.length() - 1; i >= 0; i--)
    {
      switch (date.charAt(i)) {
      case 'S':
      {
        // seconds
        multiplier = 1;
        currentPart = new StringBuffer();
        lastChar = 'S';
        break;
      }
      case 'M':
      {
        // minutes or month
        duration += parseDuration(currentPart) * multiplier;
        if (lastChar == 'S' || lastChar == '0')
        {
          // minutes
          multiplier = 60;
        } else
        {
          // month
          // TODO: real month values if needed at some point?
          multiplier = 3600 * 24 * 30;
        }
        currentPart = new StringBuffer();
        break;
      }
      case 'H':
      {
        // hours
        duration += parseDuration(currentPart) * multiplier;
        multiplier = 3600;
        currentPart = new StringBuffer();
        lastChar = 'H';
        break;
      }
      case 'T':
      case 'P':
      {
        // skip PT
        duration += parseDuration(currentPart) * multiplier;
        currentPart = new StringBuffer();
        lastChar = 'T';
        break;
      }
      case 'D':
      {
        // day
        duration += parseDuration(currentPart) * multiplier;
        currentPart = new StringBuffer();
        multiplier = 3600 * 24;
        lastChar = 'D';
        break;
      }
      case 'Y':
      {
        duration += parseDuration(currentPart) * multiplier;
        currentPart = new StringBuffer();
        multiplier = 3600 * 365;
        break;
      }
      default:
      {
        currentPart.insert(0, date.charAt(i));
      }
      }
    }
    duration += parseDuration(currentPart) * multiplier;

    return duration;
  }

  private static double parseDuration(StringBuffer sb) {
    if (sb == null || sb.length() == 0)
    {
      return 0;
    }
    return Double.parseDouble(sb.toString());
  }

}
