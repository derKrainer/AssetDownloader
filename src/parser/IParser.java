package parser;

import java.io.IOException;
import java.net.MalformedURLException;

import download.types.ManifestDownloadnfo;
import download.types.Representation;

public interface IParser
{

  ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl)
      throws MalformedURLException, IOException;

  String getUpdatedManifest(Representation[] selectedRepresentations);
  
  int getLiveUpdateFrequency();
  
  String getManifestLocation();
  String getTargetFolderName();
}