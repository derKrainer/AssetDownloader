package parser.dash.subcomponents;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import download.types.DownloadTarget;
import parser.dash.DashComponent;
import parser.dash.DashRepresentation;

public class SegmentTemplate extends DashComponent
{
	public String mediaUrl;
	public String initUrl;
	public int startNumber;
	public int duration;
	public int timescale;
	
	public SegmentTemplate(Node xmlNode)
	{
		super(xmlNode);
	}

	@Override
	protected void parseSpecialNodes(List<Node> specialNodes)
	{
		// TODO Auto-generated method stub
		for(Node childNode: specialNodes) {
			System.out.println("Unhandled SegmentTemplateNode: " + childNode);
		}
	}

	@Override
	protected void parseAttributes(List<Node> specialAttributesList)
	{
		for (Node attr : specialAttributesList) {
			if (attr.getNodeName().equals("media")) {
				this.mediaUrl = attr.getNodeValue();
			}
			else if(attr.getNodeName().equals("initialization")) {
				this.initUrl = attr.getNodeValue();
			}
			else if(attr.getNodeName().equals("startNumber")) {
				this.startNumber = Integer.parseInt(attr.getNodeValue());
			}
			else if(attr.getNodeName().equals("duration")) {
				this.duration = Integer.parseInt(attr.getNodeValue());
			}
			else if(attr.getNodeName().equals("timescale")) {
				this.timescale = Integer.parseInt(attr.getNodeValue());
			}
			else {
				System.out.println("Unknown SegmentTemplate attribute: " + attr.getNodeName());
			}
		}
	}
	
	public List<DownloadTarget> getTargetFiles(DashRepresentation rep) {
		// TODO: calculate amount of segments and replace mediaUrl
		List<DownloadTarget> allFiles = new ArrayList<>();
		double segmentDuration = this.duration / this.timescale;
		double streamDuration = rep.parent.parent.getDuration();
		
		double numberOfSegments = Math.ceil(streamDuration / segmentDuration);
		
		for (int i = this.startNumber; i < this.startNumber + numberOfSegments; i++) {
			String dlUrl = replacePlaceholders(getUrlForIndex(i), i);
			allFiles.add(new DownloadTarget(replacePlaceholders(dlUrl, i), getTargetFileForUrl(dlUrl)));
		}
		
		return allFiles;
	}
	
	private String getUrlForIndex(int index) {
		return this.mediaUrl;
	}
	
	private String replacePlaceholders(String url, int index) {
		return url
				.replace("$Number$", Integer.toString(index));
	}
	
	private String getTargetFileForUrl(String url) {
		// TODO: period + adSet
		return url.substring(url.lastIndexOf('/') + 1);
	}
}
