package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import download.DownloadHelper;
import download.compare.ComparisonResult;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.DashParser;
import parser.FallbackCounters;
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

    manifestParser.writeUpdatedManfiest(toDownload, 0);
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
    // 0th update is the initial one
    int numberOfUpdates = 1;
    super.run();
    while (!this.isStopped)
    {
      try
      {
        Thread.sleep(parser.getLiveUpdateFrequency());

        // next parser
        Constructor<? extends IParser> nextParser = this.parser.getClass().getConstructor(String.class);
        IParser newParser = nextParser.newInstance(parser.getTargetFolderName());
        String updatedContent = DownloadHelper.getContent(parser.getManifestLocation());
        // hls master manifests don't updated (only the media playlists), skip manifest updated check in that case
        if (parser instanceof DashParser && parser.getManifestContent().equals(updatedContent))
        {
          System.out.println("No update happend in manifest");
        }
        else
        {
          FallbackCounters.reset();
          ManifestDownloadnfo nextInfo = newParser.parseManifest(updatedContent, parser.getManifestLocation());
          ComparisonResult updateDiff = nextInfo.compareToOldManifest(
              this.parser.parseManifest(this.parser.getManifestContent(), parser.getManifestLocation()));
          // collect all representations which are still relevant and update the manifest with it
          Set<Representation> allRepresentationsForManifest = updateDiff.getSameAndNewRepresentations();
          if (allRepresentationsForManifest.isEmpty())
          {
            System.err.println("No more representations found... something is fishy.");
            continue;
          }
          Representation[] newRepArray = new Representation[allRepresentationsForManifest.size()];
          allRepresentationsForManifest.toArray(newRepArray);
          newParser.writeUpdatedManfiest(newRepArray, numberOfUpdates++);

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
