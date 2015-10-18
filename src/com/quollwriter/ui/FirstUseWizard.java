package com.quollwriter.ui;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.data.*;
import com.quollwriter.importer.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.text.*;

public class FirstUseWizard extends PopupWizard
{
    
    private final static String SELECT_FILE_STAGE = "select-file";
    private final static String DECIDE_STAGE = "decide";
    private final static String START_STAGE = "start";
    private static final String EXISTING_STAGE = "existing";
    private static final String NEW_PROJECT_STAGE = "newproject";
    private static final String IMPORT_STAGE = "import";
    private static final String SELECT_PROJECT_DB_STAGE = "select-project-db";
    
    private ImportTransferHandlerOverlay   importOverlay = null;
    private ImportProject importProject = null;
    private JRadioButton    importFile = null;
    private JTree           itemsTree = null;
    private Project importProj = null;
    
    private JRadioButton    createNewProject = null;
    private NewProjectPanel newProjectPanel = null;

    private JRadioButton selectProjectDB = null;
    private JRadioButton findProjects = null;
    private FileFinder projDBFind = null;
    private JLabel projDBFindError = null;
    
    private FileFinder      fileFind = null;
    private JLabel          fileFindError = null;
    
    public FirstUseWizard ()
    {
        
        super (null);
        
        final FirstUseWizard _this = this;
        
        this.importFile = new JRadioButton ();
        this.createNewProject = new JRadioButton ();        
        this.findProjects = new JRadioButton ();
        this.selectProjectDB = new JRadioButton ();        
        this.importFile.setSelected (false);
        this.createNewProject.setSelected (false);
        
        this.newProjectPanel = new NewProjectPanel ();        
        
        this.importProject = new ImportProject ();
        this.importProject.init ();
        
        this.projDBFind = new FileFinder ();
        this.projDBFindError = UIUtils.createErrorLabel ("Please select a directory.");
        
        this.fileFindError = UIUtils.createErrorLabel ("Please select a file to import.");
        this.fileFind = new FileFinder ();
        this.fileFind.setClearOnCancel (true);
        this.fileFind.showCancel (true,
                                  new ActionListener ()
                                  {
                                    
                                      @Override
                                      public void actionPerformed (ActionEvent ev)
                                      {
                                        
                                          _this.enableButton ("next",
                                                              false);
                                        
                                      }
                                    
                                  });
        
        this.itemsTree = UIUtils.createTree ();
        this.itemsTree.setModel (null);
                
		this.importOverlay = new ImportTransferHandlerOverlay ();

        this.importOverlay.addMouseListener (new MouseEventHandler ()
        {
           
            public void handlePress (MouseEvent ev)
            {
                
                _this.importOverlay.setVisible (false);
                
                _this.validate ();
                _this.repaint ();
                
            }
            
        });
        
		this.setTransferHandler (new ImportTransferHandler (new ActionListener ()
		{
			
			public void actionPerformed (ActionEvent ev)
			{
			
				File f = (File) ev.getSource ();
                
                _this.importOverlay.setFile (f);
                                
                _this.setGlassPane (_this.importOverlay);
            
                _this.importOverlay.setVisible (true);
                _this.validate ();
                _this.repaint ();
				
			}
			
		},
		new ActionListener ()
		{
			
			public void actionPerformed (ActionEvent ev)
			{
				
                _this.importOverlay.setVisible (false);
                _this.validate ();
                _this.repaint ();
                            
				File f = (File) ev.getSource ();

                _this.importProject.setFile (f);
                _this.fileFind.setFile (f);
                _this.importFile.setSelected (true);
                
                _this.setImportFile (f);
                
                _this.showStage (IMPORT_STAGE);
				
			}
			
		},
		new ActionListener ()
		{
			
			public void actionPerformed (ActionEvent ev)
			{

                _this.importOverlay.setVisible (false);
                _this.validate ();
                _this.repaint ();
								
			}
			
		},
        new java.io.FileFilter ()
		{
			
			@Override
			public boolean accept (File f)
			{
				                				
                return ImportProject.isSupportedFileType (f);				
				
			}
			
		}));        
        
    }
    
    @Override
    public String getWindowTitle ()
    {
        
        return this.getHeaderTitle ();
        
    }
    
    @Override
    public String getHeaderTitle ()
    {
        
        return "Welcome to Quoll Writer";
        
    }
    
    @Override
    public String getHeaderIconType ()
    {
        
        return null;
        
    }
    
    @Override
    public String getHelpText ()
    {

        return "Welcome to Quoll Writer, this page will help you get started.  Don't worry, there are only a couple of questions and then you can get started on your first {project}.";

    }

    @Override
    public int getMaximumContentHeight ()
    {
        
        return 350;
        
    }    
    
    @Override
    public boolean handleFinish ()
    {

        if ((this.importFile.isSelected ())
            ||
            (this.createNewProject.isSelected ())
           )
        {
                        
            return this.newProjectPanel.createProject (this);
            
        }
    
        return true;
    
    }
    
    @Override
    public void handleCancel ()
    {
        
    }
        
    @Override
    public String getNextStage (String currStage)
    {
        
        if (START_STAGE.equals (currStage))
        {
            
            return DECIDE_STAGE;
            
        }
        
        if (SELECT_FILE_STAGE.equals (currStage))
        {
            
            return IMPORT_STAGE;
            
        }
        
        if (EXISTING_STAGE.equals (currStage))
        {
            
            return SELECT_PROJECT_DB_STAGE;
                            
        }
        
        if (IMPORT_STAGE.equals (currStage))
        {
            
            return NEW_PROJECT_STAGE;
            
        }
        
        if (DECIDE_STAGE.equals (currStage))
        {
            
            if (this.createNewProject.isSelected ())
            {
                
                return NEW_PROJECT_STAGE;
                
            }
            
            if (this.importFile.isSelected ())
            {
                
                return SELECT_FILE_STAGE;
                
            }
            
            return IMPORT_STAGE;
                        
        }
        
        return null;
        
    }    
 
    @Override   
    public String getPreviousStage (String currStage)
    {

        if (SELECT_PROJECT_DB_STAGE.equals (currStage))
        {
            
            return EXISTING_STAGE;
            
        }    
    
        if (START_STAGE.equals (currStage))
        {
            
            return null;
            
        }
    
        if (NEW_PROJECT_STAGE.equals (currStage))
        {
            
            if (this.importFile.isSelected ())
            {
                
                return IMPORT_STAGE;
                
            }
            
            return DECIDE_STAGE;
            
        }
    
        if (EXISTING_STAGE.equals (currStage))
        {
            
            return START_STAGE;
            
        }

        if (IMPORT_STAGE.equals (currStage))
        {
            
            return SELECT_FILE_STAGE;
            
        }
    
        if (SELECT_FILE_STAGE.equals (currStage))
        {
            
            return DECIDE_STAGE;
            
        }

        if (DECIDE_STAGE.equals (currStage))
        {
            
            return START_STAGE;
            
        }

        return null;
    
    }
 
    @Override   
    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {

        if (SELECT_FILE_STAGE.equals (oldStage))
        {
            
            if (!IMPORT_STAGE.equals (newStage))
            {
                
                return true;
                
            }
            
            this.fileFindError.setVisible (false);
            
            File f = this.fileFind.getSelectedFile ();
            
            if (!f.isFile ())
            {
                                
                this.fileFindError.setVisible (true);
                
                return false;
                
            }
            
            return true;
            
        }
    
        if (DECIDE_STAGE.equals (oldStage))
        {
            
            if (START_STAGE.equals (newStage))
            {
                
                return true;
                
            }
            
            if (this.importFile.isSelected ())
            {
                
                this.showStage (SELECT_FILE_STAGE);
                
                return false;
                
            }
            
            if (this.createNewProject.isSelected ())
            {
                
                this.newProjectPanel.setProject (null);
                this.newProjectPanel.setName (null);
                
                this.showStage (NEW_PROJECT_STAGE);
                
                return false;
                
            }

        }
    
        if ((START_STAGE.equals (oldStage))
            &&
            (DECIDE_STAGE.equals (newStage))
           )
        {
            
            this.enableButton ("next",
                               false);
            
            return true;
            
        }
    
        if ((EXISTING_STAGE.equals (oldStage))
            &&
            (newStage == null)
           )
        {
                        
            if (this.findProjects.isSelected ())
            {
                
                Environment.showLanding ();
                
                Environment.getLanding ().showFindProjects ();
                
                this.close ();
            
                return false;
            
            }
            
        }
    
        return true;
    
    }    
    
    @Override
    public String getStartStage ()
    {

        return START_STAGE;
    
    }
    
    @Override
    public WizardStep getStage (String stage)
    {
    
        final FirstUseWizard _this = this;
    
        WizardStep ws = new WizardStep ();

        if (NEW_PROJECT_STAGE.equals (stage))
        {
            
            ws.title = "{Project} details";

            ws.helpText = "Enter the name of the {project} below and select the directory where it should be saved.";

            ws.panel = this.newProjectPanel.createPanel (this,
                                                         null,
                                                         false,
                                                         null,
                                                         false);

            return ws;            
            
        }
        
        if (SELECT_PROJECT_DB_STAGE.equals (stage))
        {
            
            ws.title = "Select your {projects} directory";
            ws.helpText = String.format ("Use the finder below to find the directory where your {projects} database file is stored.  The file is called <b>%s%s</b>",
                                         Environment.getProjectInfoDBFile ().getName (),
                                         Constants.H2_DB_FILE_SUFFIX);
            
            Box b = new Box (BoxLayout.Y_AXIS);            
            
            this.projDBFindError.setVisible (false);
            this.projDBFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            b.add (this.projDBFindError);
            
            FormLayout fl = new FormLayout ("right:p, 6px, fill:200px:grow, 10px",
                                            "p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();
        
            this.projDBFind.setOnSelectHandler (new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
            
                    //_this.file = _this.fileFind.getSelectedFile ();
            
                    //_this.checkForFileToImport ();
                    
                    _this.enableButton ("next",
                                        true);
                    
                }
                
            });
            
            this.projDBFind.setApproveButtonText ("Select");
            this.projDBFind.setFinderSelectionMode (JFileChooser.DIRECTORIES_ONLY);
            this.projDBFind.setFinderTitle ("Select the directory");
                                    
            this.projDBFind.setFile (Environment.getUserQuollWriterDir ());                            
                                                            
            this.projDBFind.setFindButtonToolTip ("Click to find a directory");
            this.projDBFind.setClearOnCancel (true);
            this.projDBFind.init ();
                        
            builder.addLabel ("Directory",
                              cc.xy (1,
                                     1));
            builder.add (this.projDBFind, 
                         cc.xy (3,
                                1));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            b.add (p);

            ws.panel = b;
            
            return ws;
            
        }
        
        if (EXISTING_STAGE.equals (stage))
        {
            
            ws.title = "Find your {projects}";
            ws.helpText = "If you would like {QW} to find your {projects} then click <b>Finish</b> below.";
            
            FormLayout fl = new FormLayout ("10px, p, 10px",
                                            "p, 6px, p, 6px, p, 6px");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.findProjects.setText (Environment.replaceObjectNames ("{QW} should find my {projects}"));
        
            this.findProjects.setOpaque (false);

            this.selectProjectDB = new JRadioButton (Environment.replaceObjectNames ("I know where my {projects} database is"));

            this.selectProjectDB.setOpaque (false);

            ButtonGroup bg = new ButtonGroup ();

            bg.add (this.findProjects);
            bg.add (this.selectProjectDB);

            this.findProjects.setSelected (false);
            this.selectProjectDB.setSelected (false);
            
            this.findProjects.addActionListener (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        _this.enableButton ("next",
                                            true);

                    }

                });

            this.selectProjectDB.addActionListener (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.enableButton ("next",
                                            true);
                    
                    }

                });

            builder.add (this.findProjects,
                         cc.xy (2,
                                1));

            builder.add (this.selectProjectDB,
                         cc.xy (2,
                                3));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            ws.panel = p;
            
            return ws;
            
            
        }
        
        if (DECIDE_STAGE.equals (stage))
        {
            
            ws.title = "Your first {project}";
            ws.helpText = "If you already have your story in a .docx or .doc file then use the import option below, otherwise use the create option.  You can also just drag your .docx or .doc file onto this window.";
            
            FormLayout fl = new FormLayout ("p",
                                            "p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.importFile.setText ("Import a file");
                
            this.importFile.setOpaque (false);

            this.createNewProject = new JRadioButton (Environment.replaceObjectNames ("Create a {project}"));

            this.createNewProject.setOpaque (false);

            ButtonGroup bg = new ButtonGroup ();

            bg.add (this.importFile);
            bg.add (this.createNewProject);

            this.importFile.addActionListener (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {
                    
                        _this.enableButton ("next",
                                            true);

                    }

                });

            this.createNewProject.addActionListener (new ActionListener ()
                {

                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.enableButton ("next",
                                            true);

                    }

                });

            builder.add (this.createNewProject,
                         cc.xy (1,
                                1));

            builder.add (this.importFile,
                         cc.xy (1,
                                3));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setAlignmentY (JComponent.TOP_ALIGNMENT);

            Box b = new Box (BoxLayout.Y_AXIS);
            
            b.add (p);
            b.add (Box.createVerticalGlue ());
            
            JLabel l = UIUtils.createClickableLabel ("Not your first {project}?  Click here to find your {projects}.",
                                                       Environment.getIcon (Constants.FIND_ICON_NAME,
                                                                            Constants.ICON_CLICKABLE_LABEL),
                                                       new ActionListener ()
                                                       {
                                                        
                                                            public void actionPerformed (ActionEvent ev)
                                                            {
                                                                
                                                                Environment.showLanding ();
                                                                
                                                                Environment.getLanding ().showFindProjects ();
                                                                
                                                                _this.close ();
                                                                
                                                            }
                                                        
                                                       });
            
            b.add (l);
            
            ws.panel = b;
            
            return ws;
            
        }

        if (SELECT_FILE_STAGE.equals (stage))
        {
            
            ws.title = "Select a file to import";
            ws.helpText = "Microsoft Word files (.doc and .docx) are supported.  Please check <a href='help://projects/importing-a-file'>the import guide</a> to ensure your file has the correct format.";

            Box b = new Box (BoxLayout.Y_AXIS);

            this.fileFindError.setVisible (false);
            this.fileFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            b.add (this.fileFindError);
            
            FormLayout fl = new FormLayout ("right:p, 6px, fill:200px:grow, 10px",
                                            "p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();
        
            this.fileFind.setApproveButtonText ("Select");
            this.fileFind.setFinderSelectionMode (JFileChooser.FILES_ONLY);
            this.fileFind.setFinderTitle ("Select a file to import");
            
            if (this.fileFind.getSelectedFile () == null)
            {                                    
            
                this.fileFind.setFile (FileSystemView.getFileSystemView ().getDefaultDirectory ());
                
            }
        
            this.fileFind.setOnSelectHandler (new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
                    
                    _this.setImportFile (_this.fileFind.getSelectedFile ());
                                
                }
                
            });        
                        
            this.fileFind.setFileFilter (ImportProject.fileFilter);
                                    
            this.fileFind.setFindButtonToolTip ("Click to find a file");
            this.fileFind.setClearOnCancel (true);
            this.fileFind.init ();
                        
            builder.addLabel ("File",
                              cc.xy (1,
                                     1));
            builder.add (this.fileFind, 
                         cc.xy (3,
                                1));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            b.add (p);
            
            ws.panel = b;

            return ws;
            
        }
        
        if (IMPORT_STAGE.equals (stage))
        {
            
            ws.title = "Select the items you wish to import";

            ws.helpText = "Check the items below to ensure that they match what is in your file.  The first and last sentences of the description (if present) are shown for each item.";

            this.itemsTree.addMouseListener (new MouseAdapter ()
            {

                private void selectAllChildren (DefaultTreeModel       model,
                                                DefaultMutableTreeNode n,
                                                boolean                v)
                {

                    Enumeration<DefaultMutableTreeNode> en = n.children ();

                    while (en.hasMoreElements ())
                    {

                        DefaultMutableTreeNode c = en.nextElement ();

                        Object uo = c.getUserObject ();

                        if (uo instanceof SelectableDataObject)
                        {

                            SelectableDataObject s = (SelectableDataObject) uo;

                            s.selected = v;

                            // Tell the model that something has changed.
                            model.nodeChanged (c);

                            // Iterate.
                            this.selectAllChildren (model,
                                                    c,
                                                    v);

                        }

                    }

                }

                public void mousePressed (MouseEvent ev)
                {

                    TreePath tp = _this.itemsTree.getPathForLocation (ev.getX (),
                                                                      ev.getY ());

                    if (tp != null)
                    {

                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                        // Tell the model that something has changed.
                        DefaultTreeModel model = (DefaultTreeModel) _this.itemsTree.getModel ();

                        if (n.getUserObject () instanceof SelectableDataObject)
                        {
                        
                            SelectableDataObject s = (SelectableDataObject) n.getUserObject ();
    
                            s.selected = !s.selected;
    
                            model.nodeChanged (n);
    
                            this.selectAllChildren (model,
                                                    n,
                                                    s.selected);
                            
                        }

                    }

                }

            });

            this.itemsTree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

            this.itemsTree.setOpaque (false);
            this.itemsTree.setBorder (UIUtils.createPadding (5, 5, 5, 5));
                                                       
            JScrollPane sp = UIUtils.createScrollPane (this.itemsTree);

            ws.panel = sp;

            return ws;
            
        }
                
        if (START_STAGE.equals (stage))
        {
            
            ws.title = "Select the spell checker language";
            
            ws.helpText = "Welcome to Quoll Writer, this wizard will help you get started.<br /><br />First, select the language you would like to use for the spell checker.  You can download additional languages in the Options panel later.";
                        
            JComboBox lb = UIUtils.getSpellCheckLanguagesSelector (new ActionListener ()
                                                                   {
                                                                    
                                                                        @Override
                                                                        public void actionPerformed (ActionEvent ev)
                                                                        {
                                                                            
                                                                            // Set the property.
                                                                            String lang = ev.getActionCommand ();
                                                                            
                                                                            try
                                                                            {
                                                                            
                                                                                Environment.setUserProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME,
                                                                                                             lang);
                                                                                
                                                                            } catch (Exception e) {
                                                                                
                                                                                Environment.logError ("Unable to set spell check language to: " +
                                                                                                      lang,
                                                                                                      e);
                                                                                
                                                                            }
                                                                                                                                                        
                                                                        }
                                                                    
                                                                   },
                                                                   Environment.getUserProperties ().getProperty (Constants.SPELL_CHECK_LANGUAGE_PROPERTY_NAME));

            lb.setMaximumSize (lb.getPreferredSize ());
                                                                                                                                      
            Box wrap = new Box (BoxLayout.X_AXIS);
                 
            wrap.add (lb);
            wrap.add (Box.createHorizontalGlue ());
            wrap.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            wrap.setAlignmentY (JComponent.TOP_ALIGNMENT);
                                                                               
            Box b = new Box (BoxLayout.Y_AXIS);
            
            b.add (wrap);
            b.add (Box.createVerticalGlue ());
                        
            ws.panel = b;

            return ws;
            
        }
    
        return null;
    
    }
 
    @Override
    protected void enableButtons (String currentStage)
    {
        
        super.enableButtons (currentStage);

        if (DECIDE_STAGE.equals (currentStage))
        {
            
            this.enableButton ("next",
                               (this.importFile.isSelected () || this.createNewProject.isSelected ()));
            
        }
        
        if (SELECT_FILE_STAGE.equals (currentStage))
        {
            
            this.enableButton ("next",
                               (this.fileFind.getSelectedFile () != null));
            
        }
        
        if (SELECT_PROJECT_DB_STAGE.equals (currentStage))
        {
            
            File f = this.projDBFind.getSelectedFile ();
            
            boolean enable = false;
            
            if (f != null)
            {
                
                File pf = this.getProjectDBFile (f);
                
                enable = pf.exists () && pf.isFile ();
                
            }

            this.enableButton ("next",
                               enable);
            
            return;            
            
        }
        
        if (EXISTING_STAGE.equals (currentStage))
        {
            
            this.enableButton ("next",
                               (this.selectProjectDB.isSelected () || this.findProjects.isSelected ()));
            
        }        

    }    
    
    private File getProjectDBFile (File dir)
    {
        
        return new File (dir,
                         Constants.PROJECT_INFO_DB_FILE_NAME_PREFIX + Constants.H2_DB_FILE_SUFFIX);
        
    }
    
    private void addAssetsToTree (DefaultMutableTreeNode          root,
                                  java.util.List<? extends Asset> assets)
    {

        if (assets.size () > 0)
        {

            TreeParentNode c = new TreeParentNode (assets.get (0).getObjectType (),
                                                   Environment.getObjectTypeNamePlural (assets.get (0).getObjectType ()));

            SelectableDataObject sd = new SelectableDataObject (c);

            sd.selected = true;

            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (sd);

            root.add (tn);

            Collections.sort (assets,
                              new NamedObjectSorter ());

            for (Asset a : assets)
            {

                sd = new SelectableDataObject (a);

                sd.selected = true;

                DefaultMutableTreeNode n = new DefaultMutableTreeNode (sd);

                tn.add (n);

                String t = this.getFirstLastSentence (a.getDescriptionText ());

                if (t.length () > 0)
                {

                    // Get the first and last sentence.
                    n.add (new DefaultMutableTreeNode (t));

                }

            }

        }

    }

    private Project getSelectedItems ()
    {

        String projName = null;
        
        Project p = new Project (projName);

        Book b = new Book (p,
                           null);

        p.addBook (b);
        b.setName (projName);

        DefaultTreeModel dtm = (DefaultTreeModel) this.itemsTree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        Enumeration en = root.depthFirstEnumeration ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

            Object o = node.getUserObject ();

            if (o instanceof SelectableDataObject)
            {

                SelectableDataObject so = (SelectableDataObject) o;

                if (so.selected)
                {

                    if (so.obj instanceof Asset)
                    {

                        p.addAsset ((Asset) so.obj);

                    }

                    if (so.obj instanceof Chapter)
                    {

                        b.addChapter ((Chapter) so.obj);

                    }

                }

            }

        }

        if ((b.getChapters () != null)
            &&
            (b.getChapters ().size () == 0)
           )
        {
            
            p.getBooks ().remove (b);
            
        }

        return p;

    }
    
    private void setImportFile (final File f)
    {
        
        final FirstUseWizard _this = this;
        
        try
        {
        
            Importer.importProject (f.toURI (),
                                    new ImportCallback ()
                                    {
                                       
                                        @Override
                                        public void exceptionOccurred (Exception e,
                                                                       URI       uri)
                                        {
                                            
                                            Environment.logError ("Unable to import file: " +
                                                                  f,
                                                                  e);
                                            
                                            UIUtils.showErrorMessage (_this,
                                                                      "Unable to import file");
                                            
                                        }
                                       
                                        @Override
                                        public void projectCreated (final Project p,
                                                                    URI           uri)
                                        {
                                    
                                            UIUtils.doLater (new ActionListener ()
                                            {
                                    
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                    
                                                    if (_this.itemsTree != null)
                                                    {
                                                
                                                        _this.itemsTree.setModel (new DefaultTreeModel (_this.createTree (p)));
                                        
                                                        UIUtils.expandAllNodesWithChildren (_this.itemsTree);
                                        
                                                        _this.importProj = p;

                                                        _this.newProjectPanel.setName (p.getName ());
                                    
                                                        _this.newProjectPanel.setProject (p);
                                    
                                                    }
                                                        
                                                }
                                    
                                            });
                                    
                                        }                                   
                                        
                                    });
            
            
            
        } catch (Exception e) {
                        
            Environment.logError ("Unable to import file: " +
                                  f,
                                  e);
            
            UIUtils.showErrorMessage (this,
                                      "Unable to import file");
            
        }            
        
    }
    
    private DefaultMutableTreeNode createTree (Project p)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (new SelectableDataObject (p));

        DefaultMutableTreeNode n = null;
        DefaultMutableTreeNode tn = null;

        if (p.getBooks ().size () > 0)
        {

            Book b = p.getBooks ().get (0);

            if (b.getChapters ().size () > 0)
            {

                TreeParentNode c = new TreeParentNode (Chapter.OBJECT_TYPE,
                                                       Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));

                SelectableDataObject sd = new SelectableDataObject (c);

                sd.selected = true;

                tn = new DefaultMutableTreeNode (sd);

                root.add (tn);

                Collections.sort (b.getChapters (),
                                  new NamedObjectSorter ());

                for (Chapter ch : b.getChapters ())
                {

                    sd = new SelectableDataObject (ch);

                    sd.selected = true;

                    n = new DefaultMutableTreeNode (sd);

                    tn.add (n);

                    String t = this.getFirstLastSentence (ch.getChapterText ());

                    if (t.length () > 0)
                    {

                        // Get the first and last sentence.
                        n.add (new DefaultMutableTreeNode (t));

                    }

                }

            }

        }

        this.addAssetsToTree (root,
                              p.getCharacters ());

        this.addAssetsToTree (root,
                              p.getLocations ());

        this.addAssetsToTree (root,
                              p.getQObjects ());

        this.addAssetsToTree (root,
                              p.getResearchItems ());

        return root;

    }    
 
    private String getFirstLastSentence (String st)
    {

        if (st == null)
        {
            
            return "";
            
        }
    
        TextIterator t = new TextIterator (st);

        Paragraph p = t.getFirstParagraph ();
        
        if (p != null)
        {
            
            Sentence s = p.getFirstSentence ();
            
            StringBuilder b = new StringBuilder ();
            
            b.append (s.getText ());
            
            p = t.getLastParagraph ();
            
            if (p != null)
            {
                
                b.append (" ... ");
                
                b.append (p.getLastSentence ().getText ());
                
            }
            
            return b.toString ();
            
        }
        
        return "";
            
    }    
    
}