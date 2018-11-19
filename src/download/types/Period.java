package download.types;

import java.util.ArrayList;
import java.util.List;

public class Period
{
	public List<AdaptationSet> adaptationSets = new ArrayList<>();
	
	public final String periodId;
	
	public Period(String id) {
		this.periodId = id;
	}
	
	public void addAdaptationSet(AdaptationSet toAdd) {
		this.adaptationSets.add(toAdd);
		toAdd.containingPeriod = this;
	}
	
	@Override
	public String toString()
	{
		String adString = "\n";
		for(AdaptationSet target : this.adaptationSets)
		{
			adString += "  " + target.toString() + "\n";
		}
		
		return this.periodId + adString;
	}
}
