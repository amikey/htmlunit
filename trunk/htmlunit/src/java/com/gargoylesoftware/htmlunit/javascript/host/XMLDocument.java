/*
 * Copyright (c) 2002-2007 Gargoyle Software Inc. All rights reserved.
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.WebResponseImpl;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.javascript.SimpleScriptable;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * A JavaScript object for XMLDocument.
 *
 * @version $Revision$
 * @author Ahmed Ashour
 */
public class XMLDocument extends Document {

    private boolean async_ = true;
    private boolean preserveWhiteSpace_;
    
    /**
     * Creates a new instance. JavaScript objects must have a default constructor.
     */
    public XMLDocument() {
    }

    /**
     * Sets the <tt>async</tt> attribute.
     * @param async Whether or not to send the request to the server asynchronously.
     */
    public void jsxSet_async(final boolean async) {
        this.async_ = async;
    }

    /**
     * Returns Whether or not to send the request to the server asynchronously.
     * @return the <tt>async</tt> attribute.
     */
    public boolean jsxGet_async() {
        return async_;
    }
    

    /**
     * Loads an XML document from the specified location.
     *
     * @param xmlSrouce A string containing a URL that specifies the location of the XML file.
     * @return true if the load succeeded; false if the load failed.
     */
    public boolean jsxFunction_load(final String xmlSrouce) {
        if (async_) {
            getLog().debug("XMLDocument.load(): 'async' is true, currently treated as false.");
        }
        try {
            final WebRequestSettings settings = new WebRequestSettings(new URL(xmlSrouce));
            final WebResponse webResponse = getWindow().getWebWindow().getWebClient().loadWebResponse(settings);
            final XmlPage page = new XmlPage(webResponse, getWindow().getWebWindow());
            setDomNode(page);
            return true;
        }
        catch (final IOException e) {
            getLog().debug("Error parsing XML from '" + xmlSrouce + "'", e);
            return false;
        }
    }

    /**
     * Loads an XML document using the supplied string
     *
     * @param strXML A string containing the XML string to load into this XML document object.
     *      This string can contain an entire XML document or a well-formed fragment.
     * @return true if the load succeeded; false if the load failed.
     */
    public boolean jsxFunction_loadXML(final String strXML) {
        try {
            final WebResponseData data = new WebResponseData(strXML.getBytes(), 200, null, new ArrayList());
            final WebResponse webResponse = new WebResponseImpl(data, null, null, 0);
            final XmlPage page = new XmlPage(webResponse, getWindow().getWebWindow());
            setDomNode(page);
            return true;
        }
        catch (final IOException e) {
            getLog().debug("Error parsing XML\n" + strXML, e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected Object getWithPreemption(final String name) {
        return NOT_FOUND;
    }

    /**
     * {@inheritDoc}
     */
    public SimpleScriptable makeScriptableFor(final DomNode domNode) {
        final XMLElement element = new XMLElement();
        element.setPrototype(getPrototype(element.getClass()));
        element.setParentScope(getParentScope());
        element.setDomNode(domNode);
        return element;
    }

    /**
     * Get the JavaScript property "documentElement" for the document.
     * @return The root node for the document.
     */
    //TODO: should be removed, as super.jsxGet_documentElement should not be Html dependent
    public SimpleScriptable jsxGet_documentElement() {
        return getScriptableFor(((XmlPage) getDomNodeOrDie()).getDocumentXmlElement());
    }
    
    /**
     * Contains the XML representation of the node and all its descendants.
     * @return An XML representation of this node and all its descendants.
     */
    public String jsxGet_xml() {
        final XMLSerializer seralizer = new XMLSerializer();
        seralizer.setParentScope(getWindow());
        seralizer.setPrototype(getPrototype(seralizer.getClass()));
        return seralizer.jsxFunction_serializeToString((NodeImpl) jsxGet_documentElement());
    }

    /**
     * Gets the current white space handling.
     * @return the current white space handling.
     */
    public boolean jsxGet_preserveWhiteSpace() {
        return preserveWhiteSpace_;
    }

    /**
     * Specifies the white space handling.
     * @param preserveWhiteSpace white space handling.
     */
    public void jsxSet_preserveWhiteSpace(final boolean preserveWhiteSpace) {
        this.preserveWhiteSpace_ = preserveWhiteSpace;
    }
    
    /**
     * This method is used to set
     * <a href="http://msdn2.microsoft.com/en-us/library/ms766391.aspx">second-level properties</a>
     * on the DOM object.
     *
     * @param name The name of the property to be set.
     * @param value The value of the specified property.
     */
    public void jsxFunction_setProperty(final String name, final String value) {
        //empty implementation
    }

}
