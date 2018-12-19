# AssetDownloader
Lets you download video assets in their original form while updating the URLs to they can be hosted in your test system

## Currently supports
- HLS VOD manifest downloading
- HLS live support
- DASH VOD support
- DASH Live support (need some testing & verification)

## Issues
- the media playlist urls are wrong
- the master playlist does not have the updated media urls

## TODO
- HLS Backup Streams

## Usage

Either 
- clone the repo & start in IDE. Main class located in `assetdownloader.AssetDownloader`
- use the compiled JAR file (i try do keep it up to date, but no guarantee)

### Manifest Selection

Paste the absolute URL to the manifest file
Specify the relative path for a folder to download the asset into

### Quality Selection

Chose the Qualities you want in the downloaded manifest.
All unselected Qualities will be removed from the manifest when writing the updated one

### Progress

See the download progress and cancel at will.

If a live stream is snapshotted:
- the progress bar currently only shows the initial progress.
- the manifest will be updated periodically (specified by the manifest) and the difference to the last update will be downloaded
- this will continue until the programm is closed. See `System.out` logs for additional information
