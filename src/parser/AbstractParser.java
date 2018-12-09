package parser;

import java.io.IOException;
import java.net.MalformedURLException;

import download.types.ManifestDownloadnfo;

public abstract class AbstractParser implements IParser
{
  public String manifestContent;
  public String manifestUrl;
  public String targetFolder;
  public ManifestDownloadnfo parsedManifest;

  public AbstractParser(String targetFolder)
  {
    this.targetFolder = targetFolder;
  }

  @Override
  public ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl) throws MalformedURLException, IOException
  {
    if (this.parsedManifest != null)
    {
      return this.parsedManifest;
    }
    this.manifestContent = manifestContent;
    this.manifestUrl = manifestUrl;

    return internalParse(manifestContent, manifestUrl);
  }

  protected abstract ManifestDownloadnfo internalParse(String manifestContent, String manifestUrl)
      throws MalformedURLException, IOException;

  @Override
  public String getManifestContent()
  {
    return this.manifestContent;
  }

  @Override
  public String getManifestLocation()
  {
    return this.manifestUrl;
  }

  @Override
  public String getTargetFolderName()
  {
    return this.targetFolder;
  }

}
