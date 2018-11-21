package parser.dash.subcomponents;

import java.util.List;

import org.w3c.dom.Node;

import parser.dash.DashComponent;

public class SegementTimeline extends DashComponent
{

  public SegementTimeline(Node xmlNode)
  {
    super(xmlNode);
  }

  @Override
  protected void parseSpecialNodes(List<Node> specialNodes) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void parseAttributes(List<Node> specialAttributesList) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void fillMissingValues() {
    // nothing to do here
  }

}
