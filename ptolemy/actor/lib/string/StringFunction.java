/* Trim a string, convert a string to uppercase, or convert a string to
   lowercase depending on the user's selection.

   Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.string;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// StringFunction

/**
   Produce the output string generated by applying a user-specified
   string function on a provided input string.  The available string
   functions are a case sensitive subset of the java.lang.String
   functions.
   <ul>
   <li> <b>trim</b>: Remove leading and trailing whitespace from a string.
   <li> <b>toUpperCase</b>: Convert all letters in a string to uppercase.
   <li> <b>toLowerCase</b>: Convert all letters in a string to lowercase.
   </ul>

   @see StringCompare
   @see StringFunction
   @see StringIndexOf
   @see StringLength
   @see StringMatches
   @see StringReplace
   @see StringSubstring
   @author Mike Kofi Okyere, Ismael M. Sarmiento
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Green (ismael)
   @Pt.AcceptedRating Green (net)
*/
public class StringFunction extends Transformer {
    /** Construct an actor with the given container and name.
     *  Invoke the base class constructor and create
     *  the <i>input</i> and <i>output</i> ports.
     *  Set the default string function to "trim".
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public StringFunction(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set up ports for string input and string output.
        input.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.STRING);

        function = new Parameter(this, "function");
        function.setStringMode(true);
        function.setExpression("trim");
        function.addChoice("toLowerCase");
        function.addChoice("toUpperCase");
        function.addChoice("trim");
        _function = _TRIM;

        _attachText("_iconDescription",
            "<svg>\n" + "<rect x=\"-30\" y=\"-15\" "
            + "width=\"80\" height=\"30\" " + "style=\"fill:white\"/>\n"
            + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Parameter that stores the string function to be performed
     *  on the input string. The possible values are "trim" (the default),
     * "toUpperCase", or "toLowerCase".
     */
    public Parameter function;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine the string function to be performed on the input, and
     *  set up the necessary fields for the function to be performed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == function) {
            // Use getToken() rather than getExpression()
            // so substitutions occur.
            String functionName = ((StringToken) function.getToken())
                            .stringValue();

            if (functionName.equals("trim")) {
                _function = _TRIM;
            } else if (functionName.equals("toUpperCase")) {
                _function = _TOUPPERCASE;
            } else if (functionName.equals("toLowerCase")) {
                _function = _TOLOWERCASE;
            } else {
                throw new IllegalActionException(this,
                    "Unrecognized function: " + functionName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Perform the desired function on the input string, and send the
     *  the resulting string to the output port. If there is no input,
     *  then produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        if (input.hasToken(0)) {
            StringToken inputToken = (StringToken) input.get(0);
            String value = inputToken.stringValue();
            output.send(0, new StringToken(_doFunction(value)));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Trim the leading and trailing whitespace from a string,
     *  convert a string to uppercase, or convert a string to lower
     *  case depending on the function selected by the user.
     *  @param inputString The string received from the input port.
     */
    private String _doFunction(String inputString) {
        switch (_function) {
        case _TRIM:
            return inputString.trim();

        case _TOUPPERCASE:
            return inputString.toUpperCase();

        case _TOLOWERCASE:
            return inputString.toLowerCase();

        default:
            throw new InternalErrorException(
                "Invalid value provided as function");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //The function the user wishes to perform on a string and the possible
    //functions to perform.
    private int _function;
    private static final int _TRIM = 0;
    private static final int _TOUPPERCASE = 1;
    private static final int _TOLOWERCASE = 2;
}
