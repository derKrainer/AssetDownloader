package download.types;

import java.util.ArrayList;
import java.util.List;

public class ManifestDownloadnfo
{
	public List<Period> periods = new ArrayList<>();
	
	public final String baseURL;
	
	public ManifestDownloadnfo(String baseUrl) {
		this.baseURL = baseUrl;
	}
	
}