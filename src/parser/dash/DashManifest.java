package parser.dash;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import download.types.AdaptationSet;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;
import parser.DashParser;

public class DashManifest extends DashComponent
{
	public List<DashPeriod> periods = new ArrayList<>();

	public double duration = -1;

	public DashManifest(Node xmlNode)
	{
		super(xmlNode.getFirstChild());
	}

	@Override
	protected void parseSpecialNodes(List<Node> specialNodes)
	{
		NodeList list = this.xmlContent.getChildNodes();

		if (list.getLength() == 0)
		{
			throw new RuntimeException("Manifest has no Periods (is empty)");
		}

		for (int i = 0; i < list.getLength(); i++)
		{
			Node periodNode = list.item(i);
			if (periodNode.getNodeName().equals("Period"))
			{
				DashPeriod currentPeriod = new DashPeriod(periodNode, this);

				System.out.println("processing Period: " + periodNode);
				currentPeriod.parse();

				this.periods.add(currentPeriod);
			}
			else
			{
				System.out.println("Unexpected MPD Child: " + periodNode.getNodeName());
			}
		}
	}

	@Override
	protected void parseAttributes(List<Node> specialAttributesList)
	{
		// TODO: optional MPD values?
		for (Node attr : specialAttributesList)
		{
			if (attr.getNodeName().equals("mediaPresentationDuration"))
			{
				this.duration = DashParser.parseDuration(attr.getNodeValue());
			}
			else
			{
				System.out.println("Unhandled MPD attribute: " + attr.getNodeName());
			}
		}
	}

	public ManifestDownloadnfo generateDownloadInfo()
	{
		ManifestDownloadnfo retVal = new ManifestDownloadnfo(""); // TODO: baseUrl

		for (DashPeriod p : this.periods)
		{
			Period currentPeriod = new Period(p.id);
			retVal.periods.add(currentPeriod);

			for (DashAdaptationSet adSet : p.adaptationSets)
			{
				AdaptationSet dlSet = new AdaptationSet(adSet.id);
				currentPeriod.addAdaptationSet(dlSet);

				for (DashRepresentation rep : adSet.representations)
				{
					Representation dlRep = new Representation(rep.id, Integer.parseInt(rep.bandwidth));
					dlSet.addRepresentation(dlRep);
					
					List<DownloadTarget> targets = rep.getTargetFiles();
					for (DownloadTarget dlTarget : targets) {
						dlRep.filesToDownload.add(dlTarget);
					}
				}
			}
		}

		return retVal;
	}

}
