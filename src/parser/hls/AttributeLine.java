package parser.hls;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeLine
{
  public String attributeName;
  public Map<String, String> keyValuePairs = new HashMap<>();

  // "forgiving" attribute list psuedo-grammar:
  // attributes -> keyvalue (',' keyvalue)*
  // keyvalue   -> key '=' value
  // key        -> [^=]*
  // value      -> '"' [^"]* '"' | [^,]*
  private static Pattern attributePattern = Pattern.compile("(?:^|,)((?:[^=]*)=(?:\"[^\"]*\"|[^,]*))");

  public AttributeLine(String line)
  {
    String attributeString = this.parseAndRemoveAttributeName(line);

    if (attributeString == null) {
      return;
    }

    Matcher attributes = attributePattern.matcher(attributeString);

    boolean found = attributes.find();
    if (!found) {
      this.keyValuePairs.put("", attributeString);
    }
    else {
      while(found) {
        String singleAttributeString = attributeString.substring(attributes.start(), attributes.end());
        if (singleAttributeString.charAt(0) == ',') {
          singleAttributeString = singleAttributeString.substring(1);
        }
        singleAttributeString = singleAttributeString.trim();

        if (singleAttributeString.indexOf("=") > 0) {

          String key = singleAttributeString.substring(0, singleAttributeString.indexOf("=")).toUpperCase();
          String value = singleAttributeString.substring(singleAttributeString.indexOf("=") + 1);
          if (value.charAt(0) == '"') {
            value = value.substring(1);
          }
          if (value.charAt(value.length() - 1) == '"') {
            value = value.substring(0, value.length() - 1);
          }


          keyValuePairs.put(key, value);
        } else {
          keyValuePairs.put(singleAttributeString, null);
        }

        found = attributes.find();
      }
    }

  }

  private String parseAndRemoveAttributeName(String line) {
    if (!line.contains(":"))
    {
      this.attributeName = line;
      return null;
    }

    this.attributeName = line.substring(0, line.indexOf(":")).toUpperCase();
    return line.substring(line.indexOf(":") + 1);
  }

  public String get(String attributeName)
  {
    return this.keyValuePairs.get(attributeName);
  }

}
