/* Generated By:JJTree: Do not edit this line. ASTPtLeafNode.java */

/* PtLeafNode represents leaf nodes in the parse tree.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)

Created : May 1998

*/

//////////////////////////////////////////////////////////////////////////
//// ASTPtLeafNode
/**
 * The parse tree created from the expression string consists of a 
 * hierarchy of node objects, each of which is an instance of a class
 * derived from this class. This class represents the leaf nodes of the 
 * tree.
 *
 * The tree is resolved in a top down manner, calling resolve on the 
 * children of each node before resolving the type of the current node.
 * 
 * @author Neil Smyth
 * @version$Id$
 * @see pt.data.parser.ASTPTRootNode
 * @see pt.data.parser.PtParser 
 * @see pt.data.Token 
*/


package pt.data.parser;

import collections.LinkedList;

public class ASTPtLeafNode extends ASTPtRootNode {
  
    /////////////////////////////////////////////////////////////////////
    /// from here until next line of dashes is code for PtParser

    /** When the input String refers to another Param, we store the refered
     *  Param in the leaf node. Thus when the value of a Param changes, by 
     *  reevaluating the parse tree we get the correct value.
     */
   protected pt.data.Param _param;  

    /** If this leaf node represents a reference to a Param, return the
     *  PtToken contained in that Param. Otherwise return the PtToken
     *  object stored in this node.
     *  @return The PtToken stored/refernced by this node
     *  @exception IllegalArgumentException Thrown when an error occurs 
     *  trying to evaluate the PtToken type and/or value to be stored in 
     *  node in the tree.
     */
    public pt.data.Token evaluateParseTree() throws IllegalArgumentException {
        if (_param != null) {
            _ptToken = _param.getToken();
        } else if (_ptToken == null) {
            String str = "in a leaf node, either _ptToken or _param ";
            throw new IllegalArgumentException(str+ "must be non-null");
        }
        return _ptToken;
    }
   
    ////////////////////////////////////////////////////////////////
 public ASTPtLeafNode(int id) {
    super(id);
  }

  public ASTPtLeafNode(PtParser p, int id) {
    super(p, id);
  }

  public static Node jjtCreate(int id) {
      return new ASTPtLeafNode(id);
  }

  public static Node jjtCreate(PtParser p, int id) {
      return new ASTPtLeafNode(p, id);
  }
    
  public void jjtOpen() {
  }

  public void jjtClose() {
  }
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /* You can override these two methods in subclasses of LeafNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() { return PtParserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString() ; }

  /* Override this method if you want to customize how the node dumps
     out its children. - overridden Neil Smyth*/

  public void dump(String prefix) {
      if (_ptToken != null) {
          String str = toString(prefix) + ", Token type: ";
          str = str + _ptToken.getClass().getName() + ", Value: "; 
          System.out.println( str + _ptToken.toString());
      } else {
           System.out.println( toString(prefix) + "  _ptToken is null");
      }
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	ASTPtLeafNode n = (ASTPtLeafNode)children[i];
	if (n != null) {
	  n.dump(prefix + " ");
	}
      }
    }
  }
}

