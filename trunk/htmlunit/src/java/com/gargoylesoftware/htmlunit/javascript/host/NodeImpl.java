/*
 * Copyright (c) 2002-2006 Gargoyle Software Inc. All rights reserved.
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

import org.jaxen.JaxenException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.html.xpath.HtmlUnitXPath;
import com.gargoylesoftware.htmlunit.javascript.HTMLCollection;

/**
 * The javascript object "NodeImpl" which is the base class for all DOM
 * objects.  This will typically wrap an instance of {@link DomNode}.
 *
 * @version  $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author David K. Taylor
 * @author Barnaby Court
 * @author <a href="mailto:cse@dynabean.de">Christian Sell</a>
 * @author <a href="mailto:george@murnock.com">George Murnock</a>
 * @author Chris Erskine
 * @author Bruce Faulkner
 */
public class NodeImpl extends SimpleScriptable {

    private HTMLCollection childNodes_; //has to be a member to have equality (==) working
    private static final long serialVersionUID = -5695262053081637445L;


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
    public short jsxGet_nodeType() {
        return getDomNodeOrDie().getNodeType();
    }


    /**
     * Get the JavaScript property "nodeName" for the current node.
     * @return The node name
     */
    public String jsxGet_nodeName() {
        final DomNode domNode = getDomNodeOrDie();
        String nodeName = domNode.getNodeName();

        // If this is an HtmlElement then flip the result to uppercase.  This should really be
        // changed in HtmlElement itself but that would break backwards compatibility fairly
        // significantly as that one is documented as always returning a lowercase value.
        if( domNode instanceof HtmlElement ) {
            nodeName = nodeName.toUpperCase();
        }
        return nodeName;
    }


    /**
     * Get the JavaScript property "nodeValue" for the current node.
     * @return The node value
     */
    public String jsxGet_nodeValue() {
        return getDomNodeOrDie().getNodeValue();
    }


    /**
     * Set the JavaScript property "nodeValue" for the current node.
     * @param newValue The new node value
     */
    public void jsxSet_nodeValue( final String newValue ) {
        getDomNodeOrDie().setNodeValue( newValue );
    }


    /**
     * Add a DOM node to the node
     * @param childObject The node to add to this node
     * @return The newly added child node.
     */
    public Object jsxFunction_appendChild(final Object childObject) {

        Object appendedChild = null;
        if (childObject instanceof NodeImpl) {
            // Get XML node for the DOM node passed in
            final DomNode childDomNode =
                ((NodeImpl) childObject).getDomNodeOrDie();

            // Get the parent XML node that the child should be added to.
            final DomNode parentNode = getDomNodeOrDie();

            // Append the child to the parent node
            parentNode.appendChild(childDomNode);
            appendedChild = childObject;
        }
        return appendedChild;
    }


    /**
     * Duplicate an XML node
     * @param deep If true, recursively clone all descendents.  Otherwise,
     * just clone this node.
     * @return The newly cloned node.
     */
    public Object jsxFunction_cloneNode(final boolean deep) {
        final DomNode domNode = getDomNodeOrDie();
        final DomNode clonedNode = domNode.cloneNode( deep );
        return getJavaScriptNode(clonedNode);
    }


    /**
     * Add a DOM node as a child to this node before the referenced
     * node.  If the referenced node is null, append to the end.
     * @param newChildObject The node to add to this node
     * @param refChildObject The node before which to add the new child
     * @return The newly added child node.
     */
    public Object jsxFunction_insertBefore(
            final Object newChildObject, final Object refChildObject) {
        Object appendedChild = null;

        if (newChildObject instanceof NodeImpl) {

            final DomNode newChildNode =
                ((NodeImpl) newChildObject).getDomNodeOrDie();

            final DomNode refChildNode;
            // IE accepts non standard calls with only one arg
            if (Context.getUndefinedValue().equals(refChildObject)) {
                if (getWindow().getWebWindow().getWebClient().getBrowserVersion().isIE()) {
                    refChildNode = null;
                }
                else {
                    throw Context.reportRuntimeError("insertBefore: not enough arguments");
                }
            }
            else if (refChildObject != null) {
                refChildNode = ((NodeImpl) refChildObject).getDomNodeOrDie();
            }
            else {
                refChildNode = null;
            }

            // Append the child to the parent node
            if (refChildNode != null ) {
                refChildNode.insertBefore(newChildNode);
                appendedChild = newChildObject;
            }
            else {
                getDomNodeOrDie().appendChild(newChildNode);
            }
        }
        return appendedChild;
    }


    /**
     * Remove a DOM node from this node
     * @param childObject The node to remove from this node
     * @return The removed child node.
     */
    public Object jsxFunction_removeChild(final Object childObject) {
        Object removedChild = null;

        if (childObject instanceof NodeImpl) {
            // Get XML node for the DOM node passed in
            final DomNode childNode =
                ((NodeImpl) childObject).getDomNodeOrDie();

            // Remove the child from the parent node
            childNode.remove();
            removedChild = childObject;
        }
        return removedChild;
    }

    /**
     * Returns whether this node has any children.
     * @return boolean true if this node has any children, false otherwise.
     */
    public boolean jsxFunction_hasChildNodes() {
        return getDomNodeOrDie().getChildIterator().hasNext();
    }

    /**
     * Returns the child nodes of the current element.
     * @return The child nodes of the current element.
     */
    public Object jsxGet_childNodes() {
        if (childNodes_ == null) {
            childNodes_ = (HTMLCollection) makeJavaScriptObject(HTMLCollection.JS_OBJECT_NAME);
            try {
                childNodes_.init(getDomNodeOrDie(), new HtmlUnitXPath("(./* | text())"));
            }
            catch (final JaxenException je) {
                throw Context.reportRuntimeError("Failed to initialize collection element.childNodes: "
                        + je.getMessage());
            }
        }
        return childNodes_;
    }


    /**
     * Replace a child DOM node with another DOM node.
     * @param newChildObject The node to add as a child of this node
     * @param oldChildObject The node to remove as a child of this node
     * @return The removed child node.
     */
    public Object jsxFunction_replaceChild(
            final Object newChildObject, final Object oldChildObject) {
        Object removedChild = null;

        if (newChildObject instanceof NodeImpl &&
            oldChildObject instanceof NodeImpl) {
            // Get XML nodes for the DOM nodes passed in
            final DomNode newChildNode =
                ((NodeImpl) newChildObject).getDomNodeOrDie();

            final DomNode oldChildNode;
            if(oldChildObject != null) {
                // Replace the old child with the new child.
                oldChildNode = ((NodeImpl) oldChildObject).getDomNodeOrDie();
                oldChildNode.replace(newChildNode);
                removedChild = oldChildObject;
            }
        }
        return removedChild;
    }


    /**
     * Get the JavaScript property "parentNode" for the node that
     * contains the current node.
     * @return The parent node
     */
    public Object jsxGet_parentNode() {
        return getJavaScriptNode( getDomNodeOrDie().getParentNode() );
    }


    /**
     * Get the JavaScript property "nextSibling" for the node that
     * contains the current node.
     * @return The next sibling node or null if the current node has
     * no next sibling.
     */
    public Object jsxGet_nextSibling() {
        return getJavaScriptNode( getDomNodeOrDie().getNextSibling() );
    }


    /**
     * Get the JavaScript property "previousSibling" for the node that
     * contains the current node.
     * @return The previous sibling node or null if the current node has
     * no previous sibling.
     */
    public Object jsxGet_previousSibling() {
        return getJavaScriptNode( getDomNodeOrDie().getPreviousSibling() );
    }


    /**
     * Get the JavaScript property "firstChild" for the node that
     * contains the current node.
     * @return The first child node or null if the current node has
     * no children.
     */
    public Object jsxGet_firstChild() {
        return getJavaScriptNode( getDomNodeOrDie().getFirstChild() );
    }


    /**
     * Get the JavaScript property "lastChild" for the node that
     * contains the current node.
     * @return The last child node or null if the current node has
     * no children.
     */
    public Object jsxGet_lastChild() {
        return getJavaScriptNode( getDomNodeOrDie().getLastChild() );
    }


    /**
     * Get the JavaScript node for a given DomNode
     * @param domNode The DomNode
     * @return The JavaScript node or null if the DomNode was null.
     */
    protected Object getJavaScriptNode( final DomNode domNode ) {
        if ( domNode == null ) {
            return null;
        }
        return getScriptableFor( domNode );
    }


    /**
     * Allows the registration of event listeners on the event target.
     * @param type the event type to listen for (like "onclick")
     * @param listener the event listener
     * @see <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/methods/attachevent.asp">
     * MSDN documentation</a>
     */
    public void jsxFunction_attachEvent(final String type, final Function listener) {
        jsxFunction_addEventListener( type, listener, false );
    }


    /**
     * Allows the registration of event listeners on the event target.
     * @param type the event type to listen for (like "onclick")
     * @param listener the event listener
     * @param useCapture If <code>true</code>, indicates that the user wishes to initiate capture (not yet implemented)
     * @see <a href="http://developer.mozilla.org/en/docs/DOM:element.addEventListener">Mozilla documentation</a>
     */
    public void jsxFunction_addEventListener(final String type, final Function listener, final boolean useCapture) {
        getDomNodeOrDie().setEventHandler( type, listener );
    }


    /**
     * Allows the removal of event listeners on the event target.
     * @param type the event type to listen for (like "onclick")
     * @param listener the event listener
     * @see <a href="http://msdn.microsoft.com/workshop/author/dhtml/reference/methods/detachevent.asp">
     * MSDN documentation</a>
     */
    public void jsxFunction_detachEvent(final String type, final Function listener) {
        jsxFunction_removeEventListener( type, listener, false );
    }


    /**
     * Allows the removal of event listeners on the event target.
     * @param type the event type to listen for (like "onclick")
     * @param listener the event listener
     * @param useCapture If <code>true</code>, indicates that the user wishes to initiate capture (not yet implemented)
     * @see <a href="http://developer.mozilla.org/en/docs/DOM:element.removeEventListener">Mozilla documentation</a>
     */
    public void jsxFunction_removeEventListener(final String type, final Function listener, final boolean useCapture) {
        getDomNodeOrDie().removeEventHandler( type );
    }

}
