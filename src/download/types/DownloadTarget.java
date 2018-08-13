package download.types;

public class DownloadTarget
{
	public String downloadURL;
	public String fileName;
	
	public DownloadTarget(String url, String fileName) {
		this.downloadURL = url;
		this.fileName = fileName;
	}
}
