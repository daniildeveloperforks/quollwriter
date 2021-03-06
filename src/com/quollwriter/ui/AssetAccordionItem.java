package com.quollwriter.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class AssetAccordionItem extends ProjectObjectsAccordionItem<ProjectViewer> implements ProjectEventListener
{

    public static final String ID_PREFIX = "asset:";

    private UserConfigurableObjectType objType = null;
    private UserConfigurableObjectTypeField sortField = null;
    private Comparator<Asset> sorter = null;

    public AssetAccordionItem (UserConfigurableObjectType objType,
                               ProjectViewer              pv)
    {

        super (objType.getObjectTypeNamePlural (),
               objType.getIcon16x16 (),
               pv);

        this.objType = objType;
               
        Environment.addUserProjectEventListener (this);
               
        this.sorter = new Comparator<Asset> ()
        {
            
            @Override
            public boolean equals (Object o)
            {
                
                return this == o;
            
            }
            
            @Override
            public int compare (Asset o1,
                                Asset o2)
            {
                
                return NamedObjectSorter.getInstance ().compare (o1, o2);
            
            }
            
        };
               
    }
    
    @Override
    public String getTitle ()
    {
        
        return this.objType.getObjectTypeNamePlural ();
        
    }
    
    @Override
    public String getId ()
    {
        
        return this.objType.getObjectTypeId ();
        
    }
    
    public UserConfigurableObjectType getUserConfigurableObjectType ()
    {
        
        return this.objType;
        
    }
    
    @Override
    public void eventOccurred (ProjectEvent ev)
    {

        if (ev.getType ().equals (ProjectEvent.USER_OBJECT_TYPE))
        {
            
            if (ev.getSource ().equals (this.objType))
            {
              
                this.update ();
                
                return;
                
            }
            
        }
            
    }
    
    @Override
    public void update ()
    {
        
        this.getHeader ().setIcon (objType.getIcon16x16 ());
        
        super.update ();
        
    }
    
    @Override
    public void initFromSaveState (String ss)
    {
        
        super.initFromSaveState (ss);
                
        Map<String, Object> state = (Map<String, Object>) JSONDecoder.decode (ss);        
        
        Number sortFieldKey = (Number) state.get ("sortFieldKey");
        
        if (sortFieldKey != null)
        {
            
            try
            {
            
                this.sortBy (Environment.getUserConfigurableObjectTypeField (sortFieldKey.longValue ()));
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to get user config object type field: " +
                                      sortFieldKey,
                                      e);
                
            }
            
        } else {
        
            Collection<String> objRefs = (Collection<String>) state.get ("order");
            
            if (objRefs != null)
            {
            
                final Map<String, Number> order = new HashMap ();
                
                int c = 0;
                
                for (String ref : objRefs)
                {
                    
                    order.put (ref,
                               c++);
                    
                }
            
                this.sorter = new Comparator<Asset> ()
                {
                  
                    @Override
                    public boolean equals (Object o)
                    {
                        
                        return this.equals (o);
                        
                    }
                    
                    @Override
                    public int compare (Asset o1,
                                        Asset o2)
                    {
                        
                        Number nv1 = order.get (o1.getObjectReference ().asString ());
                        
                        Number nv2 = order.get (o2.getObjectReference ().asString ());
                        
                        if ((nv1 == null)
                            ||
                            (nv2 == null)
                           )
                        {
                            
                            return NamedObjectSorter.getInstance ().compare (o1, o2);
                            
                        }
                        
                        return nv1.intValue () - nv2.intValue ();
                        
                    }
                    
                };
                                
            }
            
            this.reloadTree ();    

        }
                                
    }    
    
    @Override
    public Map<String, Object> getSaveStateAsMap ()
    {

        Map<String, Object> ss = super.getSaveStateAsMap ();
                
        if (this.sortField != null)
        {                
        
            ss.put ("sortFieldKey",
                    this.sortField.getKey ());
        
        } else {
        
            DefaultMutableTreeNode r = (DefaultMutableTreeNode) ((DefaultTreeModel) this.tree.getModel ()).getRoot ();
            
            Set<String> objRefs = new LinkedHashSet ();
            
            for (int i = 0; i < r.getChildCount (); i++)
            {
                
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) r.getChildAt (i);
                
                NamedObject oo = (NamedObject) n.getUserObject ();
                
                objRefs.add (oo.getObjectReference ().asString ());
                
            }
            
            if (objRefs.size () > 0)
            {
                
                ss.put ("order",
                        objRefs);
                
            }

        }
        
        return ss;
    
    }    
    
    @Override
    public void initTree ()
    {

        this.reloadTree ();
    
    }

    @Override
    public void fillHeaderPopupMenu (JPopupMenu m,
                                     MouseEvent ev)
    {

        final AssetAccordionItem _this = this;

        AbstractAction addNewItem = UIUtils.createAddAssetActionListener (this.objType,
                                                                          this.viewer,
                                                                          null,
                                                                          null);

        m.add (UIUtils.createMenuItem (String.format ("Add a new %s",
                                                      this.objType.getObjectTypeName ()),
                                       Constants.ADD_ICON_NAME,
                                       addNewItem));
        
        m.add (UIUtils.createMenuItem (String.format ("Edit the %s information/fields",
                                                      this.objType.getObjectTypeName ()),
                                       Constants.EDIT_ICON_NAME,
                                        new ActionListener ()
                                        {
                                         
                                             @Override
                                             public void actionPerformed (ActionEvent ev)
                                             {
                                                 
                                                 UIUtils.showObjectTypeEdit (_this.objType,
                                                                             _this.viewer);
                                                 
                                             }
                                         
                                        }));
        
        // Get all the sortable fields for the object.
        Set<UserConfigurableObjectTypeField> fields = this.objType.getSortableFields ();
        
        if (fields.size () > 0)
        {

            JMenu sortMenu = new JMenu (String.format ("Sort the %s by",
                                                       this.objType.getObjectTypeNamePlural ()));

            m.add (sortMenu);

            ButtonGroup group = new ButtonGroup ();

            sortMenu.setIcon (Environment.getIcon (Constants.SORT_ICON_NAME,
                                                   Constants.ICON_MENU));

            for (UserConfigurableObjectTypeField field : fields)
            {
                
                final UserConfigurableObjectTypeField f = field;
                
                JMenuItem mi = new JRadioButtonMenuItem (f.getFormName ());

                group.add (mi);
/*                
                    if (_this.ideaType.getSortBy ().equals (IdeaType.SORT_BY_RATING))
                    {

                        mi.setSelected (true);

                    }
*/
                mi.addActionListener (new ActionAdapter ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.sortBy (f);

                    }

                });

                sortMenu.add (mi);
        
            }
            
        }        
        
/*
        if (this.forObjType.equals (QObject.OBJECT_TYPE))
        {

            // Add New Type
            m.add (UIUtils.createMenuItem (Environment.replaceObjectNames ("New {Object} Type"),
                                           Constants.ADD_ICON_NAME,
                                           this.viewer.getAction (ProjectViewer.NEW_ITEM_TYPE_ACTION)));

            m.add (UIUtils.createMenuItem (Environment.replaceObjectNames ("Manage {Object} Types"),
                                           Constants.EDIT_ICON_NAME,
                                           this.viewer.getAction (ProjectViewer.MANAGE_ITEM_TYPES_ACTION)));

        }
*/
    }

    private void sortBy (final UserConfigurableObjectTypeField f)
    {
                                
        this.sorter = new Comparator<Asset> ()
        {
          
            @Override
            public boolean equals (Object o)
            {
                
                return this.equals (o);
                
            }
            
            @Override
            public int compare (Asset o1,
                                Asset o2)
            {
                
                if (f instanceof ObjectNameUserConfigurableObjectTypeField)
                {
                    
                    return o1.getName ().toLowerCase ().compareTo (o2.getName ().toLowerCase ());
                    
                }
                
                Object v1 = o1.getValueForField (f);
                Object v2 = o2.getValueForField (f);

                if ((v1 != null)
                    &&
                    (v2 == null)
                   )
                {
                    
                    return 1;
                    
                }
                
                if ((v1 == null)
                    &&
                    (v2 != null)
                   )
                {
                    
                    return -1;
                    
                }

                if ((v1 == null)
                    &&
                    (v2 == null)
                   )
                {
                    
                    return 0;
                    
                }
                
                if ((v1 instanceof String)
                    &&
                    (v2 instanceof String)
                   )
                {
                    
                    return ((String) v1).toLowerCase ().compareTo (((String) v2).toLowerCase ());
                    
                }
                
                if ((v1 instanceof Number)
                    &&
                    (v2 instanceof Number)
                   )
                {
                    
                    Double d1 = ((Number) v1).doubleValue ();
                    Double d2 = ((Number) v2).doubleValue ();
                    
                    return d1.compareTo (d2);
                    
                }

                if ((v1 instanceof Date)
                    &&
                    (v2 instanceof Date)
                   )
                {
                    
                    return ((Date) v1).compareTo ((Date) v2);
                    
                }

                String sv1 = v1.toString ().toLowerCase ();
                String sv2 = v2.toString ().toLowerCase ();
                
                return sv1.compareTo (sv2);
                
            }
            
        };
        
        this.reloadTree ();

        this.sortField = f;
        
    }
    
    @Override
    public void reloadTree ()
    {

        ((DefaultTreeModel) this.tree.getModel ()).setRoot (UIUtils.createAssetTree (this.objType,
                                                                                     this.viewer.getProject (),
                                                                                     this.sorter));

    }

    public boolean showItemCountOnHeader ()
    {

        return true;

    }

    public int getItemCount ()
    {

        int c = this.viewer.getProject ().getAllNamedChildObjects (this.objType).size ();

        return c;

    }

    @Override
    public DragActionHandler getTreeDragActionHandler (ProjectViewer pv)
    {

        final AssetAccordionItem _this = this;
    
        return new DragActionHandler<Asset> ()
        {
            
            @Override
            public boolean canImportForeignObject (NamedObject obj)
            {
                
                // Allow support of addition of same object type.
                return false;
                
            }
            
            @Override
            public boolean importForeignObject (NamedObject obj,
                                                int         insertRow)
            {
                
                return false;
                
            }
            
            @Override
            public boolean handleMove (int   fromRow,
                                       int   toRow,
                                       Asset object)
                                throws GeneralException
            {
                
                _this.sortField = null;
                
                return true;
                
            }

            @Override
            public boolean performAction (int         removeRow,
                                          NamedObject removeObject,
                                          int         insertRow,
                                          NamedObject insertObject)
                                   throws GeneralException
            {
                
                return true;
                
            }
            
        };

    }

    @Override
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {

        final AssetAccordionItem _this = this;

        final TreePath tp = _this.tree.getPathForLocation (ev.getX (),
                                                           ev.getY ());

        JMenuItem mi = null;

        if (tp != null)
        {

            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            final NamedObject d = (NamedObject) node.getUserObject ();

            mi = new JMenuItem ("View",
                                Environment.getIcon ("view",
                                                     Constants.ICON_MENU));

            mi.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.viewer.viewObject (d);

                }

            });

            m.add (mi);

            m.add (UIUtils.createMenuItem ("Edit",
                                           Constants.EDIT_ICON_NAME,
                                           AssetViewPanel.getEditAssetAction (this.viewer,
                                                                              (Asset) d)));

            m.add (UIUtils.createTagsMenu (d,
                                           this.viewer));
            
            mi = new JMenuItem ("Delete",
                                Environment.getIcon ("delete",
                                                     Constants.ICON_MENU));

            m.add (mi);

            mi.addActionListener (AssetViewPanel.getDeleteAssetAction ((ProjectViewer) _this.viewer,
                                                                       (Asset) d));

        }

    }

    @Override
    public TreeCellEditor getTreeCellEditor (ProjectViewer pv)
    {

        return new ProjectTreeCellEditor (pv,
                                          tree);

    }

    public int getViewObjectClickCount (Object d)
    {

        return 1;

    }

    @Override
    public boolean isAllowObjectPreview ()
    {
        
        return true;
        
    }
    
    @Override
    public boolean isTreeEditable ()
    {

        return false;

    }

    @Override
    public boolean isDragEnabled ()
    {

        return true;

    }

}
