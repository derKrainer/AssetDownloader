package parser;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import download.types.ManifestDownloadnfo;
import parser.dash.DashManifest;

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

	public static double parseDuration(String date) throws Exception
	{
		// parse something like P0Y0M0DT0H3M30.000S
		double duration = 0;
		char lastChar = '0';
		StringBuffer currentPart = new StringBuffer();
		int multiplier = 0;
		for (int i = date.length() - 1; i >= 0; i--)
		{
			switch (date.charAt(i))
			{
			case 'S':
			{
				// seconds
				multiplier = 1;
				currentPart = new StringBuffer();
				lastChar = 'S';
				break;
			}
			case 'M':
			{
				// minutes or month
				duration += parseDuration(currentPart) * multiplier;
				if (lastChar == 'S' || lastChar == '0')
				{
					// minutes
					multiplier = 60;
				}
				else
				{
					// month
					// TODO: real month values if needed at some point?
					multiplier = 3600 * 24 * 30;
				}
				currentPart = new StringBuffer();
				break;
			}
			case 'H':
			{
				// hours
				duration += parseDuration(currentPart) * multiplier;
				multiplier = 3600;
				currentPart = new StringBuffer();
				lastChar = 'H';
				break;
			}
			case 'T':
			case 'P':
			{
				// skip PT
				duration += parseDuration(currentPart) * multiplier;
				currentPart = new StringBuffer();
				lastChar = 'T';
				break;
			}
			case 'D':
			{
				// day
				duration += parseDuration(currentPart) * multiplier;
				currentPart = new StringBuffer();
				multiplier = 3600 * 24;
				lastChar = 'D';
				break;
			}
			case 'Y':
			{
				duration += parseDuration(currentPart) * multiplier;
				currentPart = new StringBuffer();
				multiplier = 3600 * 365;
				break;
			}
			default:
			{
				currentPart.insert(0, date.charAt(i));
			}
			}
		}
		duration += parseDuration(currentPart) * multiplier;

		return duration;
	}

	private static double parseDuration(StringBuffer sb)
	{
		if (sb == null || sb.length() == 0)
		{
			return 0;
		}
		return Double.parseDouble(sb.toString());
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
