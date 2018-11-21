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

public class DownloadHelper
{
  private DownloadHelper()
  {
  }

  public static String getContent(String urlString) {

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
    } catch (Exception e)
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
      } catch (Exception e)
      {
        // all is lost, ignore
      }
    }
  }

  public static void downloadForDownloadInfo(ManifestDownloadnfo info, IParser manifestParser) {
//		for (Period p : info.periods) {
//			System.out.println("Handling period: " + p.periodId);
//			for (AdaptationSet adSet : p.adaptationSets) {
//				System.out.println("Handling Adaptation Set: " + adSet.name);
//				Representation[] arr = new Representation[adSet.representations.size()];
//				adSet.representations.toArray(arr);
//				downloadRepresentations(arr);
//			}
//		}

    new DownloadSelector(info, manifestParser);
  }

  public static void downloadRepresentations(Representation[] toDownload) {
    for (Representation rep : toDownload)
    {
      System.out.println("Handling representation: " + rep.name + ", bandwidht: " + rep.bandwidth);
      for (DownloadTarget target : rep.filesToDownload)
      {
        if (new File(target.fileName).exists())
        {
          continue;
        }

        System.out.println("downloading: " + target.downloadURL + " to: " + target.fileName);
        downloadUrlContentToFile(target.downloadURL, target.fileName);
      }
    }
  }

  public static void downloadUrlContentToFile(String url, String fileName) {
    FileOutputStream fos = null;
    ReadableByteChannel rbc = null;
    try
    {
      URL website = new URL(url);
      rbc = Channels.newChannel(website.openStream());

      try
      {
        File target = new File(fileName);
        if (!target.getParentFile().exists())
        {
          target.getParentFile().mkdirs();
        }
        fos = new FileOutputStream(target);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
      } catch (Exception writeException)
      {
        System.err.println("could not write to file: " + fileName + ", reason: " + writeException.getMessage());
      }
    } catch (Exception e)
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
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
