/* A MultirateFSMDirector governs the execution of a modal model.

Copyright (c) 1999-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.DFUtilities;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


/**
   This class is very preliminary. The purpose of this director is to allow
   modal models to consume 0 or more tokens for each input port and send 0
   or more tokens to each output port.
   
   @author zhouye
   @version $Id$
   @since Ptolemy II 5.0
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
   @see FSMDirector
 */
public class MultirateFSMDirector extends FSMDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public MultirateFSMDirector() {
        super();    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public MultirateFSMDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MultirateFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** FIXME: comment is wrong.  
     *  Examine the transitions from the given state.
     *  If there is more than one transition enabled, an exception is
     *  thrown. If there is exactly one non-preemptive transition
     *  enabled, then it is chosen and the choice actions contained by
     *  transition are executed. Return the destination state. If no
     *  transition is enabled, return the current state.
     *  @return The destination state, or the current state if no
     *   transition is enabled.
     */
    public State chooseTransition(State state)
            throws IllegalActionException {
        FSMActor controller = getController();
        State destinationState;
        Transition transition = null;

        if ((state.getRefinement() != null)
                && (state.preemptiveTransitionList().size() != 0)) {
            throw new IllegalActionException(this,
                    state.getName() + " cannot have outgoing preemptive "
                    + "transitions because the state has a refinement.");
        }

        if (state.getRefinement() == null) {
            transition = _chooseTransition(state.preemptiveTransitionList());
        }

        if (transition == null) {
            // No preemptiveTransition enabled. Choose nonpreemptiveTransition.
            transition = _chooseTransition(state.nonpreemptiveTransitionList());
        }

        if (transition != null) {
            destinationState = transition.destinationState();

            TypedActor[] trRefinements = (transition.getRefinement());

            Actor[] actors = transition.getRefinement();

            if (actors != null) {
                throw new IllegalActionException(this,
                        "HDFFSM Director does not support transition refinements.");
            }

            _readOutputsFromRefinement();

            //execute the output actions
            Iterator actions = transition.choiceActionList().iterator();

            while (actions.hasNext()) {
                Action action = (Action) actions.next();
                action.execute();
            }
        } else {
            destinationState = state;
        }

        return destinationState;
    }

    /** Choose the next non-transient state given the current state.
     * @param currentState The current state.
     * @throws IllegalActionException If a transient state is reached
     *  but no further transition is enabled.
     */
    public void chooseNonTransientTransition(State currentState) 
    throws IllegalActionException {
        State state = chooseTransition(currentState);
        Actor[] actors = state.getRefinement();
        Transition transition;
        while (actors == null) {
            super.postfire();
            state = chooseTransition(state);
            transition = _getLastChosenTransition();
            if (transition == null) {
                throw new IllegalActionException(this,
                        "Reached a state without a refinement: "
                        + state.getName());
            }
            actors = (transition.destinationState()).getRefinement();
        }
    }

    /** Get the current state. If it does not have any refinement,
     *  consider it as a transient state and make a state transition
     *  until a state with a refinement is
     *  found. Set that non-transient state to be the current state
     *  and return it.
     * @throws IllegalActionException If a transient state is reached
     *  while no further transition is enabled.
     * @return The intransient state.
     */
    public State getNonTransientState()
                throws IllegalActionException {
        
        FSMActor controller = getController();
        State currentState = controller.currentState();
        TypedActor[] currentRefinements = currentState.getRefinement();
        while (currentRefinements == null) {
            chooseTransition(currentState);
            super.postfire();
            currentState = controller.currentState();
            Transition lastChosenTransition = _getLastChosenTransition();
            if (lastChosenTransition == null) {
                throw new IllegalActionException(this,
                    "Reached a transient state " +
                    "without an enabled transition.");
            }
            currentRefinements = currentState.getRefinement();
        }
        return currentState;       
    }

    /** Return a new receiver of a type compatible with this director.
     *  This returns an instance of SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Return true if data are transferred from the input port of
     *  the container to the connected ports of the controller and
     *  of the current refinement actor.
     *  <p>
     *  This method will transfer all of the available tokens on each
     *  input channel. The port argument must be an opaque input port.
     *  If any channel of the input port has no data, then that
     *  channel is ignored. Any token left not consumed in the ports
     *  to which data are transferred is discarded.
     *  @param port The input port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                    + "input port.");
        }

        boolean transferred = false;

        // The receivers of the current refinement that receive data
        // from "port."
        Receiver[][] insideReceivers = _currentLocalReceivers(port);

        int rate = DFUtilities.getTokenConsumptionRate(port);

        for (int i = 0; i < port.getWidth(); i++) {
            // For each channel
            try {
                if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                    for (int j = 0; j < insideReceivers[i].length; j++) {
                        // Since we only transfer number of tokens declared by
                        // the port rate, we should be safe to clear the receivers.
                        // Maybe we should move this step to prefire() or postfire(),
                        // as in FSMDirector.
                        insideReceivers[i][j].clear();

                        /*
                          while (insideReceivers[i][j].hasToken()) {
                          // clear tokens.
                          // FIXME: This could be a problem for Giotto, etc.
                          // as get() method in Giotto does not remove the
                          // token from the receiver.
                          insideReceivers[i][j].get();
                          }*/
                    }

                    // Transfer number of tokens at most the declared port rate.
                    // Note: we don't throw exception if there are fewer tokens
                    // available. The prefire() method of the refinement simply
                    // return false.
                    for (int k = 0; k < rate; k++) {
                        if (port.hasToken(i)) {
                            ptolemy.data.Token t = port.get(i);
                            port.sendInside(i, t);
                        }
                    }

                    // Successfully transferred data, so return true.
                    transferred = true;
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " + ex);
            }
        }

        return transferred;
    }

    /** Transfer data from an output port of the current refinement actor
     *  to the ports it is connected to on the outside. This method differs
     *  from the base class method in that this method will transfer <i>k</i>
     *  tokens in the receivers, where <i>k</i> is the port rate if it is
     *  declared by the port. If the port rate is not declared, this method
     *  behaves like the base class method and will transfer at most one token.
     *  This behavior is required to handle the case of multi-rate actors.
     *  The port argument must be an opaque output port.
     *  @exception IllegalActionException If the port is not an opaque
     *  output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "HDFFSMDirector: transferOutputs():"
                    + "  port argument is not an opaque output port.");
        }

        boolean transferred = false;
        int rate = DFUtilities.getRate(port);
        Receiver[][] insideReceivers = port.getInsideReceivers();

        for (int i = 0; i < port.getWidth(); i++) {
            if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                for (int k = 0; k < rate; k++) {
                    // Only transfer number of tokens declared by the port
                    // rate. Throw exception if there are not enough tokens.
                    try {
                        ptolemy.data.Token t = port.getInside(i);
                        port.send(i, t);
                    } catch (NoTokenException ex) {
                        throw new InternalErrorException(
                                "Director.transferOutputs: "
                                + "Not enough tokens for port " + port.getName()
                                + " " + ex);
                    }
                }
            }

            transferred = true;
        }

        return transferred;
    }

    /////////////////////////////////////////////////////////////////////////
    ////                           private methods                       ////

}
