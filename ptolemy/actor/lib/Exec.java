/* Execute a subprocess.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.ArrayType;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

//////////////////////////////////////////////////////////////////////////
//// Execute
/**
Execute a command and create a separately running subprocess.

The <code>commandLine</code> PortParameter contains the command to be
executed.

<p>This actor uses java.lang.Runtime.exec().
For information about Runtime.exec(), see:
<a href="http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html" target="_top">http://jw.itworld.com/javaworld/jw-12-2000/jw-1229-traps.html" target="_top</a>
and
<a href="http://mindprod.com/jgloss/exec.html" target="_top">http://mindprod.com/jgloss/exec.html</a>

@author Christopher Hylands Brooks, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public class Exec extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Exec(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        blocking = new Parameter(this, "blocking");
        blocking.setTypeEquals(BaseType.BOOLEAN);
        blocking.setToken(BooleanToken.FALSE);

        command = new PortParameter(this, "command", new StringToken("echo 'Hello, world.'"));
        // Make command be a StringParameter (no surrounding double quotes).
        command.setStringMode(true);

        directory = new FileParameter(this, "directory");
        directory.setExpression("$CWD");
        // Hide the directory parameter.
        directory.setVisibility(Settable.EXPERT);

        environment = new Parameter(this, "environment");
        environment.setTypeEquals(new ArrayType(BaseType.STRING));
        environment.setExpression("{\"\"}");
        // Hide the environment parameter.
        environment.setVisibility(Settable.EXPERT);

        error = new TypedIOPort(this, "error", false, true);
        error.setTypeEquals(BaseType.STRING);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.STRING);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** Indicator of whether fire method blocks on the process.
     *  The type is boolean with default false.
     */
    public Parameter blocking;

    /** The command to be executed.  The command is parsed by
     * {@link ptolemy.util.StringUtilities#tokenizeForExec(String)}
     * into tokens and then executed as a  separate process.
     * The initial default is the string "echo 'Hello, world.'".   
     * FIXME: Should this be an array of strings instead of a string?
     */  
    public PortParameter command;

    /** The directory to execute the command in.  The initial default
     *  value of this parameter $CWD, which corresponds with the value
     *  of the JDK user.dir property.
     */
    public FileParameter directory;

    /** The environment to execute the command in.  This parameter
     *  is an array of strings of the format name=value.  If the
     *  length of the array is zero (the default), then the environment
     *  from the current process is used in the new command.
     */
    public Parameter environment;

    /** Data that is generated by the subprocess on standard error.
     */
    public TypedIOPort error;

    /** Strings to pass to the standard in of the subprocess.
     */
    public TypedIOPort input;

    /** Data that is generated by the subprocess on standard out.
     */
    public TypedIOPort output;


    /**
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == blocking) {
            _blocking =
                ((BooleanToken)blocking.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }


    /** Send input to the subprocess and read output and errors
     */
    public synchronized void fire() throws IllegalActionException {
        super.fire();
        String line = null;

        try {
            if (_process == null) {
                // FIXME: What if the portParameter command changes?
                _exec();
            }
            if (input.hasToken(0)) {
                // FIXME: Do we need to append a new line?
                if ((line = ((StringToken)input.get(0)).stringValue())
                            != null) {
                    if (_debugging) {
                        _debug("Exec: Input: '" + line + "'");
                    }
                    if (_inputBufferedWriter != null) { 
                            _inputBufferedWriter.write(line);
                    }
                }
            }

            //if (!_blocking) {
            // FIXME: Do we need to get the value and if and only if
            // we have string call send?  What if there is no input?
            String errorString = _errorGobbler.getAndReset();
            String outputString = _outputGobbler.getAndReset();
            if (_debugging) {
                _debug("Exec: Error: '" + errorString + "'");
                _debug("Exec: Output: '" + outputString + "'");
            }

            error.send(0, new StringToken(errorString));
            output.send(0, new StringToken(outputString));
                //}
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem reading or writing '" + line + "'");
        }
    }

    /** Create the subprocess
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // FIXME: What if the portParameter command changes?
        _exec();
    }

    /** Terminate the subprocess.
     *  This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        try {
            if (_inputBufferedWriter != null) {
                _inputBufferedWriter.close();
            }
            if (_process != null) {
                if (_process.getInputStream() != null) {
                    _process.getInputStream().close();
                }
                if (_process.getOutputStream() != null) {
                    _process.getOutputStream().close();
                }
                if (_process.getErrorStream() != null) {
                    _process.getErrorStream().close();
                }
            }
            // FIXME: kill of the gobblers threads?

        } catch (IOException ex) {
            // ignore
        }

        // FIXME: Should we do a process.waitFor() and throw an exception
        // if the return value is not 0?

        //synchronized(this) {
        if (_process != null) {
            _process.destroy();
        }

        //    _process = null;
        //}
    }

    // Execute a command, set _process to point to the process
    // and set up _errorGobbler and _outputGobbler
    private void _exec() throws IllegalActionException {
       try {
            if (_process != null) {
                _process.destroy();
            }
            Runtime runtime = Runtime.getRuntime();

            String [] commandArray =
                StringUtilities.tokenizeForExec(command.getExpression());
            
            File directoryAsFile = directory.asFile();

            if (_debugging) {
                _debug("About to exec \"" + command.getExpression() + "\""
                        + "\n in \"" + directoryAsFile
                        + "\"\n with environment:");
            }

            Token [] environmentToken =
                ((ArrayToken)environment.getToken()).arrayValue();
            
            String [] environmentArray = null;
            if (environmentToken.length >= 1) {
                environmentArray = new String[environmentToken.length];
                for (int i = 0; i < environmentToken.length; i++) {
                    // FIXME: is there a better way to convert a Token []
                    // to a String [].
                    StringToken stringToken =
                        (StringToken)environmentToken[i];
                    environmentArray[i] = stringToken.stringValue();
                    if (_debugging) {
                        _debug("  " + i + ". \"" + environmentArray[i]
                                + "\"");
                    }

                    if ( i == 0 && environmentToken.length == 1
                            && environmentArray[0].length() == 0) {
                        if (_debugging) {
                            _debug("There is only one element, "
                                    + "it is a string of length 0,\n so we "
                                    + "pass Runtime.exec() an null "
                                    + "environment so that we use\n "
                                    + "the default environment");
                        }
                        environmentArray = null;
                    }
                }
            }
             

            _process = runtime.exec(commandArray, environmentArray,
                    directoryAsFile);

            _errorGobbler = new _StreamReaderThread(_process.getErrorStream(),
                    "ERROR");

            _outputGobbler = new _StreamReaderThread(_process.getInputStream(),
                    "OUTPUT");

            _errorGobbler.start();
            _outputGobbler.start();

            
            OutputStreamWriter inputStreamWriter =
                new OutputStreamWriter(_process.getOutputStream());
            _inputBufferedWriter =
                new BufferedWriter(inputStreamWriter);
            _inputBufferedWriter.write("foo");
        } catch (IOException ex) {
            throw new IllegalActionException(this, ex,
                    "Problem setting up command '" + command + "'");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    // Private class that reads a stream in a thread and updates the
    // stringBuffer
    private class _StreamReaderThread extends Thread {

        _StreamReaderThread(InputStream inputStream, String streamType) {
            _inputStream = inputStream;
            _streamType = streamType;
            _stringBuffer = new StringBuffer();
        }

        public String getAndReset() {
            String returnValue = _stringBuffer.toString();
            _stringBuffer = new StringBuffer();
            return returnValue;
        }

        // Read lines from the _inputStream and output them to the
        // JTextArea.
        public void run() {
            try {
                InputStreamReader inputStreamReader =
                    new InputStreamReader(_inputStream);
                BufferedReader bufferedReader =
                    new BufferedReader(inputStreamReader);
                String line = null;
                while ( (line = bufferedReader.readLine()) != null)
                    _stringBuffer.append(/*_streamType + ">" +*/ line);
            } catch (IOException ioe) {
                _stringBuffer.append("IOException: " + ioe);
            }
        }

        // StringBuffer to update
        private StringBuffer _stringBuffer;

        // Stream to read from.
        private InputStream _inputStream;
        // Description of the Stream that we print, usually "OUTPUT" or "ERROR"
        private String _streamType;

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Whether this actor is blocking.
    private boolean _blocking;

    //private BufferedReader _errorBufferedReader;
    private BufferedWriter _inputBufferedWriter;
    //private BufferedReader _outputBufferedReader;

    private _StreamReaderThread _errorGobbler;
    private _StreamReaderThread _outputGobbler;

    // The Process that we are running.
    private Process _process;
}
