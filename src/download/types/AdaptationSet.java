package download.types;

import java.util.ArrayList;
import java.util.List;

public class AdaptationSet {
	public List<Representation> representations = new ArrayList<>();
	public final String name;
	
	public Period containingPeriod = null;
	
	public AdaptationSet(String name) {
		this.name = name;
	}
	
	public void addRepresentation(Representation toAdd) {
		this.representations.add(toAdd);
		toAdd.containingAdaptationSet = this;
	}
	
	@Override
	public String toString()
	{
		String repString = "\n";
		for(Representation target : this.representations)
		{
			repString += "    " + target.toString() + "\n";
		}
		
		return this.name + repString;
	}
}