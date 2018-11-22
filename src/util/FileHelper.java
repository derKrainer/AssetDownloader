package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileHelper
{

  public static void writeContentToFile(String filePath, String content)
  {

    File f = new File(filePath);

    if (!f.exists())
    {
      f.getParentFile().mkdirs();
    }

    FileOutputStream out = null;

    try
    {
      out = new FileOutputStream(f);
      out.write(content.getBytes(Charset.defaultCharset()));

      out.flush();
      out.close();
    } catch (IOException e)
    {
      e.printStackTrace();

      if (out != null)
      {
        try
        {
          out.flush();
          out.close();
        } catch (Exception ignore)
        {
          // nevermind :/
        }
      }
    }
  }
}
