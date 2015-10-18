package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Date;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import org.josql.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;

public class BackupsManager extends Box 
{

    private JComboBox   exportOthersType = null;
    private JComboBox   exportChaptersType = null;
    private JComboBox   fileType = null;
    private JTextField  fileField = null;
    private JTree       itemsTree = null;
    private JScrollPane itemsTreeScroll = null;
    private JTable      wordTable = null;
    private FileWatcher watcher = null;
    private AbstractViewer viewer = null;
    private ProjectInfo proj = null;

    public BackupsManager (AbstractViewer pv,
                           ProjectInfo    pi)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = pv;
        this.proj = pi;

    }

    private void update ()
    {
        
        if (this.wordTable == null)
        {
            
            return;
            
        }
        
        // Get the words.
        File userDict = Environment.getUserDictionaryFile ();
        
        Vector<Vector> words = new Vector ();

        String w = null;

        try
        {

            w = IOUtils.getFile (userDict);

        } catch (Exception e)
        {

            w = "";
        
            Environment.logError ("Unable to get user dictionary file: " +
                                  userDict,
                                  e);

        }

        StringTokenizer tt = new StringTokenizer (w,
                                                  String.valueOf ('\n'));

        java.util.List<String> wwords = new ArrayList ();

        while (tt.hasMoreTokens ())
        {

            wwords.add (tt.nextToken ());

        }

        Collections.sort (wwords);

        for (String i : wwords)
        {

            Vector v = new Vector ();
            v.add (i);
            words.add (v);

        }

        Vector<String> cols = new Vector ();
        cols.add ("Word");

        this.wordTable.setModel (new DefaultTableModel (words,
                                                        cols)
        {

            public boolean isCellEditable (int row,
                                           int col)
            {

                return false;

            }

        });

        this.validate ();
        this.repaint ();
        
    }
    
    public void init ()
               throws Exception
    {

        final BackupsManager _this = this;
        
        JTextPane tp = UIUtils.createHelpTextPane (String.format ("Listed below are the backups you have for {project} <b>%s</b>.",
                                                                  this.proj.getName ()),
                                                   this.viewer);

        tp.setBorder (UIUtils.createPadding (0, 0, 10, 0));

        this.add (tp);
        
        JLabel dl = UIUtils.createClickableLabel ("Click to view the backups directory",
                                                  null,
                                                  this.proj.getBackupDirectory ().toURI ().toString ());

        dl.setBorder (UIUtils.createPadding (0, 0, 10, 0));
        
        this.add (dl);
        
        Box backupsBox = new ScrollableBox (BoxLayout.Y_AXIS);
        backupsBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        backupsBox.setOpaque (false);

        final JScrollPane spsp = UIUtils.createScrollPane (backupsBox);
        spsp.getVerticalScrollBar ().setUnitIncrement (20);
        spsp.getViewport ().setPreferredSize (new Dimension (450,
                                                             300));
        
        Box wspsp = new Box (BoxLayout.Y_AXIS);
        wspsp.add (spsp);
        wspsp.setBorder (UIUtils.createPadding (0, 0, 0, 0));
        this.add (wspsp);
        
        File[] _files = this.proj.getBackupDirectory ().listFiles ();
        
        if (_files != null)
        {                

            java.util.List<File> files = (java.util.List<File>) Arrays.asList (_files);
            
            Query q = new Query ();
            
            q.parse (String.format ("SELECT * FROM %s WHERE fileExtension(:_currobj) = 'zip' ORDER BY lastModified DESC",
                                    File.class.getName ()));
            
            QueryResults qr = q.execute (files);
            
            files = (java.util.List<File>) qr.getResults ();
            
            for (File f : files)
            {
                
                Backup b = new Backup (f,
                                       this.proj);
                
                b.init ();
                
                backupsBox.add (b);
                    
            }
            
        }

        this.add (Box.createVerticalStrut (10));
        
        JButton finish = new JButton ("Finish");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        JButton[] buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        this.add (bp);        
        
        UIUtils.doLater (new ActionListener ()
        {
           
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                spsp.getVerticalScrollBar ().setValue (0);
                
            }
            
        });
        
    }

    private class Backup extends Box
    {
        
        private File file = null;
        private ProjectInfo proj = null;
        
        public Backup (File        b,
                       ProjectInfo proj)
        {
            
            super (BoxLayout.Y_AXIS);
            
            this.file = b;
            this.proj = proj;

            this.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                 0,
                                                                 1,
                                                                 0,
                                                                 UIUtils.getInnerBorderColor ()),
                                                new EmptyBorder (5,
                                                                 5,
                                                                 5,
                                                                 5)));
            
        }
        
        public void restore ()
        {
            
            final Backup _this = this;
                                                     
            UIUtils.createQuestionPopup ((AbstractViewer) BackupsManager.this.viewer,
                                         "Confirm restore",
                                         Constants.RESTORE_ICON_NAME,
                                         String.format ("Please confirm you wish to restore {project} <b>%s</b> using the backup file <b>%s</b>.<br /><br />A backup of the {project} will be created before the restore occurs.",
                                                        this.proj.getName (),
                                                        this.file.getName ()),
                                         "Yes, restore it",
                                         Constants.CANCEL_BUTTON_LABEL_ID,
                                         new ActionListener ()
                                         {
                                            
                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {                                                
                                                
                                                final AbstractProjectViewer pv = Environment.getProjectViewer (_this.proj);
                                                
                                                ActionListener doRestore = new ActionListener ()
                                                {
                                                  
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        UIUtils.closePopupParent (BackupsManager.this);
                                                        
                                                        ActionListener doRestore = new ActionListener ()
                                                        {
                                                          
                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                                
                                                                try
                                                                {
                                                                    
                                                                    // Create a backup.
                                                                    File f = Environment.createBackupForProject (_this.proj,
                                                                                                                 true);
                                                                    
                                                                } catch (Exception e) {
                                                                    
                                                                    Environment.logError ("Unable to create backup for project: " +
                                                                                          _this.proj,
                                                                                          e);
                                                                    
                                                                    UIUtils.showErrorMessage (BackupsManager.this.viewer,
                                                                                              "Unable to create a backup of the {project} in its current state.");
                                                                                                                                        
                                                                }
        
                                                                // Restore using our file.
                                                                try
                                                                {
                                                                    
                                                                    Environment.restoreBackupForProject (_this.proj,
                                                                                                         _this.file);

                                                                } catch (Exception e) {
                                                                    
                                                                    Environment.logError ("Unable to restore project with file: " +
                                                                                          _this.file +
                                                                                          ", project: " +
                                                                                          _this.proj,
                                                                                          e);
                                                                    
                                                                    UIUtils.showErrorMessage (BackupsManager.this.viewer,
                                                                                              "Unable to restore backup");
                                                                    
                                                                    return;

                                                                }
                                                                
                                                                if (pv != null)
                                                                {

                                                                    try
                                                                    {
                                                                    
                                                                        // Reopen the project.
                                                                        Environment.openProject (_this.proj);
                                                                        
                                                                        // Show confirmation.
                                                                        UIUtils.showMessage ((PopupsSupported) Environment.getProjectViewer (_this.proj),
                                                                                             "{Project} restored",
                                                                                             String.format ("The {project} has been restored using file <b>%s</b>.",
                                                                                                            _this.file.getName ()));
                                                                        
                                                                        
                                                                    } catch (Exception e) {
                                                                                                                                                
                                                                        Environment.logError ("Unable to reopen project: " +
                                                                                              _this.proj,
                                                                                              e);
                                                                        
                                                                        UIUtils.showErrorMessage (BackupsManager.this.viewer,
                                                                                                  "Unable to re-open backup");
                                                                        
                                                                        return;
                                                                        
                                                                    }
                                                                    
                                                                    return;
                                                                    
                                                                }
                                                                
                                                                // Show confirmation.
                                                                UIUtils.showMessage ((PopupsSupported) BackupsManager.this.viewer,
                                                                                     "{Project} restored",
                                                                                     String.format ("{Project} <b>%s</b> has been restored using file <b>%s</b>.",
                                                                                                    _this.proj.getName (),
                                                                                                    _this.file.getName ()));
                                                                
                                                            }
                                                            
                                                        };
                                                        
                                                        UIUtils.askForPasswordForProject (proj,
                                                                                          doRestore,
                                                                                          BackupsManager.this.viewer);

                                                    }
                                                    
                                                };
                                                                                                                                                
                                                if (pv != null)
                                                {
                                                    
                                                    // Close the project.
                                                    pv.close (false,
                                                              doRestore);
                                                    
                                                } else {
                                                    
                                                    doRestore.actionPerformed (new ActionEvent ("restore", 1, "restore"));
                                                    
                                                }
                                                
            // Create a backup of the project first.
            
            // Close the project viewer, if open.
            
            // Unzip to the current dir.
            
            // Open the project, if already open.
                                                                                                
                                            }
                                            
                                         },
                                         null,
                                         null,
                                         null);
            
        }
        
        public void delete ()
        {
            
            final Backup _this = this;
                                                     
            UIUtils.createQuestionPopup ((AbstractViewer) BackupsManager.this.viewer,
                                         "Confirm delete",
                                         Constants.DELETE_ICON_NAME,
                                         String.format ("Please confirm you wish to delete backup file <b>%s</b>.",
                                                        this.file.getName ()),
                                         "Yes, delete it",
                                         "No, keep it",
                                         new ActionListener ()
                                         {
                                            
                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {
                                                
                                                _this.file.delete ();
                                                
                                                Container p = _this.getParent ();
                                                
                                                p.remove (_this);
                                                
                                                p.validate ();
                                                p.repaint ();
                                                
                                            }
                                            
                                         },
                                         null,
                                         null,
                                         null);
            
        }
        
        public void init ()
        {

            final Backup _this = this;
        
            this.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

            final Box main = new Box (BoxLayout.X_AXIS);

            this.add (main);
                        
            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setOpaque (false);
            main.setAlignmentX (Component.LEFT_ALIGNMENT);
            main.setOpaque (false);

            JComponent name = UIUtils.createHelpTextPane (String.format ("%s (%s)",
                                                                         Environment.formatDateTime (new Date (this.file.lastModified ())),
                                                                         this.file.getName ()),
                                                          BackupsManager.this.viewer);
        
            main.add (name);
            main.add (Box.createHorizontalGlue ());

            java.util.List<JButton> buttons = new ArrayList ();
            
            buttons.add (UIUtils.createButton (Constants.RESTORE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Restore the {project} using this backup",
                                               new ActionAdapter ()
                                               {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.restore ();
                                                        
                                                    }
                                                
                                               }));

            buttons.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               "Click to delete this backup",
                                               new ActionAdapter ()
                                               {
                                                
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _this.delete ();
                                                        
                                                    }
                                                    
                                               }));
                                                           
            JComponent bbar = UIUtils.createButtonBar (buttons);

            //this.buttons.setVisible (false);
            bbar.setAlignmentX (Component.LEFT_ALIGNMENT);
            
            main.add (bbar);
            
        }
        
    }
    
}
