/* An FFT.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.lib;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.math.Complex;
import ptolemy.math.SignalProcessing;

//////////////////////////////////////////////////////////////////////////
//// FFT

/** This actor implements a forward FFT of a real input array of
doubles. The order of the FFT determines the number of tokens that
will be consumed and produced. The order gives the size of the
transform as the base-2 logarithm of order. The default order is 8,
which means that 2^8 = 256 tokens are read and 2^8 = 256 tokens are
produced. The result of the FFT is a new array of Complex's.

@author Bart Kienhuis.
@version $Id$
@see ptolemy.math.SignalProcessing#FFTComplexOut
*/

public class FFT extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FFT(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        input = new SDFIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);

        output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        order = new Parameter(this, "order", new IntToken(8));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The input port, which has type double. */
    public SDFIOPort input;

    /** The output port, which has type complex. */
    public SDFIOPort output;

    /** The order of the FFT, which is an integer that defaults to 8. */
    public Parameter order;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>order</i> parameter, then
     *  set up the consumption and production constants, and invalidate
     *  the schedule of the director.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the parameters are out of range.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == order) {
            // Get the size of the FFT transform
            _orderValue = ((IntToken)order.getToken()).intValue();
            _transformSize = (int)Math.pow(2, _orderValue );
            
            // Set the correct consumption/production values
            _productionRate = _transformSize;
            _consumptionRate = _transformSize;
            
            input.setTokenConsumptionRate(_consumptionRate);
            output.setTokenProductionRate(_productionRate);
            
            inDoubleArray = new double[_consumptionRate];
            outComplexArray = new Complex[_productionRate];
            
            inTokenArray = new DoubleToken[ _consumptionRate ];
            outTokenArray = new ComplexToken[ _productionRate ];

            Director dir = getDirector();
            if (dir != null) {
                dir.invalidateSchedule();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Consume the inputs and produce the outputs of the FFT filter.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        int i;
	inTokenArray = input.get(0, _consumptionRate);
        for (i = 0; i < _consumptionRate; i++) {
            inDoubleArray[i] = ((DoubleToken)inTokenArray[i]).doubleValue();
        }
        Complex[] outComplexArray =
            SignalProcessing.FFTComplexOut( inDoubleArray, _orderValue );
        for (i = 0; i < _productionRate; i++) {
            outTokenArray[i] = new ComplexToken( outComplexArray[i] );
        }
	output.send(0, outTokenArray, _productionRate);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _productionRate;
    private int _consumptionRate;

    private int _transformSize;
    private int _orderValue;

    private Token[] inTokenArray;
    private ComplexToken[] outTokenArray;

    private double[] inDoubleArray;
    private Complex[] outComplexArray;

}
