package parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import parser.dash.DashManifest;
import parser.dash.DashPeriod;
import parser.dash.DashRepresentation;

public class DashParser implements IParser
{
	private Document manifestDocument;
	private String targetFolder;

	public DashParser(String targetFolder)
	{
		this.targetFolder = targetFolder;
	}

	@Override
	public ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl)
	{
		this.parseXml(manifestContent);

		DashManifest dashManifest = this.parseMPDInformation();
		
		System.out.println(dashManifest.generateDownloadInfo().toString());
		
		return null;
	}

	private DashManifest parseMPDInformation()
	{
		DashManifest manifest = new DashManifest(manifestDocument);
		
		manifest.parse();
		
		return manifest;
	}
	
	public static double parseDuration(String date) {
		// P0Y0M0DT0H3M30.000S
		// TODO: actually implement
		return 210;
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
