package parser.hls;

import java.util.HashMap;
import java.util.Map;

public class AttributeLine
{
  public String attributeName;
  public Map<String, String> keyValuePairs = new HashMap<>();

  public AttributeLine(String line)
  {
    if (!line.contains(":"))
    {
      this.attributeName = line;
      return;
    }

    this.attributeName = line.substring(0, line.indexOf(":")).toUpperCase();
    String attributeStr = line.substring(line.indexOf(":") + 1);
    String[] attributePairs = attributeStr.split(",");

    if (attributePairs.length == 1)
    {
      // if only one attribute pair exists it's in the form of
      // #EXT-X-VERSION:3
      // so just one value, put it in as default ""
      this.keyValuePairs.put("", attributePairs[0]);
      return;
    }

    for (String pair : attributePairs)
    {
      String[] keyValue = pair.split("=");
      if (keyValue.length >= 2)
      {
        // if there are = in the value, reduce them again to a single value
        if (keyValue.length > 2)
        {
          for (int i = 2; i < keyValue.length; i++)
          {
            keyValue[1] += '=' + keyValue[i];
          }
        }

        // ensure upper string for further checks and remoe "" around the value
        int start = 0, end = keyValue[1].length();
        if (keyValue[1].charAt(0) == '"')
        {
          start++;
          end--;
        }

        keyValuePairs.put(keyValue[0].toUpperCase(), keyValue[1].substring(start, end));
      }
    }
  }

  public String get(String attributeName)
  {
    return this.keyValuePairs.get(attributeName);
  }

}
