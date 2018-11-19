package parser.dash.subcomponents;

import java.util.List;

import org.w3c.dom.Node;

import parser.dash.DashComponent;

public class BaseUrl extends DashComponent
{
	public static final String NODE_NAME = "BaseURL";
	
	public String baseUrl;
	
	public BaseUrl(Node xmlContent)
	{
		super(xmlContent);
	}

	@Override
	protected void parseSpecialNodes(List<Node> specialNodes)
	{
		this.baseUrl = this.xmlContent.getTextContent();
	}

	@Override
	protected void parseAttributes(List<Node> specialAttributesList)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void fillMissingValues()
	{
		// nothing to do here
	}
}
