package assetdownloader;

import download.DownloadHelper;
import download.types.ManifestDownloadnfo;
import parser.DashParser;
import parser.HlsParser;
import parser.IParser;

enum ManifestType
{
  HLS, DASH, SMOOTH, UNKNOWN,
}

public class AssetDownloader
{
  private String manifestURL;
  private ManifestType manifestType;
  private String manifestContent;
  private ManifestDownloadnfo toDownload;
  private String targetFolder;
  private IParser manifestParser;

  public AssetDownloader(String manifestURL, String targetFolder)
  {
    this.manifestURL = manifestURL;
    this.targetFolder = targetFolder;

    this.getManifest();
    this.getTypeForManifest(manifestURL);
    this.parseManifest();

    DownloadHelper.downloadForDownloadInfo(this.toDownload, this.manifestParser);
  }

  private void getManifest()
  {
    this.manifestContent = DownloadHelper.getContent(this.manifestURL);
    System.out.println(this.manifestContent);
  }

  protected void getTypeForManifest(String manifestUrl)
  {
    if (manifestURL.contains(".m3u8"))
    {
      this.manifestType = ManifestType.HLS;
    }
    else if (manifestURL.contains(".mpd"))
    {
      this.manifestType = ManifestType.DASH;
    }
    // dont do that as smooth streams converted to dash will fail
    // else if (manifestURL.contains("/manifest"))
    // {
    // this.manifestType = ManifestType.SMOOTH;
    // }
    else
    {
      // try and extract information from the manifest content if we did not have any
      // luck with the URL
      if (this.manifestContent.contains("<MPD"))
      {
        this.manifestType = ManifestType.DASH;
      }
      else if (this.manifestContent.contains("#EXT-INF"))
      {
        this.manifestType = ManifestType.HLS;
      }
      else if (this.manifestContent.contains("<Smooth"))
      {
        this.manifestType = ManifestType.SMOOTH;
      }
      else
      {
        this.manifestType = ManifestType.UNKNOWN;
      }
    }
  }

  private void parseManifest()
  {
    switch (this.manifestType) {
    case HLS:
      this.manifestParser = new HlsParser(this.targetFolder);
      break;
    case DASH:
      this.manifestParser = new DashParser(this.targetFolder);
      break;
    default:
      throw new RuntimeException("not implemented yet");
    }
    this.toDownload = this.manifestParser.parseManifest(this.manifestContent, this.manifestURL);
  }

  public static void main(String[] args)
  {
//		String manifestUrl = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8";
    // String manifestUrl =
    // "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd";
    String manifestUrl = "http://samplescdn.origin.mediaservices.windows.net/e0e820ec-f6a2-4ea2-afe3-1eed4e06ab2c/AzureMediaServices_Overview.ism/manifest(format=mpd-time-csf)";
    String targetFolder = "download/";
    if (args.length > 0)
    {
      manifestUrl = args[0];
    }
    if (args.length > 1)
    {
      targetFolder = args[1];
    }
    if (targetFolder.charAt(targetFolder.length() - 1) != '/')
    {
      targetFolder += '/';
    }

    AssetDownloader dl = new AssetDownloader(manifestUrl, targetFolder);
  }
}
