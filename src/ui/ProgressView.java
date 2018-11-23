package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import download.types.DownloadTarget;
import download.types.Representation;
import parser.dash.FallbackCounters;

public class ProgressView extends AbstractUIComponent
{
  private DownloadTarget[] downloadItems;

  private JProgressBar progress;

  public ProgressView(Representation[] toDownload)
  {
    super("Downloading", new Dimension(500, 300), false);
    List<DownloadTarget> allItems = new ArrayList<>();
    for (Representation dlRep : toDownload)
    {
      allItems.addAll(dlRep.filesToDownload);
    }
    this.downloadItems = new DownloadTarget[allItems.size()];
    allItems.toArray(this.downloadItems);

    this.initComponents();
    this.show();
  }

  @Override
  protected void initComponents()
  {
    this.progress = new JProgressBar(0, this.downloadItems.length);
    this.progress.setBounds(5, 5, 450, 25);
    this.progress.setValue(0);
    this.currentView.add(this.progress);
  }

  public void onFileHandled(DownloadTarget doneTarget)
  {
    this.progress.setValue(this.progress.getValue() + 1);
    this.progress.paint(this.progress.getGraphics());
  }

  public void onDone()
  {
    // this.currentView.remove(this.progress);
    JLabel hooray = new JLabel("!!!DONE!!!");
    hooray.setBounds(30, 30, 300, 50);
    this.currentView.add(hooray);

    JButton again = new JButton("Another asset");
    this.currentView.add(again);
    again.setBounds(125, 100, 300, 50);

    again.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        FallbackCounters.reset();
        new ManifestSelector();
        destroy();
      }
    });
  }

  public void onRepresentationDone(Representation rep)
  {

  }
}