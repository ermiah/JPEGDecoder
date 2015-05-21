/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Nosrati
 */
public class JPEGDecoderDemo
{
    
    JFrame frame = new JFrame("Jpeg Decoder");
    JMenuBar mnuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    JMenuItem mnuOpen = new JMenuItem("Open");
    JMenu menuJpeg = new JMenu("JPEG");
    JMenuItem mnuInfo = new JMenuItem("Show Info...");
    
    ImageIcon ii = new ImageIcon();
    
    JLabel lblImage = new JLabel();
    JScrollPane scrollPane = new JScrollPane(lblImage);
    JPanel toolPanel = new JPanel();
    JSplitPane splitPane = new JSplitPane();
    
    JTextArea textPane = new JTextArea();
    
    ImageIcon zoomIcon = new ImageIcon();
    JLabel zoomImage = new JLabel(zoomIcon);
    
    JPEGDecoder jd;// = new JPEGDecoder();
    String jpegInfo;
    
    JFileChooser fileChooser = new JFileChooser();
    
    public static void main(String[] args) throws Exception
    {
        JPEGDecoderDemo jdd = new JPEGDecoderDemo();
        jdd.init();
    }
    
    void init() throws Exception
    {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        
        frame.setJMenuBar(mnuBar);
        mnuBar.add(menu);
        mnuBar.add(menuJpeg);
        menu.add(mnuOpen);
        menuJpeg.add(mnuInfo);
        
        splitPane.setLeftComponent(scrollPane);
        splitPane.setRightComponent(toolPanel);
        splitPane.setResizeWeight(0.75);
        
        lblImage.setHorizontalAlignment(JLabel.CENTER);
        lblImage.setVerticalAlignment(JLabel.CENTER);
        lblImage.setIcon(ii);
        
        toolPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0.6;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        
        toolPanel.add(new JScrollPane(textPane), gbc);
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        toolPanel.add(zoomImage, gbc);
        
        DefaultCaret caret = (DefaultCaret) textPane.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        textPane.setFont(new Font("Courier New", Font.PLAIN, 11));
        
        frame.add(splitPane);
        frame.setVisible(true);

        mnuOpen.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (fileChooser.showOpenDialog(JPEGDecoderDemo.this.frame) == JFileChooser.APPROVE_OPTION)
                {
                    File f = fileChooser.getSelectedFile();
                    
                    jd = new JPEGDecoder();
                    try
                    {
                        jd.decode(f);
                        if (jd.image != null)
                        {
                            lblImage.setText(null);
                            lblImage.setIcon(ii);
                            ii.setImage(jd.image);
                        }
                        else
                        {
                            lblImage.setIcon(null);
                            if (jd.mode != "" && jd.mode != "baseline")
                                lblImage.setText("Not supported - " + jd.mode + " mode");
                            else
                                lblImage.setText("Error");
                        }
                        scrollPane.revalidate();
                        frame.repaint();
                        
                        jpegInfo = jd.getLastLog();
                        textPane.setText(jpegInfo);
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(JPEGDecoderDemo.class.getName()).log(Level.SEVERE, null, ex);
                        JOptionPane.showMessageDialog(frame, "Not a valid JPEG (or not supported by this decoder).\n\nMessage: " + ex.getMessage());
                    }
                    
                }
            }
        });
        
        lblImage.addMouseListener(new MouseInputAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                
                Image i = jd.getMCUInfo(e.getX(), e.getY());
                
                if (i != null)
                {
                    zoomIcon.setImage(i);
                    zoomImage.repaint();
                    textPane.setText(jd.getLastLog());
                    
                }
            }
        });
        
        mnuInfo.addActionListener(new ActionListener()
        {
            
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showJpegInfoDlg();
            }
        });
    }
    
    void showJpegInfoDlg()
    {
        JDialog dlg = new JDialog(frame, "JPEG Info");
        
        JTextArea txt = new JTextArea(20, 30);
        dlg.add(new JScrollPane(txt));
        
        DefaultCaret caret = (DefaultCaret) txt.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        txt.setFont(new Font("Courier New", Font.PLAIN, 11));
        
        txt.setText(jpegInfo);
        dlg.setPreferredSize(new Dimension(500, 400));
        
        dlg.pack();
        dlg.setVisible(true);
        
        dlg.setLocationRelativeTo(frame);
    }
}
