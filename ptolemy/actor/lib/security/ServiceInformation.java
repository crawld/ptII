/* Determine the cryptographic services available on the system.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (rnreddy@andrew.cmu.edu)
@AcceptedRating Red (ptolemy@ptolemy.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.security;


import java.security.Provider;
import java.security.Security;
import java.util.Iterator;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// ServiceInformation
/**
This actor lists the following services:

TODO:put in a list format
Provider
Cipher
Signature
KeyGenerator
KeyPairGenerator
MessageDigest

Services are those algorithms have been implemented by providers and are
installed on the local system.  To add providers please refer to the JCE and
JCA.

The following actor relies on the Java Cryptography Architecture (JCA) and Java
Cryptography Extension (JCE).

@author Rakesh Reddy
@version $Id$
*/

public class ServiceInformation extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ServiceInformation(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new SDFIOPort(this, "output", false, true);
        output.setTypeEquals(new ArrayType(BaseType.STRING));

        service = new StringAttribute(this, "mode");
        service.setExpression("Providers");

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    /** This StringAttribute determines whether decryption will be performed on
     *  a asymmetric or symmetric cryptographic algorithm.  The to possible
     *  values are "symmetric" or "asymmetric".  THe default value is
     *  "asymmetric."
     */
    public StringAttribute service;

    /** Multi-port channel that sends the requested information as Strings.
     *
     */
    public SDFIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Determine what information to send to a display.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException if naming conflict is encountered.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == service) {
            request = service.getExpression();

            if (request.equals("Cipher") || request.equals("Signature")
                    || request.equals("MessageDigest")
                    || request.equals("KeyGenerator")
                    || request.equals("KeyPairGenrator")){

                Iterator it = (Security.getAlgorithms("Request")).iterator();
                int i = 0;
                while (it.hasNext()) {
                    output.send(i, new StringToken((String)it.next()));
                    i++;
                }
            } else if(request.equals("Providers")){
                Provider [] providers = Security.getProviders();
                for (int i = 0; i < providers.length; i++) {
                    output.send(i, new StringToken(providers[i].getName()));
                }
            } else {
                throw new IllegalActionException(this.getName()
                        + "Service Cannot Be Listed");
            }
        } else super.attributeChanged(attribute);
    }

    private String request;
}
