/* Handle model and layout file operations.

 Copyright (c) 2011 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */

package ptolemy.homer.kernel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.expr.Parameter;
import ptolemy.homer.gui.TabScenePanel;
import ptolemy.homer.gui.UIDesignerFrame;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.MessageHandler;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;

/** Handle model and layout file operations.
 * 
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class LayoutFileOperations {

    private LayoutFileOperations() {
        // TODO Auto-generated constructor stub
    }

    public static void save(UIDesignerFrame parent) {
        // TODO
    }

    public static CompositeActor openModelFile(URL url) {
        CompositeActor topLevel = null;
        MoMLParser parser = new MoMLParser(new Workspace());
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        try {
            topLevel = (CompositeActor) parser.parse(null, url);
        } catch (Exception e1) {
            MessageHandler.error("Unable to parse the file", e1);
        }

        return topLevel;
    }

    public static void saveAs(UIDesignerFrame mainFrame, File layoutFile) {

        CompositeActor model = null;
        BufferedWriter out = null;
        try {
            // Get the original model
            model = (CompositeActor) openModelFile(mainFrame.getModelURL());

            // Add remote attributes to elements
            for (NamedObj element : mainFrame.getRemoteObjectSet()) {
                String strippedFullName = stripFullName(element.getFullName());

                // Check if the element is a sink or a source
                if (element instanceof ComponentEntity) {
                    SinkOrSource sinkOrSource = isSinkOrSource((ComponentEntity) element);

                    if (sinkOrSource == SinkOrSource.SOURCE
                            || sinkOrSource == SinkOrSource.SINK_AND_SOURCE) {
                        new Parameter(model.getEntity(strippedFullName),
                                HomerConstants.REMOTE_NODE)
                                .setExpression(HomerConstants.REMOTE_SOURCE);
                    } else if (sinkOrSource == SinkOrSource.SINK) {
                        new Parameter(model.getEntity(strippedFullName),
                                HomerConstants.REMOTE_NODE)
                                .setExpression(HomerConstants.REMOTE_SINK);
                    }

                } else if (element instanceof Attribute) {
                    new Parameter(model.getAttribute(strippedFullName),
                            HomerConstants.REMOTE_NODE)
                            .setExpression(HomerConstants.REMOTE_ATTRIBUTE);
                }
            }

            // Add location and tab information to elements
            Attribute tabs = new Attribute(model, HomerConstants.TABS_NODE);

            HashMap<TabScenePanel, StringAttribute> tabTags = new HashMap<TabScenePanel, StringAttribute>();
            for (NamedObj element : mainFrame.getWidgetMap().keySet()) {
                Widget widget = (Widget) mainFrame.getWidgetMap().get(element);
                String strippedFullName = stripFullName(element.getFullName());
                // Add location
                NamedObj elementInModel = null;

                if (element instanceof Attribute) {
                    elementInModel = model.getAttribute(strippedFullName);
                } else if (element instanceof ComponentEntity) {
                    elementInModel = model.getEntity(strippedFullName);
                } else {
                    // TODO throw exception
                }

                new HomerLocation(elementInModel, HomerConstants.POSITION_NODE)
                        .setToken(getLocationToken(widget));

                StringAttribute tabTag = tabTags.get(mainFrame
                        .getWidgetTabMap().get(widget));
                if (tabTag == null) {
                    tabTag = new StringAttribute(tabs, tabs.uniqueName("tab_"));
                    // FIXME set correct name
                    tabTag.setExpression(tabTag.getName());
                    tabTags.put(mainFrame.getWidgetTabMap().get(widget), tabTag);
                }
                // Add tab information
                new StringAttribute(elementInModel, HomerConstants.TAB_NODE)
                        .setExpression(tabTag.getName());
                // Store tag identifier for later
            }

            // Create layout model
            System.out.println(model.exportMoML());
            RemoteModel clientModel = new RemoteModel(RemoteModelType.CLIENT);
            clientModel.loadModel(model);

            // Save in file
            out = new BufferedWriter(new FileWriter(layoutFile));
            model.exportMoML(out);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private static String stripFullName(String fullName) {
        // TODO check string.
        return fullName.substring(fullName.substring(1).indexOf(".") + 2);
    }

    public static IntMatrixToken getLocationToken(Widget widget) {
        int[][] location = new int[][] { { widget.getBounds().x,
                widget.getBounds().y, widget.getBounds().width,
                widget.getBounds().height } };
        IntMatrixToken locationToken = null;
        try {
            locationToken = new IntMatrixToken(location);
        } catch (IllegalActionException e) {
            // This is reached only if the location matrix is null.
            e.printStackTrace();
        }
        return locationToken;
    }

    public static SinkOrSource isSinkOrSource(ComponentEntity entity) {
        boolean isSink = true;
        boolean isSource = true;

        for (Object portObject : ((ComponentEntity) entity).portList()) {
            if (!(portObject instanceof IOPort)) {
                continue;
            }

            IOPort port = (IOPort) portObject;
            for (Object relationObject : port.linkedRelationList()) {
                Relation relation = (Relation) relationObject;
                List<Port> linkedPortList = relation.linkedPortList(port);

                for (Port connectingPort : linkedPortList) {
                    if (connectingPort instanceof IOPort) {
                        if (port.isOutput()) {
                            isSink = false;
                        }
                        if (port.isInput()) {
                            isSource = false;
                        }
                    }
                }
            }
        }

        if (isSink && isSource) {
            return SinkOrSource.SINK_AND_SOURCE;
        } else if (isSource) {
            return SinkOrSource.SOURCE;
        } else if (isSink) {
            return SinkOrSource.SINK;
        }

        return SinkOrSource.NONE;
    }

    public static enum SinkOrSource {
        SINK, SOURCE, SINK_AND_SOURCE, NONE
    }

}
