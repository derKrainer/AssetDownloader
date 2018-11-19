package parser.dash;

import org.w3c.dom.Node;

public class DashComponent
{
	public Node xmlContent;

	public DashComponent(Node xmlNode)
	{
		this.xmlContent = xmlNode;
	}
}
