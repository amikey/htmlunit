/*
 * Copyright (c) 2002, 2003 Gargoyle Software Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment:
 *
 *       "This product includes software developed by Gargoyle Software Inc.
 *        (http://www.GargoyleSoftware.com/)."
 *
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 4. The name "Gargoyle Software" must not be used to endorse or promote
 *    products derived from this software without prior written permission.
 *    For written permission, please contact info@GargoyleSoftware.com.
 * 5. Products derived from this software may not be called "HtmlUnit", nor may
 *    "HtmlUnit" appear in their name, without prior written permission of
 *    Gargoyle Software Inc.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GARGOYLE
 * SOFTWARE INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import org.w3c.dom.Node;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;

/**
 * The javascript object "NodeImpl" which is the base class for all DOM
 * objects.  This will typically wrap an instance of {@link DomNode}.
 *
 * @version  $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author Barnaby Court
 */
public class NodeImpl extends SimpleScriptable {

     /**
      * Create an instance.
      */
     public NodeImpl() {
     }


    /**
     * Javascript constructor.  This must be declared in every javascript file because
     * the rhino engine won't walk up the hierarchy looking for constructors.
     */
    public void jsConstructor() {
    }


    /**
     * Get the JavaScript property "nodeType" for the current node.
     * @return The node type
     */
    public short jsGet_nodeType() {
        return getDomNodeOrDie().getNodeType();
    }


    /**
     * Get the JavaScript property "nodeName" for the current node.
     * @return The node name
     */
    public String jsGet_nodeName() {
        return getDomNodeOrDie().getNodeName();
    }


    /**
     * Get the JavaScript property "nodeValue" for the current node.
     * @return The node value
     */
    public String jsGet_nodeValue() {
        return getDomNodeOrDie().getNodeValue();
    }


    /**
     * Set the JavaScript property "nodeValue" for the current node.
     * @param newValue The new node value
     */
    public void jsSet_nodeValue( String newValue ) {
        getDomNodeOrDie().getNode().setNodeValue( newValue );
    }


    /**
     * Add a DOM node to the node
     * @param childObject The node to add to this node
     * @return The newly added child node.
     */
    public Object jsFunction_appendChild(final Object childObject) {
        final Object appendedChild;
        if (childObject instanceof NodeImpl) {
            // Get XML node for the DOM node passed in
            final DomNode childDomNode =
                ((NodeImpl) childObject).getDomNodeOrDie();
            final Node childXmlNode = childDomNode.getNode();

            // Get the parent XML node that the child should be added to.
            final DomNode parentNode = this.getDomNodeOrDie();
            final Node parentXmlNode = parentNode.getNode();

            // Append the child to the parent node
            if ( parentXmlNode.appendChild(childXmlNode) == null ) {
                appendedChild = null;
            }
            else {
                appendedChild = childObject;
            }
        }
        else {
            appendedChild = null;
        }
        return appendedChild;
    }


    /**
     * Duplicate an XML node
     * @param deep If true, recursively clone all descendents.  Otherwise,
     * just clone this node.
     * @return The newly cloned node.
     */
    public Object jsFunction_cloneNode(final boolean deep) {
        final DomNode domNode = getDomNodeOrDie();
        final Node clonedXmlNode = domNode.getNode().cloneNode( deep );
        return getJavaScriptNode(domNode.getPage().getDomNode(clonedXmlNode));
    }


    /**
     * Add a DOM node as a child to this node before the referenced
     * node.  If the referenced node is null, append to the end.
     * @param newChildObject The node to add to this node
     * @param refChildObject The node before which to add the new child
     * @return The newly added child node.
     */
    public Object jsFunction_insertBefore(final Object newChildObject,
        final Object refChildObject) {
        final Object appendedChild;
        if (newChildObject instanceof NodeImpl &&
            refChildObject instanceof NodeImpl) {
            // Get XML nodes for the DOM nodes passed in
            final DomNode newChildDomNode =
                ((NodeImpl) newChildObject).getDomNodeOrDie();
            final Node newChildXmlNode = newChildDomNode.getNode();
            Node refChildXmlNode = null;
            if (refChildObject != null) {
                final DomNode refChildDomNode =
                    ((NodeImpl) refChildObject).getDomNodeOrDie();
                refChildXmlNode = refChildDomNode.getNode();
            }

            // Get the parent XML node that the child should be added to.
            final DomNode parentNode = this.getDomNodeOrDie();
            final Node parentXmlNode = parentNode.getNode();

            // Append the child to the parent node
            if ( parentXmlNode.insertBefore(newChildXmlNode,
                refChildXmlNode) == null ) {
                appendedChild = null;
            }
            else {
                appendedChild = newChildObject;
            }
        }
        else {
            appendedChild = null;
        }
        return appendedChild;
    }


    /**
     * Remove a DOM node from this node
     * @param childObject The node to remove from this node
     * @return The removed child node.
     */
    public Object jsFunction_removeChild(final Object childObject) {
        final Object removedChild;
        if (childObject instanceof NodeImpl) {
            // Get XML node for the DOM node passed in
            final DomNode childDomNode =
                ((NodeImpl) childObject).getDomNodeOrDie();
            final Node childXmlNode = childDomNode.getNode();

            // Get the parent XML node that the child should be added to.
            final DomNode parentNode = this.getDomNodeOrDie();
            final Node parentXmlNode = parentNode.getNode();

            // Remove the child from the parent node
            if ( parentXmlNode.removeChild(childXmlNode) == null ) {
                removedChild = null;
            }
            else {
                removedChild = childObject;
                parentNode.getPage().removeDomNode(childXmlNode);
            }
        }
        else {
            removedChild = null;
        }
        return removedChild;
    }


    /**
     * Replace a child DOM node with another DOM node.
     * @param newChildObject The node to add as a child of this node
     * @param oldChildObject The node to remove as a child of this node
     * @return The removed child node.
     */
    public Object jsFunction_replaceChild(final Object newChildObject,
        final Object oldChildObject) {
        final Object removedChild;
        if (newChildObject instanceof NodeImpl &&
            oldChildObject instanceof NodeImpl) {
            // Get XML nodes for the DOM nodes passed in
            final DomNode newChildDomNode =
                ((NodeImpl) newChildObject).getDomNodeOrDie();
            final Node newChildXmlNode = newChildDomNode.getNode();
            Node oldChildXmlNode = null;
            if (oldChildObject != null) {
                final DomNode oldChildDomNode =
                    ((NodeImpl) oldChildObject).getDomNodeOrDie();
                oldChildXmlNode = oldChildDomNode.getNode();
            }

            // Get the parent XML node that the child should be added to.
            final DomNode parentNode = this.getDomNodeOrDie();
            final Node parentXmlNode = parentNode.getNode();

            // Replace the old child with the new child.
            if ( parentXmlNode.replaceChild(newChildXmlNode,
                oldChildXmlNode) == null ) {
                removedChild = null;
            }
            else {
                removedChild = oldChildObject;
            }
        }
        else {
            removedChild = null;
        }
        return removedChild;
    }


    /**
     * Get the JavaScript property "parentNode" for the node that
     * contains the current node.
     * @return The parent node
     */
    public Object jsGet_parentNode() {
        return getJavaScriptNode( getDomNodeOrDie().getParentNode() );
    }


    /**
     * Get the JavaScript property "nextSibling" for the node that
     * contains the current node.
     * @return The next sibling node or null if the current node has
     * no next sibling.
     */
    public Object jsGet_nextSibling() {
        return getJavaScriptNode( getDomNodeOrDie().getNextSibling() );
    }


    /**
     * Get the JavaScript property "previousSibling" for the node that
     * contains the current node.
     * @return The previous sibling node or null if the current node has
     * no previous sibling.
     */
    public Object jsGet_previousSibling() {
        return getJavaScriptNode( getDomNodeOrDie().getPreviousSibling() );
    }


    /**
     * Get the JavaScript property "firstChild" for the node that
     * contains the current node.
     * @return The first child node or null if the current node has
     * no children.
     */
    public Object jsGet_firstChild() {
        return getJavaScriptNode( getDomNodeOrDie().getFirstChild() );
    }


    /**
     * Get the JavaScript property "lastChild" for the node that
     * contains the current node.
     * @return The last child node or null if the current node has
     * no children.
     */
    public Object jsGet_lastChild() {
        return getJavaScriptNode( getDomNodeOrDie().getLastChild() );
    }


    /**
     * Get the JavaScript node for a given DomNode
     * @param domNode The DomNode
     * @return The JavaScript node or null if the DomNode was null.
     */
    protected Object getJavaScriptNode( DomNode domNode ) {
        if ( domNode == null ) {
            return null;
        }
        return getScriptableFor( domNode );
    }
}
