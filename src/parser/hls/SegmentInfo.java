package parser.hls;

import java.util.ArrayList;
import java.util.List;

import download.types.DownloadTarget;

public class SegmentInfo
{
  public List<AttributeLine> preceedingAttributes = new ArrayList<>();
  public List<String> preceedingLines = new ArrayList<>();

  public MediaPlaylist parent;

  public String segmentUrl;

  public int discontinuityNumber;

  public SegmentInfo(MediaPlaylist parent)
  {
    this.parent = parent;
  }

  public DownloadTarget toDownloadTarget()
  {
    return new DownloadTarget(this.segmentUrl, this.getTargetFileName(false));
  }

  public String getTargetFileName(boolean manfiestEntry)
  {
    StringBuffer sb = new StringBuffer();
    if (!manfiestEntry)
    {
      sb.append(this.parent.parser.getTargetFolderName()).append('/');
    }
    sb.append(this.parent.id);
    sb.append(this.segmentUrl.substring(this.segmentUrl.lastIndexOf('/')));
    return sb.toString();
  }

  public String toUpdatedManiest()
  {
    StringBuffer newContent = new StringBuffer();

    for (String line : this.preceedingLines)
    {
      newContent.append(line).append('\n');
    }

    newContent.append(this.getTargetFileName(true)).append('\n');

    return newContent.toString();
  }

  /**
   * called when all information of this segmentInfo has been collected
   */
  public void finish(int discontinuityNumber)
  {
    this.discontinuityNumber = discontinuityNumber;
  }
}