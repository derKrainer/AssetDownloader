package ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import download.DownloadHelper;
import download.types.AdaptationSet;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;

public class DownloadSelector
{
	private ManifestDownloadnfo toDownload;
	
	public DownloadSelector(ManifestDownloadnfo info)
	{
		this.toDownload = info;
		
		this.initUI();
	}
	
	private void initUI() {
		
		JFrame frame = new JFrame("Select all Qualities you want to download");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JList<Representation> repList = new JList(new RepresentationListModel(this.toDownload));
		frame.add(repList);
		
		JButton dlButton = new JButton("Download");
		dlButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				List<Representation> selection = repList.getSelectedValuesList();
				Representation[] selectedRepresentations = new Representation[selection.size()];
				selection.toArray(selectedRepresentations);
				DownloadHelper.downloadRepresentations(selectedRepresentations);
			}
		});
		frame.add(dlButton);
		
		
		frame.setVisible(true);
		
		frame.setSize(new Dimension(600, 400));
	}
	
}

class RepresentationListModel implements ListModel<Representation> {
	
	private ManifestDownloadnfo info;
	private Representation[] combinedReps;
	
	public RepresentationListModel(ManifestDownloadnfo info)
	{
		this.info = info;
		this.buildRepresentationList();
	}
	
	public void buildRepresentationList() {
		Map<Integer, Representation> repsPerBandwidth = new HashMap<>();
		
		for(Period p : this.info.periods) {
			for (AdaptationSet adSet : p.adaptationSets) {
				for (Representation rep : adSet.representations) {
					if (!repsPerBandwidth.containsKey(rep.bandwidth)) {
						repsPerBandwidth.put(rep.bandwidth, new Representation(rep.name, rep.bandwidth));
					}
					repsPerBandwidth.get(rep.bandwidth).filesToDownload.addAll(rep.filesToDownload);
				}
			}
		}
		
		this.combinedReps = new Representation[repsPerBandwidth.values().size()];
		repsPerBandwidth.values().toArray(this.combinedReps);
	}
	
	@Override
	public void addListDataListener(ListDataListener l)
	{
		// not needed
	}

	@Override
	public Representation getElementAt(int index)
	{
		return this.combinedReps[index];
	}

	@Override
	public int getSize()
	{
		return this.combinedReps.length;
	}

	@Override
	public void removeListDataListener(ListDataListener l)
	{
		// not needed
	}
	
}