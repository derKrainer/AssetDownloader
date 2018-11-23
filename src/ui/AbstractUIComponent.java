package ui;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class AbstractUIComponent
{
  public JFrame currentView;

  public String title;
  public Dimension targetSize;

  public AbstractUIComponent(String title, Dimension size, boolean shouldInitComponents)
  {
    this.title = title;
    this.targetSize = size;

    this.initView();
    if (shouldInitComponents)
    {
      this.initComponents();
      this.show();
    }
  }

  protected void show()
  {
    currentView.setSize(this.targetSize);
    currentView.setVisible(true);
  }

  protected void initView()
  {
    this.currentView = new JFrame(this.title);
    this.currentView.setLayout(null);
    currentView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }

  protected abstract void initComponents();

  public void destroy()
  {
    this.currentView.dispose();
  }

  public JPanel wrapInputAndLabel(JLabel label, JComponent input, Dimension targetSize)
  {
    JPanel wrapper = new JPanel();
    wrapper.setLayout(null);
    wrapper.setSize(targetSize);

    wrapper.add(label);
    label.setBounds(0, 0, 130, targetSize.height);

    wrapper.add(input);
    input.setBounds(135, 0, targetSize.width - 140, targetSize.height);

    return wrapper;
  }

}