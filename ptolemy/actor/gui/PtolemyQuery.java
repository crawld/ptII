/* A subclass of Query that provides a method to automatically set
 * a Variable when an entry is changed and vice versa.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import java.util.*;
import ptolemy.data.expr.ValueListener;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.*;
import ptolemy.actor.gui.style.*;
import ptolemy.data.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.Documentation;
import ptolemy.actor.event.SetParameter;

//////////////////////////////////////////////////////////////////////////
//// PtolemyQuery
/**
This class provides a method to create a mapping from a Query entry to
a variable. It is possible to create a mapping from more than one Querey
entries to the same variable. The Variable will be automatically set each
time that a corresponding Query entry that the variable is attached to
changes. All Query entries that a variable is attached to will also be
automatically set when the value of the variable changes.
<p>
One might think that this could cause a race condition, since changing
an entry would cause the corresponding variable's value to change, 
which would in turn cause the same entry to be set again. This class
has code that will block such a condition from occuring, so this is
not a problem.
<p>
When an entry is changed, this class queues a change request with
the director. It is important to note, therefore, that it may take
some time before the variable's value is actually changed, since it 
is up to the director to decide when topology mutations may occur.
The director will typically delagate mutation requests to the 
Manager, although this is not necessarilly the case.
<p>
To use this class, first add an entry to
the query, and then use the attachParameter method in this class
to associate a variable to that entry.

@author Brian K. Vogel
@version $Id$
*/
public class PtolemyQuery extends Query
    implements QueryListener, ValueListener {

    /** Construct a panel with no queries in it.  Equivalent to 
     *  calling the second constructor with a null argument.
     */
    public PtolemyQuery() {
	this(null);
    }

    /** Construct a panel with no queries in it.
     *  When an entry changes, a change request is
     *  queued with the given composite entity. The director
     *  will then schedule the corresponding variable's
     *  value to be updated at an appropriate time in 
     *  the execution of the model. Note that
     *  only one PtolemyQuery object is allowed per
     *  director.   If the entity is null, this query will
     *  attempt to find an entity.
     *  @param entity The entity for a model. This should
     *  be the entity that deeply contains all variables that
     *  are attached to query entries.
     */
    public PtolemyQuery(CompositeEntity entity) {
	super();
	addQueryListener(this);
	_parameters = new HashMap();
	_entity = entity;
        _varToListOfEntries = new HashMap();
    }

    /** 
     * Add a new entry to this query that represents the given parameter.
     * The name of the entry will be set to the name of the parameter.
     * If the parameter contains a parameter style, then use the style to 
     * create the entry, otherwise just create a new line entry.
     * Attach the variable to the new entry.
     */
    public void addStyledEntry(Variable param) {
	// Look for a ParameterEditorStyle.
	Iterator styles
	    = param.attributeList(ParameterEditorStyle.class).iterator();
	boolean foundStyle = false;
	while (styles.hasNext() && !foundStyle) {
	    ParameterEditorStyle style
		= (ParameterEditorStyle)styles.next();
	    try {
		style.addEntry(this);
		foundStyle = true;
	    } catch (IllegalActionException ex) {
		// Ignore failures here, and just present the default
		// dialog.
	    }
	}
	if (!(foundStyle)) {
	    addLine(param.getName(),
		    param.getName(),
		    param.stringRepresentation());
	    attachParameter(param, param.getName());
	}
    }

    /** Attach a variable <i>var</i> to an entry, <i>entryName</i>,
     *  of a Query. After attaching the <i>var</i> to the entry,
     *  automatically set <i>var</i> when <i>entryName</i> changes.
     *  If <i>var</i> has previously been attached to an entry,
     *  then the old value is replaced with <i>entryName</i>.
     *  @param var The variable to attach to an entry.
     *  @param entryName The entry to attach the variable to.
     */
    public void attachParameter(Variable var, String entryName) {
	// Put the variable in a Map from entryName -> var
	_parameters.put(entryName, var);
        var.addValueListener(this);
        String tip = Documentation.consolidate(var);
        if (tip != null) {
            setToolTip(entryName, tip);
        }
	// Put the variable in a Map from var -> (list of entry names
	// attached to var), but only if entryName is not already
	// contained by the list.
	if (_varToListOfEntries.get(var) == null) {
	    // No mapping for var exists.
	    List entryNameList = new LinkedList();
	    entryNameList.add(entryName);
	    _varToListOfEntries.put(var, entryNameList);
	} else {
	    // var is mapped to a list of entry names, but need to check
	    // if entryName is in the list. If not, add it.
	    List entryNameList = (List)_varToListOfEntries.get(var);
	    Iterator entryNames = entryNameList.iterator();
	    boolean found = false;
	    while (entryNames.hasNext()) {
		// Check if entryName is in the list. If not, add it.
		String name = (String)entryNames.next();
		if (name == entryName) {
		    found = true;
		}
	    }
	    if (found == false) {
		// Add entryName to the list.
		entryNameList.add(entryName);
	    }
	}
    }

    /** Set the variable to the value of the Query entry that
     *  changed. This method is called whenever an entry changes.
     *  If the variable has a director, then queue a change request with
     *  the director. If the variable does not have a director,
     *  then set the variable imediately, without queuing
     *  a change request. This method attemps to get a director
     *  from a variable as follows: First check if the variable's
     *  container is an actor or a director. If it is an actor,
     *  call getDirector() and check that the director is not null.
     *  If this fails, than repeat on the container's container, until
     *  we reach the toplevel container.
     *  @param name The name of the entry that has changed.
     */
    // FIXME: This only works with a Parameter, not a Variable.
    // See note below.
    public void changed(String name) {
	// Check if the entry that changed is in the mapping.
	if (_parameters.containsKey(name)) {
	    Variable var = (Variable)(_parameters.get(name));

            // Check if the variable exists.
            if ( var == null ) {
                return;
            }
            
            // Check to see if we should ignore this parameter set.
            String expr;
            try {
                expr = var.getToken().toString();
            } catch(Exception ex) {
                expr = var.getExpression();
            }

            // Check if the variable contains an expression or
            // variable.
            if ( expr == null ) {
                return;
            }

            if(expr.equals(stringValue(name))) {
                return;
            } 

	    // Don't ignore.	    
	    CompositeEntity entity = _entity;
	    if (_entity == null) {
		// Entity not specified in constructor,
		// so get it from the variable.
		// Get the entity from the variable.
		Nameable container = var.getContainer();
		// Walk up the tree until we hit a composite entity.
		while (container != null) {
		    if(container instanceof CompositeEntity) {
			entity = (CompositeEntity)container;
			break;
		    }
		    container = container.getContainer();
		}
	    }
            try {
                ChangeRequest request = new SetParameter((Parameter)var, 
                        (Parameter)var, stringValue(name));
                if(entity != null) {
                    // FIXME the change may happen at sometime in the future.
                    // we should listen and revert if the change fails.
                    entity.requestChange(request);
                } else {
                    request.execute();
                }
            } catch (ChangeFailedException e) {
                // FIXME: This method should probably throw an
                // exception, but then a lot of code (including
                // the base class), would need to be changed.
                System.err.println("parameter change failed, reverting" + 
                        " to previous expression.");
                String finalExpression = var.stringRepresentation();
                set(name, finalExpression);
            }
	}
    }

    /** Notify this query that the value of the specified variable has
     *  changed.  This is called by an attached parameter when its
     *  value changes. Note that more than one entry may be attached
     *  to the same variable. In this case, all such entries will
     *  be notified.
     *  @param variable The variable that has changed.
     */
    public void valueChanged(Variable variable) {
	//System.out.println("PtolemyQuery: valueChanged: invoked");
        // Check that variable is attached to at least one entry.
        if (_parameters.containsValue(variable)) {
            
            //System.out.println("PtolemyQuery: valueChanged(): " +
            //	       "getFullName of var" +
            //	       variable.getFullName() + ".");
            //System.out.println("PtolemyQuery: valueChanged(): " +
            //	       "stringRepresentation " + 
            //        variable.stringRepresentation());
            
            // Get the list of entry names that variable is
            // attached to.
            List entryNameList = (List)_varToListOfEntries.get(variable);
            // For each entry name, call set() to update its
            // value with the value of variable.
            Iterator entryNames = entryNameList.iterator();
            
            while (entryNames.hasNext()) {
                // Check if entryName is in the list. If not, add it.
                String name = (String)entryNames.next();
                
                //System.out.println("setting " + name);
                // Set the entry name's value to the variable's
                // value.
                // FIXME have some way of viewing the evaluated value.
                /*String finalExpression;
                try {
                    finalExpression = variable.getToken().toString();
                } catch(Exception ex) {
                    finalExpression = variable.getExpression();
                }
                set(name, finalExpression);
                */
                set(name, variable.stringRepresentation());
            }
        } else {
            // FIXME: throw exception?
            //System.out.println("PtolemyQuery: valueChanged(): " +
            //        "No entry attached to variable " +
            //        variable.getFullName());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Maps an entry name to the variable that is attached to it.
    private Map _parameters;
    // Maps a variable name to a list of entry names that the
    // variable is attached to.
    private Map _varToListOfEntries;
    // The entity that was specified in the constructors.
    private CompositeEntity _entity;
}

