package parser.dash;

public class FallbackCounters
{
  public static int PeriodId = 0;

  public static int AdaptationSetId = 0;

  public static int RepresentationId = 0;

  public static int Bandwidth = 1;

  public static void reset()
  {
    PeriodId = 0;
    AdaptationSetId = 0;
    RepresentationId = 0;
    Bandwidth = 1;
  }
}