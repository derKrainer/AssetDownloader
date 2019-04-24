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

    return updatedManifest;
  }

  @Override
  public void writeUpdatedManfiest(Representation[] selectedRepresentations, int numberDOfUpdates)
  {
    String updatedManifest = this.getUpdatedManifest(selectedRepresentations);

    String targetFileName = this.targetFolder + "manifest.mpd";
    int index = 1;
    while (new File(targetFileName).exists())
    {
      targetFileName = this.targetFolder + Integer.toString(index) + ".mpd";
      index++;
    }

    System.out.println("Writing: " + updatedManifest + "\nto: " + targetFileName);

    FileHelper.writeContentToFile(targetFileName, updatedManifest);
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
    try {
      Duration d = Duration.parse(date);
      return d.toMillis() / 1000.0;
    }
    catch(Exception ex) {
      System.err.println("Error during parsing date string " + date + ", exception: " + ex.getMessage());
      ex.printStackTrace();

    // try manual fallback
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
        }	
        else	
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
  }

  private static double parseDuration(StringBuffer sb)	
  {	
    if (sb == null || sb.length() == 0)	
    {	
      return 0;	
    }	
    return Double.parseDouble(sb.toString());	
  }

  @Override
  public int getLiveUpdateFrequency()
  {
    return (int) this.dashManifest.minimumUpdatePeriodInSeconds * 1000;
  }
}
