# Tcl procs that are useful within Tcl Blend
#
# @Author: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c)  The Regents of the University of California.
# All rights reserved.
# 
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
# 
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
# 
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
# 
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
#######################################################################

# This file sources files that contain methods that are useful
# from within an Tcl interpreter that has Tcl Blend.

if {[package versions java] != ""} {


    puts "Sourcing [file join $TYCHO java pt kernel test init.tcl]"
    puts "Type 'helpTclBlend' for a list of Tcl Blend helper procs."
    source [file join $TYCHO java pt kernel test testDefs.tcl]
    source [file join $TYCHO java pt kernel test enums.tcl]
    source [file join $TYCHO java pt kernel test description.tcl]
}

proc helpTclBlend {} {
    puts "Tcl procs of use:\n\
	    getJavaInfo {obj} - Print the fields, methods and other info\n\
	    enumToNames {enum} - Given a enum of Nameables, print the names\n\
	    description2TclBlend {desc} - return Tcl Blend version of desc"
}
