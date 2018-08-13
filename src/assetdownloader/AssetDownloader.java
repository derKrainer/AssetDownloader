package assetdownloader;

import download.DownloadHelper;
import download.types.ManifestDownloadnfo;
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
		
		this.getTypeForManifest(manifestURL);
		this.getManifest();
		this.parseManifest();
		
		DownloadHelper.downloadForDownloadInfo(this.toDownload);;
	}
	
	private void getManifest() {
		this.manifestContent = DownloadHelper.getContent(this.manifestURL);
		System.out.println(this.manifestContent);
	}
	
	protected void getTypeForManifest(String manifestUrl) {
		if (manifestURL.indexOf(".m3u8") > -1 ) {
			this.manifestType = ManifestType.HLS;
		}
		else if (manifestURL.indexOf(".mpd") > -1) {
			this.manifestType = ManifestType.DASH;
		}
		else if (manifestURL.indexOf("/manifest") > -1) {
			this.manifestType = ManifestType.SMOOTH;
		}
		else {
			// TODO: actually check the manifest content for <MPD / <Smooth / #EXT-INF
			this.manifestType = ManifestType.UNKNOWN;
		}
	}
	
	private void parseManifest() {
		switch (this.manifestType)
		{
		case HLS:
			this.toDownload = new HlsParser(this.targetFolder).parseManifest(this.manifestContent, this.manifestURL);
			break;

		default:
			throw new RuntimeException("not implemented yet");
		}
	}
	
	public static void main(String[] args)
	{
		String manifestUrl = "https://url-to-your-stream.m3u8";
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
