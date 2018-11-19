package parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import download.types.AdaptationSet;
import download.types.ManifestDownloadnfo;
import parser.dash.DashAdaptationSet;
import parser.dash.DashPeriod;
import parser.dash.DashRepresentation;

public class DashParser implements IParser
{
	private Document manifestDocument;
	private String targetFolder;
	private List<DashPeriod> periods = new ArrayList<>();

	public DashParser(String targetFolder)
	{
		this.targetFolder = targetFolder;
	}

	@Override
	public ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl)
	{
		this.parseXml(manifestContent);

		this.parseMPDInformation();
		this.parsePeriods();

		return null;
	}

	private void parseMPDInformation()
	{
		// nothing interesting here
	}

	private void parsePeriods()
	{
		NodeList list = this.manifestDocument.getElementsByTagName("Period");

		if (list.getLength() == 0)
		{
			throw new RuntimeException("Manifest has no Periods (is empty)");
		}

		for (int i = 0; i < list.getLength(); i++)
		{
			Node periodNode = list.item(i);
			DashPeriod currentPeriod = new DashPeriod(periodNode);

			System.out.println("processing Period: " + periodNode);
			this.parsePeriod(periodNode, currentPeriod);

			this.periods.add(currentPeriod);
		}
	}

	private void parsePeriod(Node periodNode, DashPeriod parent)
	{
		NodeList possibleAdaptationSets = periodNode.getChildNodes();

		for (int i = 0; i < possibleAdaptationSets.getLength(); i++)
		{
			Node adaptationSetNode = possibleAdaptationSets.item(i);
			if (adaptationSetNode.getNodeName().equals("AdaptationSet"))
			{
				DashAdaptationSet currentSet = parseAdaptationSet(adaptationSetNode, parent);
				System.out.println("processing Adaptation Set: " + adaptationSetNode);
				parent.adaptationSets.add(currentSet);
			}
			else
			{
				System.out.println("Unexpected child of Period: " + adaptationSetNode);
			}
		}
	}
	
	private DashAdaptationSet parseAdaptationSet(Node xmlContent, DashPeriod parent) {
		DashAdaptationSet retVal = new DashAdaptationSet(xmlContent);
		
		NodeList childNodes = xmlContent.getChildNodes();
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			
			if (child.getNodeName().equals("Representation")) {
				retVal.representations.add(this.parseRepresentation(child, retVal));
			} else {
				// TODO: parse AdaptationSet Child != representation
			}
		}
		
		return retVal;
	}
	
	private DashRepresentation parseRepresentation(Node xml, DashAdaptationSet parent) {
		DashRepresentation retVal = new DashRepresentation(xml);
		
		// TODO: parse
		
		return retVal;
	}

	private void parseXml(String manifestContent)
	{
		try
		{
			this.manifestDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(manifestContent)));
		} catch (SAXException e)
		{
			throw new RuntimeException("Invalid XML encountered", e);
		} catch (Exception e)
		{
			throw new RuntimeException("Unexpected error during xml parsing", e);
		}
	}
}
