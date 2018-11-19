package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class DashPeriod extends DashComponent
{
	public DashPeriod(Node xmlNode)
	{
		super(xmlNode);
	}

	public List<DashAdaptationSet> adaptationSets = new ArrayList<>();
	
	public String id;
}
