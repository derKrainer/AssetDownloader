package parser;

import download.types.ManifestDownloadnfo;
import download.types.Representation;

public interface IParser
{

  ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl);

  String getUpdatedManifest(Representation[] selectedRepresentations);
}