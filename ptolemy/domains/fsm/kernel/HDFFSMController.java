/* An FSM Controller to be used with HDF

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.graph.*;
import java.util.Enumeration;
import collections.LinkedList;
import ptolemy.domains.fsm.*;
import ptolemy.domains.fsm.kernel.util.VariableList;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMController
/**
An HDFFSMController should be used instead of an FSMController when the
FSM refines a heterochronous dataflow (HDF) graph. In this case,
it is necessary that each of the states (instance of FSMState) refine
to a SDF graph.
FIXME: Each state of the FSM must refine to a homogenous SDF graph.
FIXME: The FSM guards are evaluated after each call to fire() of its
refining SDF graph.

@version $Id$
@author Brian K. Vogel
*/

public class HDFFSMController  extends FSMController implements TypedActor {



    public HDFFSMController() {
        super();
    }


    public HDFFSMController(Workspace workspace) {
        super(workspace);
    }


    public HDFFSMController(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public Enumeration typeConstraints()  {
	try {
	    workspace().getReadAccess();

	    LinkedList result = new LinkedList();
	    Enumeration inPorts = inputPorts();
	    while (inPorts.hasMoreElements()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		boolean isUndeclared = inport.getTypeTerm().isSettable();
		if (isUndeclared) {
		    Enumeration outPorts = outputPorts();
	    	    while (outPorts.hasMoreElements()) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.nextElement();

			isUndeclared = outport.getTypeTerm().isSettable();
		    	if (isUndeclared && inport != outport) {
			    // output also undeclared, not bi-directional port,
		            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(), outport.getTypeTerm());
			    result.insertLast(ineq);
			}
		    }
		}
	    }
	    return result.elements();

	}finally {
	    workspace().doneReading();
	}
    }


    public void addLocalVariable(String name, Token initialValue)
            throws IllegalActionException, NameDuplicationException {
        if (_localVariables == null) {
            _localVariables = new VariableList(this, LOCAL_VARIABLE_LIST);
        }
        Variable var = new Variable(_localVariables, name);
        try {
            // var's type is determined by initialValue's type.
            var.setToken(initialValue);
        } catch (IllegalArgumentException ex) {
            // this should not happen
        }
    }

    public FSMTransition createTransition(HDFFSMState source, HDFFSMState dest) {
        return (FSMTransition)source.createTransitionTo(dest);
    }

    public void initialize() throws IllegalActionException {
        // FIXME: What should this do?
    }

    public Object clone(Workspace ws) throws CloneNotSupportedException {
        // FIXME
        // This method needs careful refinement.
        HDFFSMController newobj = (HDFFSMController)super.clone();
        if (_initialState != null) {
            newobj._initialState =
                (FSMState)newobj.getEntity(_initialState.getName());
        }
        newobj._inputStatusVars = null;
        newobj._inputValueVars = null;
        try {
            VariableList vlist =
                (VariableList)newobj.getAttribute(INPUT_STATUS_VAR_LIST);
            if (vlist != null) {
                vlist.setContainer(null);
            }
            vlist = (VariableList)newobj.getAttribute(INPUT_VALUE_VAR_LIST);
            if (vlist != null) {
                vlist.setContainer(null);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
        } catch (NameDuplicationException ex) {
            // this should not happen
        }
        if (_initialTransitions != null) {
            newobj._initialTransitions = new LinkedList();
            Enumeration trans = _initialTransitions.elements();
            while (trans.hasMoreElements()) {
                FSMTransition tr = (FSMTransition)trans.nextElement();
                newobj._initialTransitions.insertLast(newobj.getRelation(tr.getName()));
            }
        }
        newobj._currentState = null;
        newobj._takenTransition = null;
        if (_localVariables != null) {
            newobj._localVariables = (VariableList)newobj.getAttribute("LocalVariables");
        }
        // From AtomicActor.
        newobj._inputPortsVersion = -1;
        newobj._outputPortsVersion = -1;
        return newobj;
    }




    public FSMState currentState() {
        return _currentState;
    }


    public Actor currentRefinement() {
        if (_currentState != null) {
            return _currentState.getRefinement();
        } else {
            return null;
        }
    }

    /*
     *
     */
    public void fire() throws IllegalActionException {
	System.out.println("FSMController: fire()");
        _takenTransition = null;
        //_setInputVars();
	//System.out.println("FSMController:  fire(): finished _setInputVars();");
        
        FSMTransition trans;
	
	// Check if the guard token is null. If the guard token is not
	// null, then this means that a token(s) was successfully read
	// from the single input port of this controller's container. This
	// means it is safe to fire the refinment, since token(s) will
	// be available to it.
        if (((HDFFSMDirector)getDirector()).t != null) {
            
	    
	    /** Set the value of the local input status variables to ABSENT.
	     */

            //_currentState.resetLocalInputStatus();

            // Invoke refinement.
            if (currentRefinement() != null) {
		System.out.println("FSMController:  fire(): firing current refinment");
                currentRefinement().fire();
            }

            // Evaluate the nonpreemptive transitions.
            Enumeration nonPreTrans = _currentState.getNonPreemptiveTrans();
            while (nonPreTrans.hasMoreElements()) {
                trans = (FSMTransition)nonPreTrans.nextElement();
		System.out.println("FSMController:  fire(): transistion name: " + trans.getFullName());
                if (trans.isEnabled()) {
		    System.out.println("FSMController:  fire(): cccc");
                    if (_takenTransition != null) {
                        // Nondeterminate transition!
                        //System.out.println("Nondeterminate transition!");
                    } else {
                        _takenTransition = trans;
                    }
                }
            }
        }
	System.out.println("FSMController:  fire(): ***********");
        if (_takenTransition != null) {
            _outputTriggerActions(_takenTransition.getTriggerActions());
            _updateLocalVariables(_takenTransition.getLocalVariableUpdates());
            // _takenTransition.executeTransitionActions();
            // do not change state, that's done in postfire()
        }
	System.out.println("FSMController:  fire(): finished");
    }



    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            return container.getDirector();
        }
        return null;
    }


    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
        try {
            workspace().getReadAccess();
            CompositeActor container = (CompositeActor)getContainer();
            if (container != null) {
                return container.getManager();
            }
            return null;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return an enumeration of the input ports contained by the
     *  container of this controller.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration inputPorts() {
        try {
            workspace().getReadAccess();
            if(_inputPortsVersion != workspace().getVersion()) {
                // Update the cache.
                LinkedList inports = new LinkedList();
                //Enumeration ports = getPorts();
		// Is this right?
		Enumeration ports = ((CompositeEntity)getContainer()).getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isInput()) {
                        inports.insertLast(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = workspace().getVersion();
            }
            return _cachedInputPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }


    // FSMController is a standalone sequential logic controller.
    public boolean isOpaque() {
        return true;
    }


    /*  public double getCurrentTime() throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        if (dir == null) {
        throw new IllegalActionException("No director available");
        }
        return dir.getCurrentTime();
        }

        public double getStartTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
        throw new IllegalActionException("No director available");
	}
	return dir.getStartTime();
        }

        public double getStopTime() throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	if (dir == null) {
        throw new IllegalActionException("No director available");
	}
	return dir.getStopTime();
        }

        public void refireAfterDelay(double delay) throws IllegalActionException {
	DEDirector dir = (DEDirector)getDirector();
	// FIXME: the depth is equal to zero ???
        // If this actor has input ports, then the depth is set to be
        // one higher than the max depth of the input ports.
        // If this actor has no input ports, then the depth is set to
        // to be zero.

        dir.fireAfterDelay(this, delay);
        }
    */
    
    public Port newPort(String name) throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            workspace().doneWriting();
        }
    }


    // newRelation
    // Should return an FSMTransition?


    /** Return a new receiver of a type compatible with the director.
     *  Derived classes may further specialize this to return a receiver
     *  specialized to the particular actor.
     *
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return dir.newReceiver();
    }


    /** Return an enumeration of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return An enumeration of IOPort objects.
     */
    public Enumeration outputPorts() {
        try {
            workspace().getReadAccess();
            if(_outputPortsVersion != workspace().getVersion()) {
                _cachedOutputPorts = new LinkedList();
                Enumeration ports = getPorts();
                while(ports.hasMoreElements()) {
                    IOPort p = (IOPort)ports.nextElement();
                    if( p.isOutput()) {
                        _cachedOutputPorts.insertLast(p);
                    }
                }
                _outputPortsVersion = workspace().getVersion();
            }
            return _cachedOutputPorts.elements();
        } finally {
            workspace().doneReading();
        }
    }


    public boolean prefire() {
        // Anything useful to do?
        return true;
    }


    /** What does this do?
     *
     */
    public void preinitialize() throws IllegalActionException {
	System.out.println("HDFFSMController: preinitialize() called from?");
        try {
            _createReceivers();
            setupScope();
        } catch (NameDuplicationException ex) {
            // FIXME!!
            // ignore for now

            throw new InvalidStateException(this, "DEAL WITH IT" + ex.getMessage());

        }
        // Set local variables to their initial value.
        if (_localVariables != null) {
            Enumeration localVars = _localVariables.getVariables();
            Variable var = null;
            while (localVars.hasMoreElements()) {
                var = (Variable)localVars.nextElement();
                var.reset();
            }
        }
        // Evaluate initial transitions, determine initial state.
        _setInputVars();
        _takenTransition = null;
        if (_initialTransitions != null) {
            Enumeration trs = _initialTransitions.elements();
            FSMTransition trans;
            while (trs.hasMoreElements()) {
                trans = (FSMTransition)trs.nextElement();
                if (trans.isEnabled()) {
                    if (_takenTransition != null) {
                        // Nondeterminate initial transition!
                        //System.out.println("Multiple initial transitions "
                        //        + "enabled!");
                    } else {
                        _takenTransition = trans;
                    }
                }
            }
        }
        if (_takenTransition != null) {
            _outputTriggerActions(_takenTransition.getTriggerActions());
            _updateLocalVariables(_takenTransition.getLocalVariableUpdates());
            // _takenTransition.executeTransitionActions();
            _currentState = _takenTransition.destinationState();
        } else {
            _currentState = _initialState;
        }
        if (_currentState == null) {
            // FIXME!!
            // Throw exception!
            //System.out.println("Initialization error: no initial state!");
        }
        if (currentRefinement() != null) {
            // FIXME!!
            // Initialize the refinement.
            // Delegate to the director or just call preinitialize() on
            // the refinement?
            // Now we are doing initialization in FSM system.
            //currentRefinement().preinitialize();

            //System.out.println("Initializing refinement "+
            //((ComponentEntity)currentRefinement()).getFullName());

        }
    }

    /** Change state according to the enabled transition determined
     *  from last fire.
     *  @return True, the execution can continue into the next iteration.
     *  @exception IllegalActionException If the refinement of the state
     *   transitioned into cannot be initialized.
     */
    public boolean postfire() throws IllegalActionException {
        if (_takenTransition == null) {
            // No transition is enabled when last fire. FSMController does not
            // change state. Note this is different from when a transition
            // back to the current state is taken.
            return true;
        }

        // What to do to the refinement of the state left?

        _currentState = _takenTransition.destinationState();

        // execute the transition actions
        //_takenTransition.executeTransitionActions();

        if (_takenTransition.isInitEntry() || _currentState.isInitEntry()) {
            // Initialize the refinement.
            Actor actor = currentRefinement();
            if (actor == null) {
                return true;
            }
            // If the refinement is an FSMController or an FSM system, then the trigger
            // actions of the taken transition should be input to the actor to enable
            // initial transitions.
            // ADD THIS!
            //            if (actor instanceof FSMController) {
            //                // Do what's needed.
            //            } else {
            //                // Do what's needed.
            //            }
            // FIXME!
            // FIXME: Is this correct?  initialize() is supposed to be
            // called only once, per documentation.
            actor.initialize();
        }
        return true;
    }


    /** Override the base class to ensure that the proposed container
     *  is an instance of CompositeActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the actor from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the argument is not a CompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof CompositeActor) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "FSMController can only be contained by instances of " +
                    "CompositeActor.");
        }
        super.setContainer(container);
    }


    public void setInitialState(FSMState initialState)
            throws IllegalActionException {
        if (initialState != null && initialState.getContainer() != this) {
            throw new IllegalActionException(this, initialState,
                    "Initial state is not contained by FSMController.");
        }
        _initialState = initialState;
    }


    public void setInitialTransition(FSMTransition initialTransition)
            throws IllegalActionException {
        if (initialTransition != null && initialTransition.getContainer() != this) {
            throw new IllegalActionException(this, initialTransition,
                    "Initial transition is not contained by FSMController.");
        }
        if (_initialTransitions == null) {
            _initialTransitions = new LinkedList();
            _initialTransitions.insertFirst(initialTransition);
        } else if (_initialTransitions.includes(initialTransition)) {
            return;
        } else {
            _initialTransitions.insertFirst(initialTransition);
        }
    }

    /* Add all ports contained by this controller and all ports contained
     * by the container of this controller to the scope.
     */
    // FIXME: Why is this public?
    public void setupScope()
            throws NameDuplicationException, IllegalActionException {
	
        try {
            // remove old variable lists
            if (_inputStatusVars != null) {
                _inputStatusVars.setContainer(null);
                _inputValueVars.setContainer(null);
            }
            // create new variable lists
            _inputStatusVars = new VariableList(this, INPUT_STATUS_VAR_LIST);
            _inputValueVars = new VariableList(this, INPUT_VALUE_VAR_LIST);
	    // Add all the input ports of this Controller to the scope.
            _inputStatusVars.createVariables(inputPorts());
	    // Add all the input ports of this Controller to the scope.
            _inputValueVars.createVariables(inputPorts());
	    // Add all the input ports of the Controller's container
	    // to the scope.
	    _inputStatusVars.createVariables(((CompositeEntity)getContainer()).getPorts());
	    _inputValueVars.createVariables(((CompositeEntity)getContainer()).getPorts());
	    
        } catch (IllegalActionException ex) {
        } catch (NameDuplicationException ex) {
        }
        Enumeration states = getEntities();
        FSMState state;
	// Setup the scope associated with each state. The scope
	// associated with a state consists of all of its refining
	// actor's input and output ports.
        while (states.hasMoreElements()) {
            state = (FSMState)states.nextElement();
            state.setupScope();
        }
        Enumeration transitions = getRelations();
        FSMTransition trans;
	// Setup the scope associated with each transition. The scope
	// associated with a transition consists of the union of the
	// scope of its source state and the scope of its controller.
        while (transitions.hasMoreElements()) {
            trans = (FSMTransition)transitions.nextElement();
            trans.setupScope();
        }
    }


    /** FIXME: This should do something like calling stopfire on the contained
     *  actors.
     */
    public void stopFire() {
    }


    /** By default, an AtomicActor does nothing incredible in its
     *  terminate, it just wraps up.
     */
    public void terminate() {
        try {
            wrapup();
        }
        catch (IllegalActionException e) {
            // Do not pass go, do not collect $200.  Most importantly,
            // just ignore everything and terminate.
        }
    }


    /** Do nothing.  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
    }


    /** Add a state to this controller with minimal error checking.
     *  This overrides the base-class method to make sure the argument
     *  is an instance of FSMState.
     *  It is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to add.
     *  @exception IllegalActionException If the actor has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of FSMState.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the states list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof FSMState)) {
            throw new IllegalActionException(this, entity,
                    "FSMController can only contain entities that are instances"
                    + " of FSMState");
        }
        // FSMState is not an Actor
        super._addEntity(entity);
    }


    /** Override the base class to throw an exception if the added port
     *  is not an instance of IOPort.  This method should not be used
     *  directly.  Call the setContainer() method of the port instead.
     *  This method does not set the container of the port to point to
     *  this entity. It assumes that the port is in the same workspace
     *  as this entity, but does not check.  The caller should check.
     *  Derived classes may override this method to further constrain to
     *  a subclass of IOPort. This method is <i>not</i> synchronized on
     *  the workspace, so the caller should be.
     *
     *  @param port The port to add to this entity.
     *  @exception IllegalActionException If the port class is not
     *   acceptable to this entity, or the port has no name.
     *  @exception NameDuplicationException If the port name coincides with a
     *   name already in the entity.
     */
    protected void _addPort(Port port)
            throws IllegalActionException, NameDuplicationException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "Incompatible port class for this entity.");
        }
        super._addPort(port);
    }


    /** Add a transition to this controller. This method should not be used
     *  directly.  Call the setContainer() method of the transition instead.
     *  This method does not set the container of the transition to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Relation to contain.
     *  @exception IllegalActionException If the relation has no name, or is
     *   not an instance of FSMTransition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained relations list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof FSMTransition)) {
            throw new IllegalActionException(this, relation,
                    "FSMController can only contain instances of FSMTransition.");
        }
        super._addRelation(relation);
    }


    protected void _outputTriggerActions(VariableList vlist)
            throws IllegalActionException, NoRoomException {
        if (vlist == null) {
            return;
        }
        Enumeration vars = vlist.getVariables();
        Variable var;
        IOPort port;
        while (vars.hasMoreElements()) {
            var = (Variable)vars.nextElement();
            var.getToken();
            // If this controller is a refinement, set the corresponding
            // state's local input variable.
            // This cannot be done in the FSMDirector.transferOutput() because
            // there is no inside receiver at FSMController's output ports.
            // FIXME!!
            if (getDirector() instanceof FSMDirector) {
                FSMDirector dir = (FSMDirector)getDirector();
                if (dir.currentRefinement() == this) {

                    FSMState state = dir.currentState();
                    //System.out.println("FSMController " +
                    //this.getFullName() + " setting " +
                    //        "local variable " + var.getName() +
                    //":" + var.getExpression() + " of state "
                    //+ state.getFullName());

                    dir.currentState().setLocalInputVar(var.getName(),
                            var.getToken());
                }
            }
            port = (IOPort)getPort(var.getName());
            if (port == null || !port.isOutput()) {
                continue;
            }
            if (port.numLinks() > 0) {

                //System.out.println("Sending trigger action to "+
                //port.getFullName());

                port.send(0, var.getToken());
            } else {

                // This branch will not be executed, because now all ports
                // in FSM system are connected.

                //System.out.println("Port " + port.getFullName()
                //+ " is floating.");

                // Here we must be careful, port does not have inside
                // receivers created!
                port = (IOPort)((CompositeActor)getContainer()).getPort(
                        port.getName());
                if (port == null || !port.isOutput()) {
                    continue;
                }
                Receiver[][] recs = port.getInsideReceivers();
                if (recs != null) {
                    recs[0][0].put(var.getToken());
                }
            }
        }
    }
    
    protected void _setInputVars()
            throws IllegalArgumentException, IllegalActionException {
        // Do not deal with multiport, multiple tokens now.
	// Get an enumeration of all the input ports contained by the
	// container of this controller.
        Enumeration inports = inputPorts();
	//Enumeration inports = ((CompositeEntity)getContainer()).inputPorts();
        IOPort port;
	System.out.println("***************************");
        while (inports.hasMoreElements()) {
            port = (IOPort)inports.nextElement();
            if (port.numLinks() > 0) {
                if (port.hasToken(0)) {

                    System.out.println("HDFSMController: _setInputVars(): Port " + port.getFullName() + " has token.");

                    _inputStatusVars.setVarValue(port.getName(), PRESENT);
                    _inputValueVars.setVarValue(port.getName(), port.get(0));
                } else {

                    System.out.println("HDFSMController: _setInputVars(): Port " + port.getFullName() + " has no token.");

                    _inputStatusVars.setVarValue(port.getName(), ABSENT);
                }
            } else {
                // FIXME!!
                // Outside receivers are always created, so this branch
                // is not necessary.		
                Receiver[][] recs = port.getReceivers();
                if (recs != null) {
                    Receiver rec = recs[0][0];
                    if (rec.hasToken()) {
                        _inputStatusVars.setVarValue(port.getName(), PRESENT);
                        _inputValueVars.setVarValue(port.getName(), rec.get());
                    } else {
                        _inputStatusVars.setVarValue(port.getName(), ABSENT);
                    }
                } else {
                    // Port desired width is not set, same as no input.
                    _inputStatusVars.setVarValue(port.getName(), ABSENT);
                }
            }
        }
    }
    
    /*
    protected void _setInputVars()
            throws IllegalArgumentException, IllegalActionException {
        // Do not deal with multiport, multiple tokens now.
	// Get an enumeration of all the input ports contained by the
	// container of this controller.
        Enumeration inports = inputPorts();
	
        IOPort port;
	System.out.println("***************************");
	 while (inports.hasMoreElements()) {
            port = (IOPort)inports.nextElement();
	    if (getDirector() instanceof HDFFSMDirector) {
                HDFFSMDirector dir = (HDFFSMDirector)getDirector();
		Token guardToken = dir.t;
		if (guardToken.stringValue() == "present") {
		    System.out.println("Port " + port.getFullName() + " has token.");
		System.out.println("* " + guardToken.toString());
		_inputStatusVars.setVarValue(port.getName(), PRESENT);
		_inputValueVars.setVarValue(port.getName(), guardToken);
		} else {
		    System.out.println("Port " + port.getFullName() + " has no token.");

                    _inputStatusVars.setVarValue(port.getName(), ABSENT);
		}
	    }
	 }
    }
    */

    protected void _updateLocalVariables(VariableList vlist)
            throws IllegalActionException, IllegalArgumentException {
        if (vlist == null) {
            return;
        }
        Enumeration vars = vlist.getVariables();
        Variable var;
        while (vars.hasMoreElements()) {
            var = (Variable)vars.nextElement();
            var.getToken();
            _localVariables.setVarValue(var.getName(), var.getToken());
        }
    }


 
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Enumeration inports = inputPorts();
        while (inports.hasMoreElements()) {
            IOPort inport = (IOPort)inports.nextElement();
            inport.createReceivers();
        }
    }

}
