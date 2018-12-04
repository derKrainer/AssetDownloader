package ui;

import java.awt.Dimension;

import javax.swing.JProgressBar;

import download.types.DownloadTarget;
import download.types.Representation;

public abstract class AbstractProgressView extends AbstractUIComponent
{

  public AbstractProgressView(String title, Dimension size, boolean shouldInitComponents)
  {
    super(title, size, shouldInitComponents);
  }
  
  protected abstract JProgressBar getProgressBar(); 
  
  
  public void onFileHandled(DownloadTarget doneTarget)
  {
    this.getProgressBar().setValue(this.getProgressBar().getValue() + 1);
  }
  
  public void onRepresentationDone(Representation rep)
  {

  }
  
  public void onDone()
  {
    
  }
}
