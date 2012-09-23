/*
 * Copyright (c) 2002-2012 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host;

import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.FF;
import static com.gargoylesoftware.htmlunit.javascript.configuration.BrowserName.IE;

import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;
import com.gargoylesoftware.htmlunit.html.DomAttr;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxSetter;
import com.gargoylesoftware.htmlunit.javascript.configuration.WebBrowser;

/**
 * A JavaScript object for an Attribute.
 *
 * @see <a href="http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID-63764602">W3C DOM Level 2</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/ms535187.aspx">MSDN documentation</a>
 * @version $Revision$
 * @author Daniel Gredler
 * @author Chris Erskine
 * @author Ahmed Ashour
 * @author Sudhan Moghe
 */
@JsxClass(domClass = DomAttr.class)
public class Attr extends Node {

    /**
     * Creates an instance. JavaScript objects must have a default constructor.
     */
    public Attr() { }

    /**
     * Detaches this attribute from the parent HTML element after caching the attribute value.
     */
    public void detachFromParent() {
        final DomAttr domNode = getDomNodeOrDie();
        final DomElement parent = (DomElement) domNode.getParentNode();
        if (parent != null) {
            domNode.setValue(parent.getAttribute(get_name()));
        }
        domNode.remove();
    }

    /**
     * Returns <tt>true</tt> if this attribute is an ID.
     * @return <tt>true</tt> if this attribute is an ID
     */
    @JsxGetter(@WebBrowser(FF))
    public boolean get_isId() {
        return getDomNodeOrDie().isId();
    }

    /**
     * Returns <tt>true</tt> if arbitrary properties can be added to this attribute.
     * @return <tt>true</tt> if arbitrary properties can be added to this attribute
     */
    @JsxGetter(@WebBrowser(IE))
    public boolean get_expando() {
        return true;
    }

    /**
     * Returns the name of the attribute.
     * @return the name of the attribute
     */
    @JsxGetter
    public String get_name() {
        return getDomNodeOrDie().getName();
    }

    /**
     * Returns the value of this attribute.
     * @return the value of this attribute
     */
    @Override
    public String get_nodeValue() {
        return get_value();
    }

    /**
     * Returns the owner element.
     * @return the owner element
     */
    @JsxGetter(@WebBrowser(FF))
    public Object get_ownerElement() {
        final DomElement parent = getDomNodeOrDie().getOwnerElement();
        if (parent != null) {
            return parent.getScriptObject();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * @return <code>null</code>
     */
    @Override
    public Node get_parentNode() {
        return null;
    }

    /**
     * Returns <tt>true</tt> if this attribute has been specified.
     * @return <tt>true</tt> if this attribute has been specified
     */
    @JsxGetter
    public boolean get_specified() {
        return getDomNodeOrDie().getSpecified();
    }

    /**
     * Returns the value of this attribute.
     * @return the value of this attribute
     */
    @JsxGetter
    public String get_value() {
        return getDomNodeOrDie().getValue();
    }

    /**
     * Sets the value of this attribute.
     * @param value the new value of this attribute
     */
    @JsxSetter
    public void setValue(final String value) {
        getDomNodeOrDie().setValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node get_firstChild() {
        return get_lastChild();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node get_lastChild() {
        if (getBrowserVersion().hasFeature(BrowserVersionFeatures.JS_ATTR_FIRST_LAST_CHILD_RETURNS_NULL)) {
            return null;
        }

        final DomText text = new DomText(getDomNodeOrDie().getPage(), get_nodeValue());
        return (Node) text.getScriptObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public DomAttr getDomNodeOrDie() throws IllegalStateException {
        return super.getDomNodeOrDie();
    }
}
