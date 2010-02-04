/*
 * A extended base abstract class for an ontology solver.
 * 
 * Copyright (c) 1998-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 */
package ptolemy.data.ontologies;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.ontologies.gui.OntologyDisplayActions;
import ptolemy.domains.tester.lib.Testable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.ClassUtilities;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
////OntologySolver

/**
 * A extended base abstract class for an ontology solver.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public abstract class OntologySolver extends OntologySolverBase implements
        Testable {

    /**
     * Construct an OntologySolver with the specified container and name. If this
     * is the first OntologySolver created in the model, the shared utility
     * object will also be created.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the PropertySolver is not of an
     * acceptable attribute for the container.
     * @exception NameDuplicationException If the name coincides with an
     * attribute already in the container.
     */
    public OntologySolver(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _momlHandler = new OntologyMoMLHandler(this, "OntologyMoMLHandler");

        // FIXME: We do not want this GUI dependency here...
        // This attribute should be put in the MoML in the library instead
        // of here in the Java code.
        new OntologyDisplayActions(this, "PropertyDisplayActions");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Check whether there are any regression testing errors after resolving
     * properties. If so, throw a new PropertyResolutionException with
     * an error message that includes all the properties that does not match the
     * regression test values.
     * 
     * @exception OntologyResolutionException Thrown if there are any
     * errors from running the OntologySolver in the regression test.
     */
    public void checkErrors() throws OntologyResolutionException {

        List<String> errors = _ontologySolverUtilities.removeErrors();
        Collections.sort(errors);

        if (!errors.isEmpty()) {
            String errorMessage = errors.toString();

            throw new OntologyResolutionException(this, errorMessage);
        }
    }

    /**
     * Check whether there are any OntologySolver resolution errors after resolving
     * properties. If so, throw an IllegalActionException.
     * 
     * @exception IllegalActionException If an exception is thrown by calling checkErrors()
     */
    public void checkResolutionErrors() throws IllegalActionException {
        for (Object propertyable : getAllPropertyables()) {
            _recordUnacceptableSolution(propertyable, getProperty(propertyable));
        }
        checkErrors();
    }

    /**
     * If the value of the highlight parameter is set to true, highlight the
     * given property-able object with the specified color associated with the
     * given property, if there exists any. If the value of the showText
     * parameter is true, show the given property value for the given
     * property-able object. If the property is not null, this looks for the
     * _showInfo parameter in the property-able object. Create a new _showInfo
     * StringParameter if one does not already exist. Set its value to
     * the given property value. If the given property is null, this removes the
     * _showInfo parameter from the property-able object.
     * @exception IllegalActionException Thrown if an error occurs when creating
     * or setting the value for the _showInfo parameter in the property-able
     * object. Thrown if an error occurs when creating or setting the value for
     * the highlightColor attribute in the property-able object.
     */
    public void displayProperties() throws IllegalActionException {

        if (_momlHandler.highlight.getToken() == BooleanToken.TRUE) {
            _momlHandler.highlightProperties();
        }
        if (_momlHandler.showText.getToken() == BooleanToken.TRUE) {
            _momlHandler.showProperties();
        }
    }

    /**
     * Return the PropertyMoMLHandler for this OntologySolver.
     * 
     * @return The PropertyMoMLHandler for the OntologySolver
     */
    public OntologyMoMLHandler getMoMLHandler() {
        return _momlHandler;
    }

    /**
     * Return the previous resolved property for the given object.
     * @param object The given object.
     * @return The previous resolved property for the given object.
     */
    public Concept getPreviousProperty(Object object) {
        return _previousProperties.get(object);
    }

    /**
     * Invoke the solver directly.
     * @return True if the invocation succeeds; otherwise false which means an
     * error has occurred during the process.
     */
    // FIXME: Why have this method? Why not call resolveProperties() directly?
    public boolean invokeSolver() {
        boolean success = false;
        try {
            resolveProperties();

            updateProperties();

            checkErrors();

            displayProperties();

        } catch (KernelException e) {
            resetAll();
            throw new InternalErrorException(e);
        }

        return success;
    }

    /**
     * Return true if the specified property-able object is settable; otherwise
     * false which means that its property has been set by
     * PropertyHelper.setEquals().
     * @param object The specified property-able object.
     * @return True if the specified property-able object is settable, otherwise
     * false.
     */
    public boolean isSettable(Object object) {
        return !_nonSettables.contains(object);
    }

    /**
     * Record the previous property of the given object.
     * @param object The given object.
     * @param property The given property.
     */
    public void recordPreviousProperty(Object object, Concept property) {
        _previousProperties.put(object, property);
    }

    /**
     * Reset the solver. This removes the internal states of the solver (e.g.
     * previously recorded properties, statistics, etc.).
     */
    public void reset() {
        super.reset();
        _previousProperties = new HashMap<Object, Concept>();
    }

    /**
     * Invoke the OntologySolver and run its algorithm to resolve
     * which Concepts in the Ontology are assigned to each object in the
     * model.
     * 
     * @throws KernelException If the ontology resolution fails.
     */
    public void resolveProperties() throws KernelException {
        getOntologySolverUtilities().addRanSolvers(this);
        _resolveProperties();
        checkResolutionErrors();
    }

    /**
     * Update the property. This method is called from both invoked and
     * auxiliary solvers.
     * @exception IllegalActionException If the properties cannot be updated.
     */
    public void updateProperties() throws IllegalActionException {
        for (Object propertyable : getAllPropertyables()) {

            if (!NamedObj.class.isInstance(propertyable)) {
                // FIXME: This happens when the propertyable is an ASTNodes,
                // or any non-Ptolemy objects. We are not updating their
                // property values, nor doing regression test for them.
                continue;
            }

            NamedObj namedObj = (NamedObj) propertyable;

            // Get the value resolved by the solver.
            getProperty(namedObj);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Add choices to the parameter where the choices are subdirectories of the
     * directoryPath. DirectoryPaths in the file system and in Jar URLs are
     * handled.
     * @param parameter The parameter to be updated with the subdirectories
     * @param directoryPath The directory to be searched for subdirectories.
     * @exception IllegalActionException If there is a problem reading the
     * directory as a file or JAR URL.
     */
    protected void _addChoices(Parameter parameter, String directoryPath)
            throws IllegalActionException {

        // Use a FilenameFilter so that we can access files via
        // Web Start.
        try {
            URI directoryURI = new URI(FileUtilities.nameToURL(directoryPath,
                    null, null).toExternalForm().replaceAll(" ", "%20"));
            File directory = null;
            try {
                try {
                    directory = new File(directoryURI);
                } catch (Throwable throwable) {
                    throw new InternalErrorException(this, throwable,
                            "Failed to find directories in the URI: \""
                                    + directoryURI + "\"");
                }
                DirectoryNameFilter filter = new DirectoryNameFilter();
                File[] directories = directory.listFiles(filter);
                if (directories == null) {
                    throw new InternalErrorException(this, null,
                            "Failed to find directories in \"" + directoryPath
                                    + "\"");
                } else {
                    for (File element : directories) {
                        String directoryName = element.getName();
                        parameter.addChoice(directoryName);
                    }
                }
            } catch (Throwable throwable) {
                try {
                    if (!directoryURI.toString().startsWith("jar:")) {
                        throw throwable;
                    } else {
                        // We have a jar URL, we are probably in Web Start
                        List<String> directories = ClassUtilities
                                .jarURLDirectories(directoryURI.toURL());
                        for (String directoryFullPath : directories) {
                            // Get the name of just the directory
                            String directoryName = directoryFullPath;
                            // System.out.println("PropertyConstraintSolver0: "
                            //        + directoryName.lastIndexOf("/")
                            //        + " " + directoryName.length()
                            //        + " " + directoryName);
                            if (directoryName.lastIndexOf("/") > -1) {
                                if (directoryName.lastIndexOf("/") == directoryName
                                        .length() - 1) {
                                    // Remove the trailing /
                                    directoryName = directoryName.substring(0,
                                            directoryName.length() - 1);
                                    //System.out.println("PropertyConstraintSolver1: "
                                    //        + directoryName);
                                }

                                directoryName = directoryName
                                        .substring(directoryName
                                                .lastIndexOf("/") + 1);
                                //System.out.println("PropertyConstraintSolver2: "
                                //    + directoryName);
                            }
                            //System.out.println("PropertyConstraintSolver3: "
                            //        + directoryName);
                            parameter.addChoice(directoryName);
                        }
                    }
                } catch (Throwable throwable2) {
                    System.err.println("Tried to look in jarURL");
                    throwable2.printStackTrace();
                    throw new IllegalActionException(this, throwable,
                            "Failed to process " + directoryURI);
                }
            }
        } catch (Throwable ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to find directories in \"" + directoryPath
                            + "\", the parameter \"" + parameter.getFullName()
                            + "\"cannot be set.");
        }
    }

    /**
     * Get the propertyable attribute contained by the given propertyable.
     * @param propertyable The given propertyable object.
     * @return The property attribute contained by the given propertyable.
     * @exception IllegalActionException If the attribute cannot be found for the object.
     */
    protected ConceptAttribute _getPropertyAttribute(NamedObj propertyable)
            throws IllegalActionException {
        ConceptAttribute attribute = null;

        // write results to attribute
        /* FIXME: Not using attribute.
        if (getExtendedUseCaseName().startsWith("lattice")) {
            attribute = (ConceptAttribute) propertyable
                    .getAttribute(getExtendedUseCaseName());

            if (attribute == null) {
                try {
                    attribute = new ConceptAttribute(propertyable,
                            getExtendedUseCaseName());
                } catch (NameDuplicationException e) {
                    // This shouldn't happen. If another attribute
                    // has the same name, we should find it before.
                    assert false;
                }
            }
        } else {
            // FIXME: Error checking?
            throw new PropertyResolutionException(this, propertyable,
                    "Failed to get the PropertyAttribute.");
        }
        */
        return attribute;
    }

    /** Run the solver.
     *  @exception KernelException If the solver fails.
     */
    protected abstract void _resolveProperties() throws KernelException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The handler that issues MoML requests and makes model changes.
     */
    protected OntologyMoMLHandler _momlHandler;

    /**
     * True if the solver is invoked directly; otherwise, false which means it
     * acts as an intermediate solver.
     */
    protected boolean _isInvoked;

    /**
     * The system-specific end-of-line character.
     */
    protected static final String _eol = StringUtilities
            .getProperty("line.separator");

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Record as an error for the given property-able object and its resolved
     * property. If the given property is null, it does nothing. If the given
     * property is unacceptable, an error is recorded for the given
     * property-able object and the property.
     * 
     * @param propertyable The model object to which the concept is attached.
     * @param property The concept attached to the model object.
     */
    private void _recordUnacceptableSolution(Object propertyable,
            Concept property) {

        // Check for unacceptable solution.
        if (property != null && !property.isValueAcceptable()) {
                _ontologySolverUtilities.addErrors(
                    "Property \"" + property
                    + "\" is not an acceptable solution for " + propertyable
                    + "." + _eol);
        }
    }

    /**
     * The record of the previously resolved properties. It is a map between the
     * property-able objects and their resolved properties.
     */
    private HashMap<Object, Concept> _previousProperties = new HashMap<Object, Concept>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /**
     * Look for directories that are not CVS or .svn.
     */
    static class DirectoryNameFilter implements FilenameFilter {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /**
         * Return true if the specified file names a directory that is not named
         * "CVS" or ".svn".
         * @param directory the directory in which the potential directory was
         * found.
         * @param name the name of the directory or file.
         * @return true if the file is a directory that contains a file called
         * configuration.xml
         */
        public boolean accept(File directory, String name) {
            try {
                File file = new File(directory, name);

                if (!file.isDirectory() || file.getName().equals("CVS")
                        || file.getName().equals(".svn")) {
                    return false;
                }
            } catch (Exception ex) {
                return false;
            }

            return true;
        }
    }

}
