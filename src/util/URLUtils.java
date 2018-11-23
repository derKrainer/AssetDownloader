package util;

import java.net.URI;

public class URLUtils
{

  public static boolean isUrlAbsolute(String url)
  {
    try
    {
      URI test = new URI(url);
      return test.isAbsolute();
    }
    catch (Exception ex)
    {
      ex.printStackTrace(); // TODO: remove once we know that relative urls work
      return false;
    }
  }

  public static String makeAbsoulte(String url, String baseUrl)
  {
    try
    {
      if (url.startsWith("//"))
      {
        url = "http:" + url;
      }
      if (baseUrl.startsWith("//"))
      {
        baseUrl = "http:" + baseUrl;
      }

      URI test = new URI(url);
      if (test.isAbsolute())
      {
        return url;
      }
      else
      {
        if (baseUrl.charAt(baseUrl.length() - 1) == '/' || url.charAt(0) == '/')
        {
          test = new URI(baseUrl + url);
        }
        else
        {
          test = new URI(baseUrl + '/' + url);
        }
        return test.toString();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return baseUrl + url;
    }
  }

  public static String makeUrlRelative(String urlToAdjust, String baseUrl)
  {
    if (urlToAdjust.contains(baseUrl))
    {
      return "./" + urlToAdjust.substring(baseUrl.length());
    }
    return urlToAdjust;
  }

  public static String resolveRelativeParts(String absoluteUrl)
  {
    if (!absoluteUrl.contains(".."))
    {
      return absoluteUrl;
    }

    StringBuffer newUrl = new StringBuffer(absoluteUrl.length());

    String[] urlParts = absoluteUrl.split("/");

    for (int i = 0; i < urlParts.length - 1; i++)
    {
      if (urlParts[i + 1].equals(".."))
      {
        i++;
      }
      else
      {
        newUrl.append(urlParts[i]).append('/');
      }
    }
    newUrl.append(urlParts[urlParts.length - 1]);

    return newUrl.toString();
  }
}