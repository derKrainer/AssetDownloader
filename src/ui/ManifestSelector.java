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

public class ManifestSelector
{
  public JFrame frame;

  public ManifestSelector()
  {
    this.initUI();
  }

  private void initUI()
  {
    this.frame = new JFrame("Enter the manifest URL and the folder to download into");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame.setLayout(null);

    Dimension targetSize = new Dimension(870, 30);

    JLabel manifestInputLabel = new JLabel("Manifest URL");
    JTextField manifestUrlInput = new JTextField();
    JPanel wrapper = wrapInputAndLabel(manifestInputLabel, manifestUrlInput, targetSize);
    frame.add(wrapper);
    wrapper.setLocation(5, 5);

    JLabel targetFolderLabel = new JLabel("Target Folder");
    JTextField targetFolderInput = new JTextField("./download");
    wrapper = wrapInputAndLabel(targetFolderLabel, targetFolderInput, targetSize);
    frame.add(wrapper);
    wrapper.setLocation(5, 40);

    JButton processButton = new JButton("Process Manifest");
    processButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String manifestUrl = manifestUrlInput.getText();
        String targetFolder = targetFolderInput.getText();
        char lastTargetFolderChar = targetFolder.charAt(targetFolder.length() - 1 );
        if (lastTargetFolderChar != '/' && lastTargetFolderChar != '\\')
        {
          targetFolder += '/';
        }
        // TODO: check if url is valid
        AssetDownloader.instance = new AssetDownloader(manifestUrl, targetFolder);
        frame.dispose();
      }
    });
    frame.add(processButton);
    processButton.setBounds(400, 90, 150, 40);

    frame.setVisible(true);

    frame.setSize(new Dimension(900, 200));
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