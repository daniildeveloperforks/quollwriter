package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

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
import com.quollwriter.events.*;

public abstract class TypesEditor extends Box implements PropertyChangedListener 
{

    private TypesHandler handler = null;
    private DefaultTableModel typeModel = null;
    private AbstractViewer viewer = null;

    public TypesEditor (AbstractViewer pv,
                        TypesHandler   handler)
    {

        super (BoxLayout.Y_AXIS);
        
        this.viewer = pv;
        this.handler = handler;
        
        this.handler.addPropertyChangedListener (this);
        
    }
    
    @Override
    public void propertyChanged (PropertyChangedEvent ev)
    {
        
        this.reloadTypes ();
        
    }

    public TypesHandler getTypesHandler ()
    {
        
        return this.handler;
        
    }
    
    public abstract String getTypesName ();
    
    public abstract String getNewTypeHelp ();

    public String getHelpText ()
    {

        return null;

    }

    //public JComponent getContentPanel ()
    public void init ()
    {

        final TypesEditor _this = this;

        
        //Box b = new Box (BoxLayout.Y_AXIS);

        this.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.setOpaque (true);
        this.setBackground (null);
        this.add (Box.createVerticalStrut (5));

        this.add (UIUtils.createBoldSubHeader (String.format ("New %s",
                                                              this.getTypesName ()),
                                               null));

        JTextPane tp = UIUtils.createHelpTextPane (String.format ("Enter the new %s to add below, separate the %s with commas or semi-colons.",
                                                                  this.getTypesName ().toLowerCase (),
                                                                  this.getTypesName ().toLowerCase ()),
                                                   this.viewer);

        tp.setBorder (new EmptyBorder (5,
                                       5,
                                       0,
                                       5));

        this.add (tp);

        final JTextField newTypes = UIUtils.createTextField ();
        newTypes.setAlignmentX (Component.LEFT_ALIGNMENT);

        final JTable typeTable = UIUtils.createTable ();
        typeTable.setTableHeader (null);
        typeTable.setToolTipText ("Double click to edit, press Enter when done.");
                
        typeTable.setModel (new DefaultTableModel ()
        {

            public boolean isCellEditable (int row,
                                           int col)
            {

                return _this.handler.typesEditable ();

            }

        });

        UIUtils.listenToTableForCellChanges (typeTable,
                                             new ActionAdapter ()
        {
           
            public void actionPerformed (ActionEvent ev)
            {
                
                TableCellListener tcl = (TableCellListener) ev.getSource ();
                
                _this.handler.renameType (tcl.getOldValue ().toString (),
                                          tcl.getNewValue ().toString (),
                                          true);
                
            }
            
        });        
        
        typeTable.addMouseListener (new MouseEventHandler ()
        {
                        
            @Override
            public void fillPopup (final JPopupMenu m,
                                   final MouseEvent ev)
            {
                
                final int rowInd = typeTable.rowAtPoint (ev.getPoint ());
                        
                if (rowInd < 0)
                {
                    
                    return;
                    
                }
                        
                typeTable.setRowSelectionInterval (rowInd,
                                                   rowInd);

                m.add (UIUtils.createMenuItem ("Edit",
                                               Constants.EDIT_ICON_NAME,
                                               new ActionListener ()
                                               {
                                                
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        typeTable.editCellAt (rowInd,
                                                                              0);
                                                        
                                                    }
                                                
                                               }));
                
            }
            
        });
        
        this.typeModel = (DefaultTableModel) typeTable.getModel ();

        this.reloadTypes ();

        Box fb = new Box (BoxLayout.Y_AXIS);
        fb.setAlignmentX (Component.LEFT_ALIGNMENT);
        fb.add (newTypes);
        fb.add (Box.createVerticalStrut (5));
        fb.setBorder (new EmptyBorder (5,
                                       5,
                                       20,
                                       5));

        final JButton add = new JButton ("Add");

        JButton[] buts = new JButton[] { add };
        
        final ActionAdapter aa = new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                String n = newTypes.getText ();

                StringTokenizer t = new StringTokenizer (n,
                                                         ",;");

                while (t.hasMoreTokens ())
                {

                    String w = t.nextToken ().trim ();

                    DefaultTableModel m = (DefaultTableModel) typeTable.getModel ();

                    Vector r = new Vector ();
                    r.add (w);
                    m.insertRow (0,
                                 r);

                    _this.handler.addType (w,
                                           true);

                }

                newTypes.setText ("");

            }

        };

        add.addActionListener (aa);

        UIUtils.addDoActionOnReturnPressed (newTypes,
                                            aa);

        fb.add (UIUtils.createButtonBar2 (buts, Component.LEFT_ALIGNMENT));
        
        this.add (fb);
        
        this.add (UIUtils.createBoldSubHeader (Environment.replaceObjectNames (this.getTypesName ()),
                                               null));

        fb = new Box (BoxLayout.Y_AXIS);
        fb.setAlignmentX (Component.LEFT_ALIGNMENT);
        fb.setBorder (new EmptyBorder (5,
                                       5,
                                       0,
                                       5));                                            
                                            
        if (this.getNewTypeHelp () != null)
        {
                                            
            tp = UIUtils.createHelpTextPane (Environment.replaceObjectNames (this.getNewTypeHelp ()),
                                             this.viewer);
            tp.setBorder (null);
            fb.add (tp);
            fb.add (Box.createVerticalStrut (10));

        }
        
        final JScrollPane ppsp = UIUtils.createScrollPane (typeTable);

        typeTable.setPreferredScrollableViewportSize (new Dimension (-1,
                                                                     (typeTable.getRowHeight () + 3) * 5));

        fb.add (ppsp);

        final JButton remove = new JButton ("Remove Selected");

        buts = new JButton[] { remove };
        
        remove.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                DefaultTableModel m = (DefaultTableModel) typeTable.getModel ();

                int[] selection = typeTable.getSelectedRows ();

                for (int i = selection.length - 1; i > -1; i--)
                {

                    String s = (String) m.getValueAt (selection[i],
                                                      0);

                    // Remove the row.
                    m.removeRow (selection[i]);
                    
                    _this.handler.removeType (s,
                                              true);

                }

                ((DefaultListSelectionModel) typeTable.getSelectionModel ()).clearSelection ();

            }

        });

        fb.add (Box.createVerticalStrut (5));
        
        fb.add (UIUtils.createButtonBar2 (buts, Component.LEFT_ALIGNMENT));

        this.add (fb);
        
        JButton finish = new JButton ("Finish");

        finish.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        buts = new JButton[] { finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT); 
        bp.setOpaque (false);

        this.add (Box.createVerticalStrut (10));
        
        this.add (bp);        
        
        //return b;

    }

    public void reloadTypes ()
    {

        Set<String> types = this.handler.getTypes ();

        Vector typeData = new Vector ();

        for (String i : types)
        {
            
            Vector d = new Vector ();
            d.add (i);
                        
            typeData.add (d);
            
        }

        Vector<String> cols = new Vector ();
        cols.add (this.getTypesName ());

        this.typeModel.setDataVector (typeData,
                                      cols);
        
    }
/*
    public JButton[] getButtons ()
    {

        final TypesEditor _this = this;

        JButton b = new JButton ("Finish");

        b.addActionListener (this.getCloseAction ());

        JButton[] buts = new JButton[] { b };

        return buts;

    }
*/
}
