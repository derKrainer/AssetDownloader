package ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import assetdownloader.AssetDownloader;

public class ManifestSelector extends AbstractUIComponent
{
  public ManifestSelector()
  {
    super("Enter the manifest URL and the folder to download into", new Dimension(900, 200), true);
  }

  @Override
  protected void initComponents()
  {
    Dimension textSize = new Dimension(870, 30);
    JLabel manifestInputLabel = new JLabel("Manifest URL");
    JTextField manifestUrlInput = new JTextField();
    JPanel wrapper = wrapInputAndLabel(manifestInputLabel, manifestUrlInput, textSize);
    currentView.add(wrapper);
    wrapper.setLocation(5, 5);

    JLabel targetFolderLabel = new JLabel("Target Folder");
    JTextField targetFolderInput = new JTextField("./download");
    wrapper = wrapInputAndLabel(targetFolderLabel, targetFolderInput, textSize);
    currentView.add(wrapper);
    wrapper.setLocation(5, 40);

    JButton processButton = new JButton("Process Manifest");
    processButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String manifestUrl = manifestUrlInput.getText();
        String targetFolder = targetFolderInput.getText();
        char lastTargetFolderChar = targetFolder.charAt(targetFolder.length() - 1);
        if (lastTargetFolderChar != '/' && lastTargetFolderChar != '\\')
        {
          targetFolder += '/';
        }
        AssetDownloader.instance = new AssetDownloader(manifestUrl, targetFolder);
        destroy();
      }
    });
    currentView.add(processButton);
    processButton.setBounds(400, 90, 150, 40);
  }
}