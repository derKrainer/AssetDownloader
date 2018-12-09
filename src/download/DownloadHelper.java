package download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import download.types.DownloadTarget;
import download.types.Representation;
import ui.AbstractProgressView;
import util.URLUtils;

public class DownloadHelper
{
  public static ThreadedDownloader downloaderThread;

  public static String getContent(String urlString) throws MalformedURLException, IOException
  {
    InputStreamReader reader = null;
    BufferedReader bufferdReader = null;
    InputStream stream = null;
    try
    {
      URL url = new URL(urlString);
      stream = url.openStream();
      reader = new InputStreamReader(stream);
      bufferdReader = new BufferedReader(reader);
      StringBuilder content = new StringBuilder();

      String line = bufferdReader.readLine();
      while (line != null)
      {
        content.append(line).append("\n");
        line = bufferdReader.readLine();
      }
      stream.close();
      return content.toString();
    }
    finally
    {
      try
      {
        if (stream != null)
        {
          stream.close();
        }
        if (reader != null)
        {
          reader.close();
        }
        if (bufferdReader != null)
        {
          bufferdReader.close();
        }
      }
      catch (Exception e)
      {
        // all is lost, ignore
      }
    }
  }

  public static void downloadRepresentations(Representation[] toDownload, AbstractProgressView currentUI)
  {
    downloaderThread = new ThreadedDownloader(toDownload, currentUI);
    downloaderThread.start();
  }

  public static void downloadUpdateDiff(Collection<DownloadTarget> updatedTargets)
  {
    downloaderThread.downloadUpdateDiff(updatedTargets);
  }

  public static void cancelDownloading()
  {
    if (downloaderThread != null)
    {
      downloaderThread.cancel();
    }
  }

  public static void downloadUrlContentToFile(String url, String fileName)
  {
    FileOutputStream fos = null;
    ReadableByteChannel rbc = null;
    try
    {
      URL dataChunk = new URL(URLUtils.resolveRelativeParts(url));
      rbc = Channels.newChannel(dataChunk.openStream());

      try
      {
        File target = new File(fileName);
        if (!target.getParentFile().exists())
        {
          target.getParentFile().mkdirs();
        }
        fos = new FileOutputStream(target);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      }
      catch (Exception writeException)
      {
        System.err.println("could not write to file: " + fileName + ", reason: " + writeException.getMessage());
      }
    }
    catch (Exception e)
    {
      System.err.println("Could not open connection to " + url + ", reason: " + e.getMessage() + " --> skipping file.");
    }
    finally
    {
      try
      {
        if (fos != null)
        {
          fos.flush();
          fos.close();
        }
        if (rbc != null)
        {
          rbc.close();
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}

class ThreadedDownloader extends Thread
{
  private Representation[] toDownload;
  public AbstractProgressView currentUI;
  public boolean canceled;
  public RepresentationDownloadThread[] startedDownloaders;
  public UpdateLoaderThread updateLoader;
  protected int numberOfFinishedDownloaders = 0;

  public ThreadedDownloader(Representation[] toDownload, AbstractProgressView currentUI)
  {
    this.toDownload = toDownload;
    this.currentUI = currentUI;
    this.canceled = false;
  }

  public void cancel()
  {
    this.canceled = true;
  }

  @Override
  public void run()
  {
    super.run();

    this.startedDownloaders = new RepresentationDownloadThread[this.toDownload.length];
    int idx = 0;
    for (Representation rep : toDownload)
    {
      startedDownloaders[idx] = new RepresentationDownloadThread(rep, this);
      startedDownloaders[idx].start();
      idx++;
    }
  }

  public void downloadUpdateDiff(Collection<DownloadTarget> diff)
  {
    if (updateLoader == null)
    {
      this.updateLoader = new UpdateLoaderThread(diff, this);
      this.updateLoader.start();
    }
    else
    {
      this.updateLoader.addTargets(diff);
    }
  }

  public void done()
  {
    if (currentUI != null)
    {
      currentUI.onDone();
    }
  }

  protected void onRepresentationDone()
  {
    this.numberOfFinishedDownloaders++;
    if (this.numberOfFinishedDownloaders >= this.startedDownloaders.length)
    {
      this.done();
    }
  }
}

class RepresentationDownloadThread extends Thread
{
  private final Representation toDownload;
  private final ThreadedDownloader parent;

  public RepresentationDownloadThread(Representation toDownload, ThreadedDownloader mainDownloader)
  {
    this.toDownload = toDownload;
    this.parent = mainDownloader;
  }

  @Override
  public void run()
  {
    super.run();

    System.out.println("Handling representation: " + this.toDownload.id + ", bandwidth: " + this.toDownload.bandwidth);
    for (DownloadTarget target : this.toDownload.filesToDownload)
    {
      if (this.parent.canceled)
      {
        this.parent.done();
        return;
      }

      if (new File(target.fileName).exists())
      {
        if (parent.currentUI != null)
        {
          parent.currentUI.onFileHandled(target);
        }
        continue;
      }

      System.out.println("downloading: " + target.downloadURL + " to: " + target.fileName);
      DownloadHelper.downloadUrlContentToFile(target.downloadURL, target.fileName);

      if (parent.currentUI != null)
      {
        parent.currentUI.onFileHandled(target);
      }
    }

    if (parent.currentUI != null)
    {
      parent.onRepresentationDone();
    }
  }
}

class UpdateLoaderThread extends Thread
{
  private final ThreadedDownloader parent;
  private List<DownloadTarget> toDownload;

  public UpdateLoaderThread(Collection<DownloadTarget> toDownload, ThreadedDownloader mainDownloader)
  {
    this.parent = mainDownloader;
    this.toDownload = new ArrayList<>(toDownload);
  }

  public void addTargets(Collection<DownloadTarget> updatedTargets)
  {
    this.toDownload.addAll(updatedTargets);
  }

  @Override
  public void run()
  {
    super.run();

    while (true)
    {
      if (this.toDownload.size() == 0)
      {
        try
        {
          this.sleep(1000);
        }
        catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        continue;
      }

      DownloadTarget target = this.toDownload.remove(0);
      if (this.parent.canceled)
      {
        this.parent.done();
        return;
      }

      if (new File(target.fileName).exists())
      {
        if (parent.currentUI != null)
        {
          parent.currentUI.onFileHandled(target);
        }
        continue;
      }

      System.out.println("downloading: " + target.downloadURL + " to: " + target.fileName);
      DownloadHelper.downloadUrlContentToFile(target.downloadURL, target.fileName);

      if (parent.currentUI != null)
      {
        parent.currentUI.onFileHandled(target);
      }
    }
  }
}