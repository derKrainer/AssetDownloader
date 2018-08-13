package download.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Representation {
	public List<DownloadTarget> filesToDownload = new ArrayList<>();
	public Map<String, String> attributes = new HashMap<>();
	
	public final String name;
	public final int bandwidth;
	public String manifestContent = null;
	public AdaptationSet containingAdaptationSet = null;
	
	public Representation(String name, int bandwidth)
	{
		this.name = name;
		this.bandwidth = bandwidth;
	}
	
	@Override
	public String toString()
	{
		return this.name + " @" + this.bandwidth;
	}
	
}