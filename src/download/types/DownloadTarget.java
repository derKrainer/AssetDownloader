package download.types;

public class DownloadTarget
{
	public String downloadURL;
	public String fileName;
	
	public DownloadTarget(String url, String fileName) {
		this.downloadURL = url;
		this.fileName = fileName;
	}
	
	@Override
	public String toString()
	{
		return "Download: " + this.downloadURL + " to: " + this.fileName;
	}
}
