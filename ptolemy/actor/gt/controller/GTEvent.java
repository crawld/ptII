/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.gt.controller;

import ptolemy.data.expr.Parameter;
import ptolemy.domains.erg.kernel.ERGController;
import ptolemy.domains.erg.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// GTEvent

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTEvent extends Event {

    public GTEvent(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        fireOnInput.setVisibility(Settable.NONE);
    }

    public Parameter getMatchedParameter() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        Parameter parameter = null;
        while (parameter == null && controller != null) {
            parameter = (Parameter) controller.getAttribute("Matched",
                Parameter.class);
            if (parameter == null) {
                Event event = (Event) controller.getRefinedState();
                if (event != null) {
                    controller = (ERGController) event.getContainer();
                }
            }
        }
        if (parameter == null) {
            throw new IllegalActionException("Unable to find the Matched " +
                    "parameter in the ERG controller.");
        }
        return parameter;
    }

    public ModelAttribute getModelAttribute() throws IllegalActionException {
        ERGController controller = (ERGController) getContainer();
        ModelAttribute actorParameter = null;
        while (actorParameter == null && controller != null) {
            actorParameter = (ModelAttribute) controller.getAttribute(
                    "HostModel", ModelAttribute.class);
            if (actorParameter == null) {
                Event event = (Event) controller.getRefinedState();
                if (event != null) {
                    controller = (ERGController) event.getContainer();
                }
            }
        }
        if (actorParameter == null) {
            throw new IllegalActionException("Unable to find the HostModel " +
                    "parameter in the ERG controller of type ActorParameter.");
        }
        return actorParameter;
    }
}
