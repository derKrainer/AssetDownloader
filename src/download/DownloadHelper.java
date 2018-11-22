package download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Representation;
import parser.IParser;
import ui.DownloadSelector;
import ui.ProgressView;
import util.URLUtils;

public class DownloadHelper
{
  private DownloadHelper()
  {
  }

  public static String getContent(String urlString)
  {

    InputStreamReader reader = null;
    BufferedReader bufferdReader = null;
    try
    {
      URL url = new URL(urlString);
      InputStream stream = url.openStream();
      reader = new InputStreamReader(stream);
      bufferdReader = new BufferedReader(reader);
      StringBuilder content = new StringBuilder();

      String line = bufferdReader.readLine();
      while (line != null)
      {
        content.append(line).append("\n");
        line = bufferdReader.readLine();
      }
      return content.toString();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    } finally
    {
      try
      {
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

  public static void downloadRepresentations(Representation[] toDownload, ProgressView currentUI)
  {
    new ThreadedDownloader(toDownload, currentUI).run();
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
    } finally
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
  private ProgressView currentUI;

  public ThreadedDownloader(Representation[] toDownload, ProgressView currentUI)
  {
    this.toDownload = toDownload;
    this.currentUI = currentUI;
  }

  @Override
  public void run() {
    super.run();

    for (Representation rep : toDownload)
    {
      System.out.println("Handling representation: " + rep.name + ", bandwidth: " + rep.bandwidth);
      for (DownloadTarget target : rep.filesToDownload)
      {
        if (new File(target.fileName).exists())
        {
          if (currentUI != null)
          {
            currentUI.onFileHandled(target);
          }
          continue;
        }

        System.out.println("downloading: " + target.downloadURL + " to: " + target.fileName);
        DownloadHelper.downloadUrlContentToFile(target.downloadURL, target.fileName);

        if (currentUI != null)
        {
          currentUI.onFileHandled(target);
        }
      }
      
      if (currentUI != null)
      {
        currentUI.onRepresentationDone(rep);
      }
    }
    if (currentUI != null)
    {
      currentUI.onDone();
    }
  }
}