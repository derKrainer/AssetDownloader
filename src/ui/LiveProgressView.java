package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import download.DownloadHelper;
import download.compare.ComparisonResult;
import download.compare.ListComparison;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.IParser;

public class LiveProgressView extends AbstractProgressView
{
  public DownloadTarget[] initialDownload;
  public JProgressBar allDownloads;
  private JButton stop;
  public ReloadThread manifestUpdater;

  public LiveProgressView(Representation[] toDownload, IParser manifestParser, ManifestDownloadnfo initalDownloadInfo)
  {
    super("Live progress", new Dimension(500, 500), false);
    List<DownloadTarget> allItems = new ArrayList<>();
    for (Representation dlRep : toDownload)
    {
      allItems.addAll(dlRep.filesToDownload);
    }
    this.initialDownload = new DownloadTarget[allItems.size()];
    allItems.toArray(this.initialDownload);

    this.initComponents();
    this.show();

    this.manifestUpdater = new ReloadThread(manifestParser, initalDownloadInfo);
    manifestUpdater.start();
    DownloadHelper.downloadRepresentations(toDownload, this);
  }

  @Override
  protected void initComponents()
  {
    this.allDownloads = new JProgressBar(0, this.initialDownload.length);
    this.allDownloads.setBounds(5, 5, 450, 25);
    this.allDownloads.setValue(0);
    this.currentView.add(this.allDownloads);

    this.stop = new JButton("Cancel");
    this.stop.setBounds(30, 30, 300, 50);
    this.stop.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        DownloadHelper.cancelDownloading();
        manifestUpdater.isStopped = true;
      }
    });
    this.currentView.add(this.stop);
  }

  @Override
  protected JProgressBar getProgressBar()
  {
    return this.allDownloads;
  }
}

class ReloadThread extends Thread
{
  private IParser parser;
  public boolean isStopped = false;

  public ReloadThread(IParser manifestParser, ManifestDownloadnfo initialInfo)
  {
    this.parser = manifestParser;
  }

  @Override
  public void run()
  {
    super.run();
    while (!this.isStopped)
    {
      try
      {
        this.sleep(parser.getLiveUpdateFrequency());

        // next parser
        Constructor<? extends IParser> nextParser = this.parser.getClass().getConstructor(String.class);
        IParser newParser = nextParser.newInstance(parser.getTargetFolderName());
        String updatedContent = DownloadHelper.getContent(parser.getManifestLocation());
        if (parser.getManifestContent().equals(updatedContent))
        {
          System.out.println("No update happend in manifest");
        }
        else
        {
          ManifestDownloadnfo nextInfo = newParser.parseManifest(updatedContent, parser.getManifestLocation());
          ComparisonResult updateDiff = nextInfo.compareToOldManifest(
              this.parser.parseManifest(this.parser.getManifestContent(), parser.getManifestLocation()));
          // collect all representations which are still relevant and update the manifest with it
          List<Representation> allRepresentationsForManifest = new ArrayList<>();
          Collection<ListComparison<Representation>> repsInUpdatedManifest = updateDiff.representationChangesInAdaptationSets
              .values();
          for (ListComparison<Representation> repInNewManifest : repsInUpdatedManifest)
          {
            allRepresentationsForManifest.addAll(repInNewManifest.sameItems);
            allRepresentationsForManifest.addAll(repInNewManifest.newItems);
          }
          Representation[] newRepArray = new Representation[allRepresentationsForManifest.size()];
          allRepresentationsForManifest.toArray(newRepArray);
          newParser.getUpdatedManifest(newRepArray);

          // actually download the new files
          Set<DownloadTarget> newTargets = updateDiff.getNewDownloadTargets();
          for (DownloadTarget t : newTargets)
          {
            System.out.println("New target --> from: " + t.downloadURL + ", to: " + t.fileName);
          }
          DownloadHelper.downloadUpdateDiff(newTargets);

          // update the parser so the next iteration will have differences
          this.parser = newParser;
        }
      }
      catch (Exception e)
      {
        System.err.println("Unable to update manifest" + e.getMessage());
        e.printStackTrace();
      }
    }
  }
}
