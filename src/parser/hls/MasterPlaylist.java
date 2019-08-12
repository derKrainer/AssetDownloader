package parser.hls;

import java.lang.Thread.State;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.HlsParser;
import util.FileHelper;
import util.URLUtils;

public class MasterPlaylist extends AbstractPlaylist
{
  public List<MediaPlaylist> childLists = new ArrayList<>();
  private String updatedMaster;
  private List<Thread> childParserThreads = new ArrayList<>();

  public MasterPlaylist(String manifestContent, String manifestUrl, HlsParser parser)
  {
    super(manifestContent, manifestUrl, parser);
  }

  @Override
  public List<DownloadTarget> getAllSegments()
  {
    List<DownloadTarget> allTargets = new ArrayList<>();

    for (MediaPlaylist playlist : this.childLists)
    {
      allTargets.addAll(playlist.getAllSegments());
    }

    return allTargets;
  }

  @Override
  public String getUpdatedManifest(Representation[] selectedReps)
  {
    String newManifestContent = this.updatedMaster;
    List<MediaPlaylist> remainingPlaylists = new ArrayList<>();
    List<MediaPlaylist> deletedPlaylists = new ArrayList<>();
    for (Representation rep : selectedReps)
    {
      MediaPlaylist media = this.findMediaPlaylistForRepresentation(rep);
      remainingPlaylists.add(media);
    }

    // remove all deleted media playlists from the updated manifest
    for (MediaPlaylist media : this.childLists)
    {
      if (remainingPlaylists.indexOf(media) == -1)
      {
        deletedPlaylists.add(media);

        int positionInManifest = newManifestContent.indexOf(media.masterPlaylistString);
        if (positionInManifest >= 0)
        {
          String preDelete = newManifestContent.substring(0, positionInManifest);
          String postDelete = newManifestContent
              .substring(positionInManifest + 1 + media.masterPlaylistString.length());
          newManifestContent = preDelete + postDelete;
        }
        else
        {
          System.err.println("Could not find String " + media.masterPlaylistString + " in master, skipping it");
        }
      }
    }

    System.out.println(newManifestContent);
    return newManifestContent;
  }

  private MediaPlaylist findMediaPlaylistForRepresentation(Representation rep)
  {
    for (MediaPlaylist media : this.childLists)
    {
      if (media.id.equals(rep.id))
      {
        return media;
      }
    }
    throw new RuntimeException("Unable to find matching MediaPlaylist for Representation");
  }

  @Override
  public void writeUpdatedManifest(Representation[] selectedReps, int numUpdate)
  {
    String targetFile = this.getFileNameForUpdatedManifest("master", numUpdate);
    FileHelper.writeContentToFile(targetFile, this.getUpdatedManifest(selectedReps));

    for (MediaPlaylist child : this.childLists)
    {
      child.writeUpdatedManifest(selectedReps, numUpdate);
    }
  }

  private static int numberOfParses = 0;

  @Override
  public void parse()
  {
    StringBuffer updatedManifest = new StringBuffer();
    String[] allLines = this.getManifestLines();
    List<AttributeLine> preceedingAttributes = new ArrayList<>();

    for (int i = 0; i < allLines.length; i++)
    {
      String currentLine = allLines[i];

      if (currentLine.startsWith("#"))
      {
        preceedingAttributes.add(this.parseKeyValuePairs(currentLine));
      }

      if (currentLine.startsWith("#EXT-X-STREAM-INF"))
      {
        String mediaPlaylistLocation = allLines[++i];
        mediaPlaylistLocation = URLUtils.makeAbsoulte(mediaPlaylistLocation, this.parser.getBaseUrl());
        // String updatedLocation = URLUtils.makeUrlRelative(mediaPlaylistLocation, this.parser.getBaseUrl());
        // updatedManifest.append(updatedLocation).append('\n');
        MediaPlaylist newList = this.addMediaPlaylist(mediaPlaylistLocation, preceedingAttributes);
        updatedManifest.append(currentLine).append('\n');
        String updatedMediaPlaylistLocation = newList.getUpdatedPlaylistName(numberOfParses);
        String masterPlaylistContent = currentLine + '\n' + updatedMediaPlaylistLocation;
        newList.masterPlaylistString = masterPlaylistContent;
        updatedManifest.append(newList.getUpdatedPlaylistName(numberOfParses)).append('\n');
      }
      else if ((currentLine.startsWith("#EXT-X-MEDIA") && !currentLine.startsWith("#EXT-X-MEDIA-SEQUENCE"))
          || currentLine.startsWith("#EXT-X-I-FRAME-STREAM-INF"))
      {
        AttributeLine attributes = preceedingAttributes.get(preceedingAttributes.size() - 1);
        String mediaPlaylistLocation = attributes.get("URI");

        // caption streams may not include URIs
        if (mediaPlaylistLocation == null) {
          continue;
        }

        MediaPlaylist newList = this.addMediaPlaylist(mediaPlaylistLocation, preceedingAttributes);
        String updatedEntry = currentLine.replace(mediaPlaylistLocation,
            newList.getUpdatedPlaylistName(numberOfParses));
        newList.masterPlaylistString = updatedEntry;
        updatedManifest.append(updatedEntry).append('\n');
      }
      else
      {
        updatedManifest.append(currentLine).append('\n');
      }
    }
    this.updatedMaster = updatedManifest.toString();

    // wait until all children are done too
    while (this.areChildParsersRunning())
    {
      try
      {
        Thread.sleep(100);
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    numberOfParses++;
  }

  private boolean areChildParsersRunning()
  {
    for (Thread t : this.childParserThreads)
    {
      if (t.getState() != State.TERMINATED)
      {
        return true;
      }
    }
    return false;
  }

  private MediaPlaylist addMediaPlaylist(String manifestUrl, List<AttributeLine> preceedingAttributes)
  {
    String mediaPlaylistLocation = URLUtils.makeAbsoulte(manifestUrl, this.parser.getBaseUrl());
    try
    {
      URL mediaPlaylistUrl = new URL(mediaPlaylistLocation);
      MediaPlaylist newPlaylist = new MediaPlaylist(mediaPlaylistUrl, preceedingAttributes, this.parser);
      Thread childParser = new Thread(new Runnable() {
        @Override
        public void run()
        {
          newPlaylist.parse();
        }
      });
      this.childParserThreads.add(childParser);
      childParser.start();

      this.childLists.add(newPlaylist);
      preceedingAttributes = new ArrayList<>();

      return newPlaylist;
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Invalid playlist url: " + mediaPlaylistLocation, ex);
    }
  }

  @Override
  public ManifestDownloadnfo toDownloadInfo(ManifestDownloadnfo previousResult)
  {
    ManifestDownloadnfo retVal = previousResult;

    for (MediaPlaylist childPlaylist : this.childLists)
    {
      retVal = childPlaylist.toDownloadInfo(retVal);
    }

    return retVal;
  }

  @Override
  public double getTargetDuration()
  {
    if (this.childLists.isEmpty())
    {
      return 0;
    }
    return this.childLists.get(0).getTargetDuration();
  }
}