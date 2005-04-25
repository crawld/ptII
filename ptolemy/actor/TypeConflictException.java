/* Exception thrown on detecting type conflicts.

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.kernel.util.KernelException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// TypeConflictException

/**
   Thrown on detecting type conflicts.
   This class contains all the instances of Inequality where type conflicts
   occurred. These inequalities are either not satisfied after type resolution,
   or contain terms with unacceptable value, such as BaseType.UNKNOWN.

   @author Yuhong Xiong
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (yuhong)
   @Pt.AcceptedRating Green (liuxj)
*/
public class TypeConflictException extends KernelException {
    /** Construct an Exception with a list of Inequality instances where
     *  type conflicts occurred.
     *  The detailed message of this Exception will be the string
     *  "Type conflicts occurred at the following inequalities:",
     *  followed by the list of inequalities. The string describing the
     *  inequalities are generated by the toString() method of the class
     *  Inequality.
     *  @param inequalities a list of Inequality instances.
     *  @see ptolemy.graph.Inequality#toString
     */
    public TypeConflictException(List inequalities) {
        this(inequalities,
            "Type conflicts occurred at the following inequalities:");
    }

    /** Construct an Exception with a list of Inequality instances where
     *  type conflicts occurred.
     *  The detailed message of this Exception will be the specified message,
     *  followed by a list of inequalities. The string describing the
     *  inequalities are generated by the toString() method of the class
     *  Inequality.
     *  @param inequalities a list of Inequality instances.
     *  @param detail a message.
     *  @see ptolemy.graph.Inequality#toString
     */
    public TypeConflictException(List inequalities, String detail) {
        _inequalities.addAll(inequalities);
        _setMessage(detail + "\n" + _listInequalities());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a list of Inequality instances where type conflicts occurred.
     *  @return A List of Inequality instances where type conflicts occurred.
     */
    public List inequalityList() {
        return _inequalities;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Create a string listing all the inequalities in _inequalities.
    // Each inequality takes one line, and each line starts
    // with 2 white spaces to make the String more readable.
    private String _listInequalities() {
        String result = "";
        Iterator iterator = inequalityList().iterator();

        while (iterator.hasNext()) {
            Object inequality = iterator.next();
            result += ("  " + inequality.toString() + "\n");
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private List _inequalities = new LinkedList();
}
