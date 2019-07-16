package parser.hls;


public class InitSegmentInfo extends SegmentInfo
{
  public String currentLine;
  public String originalInitUrl;

  public InitSegmentInfo(MediaPlaylist parent, SegmentInfo replacement, String currentLine)
  {
    super(parent);

    this.discontinuityNumber = replacement.discontinuityNumber;
    this.preceedingAttributes = replacement.preceedingAttributes;
    this.preceedingLines = replacement.preceedingLines;
    // remove the current line, it's saved separately
    this.preceedingLines.remove(this.preceedingLines.size() - 1);
    this.segmentUrl = replacement.segmentUrl;

    this.currentLine = currentLine;
  }

  @Override
  public String toUpdatedManiest() {
    StringBuffer newContent = new StringBuffer();

    for (String line : this.preceedingLines)
    {
      newContent.append(line).append('\n');
    }

    String replacedLocation = this.currentLine.replace(this.originalInitUrl, this.getTargetFileName(true));
    newContent.append(replacedLocation).append('\n');

    return newContent.toString();
  }
}