package parser;

import download.types.ManifestDownloadnfo;

public interface IParser
{

	ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl);

}