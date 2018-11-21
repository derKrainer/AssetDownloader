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
    } catch (Exception ex)
    {
      ex.printStackTrace(); // TODO: remove once we know that relative urls work
      return false;
    }
  }

  public static String makeAbsoulte(String url, String baseUrl)
  {
    try
    {
      URI test = new URI(url);
      if (test.isAbsolute())
      {
        return url;
      } else
      {
        test = new URI(baseUrl + url);
        return test.toString();
      }
    } catch (Exception ex)
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
}