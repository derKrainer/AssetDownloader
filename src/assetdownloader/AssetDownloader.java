package assetdownloader;

import download.DownloadHelper;
import download.types.ManifestDownloadnfo;
import parser.DashParser;
import parser.HlsParser;

enum ManifestType {
	HLS,
	DASH,
	SMOOTH,
	UNKNOWN,
}

public class AssetDownloader
{
	private String manifestURL;
	private ManifestType manifestType;
	private String manifestContent;
	private ManifestDownloadnfo toDownload;
	private String targetFolder;
	
	public AssetDownloader(String manifestURL, String targetFolder) {
		this.manifestURL = manifestURL;
		this.targetFolder = targetFolder;

		this.getManifest();
		this.getTypeForManifest(manifestURL);
		this.parseManifest();
		
		DownloadHelper.downloadForDownloadInfo(this.toDownload);;
	}
	
	private void getManifest() {
		this.manifestContent = DownloadHelper.getContent(this.manifestURL);
		System.out.println(this.manifestContent);
	}
	
	protected void getTypeForManifest(String manifestUrl) {
		if (manifestURL.contains(".m3u8")) {
			this.manifestType = ManifestType.HLS;
		}
		else if (manifestURL.contains(".mpd")) {
			this.manifestType = ManifestType.DASH;
		}
		else if (manifestURL.contains("/manifest")) {
			this.manifestType = ManifestType.SMOOTH;
		}
		else {
			// try and extract information from the manifest content if we did not have any luck with the URL
			if (this.manifestContent.contains("<MPD")) {
				this.manifestType = ManifestType.DASH;
			}
			else if (this.manifestContent.contains("#EXT-INF")) {
				this.manifestType = ManifestType.HLS;
			}
			else if (this.manifestContent.contains("<Smooth")) {
				this.manifestType = ManifestType.SMOOTH;
			} else {
				this.manifestType = ManifestType.UNKNOWN;
			}
		}
	}
	
	private void parseManifest() {
		switch (this.manifestType)
		{
		case HLS:
			this.toDownload = new HlsParser(this.targetFolder).parseManifest(this.manifestContent, this.manifestURL);
			break;
		case DASH: 
			this.toDownload = new DashParser(this.targetFolder).parseManifest(this.manifestContent, this.manifestURL);
			break;
		default:
			throw new RuntimeException("not implemented yet");
		}
	}
	
	public static void main(String[] args)
	{
//		String manifestUrl = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/m3u8s/11331.m3u8";
//		String manifestUrl = "http://ak-accuweather.akamaized.net/02fe7174-cbe9-4b85-9dc4-a47c42efd108/9sa3dqZzE6SjuE2-yuIku2kRhCyJhkmW.ism/Manifest(format=mpd-time-csf)";
		String manifestUrl = "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/mpds/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.mpd";
		String targetFolder = "download/";
		if (args.length > 0) {
			manifestUrl = args[0];
		}
		if (args.length > 1) {
			targetFolder = args[1];
		}
		if (targetFolder.charAt(targetFolder.length() -1) != '/') {
			targetFolder += '/';
		}
		
		AssetDownloader dl = new AssetDownloader(manifestUrl, targetFolder);
	}
}
