/*
Code for runtime functions used in single-class mode.

Copyright (c) 2002 The University of Maryland.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

@author Shuvra S. Bhattacharyya
@version $Id$
*/

#include "pccg.h"
#include "pccg_runtime_single.h"

/* data to enable exception-catching */
jmp_buf env;
int epc;

/**
 *  PCCG implementation of the instanceof operator.
 */
boolean PCCG_instanceof(PCCG_CLASS_INSTANCE *operand,
        PCCG_CLASS *checkType) {
    PCCG_CLASS *p = operand->class;
    do {
        if (p == checkType) {
            return true;
        } else {
            p = (PCCG_CLASS *)(p->superclass);
        }
    } while (p != null);
    return false;
}
