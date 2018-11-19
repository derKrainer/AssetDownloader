package parser.dash;

import org.w3c.dom.Node;

public class DashRepresentation extends DashComponent
{
	public String id;
	
	public String bandwidth;
	
	public SegmentTemplate segmentTemplate = null;
	
	public DashRepresentation(Node xmlNode)
	{
		super(xmlNode);
	}

}
