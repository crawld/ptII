/* A token that contains a boolean variable.

 Copyright (c) 1997 The Regents of the University of California.
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

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// BooleanToken
/**
 * A token that contains a boolean variable.
 *
 * @author Neil Smyth
 * @version $Id$
*/

public class BooleanToken extends Token {

    /** Construct a token with value false
     */
    public BooleanToken() {
	_value = false;
    }

    /** Construct a token with the specified value.
     */
    public BooleanToken(boolean b) {
	_value = b;
    }

    /** Construct a token with the specified string.
     *  @exception IllegalArgumentException If the Token could not
     *   be created with the given String.
     */
    public BooleanToken(String init) throws IllegalArgumentException {
        _value = (Boolean.valueOf(init)).booleanValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new token whose value is the sum of this token
     *  and the argument. A BooleanToken can only have a StringToken 
     *  added to it. Note that this means adding two BooleanTokens
     *  will trigger an exception.
     *  @exception IllegalActionException If the passed token
     *   is not a StringToken.
     *  @return A new Token containing the result.
     */
    public Token add(ptolemy.data.Token tok) throws IllegalActionException {
        int typeInfo = TypeLattice.compare(this, tok);
        try {
            if (typeInfo == CPO.LOWER) {
                return tok.addR(this);
            } else {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new IllegalActionException("BooleanToken: Add method not " +
                    "supported between " + getClass().getName() + " and " + 
                    tok.getClass().getName() + ": " + ex.getMessage());
        }
    }

    /** Convert the specified token into an instance of BooleanToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of BooleanToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below BooleanToken in the type hierarchy, it is converted to
     *  an instance of BooleanToken or one of the subclasses of
     *  BooleanToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a BooleanToken.
     *  @return A BooleanToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out in a lossless fashion.
     */
    public static Token convert(Token token)
	    throws IllegalActionException {

	int compare = TypeLattice.compare(new BooleanToken(), token);
	if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
	    throw new IllegalActionException("DoubleToken.convert: " +
	    	"type of argument: " + token.getClass().getName() +
	    	"is higher or incomparable with BooleanToken in the type " +
		"hierarchy.");
	}

	if (token instanceof BooleanToken) {
	    return token;
	}

	throw new IllegalActionException("cannot convert from token " +
		"type: " + token.getClass().getName() + " to a BooleanToken");
    }

    /** Return a new BooleanToken whose value depends on whether
     *  the argument Token has the same truth value as this
     *  Token.
     *  @param the Token to compare truth values against.
     *  @exception IllegalActionException If the argument Token
     *  is not a BooleanToken.
     *  @return A new BooleanToken containing the result.
     */
    public BooleanToken equals(Token token) throws IllegalActionException {
        if ( !(token instanceof BooleanToken)) {
            String str = "Cannot compare a BooleanToken with a ";
            throw new IllegalActionException(str + "non-BooleanToken");
        }
        boolean arg = ((BooleanToken)token).getValue();
        if ((_value && arg) || !(_value || arg)) {
            return new BooleanToken(true);
        }
        return new BooleanToken(false);
    }

    /** Returns the value currently stored in this BooleanToken
     *  @return The boolean value contained in this Token.
     */
    public boolean getValue() {
        return _value;
    }

    /** Return a new BooleanToken with the logical not of the value
     *  stored in this token.
     *  @return a new BooleanToken with the opposite value to this token.
    */
    public BooleanToken negate() {
        return new BooleanToken(!getValue());
    }

    /** Get the value contained in this Token as a String.
     *  @return The value contained in this token as a String.
     */
    public String stringValue() {
        return (new Boolean(_value)).toString();
    }

    /** Create a string representation of the value in the token.
     *  @return A String representation of this Token.
     */
    public String toString() {
        String str = getClass().getName() + "(" + _value + ")";
        return str;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _value;
}

