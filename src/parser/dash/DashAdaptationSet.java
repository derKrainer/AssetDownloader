package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

public class DashAdaptationSet extends DashComponent
{
	public ContentType dataType = ContentType.Other;
	
	public String id;
	
	public SegmentTemplate segmentTemplate = null;
	
	public List<DashRepresentation> representations = new ArrayList<>();

	public DashAdaptationSet(Node xmlNode)
	{
		super(xmlNode);
	}
}

enum ContentType {
	Video,
	Audio,
	Subtitles,
	Application,
	Other
}
