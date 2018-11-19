package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.subcomponents.SegmentTemplate;

public class DashRepresentation extends DashAdaptationSet
{
	public String bandwidth;
	public int width = -1;
	public int height = -1;

	public DashAdaptationSet parent;

	public DashRepresentation(Node xmlNode, DashAdaptationSet parent)
	{
		super(xmlNode, parent.parent);
		this.parent = parent;
	}

	@Override
	protected void parseSpecialNodes(List<Node> specialNodes)
	{
		super.parseSpecialNodes(specialNodes);

		// TODO Auto-generated method stub
	}

	@Override
	protected void parseAttributes(List<Node> specialAttributesList)
	{
		super.parseAttributes(specialAttributesList);

		for (Node attr : specialAttributesList)
		{
			if (attr.getNodeName().equals("bandwidth"))
			{
				this.bandwidth = attr.getNodeValue();
			}
			else if (attr.getNodeName().equals("width"))
			{
				this.width = Integer.parseInt(attr.getNodeValue());
			}
			else if (attr.getNodeName().equals("height"))
			{
				this.height = Integer.parseInt(attr.getNodeValue());
			}
			else
			{
				System.out.println("Unknown DashRepresentation attribute: " + attr.getNodeName());
			}
		}
	}

	public SegmentTemplate getSegmentTemplate()
	{
		if (this.segmentTemplate != null)
		{
			return this.segmentTemplate;
		}
		else if (parent.getSegmentTemplate() != null)
		{
			return parent.getSegmentTemplate();
		}
		return null;
	}

	public List<DownloadTarget> getTargetFiles()
	{
		List<DownloadTarget> filesToDownload = new ArrayList<>();

		if (this.getSegmentTemplate() != null)
		{
			filesToDownload = this.getSegmentTemplate().getTargetFiles(this);
		}
		else
		{
			System.err.println("TODO: implement other representation types than segment template");
		}

		return filesToDownload;
	}

}
