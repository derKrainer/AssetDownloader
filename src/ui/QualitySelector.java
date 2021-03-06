package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import download.types.AdaptationSet;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;
import parser.IParser;

public class QualitySelector extends AbstractUIComponent
{
  private ManifestDownloadnfo toDownload;
  private IParser parser;
  public JFrame frame;

  public QualitySelector(ManifestDownloadnfo info, IParser manifestParser)
  {
    super("Select all Qualities you want to download", new Dimension(650, 550), false);
    this.toDownload = info;
    this.parser = manifestParser;

    this.initComponents();
    this.show();
  }

  @Override
  protected void initComponents()
  {
    JLabel repListLabel = new JLabel("Chose Representations to download: ");
    repListLabel.setBounds(5, 0, 590, 25);
    currentView.add(repListLabel);
    RepresentationListModel model = new RepresentationListModel(this.toDownload);
    JList<Representation> repList = new JList<Representation>(model);
    // select ALL THE INDICES!
    int[] allIndices = new int[model.combinedReps.length];
    for (int i = 0; i < allIndices.length; i++)
    {
      allIndices[i] = i;
    }
    repList.setSelectedIndices(allIndices);
    repList.setBounds(5, 30, 590, 400);
    currentView.add(repList);

    JButton dlButton = new JButton("Download");
    dlButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        List<Representation> selection = repList.getSelectedValuesList();
        Representation[] selectedRepresentations = new Representation[selection.size()];
        selection.toArray(selectedRepresentations);
        parser.getUpdatedManifest(selectedRepresentations);
        if (toDownload.isLive)
        {
          new LiveProgressView(selectedRepresentations, parser, toDownload);
        }
        else
        {
          new ProgressView(selectedRepresentations, parser);
        }
        destroy();
      }
    });
    currentView.add(dlButton);
    dlButton.setBounds(5, 440, 200, 45);
  }

}

class RepresentationListModel implements ListModel<Representation>
{

  private ManifestDownloadnfo info;
  public Representation[] combinedReps;

  public RepresentationListModel(ManifestDownloadnfo info)
  {
    this.info = info;
    this.buildRepresentationList();
  }

  public void buildRepresentationList()
  {
    Map<String, Representation> repsPerBandwidth = new HashMap<>();

    for (Period p : this.info.periods)
    {
      for (AdaptationSet adSet : p.adaptationSets)
      {
        for (Representation rep : adSet.representations)
        {
          String key = rep.generateId(false);
          if (!repsPerBandwidth.containsKey(key))
          {
            repsPerBandwidth.put(key, rep);
          }
          else
          {
            repsPerBandwidth.get(key).filesToDownload.addAll(rep.filesToDownload);
          }
        }
      }
    }

    this.combinedReps = new Representation[repsPerBandwidth.values().size()];
    repsPerBandwidth.values().toArray(this.combinedReps);
    Arrays.sort(this.combinedReps);
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