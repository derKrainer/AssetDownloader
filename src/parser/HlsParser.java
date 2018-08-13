package parser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import download.DownloadHelper;
import download.types.AdaptationSet;
import download.types.DownloadTarget;
import download.types.ManifestDownloadnfo;
import download.types.Period;
import download.types.Representation;
import files.FileHelper;

public class HlsParser
{
	private String baseURL;
	private ManifestDownloadnfo downloadInfo;
	private int periodCounter = 1;
	private String baseDir;
	private final String baseDirWithTargetFolder;
	
	public static final String PERIOD_ID_PREFIX = "period_";
	
	public static final String DISCONTINUITY_TAG = "#EXT-X-DISCONTINUITY";
	
	public HlsParser(String folderName) {
		this.baseDir = new File(".").getAbsolutePath();
		this.baseDir = this.baseDir.substring(0, this.baseDir.length() - 1);
		this.baseDirWithTargetFolder = this.baseDir + folderName;
	}
	
	public ManifestDownloadnfo parseManifest(String manifestContent, String manifestUrl) {
		
		// TODO: update all the URLs in the manfiest to relative urls and save the updated manifest(s) in the baseDir
		
		this.baseURL = manifestUrl.substring(0,  manifestUrl.lastIndexOf("/") + 1);
		String[] allLines = manifestContent.split("\n");
		this.downloadInfo = new ManifestDownloadnfo(this.baseURL);
		
		Period period = new Period(PERIOD_ID_PREFIX + 0);
		this.downloadInfo.periods.add(period);
		
		// TODO: split all different mime types into adaptation sets
		AdaptationSet adSet = new AdaptationSet("hls_default");
		period.addAdaptationSet(adSet);
		
		for(int i = 0; i < allLines.length; i++) {
			if (allLines[i].indexOf("#EXT-X-STREAM-INF") == 0) {
				// variant playlist
				adSet.addRepresentation(parseVariantPlaylist(allLines[i], allLines[i + 1]));
				i++;
			}
			else if (allLines[i].indexOf("#EXT-X-MEDIA") == 0 && allLines[i].indexOf("URI=\"") > -1) {
				Map<String, String> keyValuePairs = parseKeyValuePairs(allLines[i]);
				Representation mediaRep = new Representation(keyValuePairs.get("TYPE"), 0);
				mediaRep.attributes = keyValuePairs;
				// TODO: handle non-variant files?
				mediaRep.manifestContent = DownloadHelper.getContent(keyValuePairs.get("URI"));
				adSet.addRepresentation(mediaRep);
			}
		}
		
		if (adSet.representations.size() == 0) {
			Representation singleVariantPlaylist = new Representation("single_variant", 1);
			singleVariantPlaylist.manifestContent = manifestContent;
			
		}
		
		this.processAllVariantPlaylists();
		
		return this.downloadInfo;
	}
	
	private void processAllVariantPlaylists() {
		for (int periodIndex = 0; periodIndex < this.downloadInfo.periods.size(); periodIndex++) {
			Period period = this.downloadInfo.periods.get(periodIndex);
			for (int adSetIndex = 0; adSetIndex < this.downloadInfo.periods.get(periodIndex).adaptationSets.size(); adSetIndex++) {
				AdaptationSet adSet = period.adaptationSets.get(adSetIndex);
				for (int repIndex = 0; repIndex < adSet.representations.size(); repIndex++) {
					String newFileContent = this.processVariantPlaylist(adSet.representations.get(repIndex));
					
					System.out.println(newFileContent);
					// only write variants for the 0th period as those are the only ones with all the content,
					// everything after the 0th one is just internal
					if (periodIndex == 0) {
						String manifestFileName = this.baseDirWithTargetFolder + adSet.representations.get(repIndex).name + ".m3u8";
						
						FileHelper.writeContentToFile(manifestFileName, newFileContent);
					}
				}
			}
		}
	}
	
	protected String processVariantPlaylist(Representation variantPlaylist) {
		String[] allLines = variantPlaylist.manifestContent.split("\n");
		int lastDiscontinuityTagIndex = 0;
		
		Representation currentRepresentation = variantPlaylist;
		
//		System.out.println(variantPlaylist.manifestContent);
		
		StringBuilder updatedManifestContent = new StringBuilder(variantPlaylist.manifestContent.length());
		
		for(String line : allLines) {
			if (DISCONTINUITY_TAG.equals(line)) {
				String currentPeriodID = currentRepresentation.containingAdaptationSet.containingPeriod.periodId;
				AdaptationSet newAdaptationSet;
				int nextPeriodNumber = Integer.parseInt(currentPeriodID.substring(PERIOD_ID_PREFIX.length())) + 1;
				
				if (this.downloadInfo.periods.size() > nextPeriodNumber) {
					// take existing next period -> adaptation set
					newAdaptationSet = this.downloadInfo.periods.get(nextPeriodNumber).adaptationSets.get(0);
				}
				else {
					// this is the first representation to venture into a new period
					AdaptationSet oldAdSet = currentRepresentation.containingAdaptationSet;
					Period newPeriod = new Period(PERIOD_ID_PREFIX + this.periodCounter++);
					newAdaptationSet = new AdaptationSet(oldAdSet.name);
					this.downloadInfo.periods.add(newPeriod);
					newPeriod.addAdaptationSet(newAdaptationSet);
				}
				
				// split the manifest at the discontinuity tag so every representation contains only its part of the manifest
				String manifestContent = currentRepresentation.manifestContent;
				lastDiscontinuityTagIndex = manifestContent.indexOf(DISCONTINUITY_TAG, lastDiscontinuityTagIndex) + DISCONTINUITY_TAG.length();
				lastDiscontinuityTagIndex = Math.min(lastDiscontinuityTagIndex, manifestContent.length());
				String oldContent = manifestContent.substring(0, lastDiscontinuityTagIndex);
				currentRepresentation.manifestContent = oldContent;
				String newContent = manifestContent.substring(lastDiscontinuityTagIndex);
				
				currentRepresentation = new Representation(currentRepresentation.name, currentRepresentation.bandwidth);
				currentRepresentation.manifestContent = newContent;
				newAdaptationSet.addRepresentation(currentRepresentation);
			}
			else if (line.indexOf("http") == 0) {
				// absolute url
				String fileName = this.getFileNameForUrl(currentRepresentation, line);
				
				currentRepresentation.filesToDownload.add(new DownloadTarget(line, fileName));
				
				// update the current url to the relative url, as the new url should not point to the orginal content
				line = fileName.substring(this.baseDirWithTargetFolder.length());
			}
			else if (line.trim().length() > 0 && !line.startsWith("#")) {
				// relative url
				String fileName = this.getFileNameForUrl(currentRepresentation, '/' + line);
				
				currentRepresentation.filesToDownload.add(new DownloadTarget(this.baseURL + line,  fileName));
				
				line = fileName.substring(this.baseDirWithTargetFolder.length());
			}
			
			updatedManifestContent.append(line).append('\n');
		}
		
		return updatedManifestContent.toString();
	}
	
	private String getFileNameForUrl(Representation currentRep, String url) {
		StringBuilder sb = new StringBuilder(url.length());
		sb.append(this.baseDirWithTargetFolder);
		sb.append(currentRep.containingAdaptationSet.containingPeriod.periodId).append('/');
		sb.append(currentRep.name);
		sb.append(url.substring(url.lastIndexOf('/')));
		return sb.toString();
	}
	
	
	protected Representation parseVariantPlaylist(String variantDesc, String variantUrl) {
		Map<String, String> keyValuePairs = parseKeyValuePairs(variantDesc);
		
		int bandwidth = Integer.MAX_VALUE;
		if (keyValuePairs.get("BANDWIDTH") != null) {
			bandwidth = Integer.parseInt(keyValuePairs.get("BANDWIDTH"));
		}
		String name = "variant_" + variantUrl;
		if (keyValuePairs.get("RESOLUTION") != null) {
			name = "variant_" + keyValuePairs.get("RESOLUTION");
		}
		Representation retVal = new Representation(name, bandwidth);
		retVal.attributes = keyValuePairs;
		
		if (variantUrl.indexOf("http") != 0) {
			variantUrl = this.baseURL + variantUrl;
		}
		// TODO: async
		retVal.manifestContent = DownloadHelper.getContent(variantUrl);
		
		return retVal;
	}
	
	protected Map<String, String> parseKeyValuePairs(String hlsLine) {
		String attributeStr = hlsLine.substring(hlsLine.indexOf(":") + 1);
		String[] attributePairs = attributeStr.split(",");
		
		Map<String, String> keyValuePairs = new HashMap<>();
		for (String pair : attributePairs) {
			String[] keyValue = pair.split("=");
			if (keyValue.length >= 2) {
				// if there are = in the value, reduce them again to a single value
				if (keyValue.length > 2) {
					for (int i = 2; i < keyValue.length; i++) {
						keyValue[1] += '=' + keyValue[i];
					}
				}
				
				// ensure upper string for further checks and remoe "" around the value
				int start = 0, end = keyValue[1].length();
				if (keyValue[1].charAt(0) == '"') {
					start++;
					end--;
				}
				
				keyValuePairs.put(keyValue[0].toUpperCase(), keyValue[1].substring(start, end));
			}
		}
		return keyValuePairs;
	}
}
