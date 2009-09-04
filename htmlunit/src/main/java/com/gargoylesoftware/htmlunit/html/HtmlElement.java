/*
 * Copyright (c) 2002-2009 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.html;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sourceforge.htmlunit.corejs.javascript.BaseFunction;
import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ContextAction;
import net.sourceforge.htmlunit.corejs.javascript.ContextFactory;
import net.sourceforge.htmlunit.corejs.javascript.Function;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import com.gargoylesoftware.htmlunit.javascript.host.EventHandler;
import com.gargoylesoftware.htmlunit.javascript.host.MouseEvent;
import com.gargoylesoftware.htmlunit.javascript.host.UIEvent;
import com.gargoylesoftware.htmlunit.javascript.host.html.HTMLElement;

/**
 * An abstract wrapper for HTML elements.
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:gudujarlson@sf.net">Mike J. Bresnahan</a>
 * @author David K. Taylor
 * @author <a href="mailto:cse@dynabean.de">Christian Sell</a>
 * @author David D. Kilzer
 * @author Mike Gallaher
 * @author Denis N. Antonioli
 * @author Marc Guillemot
 * @author Ahmed Ashour
 * @author Daniel Gredler
 * @author Dmitri Zoubkov
 * @author Sudhan Moghe
 */
public abstract class HtmlElement extends DomElement {

    private static final long serialVersionUID = -2841932584831342634L;
    private static final Log LOG = LogFactory.getLog(HtmlElement.class);

    /**
     * Constant indicating that a tab index value is out of bounds (less than <tt>0</tt> or greater
     * than <tt>32767</tt>).
     *
     * @see #getTabIndex()
     */
    public static final Short TAB_INDEX_OUT_OF_BOUNDS = new Short(Short.MIN_VALUE);

    /** The listeners which are to be notified of attribute changes. */
    private List<HtmlAttributeChangeListener> attributeListeners_;

    /** The owning form for lost form children. */
    private HtmlForm owningForm_;

    /**
     * Creates an instance.
     *
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the element type to instantiate
     * @param page the page that contains this element
     * @param attributes a map ready initialized with the attributes for this element, or
     * <code>null</code>. The map will be stored as is, not copied.
     */
    protected HtmlElement(final String namespaceURI, final String qualifiedName, final SgmlPage page,
            final Map<String, DomAttr> attributes) {
        super(namespaceURI, qualifiedName, page, attributes);
        if (page != null && page.getWebClient().getBrowserVersion().isFirefox()) {
            final String value = getAttribute("class");
            if (value != ATTRIBUTE_NOT_DEFINED) {
                setAttribute("class", value.trim());
            }
        }
    }

    /**
     * Sets the value of the specified attribute. This method may be overridden by subclasses
     * which are interested in specific attribute value changes, but such methods <b>must</b>
     * invoke <tt>super.setAttributeValue()</tt>, and <b>should</b> consider the value of the
     * <tt>cloning</tt> parameter when deciding whether or not to execute custom logic.
     *
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the attribute
     * @param attributeValue the value of the attribute
     */
    @Override
    public void setAttributeNS(final String namespaceURI, final String qualifiedName,
            final String attributeValue) {

        final String oldAttributeValue = getAttribute(qualifiedName);
        String value = attributeValue;

        final boolean mappedElement = (qualifiedName.equals("name") || qualifiedName.equals("id"))
            && getOwnerDocument() instanceof HtmlPage;
        if (mappedElement) {
            ((HtmlPage) getPage()).removeMappedElement(this);
        }

        super.setAttributeNS(namespaceURI, qualifiedName, attributeValue);
        if (value.length() == 0) {
            value = ATTRIBUTE_VALUE_EMPTY;
        }

        // TODO: Clean up; this is a hack for HtmlElement living within an XmlPage.
        if (!(getOwnerDocument() instanceof HtmlPage)) {
            return;
        }

        final HtmlPage htmlPage = (HtmlPage) getPage();
        if (mappedElement) {
            htmlPage.addMappedElement(this);
        }

        final HtmlAttributeChangeEvent htmlEvent;
        if (oldAttributeValue == ATTRIBUTE_NOT_DEFINED) {
            htmlEvent = new HtmlAttributeChangeEvent(this, qualifiedName, attributeValue);
        }
        else {
            htmlEvent = new HtmlAttributeChangeEvent(this, qualifiedName, oldAttributeValue);
        }

        if (oldAttributeValue == ATTRIBUTE_NOT_DEFINED) {
            fireHtmlAttributeAdded(htmlEvent);
            ((HtmlPage) getPage()).fireHtmlAttributeAdded(htmlEvent);
        }
        else {
            fireHtmlAttributeReplaced(htmlEvent);
            ((HtmlPage) getPage()).fireHtmlAttributeReplaced(htmlEvent);
        }
        if (getPage().getWebClient().getBrowserVersion().isIE()) {
            fireEvent(Event.createPropertyChangeEvent(this, qualifiedName));
        }
    }

    /**
     * Returns the HTML elements that are descendants of this element and that have one of the specified tag names.
     * @param tagNames the tag names to match (case-insensitive)
     * @return the HTML elements that are descendants of this element and that have one of the specified tag name
     */
    public final List<HtmlElement> getHtmlElementsByTagNames(final List<String> tagNames) {
        final List<HtmlElement> list = new ArrayList<HtmlElement>();
        for (final String tagName : tagNames) {
            list.addAll(getHtmlElementsByTagName(tagName));
        }
        return list;
    }

    /**
     * Returns the HTML elements that are descendants of this element and that have the specified tag name.
     * @param tagName the tag name to match (case-insensitive)
     * @param <E> the sub-element type
     * @return the HTML elements that are descendants of this element and that have the specified tag name
     */
    @SuppressWarnings("unchecked")
    public final <E extends HtmlElement> List<E> getHtmlElementsByTagName(final String tagName) {
        final List<E> list = new ArrayList<E>();
        final String lowerCaseTagName = tagName.toLowerCase();
        final Iterable<HtmlElement> iterable = getAllHtmlChildElements();
        for (final HtmlElement element : iterable) {
            if (lowerCaseTagName.equals(element.getTagName())) {
                list.add((E) element);
            }
        }
        return list;
    }

    /**
     * Removes an attribute specified by name from this element.
     * @param attributeName the attribute attributeName
     */
    @Override
    public final void removeAttribute(final String attributeName) {
        final String value = getAttribute(attributeName);

        if (getPage() instanceof HtmlPage) {
            ((HtmlPage) getPage()).removeMappedElement(this);
        }

        super.removeAttribute(attributeName);

        if (getPage() instanceof HtmlPage) {
            ((HtmlPage) getPage()).addMappedElement(this);

            final HtmlAttributeChangeEvent event = new HtmlAttributeChangeEvent(this, attributeName, value);
            fireHtmlAttributeRemoved(event);
            ((HtmlPage) getPage()).fireHtmlAttributeRemoved(event);
        }
    }

    /**
     * Support for reporting HTML attribute changes. This method can be called when an attribute
     * has been added and it will send the appropriate {@link HtmlAttributeChangeEvent} to any
     * registered {@link HtmlAttributeChangeListener}s.
     *
     * Note that this method recursively calls this element's parent's
     * {@link #fireHtmlAttributeAdded(HtmlAttributeChangeEvent)} method.
     *
     * @param event the event
     * @see #addHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    protected void fireHtmlAttributeAdded(final HtmlAttributeChangeEvent event) {
        if (attributeListeners_ != null) {
            synchronized (this) {
                for (final HtmlAttributeChangeListener listener : attributeListeners_) {
                    listener.attributeAdded(event);
                }
            }
        }
        final DomNode parentNode = getParentNode();
        if (parentNode instanceof HtmlElement) {
            ((HtmlElement) parentNode).fireHtmlAttributeAdded(event);
        }
    }

    /**
     * Support for reporting HTML attribute changes. This method can be called when an attribute
     * has been replaced and it will send the appropriate {@link HtmlAttributeChangeEvent} to any
     * registered {@link HtmlAttributeChangeListener}s.
     *
     * Note that this method recursively calls this element's parent's
     * {@link #fireHtmlAttributeReplaced(HtmlAttributeChangeEvent)} method.
     *
     * @param event the event
     * @see #addHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    protected void fireHtmlAttributeReplaced(final HtmlAttributeChangeEvent event) {
        if (attributeListeners_ != null) {
            synchronized (this) {
                for (final HtmlAttributeChangeListener listener : attributeListeners_) {
                    listener.attributeReplaced(event);
                }
            }
        }
        final DomNode parentNode = getParentNode();
        if (parentNode instanceof HtmlElement) {
            ((HtmlElement) parentNode).fireHtmlAttributeReplaced(event);
        }
    }

    /**
     * Support for reporting HTML attribute changes. This method can be called when an attribute
     * has been removed and it will send the appropriate {@link HtmlAttributeChangeEvent} to any
     * registered {@link HtmlAttributeChangeListener}s.
     *
     * Note that this method recursively calls this element's parent's
     * {@link #fireHtmlAttributeRemoved(HtmlAttributeChangeEvent)} method.
     *
     * @param event the event
     * @see #addHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    protected void fireHtmlAttributeRemoved(final HtmlAttributeChangeEvent event) {
        if (attributeListeners_ != null) {
            synchronized (this) {
                for (final HtmlAttributeChangeListener listener : attributeListeners_) {
                    listener.attributeRemoved(event);
                }
            }
        }
        final DomNode parentNode = getParentNode();
        if (parentNode instanceof HtmlElement) {
            ((HtmlElement) parentNode).fireHtmlAttributeRemoved(event);
        }
    }

    /**
     * @return the same value as returned by {@link #getTagName()}
     */
    @Override
    public String getNodeName() {
        String name = getLocalName();
        if (getPrefix() != null) {
            name = getPrefix() + ':' + name;
        }
        name = name.toLowerCase();
        return name;
    }

    /**
     * @return the identifier of this element
     */
    public final String getId() {
        return getAttribute("id");
    }

    /**
     * Sets the identifier this element.
     *
     * @param newId the new identifier of this element
     */
    public final void setId(final String newId) {
        setAttribute("id", newId);
    }

    /**
     * Returns this element's tab index, if it has one. If the tab index is outside of the
     * valid range (less than <tt>0</tt> or greater than <tt>32767</tt>), this method
     * returns {@link #TAB_INDEX_OUT_OF_BOUNDS}. If this element does not have
     * a tab index, or its tab index is otherwise invalid, this method returns <tt>null</tt>.
     *
     * @return this element's tab index
     */
    public Short getTabIndex() {
        final String index = getAttribute("tabindex");
        if (index == null || index.length() == 0) {
            return null;
        }
        try {
            final long l = Long.parseLong(index);
            if (l >= 0 && l <= Short.MAX_VALUE) {
                return new Short(new Long(l).shortValue());
            }
            return TAB_INDEX_OUT_OF_BOUNDS;
        }
        catch (final NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns the first element with the specified tag name that is an ancestor to this element, or
     * <tt>null</tt> if no such element is found.
     * @param tagName the name of the tag searched (case insensitive)
     * @return the first element with the specified tag name that is an ancestor to this element
     */
    public HtmlElement getEnclosingElement(final String tagName) {
        final String tagNameLC = tagName.toLowerCase();

        DomNode currentNode = getParentNode();
        while (currentNode != null) {
            if (currentNode instanceof HtmlElement
                    && currentNode.getNodeName().equals(tagNameLC)) {

                return (HtmlElement) currentNode;
            }
            currentNode = currentNode.getParentNode();
        }
        return null;
    }

    /**
     * Returns the form which contains this element, or <tt>null</tt> if this element is not inside
     * of a form.
     * @return the form which contains this element
     */
    public HtmlForm getEnclosingForm() {
        if (owningForm_ != null) {
            return owningForm_;
        }
        return (HtmlForm) getEnclosingElement("form");
    }

    /**
     * Returns the form which contains this element. If this element is not inside a form, this method
     * throws an {@link IllegalStateException}.
     * @return the form which contains this element
     * @throws IllegalStateException if the element is not inside a form
     */
    public HtmlForm getEnclosingFormOrDie() throws IllegalStateException {
        final HtmlForm form = getEnclosingForm();
        if (form == null) {
            throw new IllegalStateException("Element is not contained within a form: " + this);
        }
        return form;
    }

    /**
     * Simulates typing the specified text while this element has focus.
     * Note that for some elements, typing '\n' submits the enclosed form.
     * @param text the text you with to simulate typing
     * @exception IOException If an IO error occurs
     */
    public void type(final String text) throws IOException {
        for (final char ch : text.toCharArray()) {
            type(ch);
        }
    }

    /**
     * Simulates typing the specified text while this element has focus.
     * Note that for some elements, typing '\n' submits the enclosed form.
     * @param text the text you with to simulate typing
     * @param shiftKey true if SHIFT is pressed
     * @param ctrlKey true if CTRL is pressed
     * @param altKey true if ALT is pressed
     * @exception IOException If an IO error occurs
     */
    public void type(final String text, final boolean shiftKey, final boolean ctrlKey, final boolean altKey)
        throws IOException {
        for (final char ch : text.toCharArray()) {
            type(ch, shiftKey, ctrlKey, altKey);
        }
    }

    /**
     * Simulates typing the specified character while this element has focus, returning the page contained
     * by this element's window after typing. Note that it may or may not be the same as the original page,
     * depending on the JavaScript event handlers, etc. Note also that for some elements, typing <tt>'\n'</tt>
     * submits the enclosed form.
     *
     * @param c the character you with to simulate typing
     * @return the page that occupies this window after typing
     * @exception IOException if an IO error occurs
     */
    public Page type(final char c) throws IOException {
        return type(c, false, false, false);
    }

    /**
     * Simulates typing the specified character while this element has focus, returning the page contained
     * by this element's window after typing. Note that it may or may not be the same as the original page,
     * depending on the JavaScript event handlers, etc. Note also that for some elements, typing <tt>'\n'</tt>
     * submits the enclosed form.
     *
     * @param c the character you with to simulate typing
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the typing
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the typing
     * @param altKey <tt>true</tt> if ALT is pressed during the typing
     * @return the page that occupies this window after typing
     * @exception IOException if an IO error occurs
     */
    public Page type(final char c, final boolean shiftKey, final boolean ctrlKey, final boolean altKey)
        throws IOException {
        if (this instanceof DisabledElement && ((DisabledElement) this).isDisabled()) {
            return getPage();
        }

        if (((HtmlPage) getPage()).getFocusedElement() != this) {
            focus();
        }

        final Event keyDown = new UIEvent(this, Event.TYPE_KEY_DOWN, c, shiftKey, ctrlKey, altKey);
        final ScriptResult keyDownResult = fireEvent(keyDown);

        final Event keyPress = new UIEvent(this, Event.TYPE_KEY_PRESS, c, shiftKey, ctrlKey, altKey);
        final ScriptResult keyPressResult = fireEvent(keyPress);

        if (!keyDown.isAborted(keyDownResult) && !keyPress.isAborted(keyPressResult)) {
            doType(c, shiftKey, ctrlKey, altKey);
        }

        final boolean ie = getPage().getWebClient().getBrowserVersion().isIE();
        if (!ie
            && (this instanceof HtmlTextInput
            || this instanceof HtmlTextArea
            || this instanceof HtmlPasswordInput)) {
            final Event input = new UIEvent(this, Event.TYPE_INPUT, c, shiftKey, ctrlKey, altKey);
            fireEvent(input);
        }

        final Event keyUp = new UIEvent(this, Event.TYPE_KEY_UP, c, shiftKey, ctrlKey, altKey);
        fireEvent(keyUp);

        final HtmlForm form = getEnclosingForm();
        if (form != null && c == '\n' && isSubmittableByEnter()) {
            if (!ie) {
                final HtmlSubmitInput submit = form.getFirstByXPath(".//input[@type='submit']");
                if (submit != null) {
                    return submit.click();
                }
            }
            return form.submit((SubmittableElement) this);
        }
        return getPage();
    }

    /**
     * Performs the effective type action, called after the keyPress event and before the keyUp event.
     * @param c the character you with to simulate typing
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the typing
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the typing
     * @param altKey <tt>true</tt> if ALT is pressed during the typing
     */
    protected void doType(final char c, final boolean shiftKey, final boolean ctrlKey, final boolean altKey) {
        // Empty.
    }

    /**
     * Returns <tt>true</tt> if clicking Enter (ASCII 10, or '\n') should submit the enclosed form (if any).
     * The default implementation returns <tt>false</tt>.
     * @return <tt>true</tt> if clicking Enter should submit the enclosed form (if any)
     */
    protected boolean isSubmittableByEnter() {
        return false;
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();

        buffer.append(ClassUtils.getShortClassName(getClass()));
        buffer.append("[<");

        final StringWriter writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        printOpeningTagContentAsXml(printWriter);
        buffer.append(writer.toString());

        buffer.append(">]");

        return buffer.toString();
    }

    /**
     * Searches for an element based on the specified criteria, returning the first element which matches
     * said criteria. Only elements which are descendants of this element are included in the search.
     *
     * @param elementName the name of the element to search for
     * @param attributeName the name of the attribute to search for
     * @param attributeValue the value of the attribute to search for
     * @param <E> the sub-element type
     * @return the first element which matches the specified search criteria
     * @throws ElementNotFoundException if no element matches the specified search criteria
     */
    public final <E extends HtmlElement> E getOneHtmlElementByAttribute(final String elementName,
            final String attributeName,
        final String attributeValue) throws ElementNotFoundException {

        WebAssert.notNull("elementName", elementName);
        WebAssert.notNull("attributeName", attributeName);
        WebAssert.notNull("attributeValue", attributeValue);

        final List<E> list = getElementsByAttribute(elementName, attributeName, attributeValue);

        final int listSize = list.size();
        if (listSize == 0) {
            throw new ElementNotFoundException(elementName, attributeName, attributeValue);
        }

        return list.get(0);
    }

    /**
     * Returns the element in this element's page with the specified ID. If more than one element
     * has the specified ID (not allowed by the HTML spec), this method returns the first one.
     *
     * @param id the ID value to search for
     * @param <E> the sub-element type
     * @return the element in this element's page with the specified ID
     * @exception ElementNotFoundException if no element has the specified ID
     */
    @SuppressWarnings("unchecked")
    public <E extends HtmlElement> E getElementById(final String id) throws ElementNotFoundException {
        return (E) ((HtmlPage) getPage()).getHtmlElementById(id);
    }

    /**
     * <p>Returns <tt>true</tt> if there is an element in this element's page with the specified ID.
     * This method is intended for situations where it is enough to know whether a specific
     * element is present in the document.</p>
     *
     * <p>Implementation Note: This method calls {@link #getElementById(String)} internally,
     * so writing code such as the following would be extremely inefficient:</p>
     *
     * <pre>
     * if (hasHtmlElementWithId(id)) {
     *     HtmlElement element = getHtmlElementWithId(id)
     *     ...
     * }
     * </pre>
     *
     * @param id the id to search for
     * @return <tt>true</tt> if there is an element in this element's page with the specified ID
     */
    public boolean hasHtmlElementWithId(final String id) {
        try {
            getElementById(id);
            return true;
        }
        catch (final ElementNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns all elements which are descendants of this element and match the specified search criteria.
     *
     * @param elementName the name of the element to search for
     * @param attributeName the name of the attribute to search for
     * @param attributeValue the value of the attribute to search for
     * @param <E> the sub-element type
     * @return all elements which are descendants of this element and match the specified search criteria
     */
    @SuppressWarnings("unchecked")
    public final <E extends HtmlElement> List<E> getElementsByAttribute(
            final String elementName,
            final String attributeName,
            final String attributeValue) {

        final List<E> list = new ArrayList<E>();
        final String lowerCaseTagName = elementName.toLowerCase();

        for (final HtmlElement next : getAllHtmlChildElements()) {
            if (next.getTagName().equals(lowerCaseTagName)) {
                final String attValue = next.getAttribute(attributeName);
                if (attValue != null && attValue.equals(attributeValue)) {
                    list.add((E) next);
                }
            }
        }
        return list;
    }

    /**
     * Appends a child element to this HTML element with the specified tag name
     * if this HTML element does not already have a child with that tag name.
     * Returns the appended child element, or the first existent child element
     * with the specified tag name if none was appended.
     * @param tagName the tag name of the child to append
     * @return the added child, or the first existing child if none was added
     */
    public final HtmlElement appendChildIfNoneExists(final String tagName) {
        final HtmlElement child;
        final List<HtmlElement> children = getHtmlElementsByTagName(tagName);
        if (children.isEmpty()) {
            // Add a new child and return it.
            child = ((HtmlPage) getPage()).createElement(tagName);
            appendChild(child);
        }
        else {
            // Return the first existing child.
            child = children.get(0);
        }
        return child;
    }

    /**
     * Removes the <tt>i</tt>th child element with the specified tag name
     * from all relationships, if possible.
     * @param tagName the tag name of the child to remove
     * @param i the index of the child to remove
     */
    public final void removeChild(final String tagName, final int i) {
        final List<HtmlElement> children = getHtmlElementsByTagName(tagName);
        if (i >= 0 && i < children.size()) {
            children.get(i).remove();
        }
    }

    /**
     * @return an Iterable over the HtmlElement children of this object, i.e. excluding the non-element nodes
     */
    public final Iterable<HtmlElement> getChildElements() {
        return new Iterable<HtmlElement>() {
            public Iterator<HtmlElement> iterator() {
                return new ChildElementsIterator();
            }
        };
    }

    /**
     * An iterator over the HtmlElement children.
     */
    protected class ChildElementsIterator implements Iterator<HtmlElement> {

        private HtmlElement nextElement_;

        /** Constructor. */
        protected ChildElementsIterator() {
            if (getFirstChild() != null) {
                if (getFirstChild() instanceof HtmlElement) {
                    nextElement_ = (HtmlElement) getFirstChild();
                }
                else {
                    setNextElement(getFirstChild());
                }
            }
        }

        /** @return is there a next one ? */
        public boolean hasNext() {
            return nextElement_ != null;
        }

        /** @return the next one */
        public HtmlElement next() {
            return nextElement();
        }

        /** Removes the current one. */
        public void remove() {
            if (nextElement_ == null) {
                throw new IllegalStateException();
            }
            final DomNode sibling = nextElement_.getPreviousSibling();
            if (sibling != null) {
                sibling.remove();
            }
        }

        /** @return the next element */
        public HtmlElement nextElement() {
            if (nextElement_ != null) {
                final HtmlElement result = nextElement_;
                setNextElement(nextElement_);
                return result;
            }
            throw new NoSuchElementException();
        }

        private void setNextElement(final DomNode node) {
            DomNode next = node.getNextSibling();
            while (next != null && !(next instanceof HtmlElement)) {
                next = next.getNextSibling();
            }
            nextElement_ = (HtmlElement) next;
        }
    }

    /**
     * Creates an attribute map as needed by HtmlElement. This is just used by the element factories.
     * @param attributeCount the initial number of attributes to be added to the map
     * @return the attribute map
     */
    @SuppressWarnings("unchecked")
    static Map<String, DomAttr> createAttributeMap(final int attributeCount) {
        return ListOrderedMap.decorate(new HashMap<String, DomAttr>(attributeCount)); // preserve insertion order
    }

    /**
     * Adds an attribute to the specified attribute map. This is just used by the element factories.
     * @param page the page which contains the attribute being created
     * @param attributeMap the attribute map where the attribute will be added
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the attribute
     * @param value the value of the attribute
     * @return the new attribute which was added to the specified attribute map
     */
    static DomAttr addAttributeToMap(final SgmlPage page, final Map<String, DomAttr> attributeMap,
            final String namespaceURI, final String qualifiedName, final String value) {
        final DomAttr newAttr = new DomAttr(page, namespaceURI, qualifiedName, value);
        attributeMap.put(qualifiedName, newAttr);
        return newAttr;
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * Return a Function to be executed when a given event occurs.
     * @param eventName Name of event such as "onclick" or "onblur", etc
     * @return a Rhino JavaScript executable Function, or <tt>null</tt> if no event handler has been defined
     */
    public final Function getEventHandler(final String eventName) {
        final HTMLElement jsObj = (HTMLElement) getScriptObject();
        return jsObj.getEventHandler(eventName);
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * Register a Function as an event handler.
     * @param eventName Name of event such as "onclick" or "onblur", etc
     * @param eventHandler a Rhino JavaScript executable Function
     */
    public final void setEventHandler(final String eventName, final Function eventHandler) {
        final HTMLElement jsObj = (HTMLElement) getScriptObject();
        jsObj.setEventHandler(eventName, eventHandler);
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * Register a snippet of JavaScript code as an event handler. The JavaScript code will
     * be wrapped inside a unique function declaration which provides one argument named
     * "event"
     * @param eventName Name of event such as "onclick" or "onblur", etc
     * @param jsSnippet executable JavaScript code
     */
    public final void setEventHandler(final String eventName, final String jsSnippet) {
        final BaseFunction function = new EventHandler(this, eventName, jsSnippet);
        setEventHandler(eventName, function);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created event handler " + function.getFunctionName()
                    + " for " + eventName + " on " + this);
        }
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     * Removes the specified event handler.
     * @param eventName Name of the event such as "onclick" or "onblur", etc
     */
    public final void removeEventHandler(final String eventName) {
        setEventHandler(eventName, (Function) null);
    }

    /**
     * Adds an HtmlAttributeChangeListener to the listener list.
     * The listener is registered for all attributes of this HtmlElement,
     * as well as descendant elements.
     *
     * @param listener the attribute change listener to be added
     * @see #removeHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    public void addHtmlAttributeChangeListener(final HtmlAttributeChangeListener listener) {
        WebAssert.notNull("listener", listener);
        synchronized (this) {
            if (attributeListeners_ == null) {
                attributeListeners_ = new ArrayList<HtmlAttributeChangeListener>();
            }
            attributeListeners_.add(listener);
        }
    }

    /**
     * Removes an HtmlAttributeChangeListener from the listener list.
     * This method should be used to remove HtmlAttributeChangeListener that were registered
     * for all attributes of this HtmlElement, as well as descendant elements.
     *
     * @param listener the attribute change listener to be removed
     * @see #addHtmlAttributeChangeListener(HtmlAttributeChangeListener)
     */
    public void removeHtmlAttributeChangeListener(final HtmlAttributeChangeListener listener) {
        WebAssert.notNull("listener", listener);
        synchronized (this) {
            if (attributeListeners_ != null) {
                attributeListeners_.remove(listener);
            }
        }
    }

    /**
     * Shortcut for {@link #fireEvent(Event)}.
     * @param eventType the event type (like "load", "click")
     * @return the execution result, or <code>null</code> if nothing is executed
     */
    public ScriptResult fireEvent(final String eventType) {
        return fireEvent(new Event(this, eventType));
    }

    /**
     * Fires the event on the element. Nothing is done if JavaScript is disabled.
     * @param event the event to fire
     * @return the execution result, or <tt>null</tt> if nothing is executed
     */
    public ScriptResult fireEvent(final Event event) {
        final WebClient client = getPage().getWebClient();
        if (!client.isJavaScriptEnabled()) {
            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Firing " + event);
        }
        final HTMLElement jsElt = (HTMLElement) getScriptObject();
        final ContextAction action = new ContextAction() {
            public Object run(final Context cx) {
                return jsElt.fireEvent(event);
            }
        };

        final ContextFactory cf = client.getJavaScriptEngine().getContextFactory();
        final ScriptResult result = (ScriptResult) cf.call(action);
        if (event.isAborted(result)) {
            preventDefault();
        }
        return result;
    }

    /**
     * This method is called if the current fired event is canceled by <tt>preventDefault()</tt> in FireFox,
     * or by returning <tt>false</tt> in Internet Explorer.
     *
     * The default implementation does nothing.
     */
    protected void preventDefault() {
        // Empty by default; override as needed.
    }

    /**
     * Simulates moving the mouse over this element, returning the page which this element's window contains
     * after the mouse move. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @return the page which this element's window contains after the mouse move
     */
    public Page mouseOver() {
        return mouseOver(false, false, false, MouseEvent.BUTTON_LEFT);
    }

    /**
     * Simulates moving the mouse over this element, returning the page which this element's window contains
     * after the mouse move. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse move
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse move
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse move
     * @param button the button code, must be {@link MouseEvent#BUTTON_LEFT}, {@link MouseEvent#BUTTON_MIDDLE}
     *        or {@link MouseEvent#BUTTON_RIGHT}
     * @return the page which this element's window contains after the mouse move
     */
    public Page mouseOver(final boolean shiftKey, final boolean ctrlKey, final boolean altKey, final int button) {
        return doMouseEvent(MouseEvent.TYPE_MOUSE_OVER, shiftKey, ctrlKey, altKey, button);
    }

    /**
     * Simulates moving the mouse over this element, returning the page which this element's window contains
     * after the mouse move. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @return the page which this element's window contains after the mouse move
     */
    public Page mouseMove() {
        return mouseMove(false, false, false, MouseEvent.BUTTON_LEFT);
    }

    /**
     * Simulates moving the mouse over this element, returning the page which this element's window contains
     * after the mouse move. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse move
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse move
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse move
     * @param button the button code, must be {@link MouseEvent#BUTTON_LEFT}, {@link MouseEvent#BUTTON_MIDDLE}
     *        or {@link MouseEvent#BUTTON_RIGHT}
     * @return the page which this element's window contains after the mouse move
     */
    public Page mouseMove(final boolean shiftKey, final boolean ctrlKey, final boolean altKey, final int button) {
        return doMouseEvent(MouseEvent.TYPE_MOUSE_MOVE, shiftKey, ctrlKey, altKey, button);
    }

    /**
     * Simulates moving the mouse out of this element, returning the page which this element's window contains
     * after the mouse move. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @return the page which this element's window contains after the mouse move
     */
    public Page mouseOut() {
        return mouseOut(false, false, false, MouseEvent.BUTTON_LEFT);
    }

    /**
     * Simulates moving the mouse out of this element, returning the page which this element's window contains
     * after the mouse move. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse move
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse move
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse move
     * @param button the button code, must be {@link MouseEvent#BUTTON_LEFT}, {@link MouseEvent#BUTTON_MIDDLE}
     *        or {@link MouseEvent#BUTTON_RIGHT}
     * @return the page which this element's window contains after the mouse move
     */
    public Page mouseOut(final boolean shiftKey, final boolean ctrlKey, final boolean altKey, final int button) {
        return doMouseEvent(MouseEvent.TYPE_MOUSE_OUT, shiftKey, ctrlKey, altKey, button);
    }

    /**
     * Simulates clicking the mouse on this element, returning the page which this element's window contains
     * after the mouse click. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @return the page which this element's window contains after the mouse click
     */
    public Page mouseDown() {
        return mouseDown(false, false, false, MouseEvent.BUTTON_LEFT);
    }

    /**
     * Simulates clicking the mouse on this element, returning the page which this element's window contains
     * after the mouse click. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse click
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse click
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse click
     * @param button the button code, must be {@link MouseEvent#BUTTON_LEFT}, {@link MouseEvent#BUTTON_MIDDLE}
     *        or {@link MouseEvent#BUTTON_RIGHT}
     * @return the page which this element's window contains after the mouse click
     */
    public Page mouseDown(final boolean shiftKey, final boolean ctrlKey, final boolean altKey, final int button) {
        return doMouseEvent(MouseEvent.TYPE_MOUSE_DOWN, shiftKey, ctrlKey, altKey, button);
    }

    /**
     * Simulates releasing the mouse click on this element, returning the page which this element's window contains
     * after the mouse click release. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @return the page which this element's window contains after the mouse click release
     */
    public Page mouseUp() {
        return mouseUp(false, false, false, MouseEvent.BUTTON_LEFT);
    }

    /**
     * Simulates releasing the mouse click on this element, returning the page which this element's window contains
     * after the mouse click release. The returned page may or may not be the same as the original page, depending
     * on JavaScript event handlers, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse click release
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse click release
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse click release
     * @param button the button code, must be {@link MouseEvent#BUTTON_LEFT}, {@link MouseEvent#BUTTON_MIDDLE}
     *        or {@link MouseEvent#BUTTON_RIGHT}
     * @return the page which this element's window contains after the mouse click release
     */
    public Page mouseUp(final boolean shiftKey, final boolean ctrlKey, final boolean altKey, final int button) {
        return doMouseEvent(MouseEvent.TYPE_MOUSE_UP, shiftKey, ctrlKey, altKey, button);
    }

    /**
     * Simulates right clicking the mouse on this element, returning the page which this element's window
     * contains after the mouse click. The returned page may or may not be the same as the original page,
     * depending on JavaScript event handlers, etc.
     *
     * @return the page which this element's window contains after the mouse click
     */
    public Page rightClick() {
        return rightClick(false, false, false);
    }

    /**
     * Simulates right clicking the mouse on this element, returning the page which this element's window
     * contains after the mouse click. The returned page may or may not be the same as the original page,
     * depending on JavaScript event handlers, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse click
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse click
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse click
     * @return the page which this element's window contains after the mouse click
     */
    public Page rightClick(final boolean shiftKey, final boolean ctrlKey, final boolean altKey) {
        final Page mouseDownPage = mouseDown(shiftKey, ctrlKey, altKey, MouseEvent.BUTTON_RIGHT);
        if (mouseDownPage != getPage()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("rightClick() is incomplete, as mouseDown() loaded a different page.");
            }
            return mouseDownPage;
        }
        final Page mouseUpPage = mouseUp(shiftKey, ctrlKey, altKey, MouseEvent.BUTTON_RIGHT);
        if (mouseUpPage != getPage()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("rightClick() is incomplete, as mouseUp() loaded a different page.");
            }
            return mouseUpPage;
        }
        return doMouseEvent(MouseEvent.TYPE_CONTEXT_MENU, shiftKey, ctrlKey, altKey, MouseEvent.BUTTON_RIGHT);
    }

    /**
     * Simulates the specified mouse event, returning the page which this element's window contains after the event.
     * The returned page may or may not be the same as the original page, depending on JavaScript event handlers, etc.
     *
     * @param eventType the mouse event type to simulate
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the mouse event
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the mouse event
     * @param altKey <tt>true</tt> if ALT is pressed during the mouse event
     * @param button the button code, must be {@link MouseEvent#BUTTON_LEFT}, {@link MouseEvent#BUTTON_MIDDLE}
     *        or {@link MouseEvent#BUTTON_RIGHT}
     * @return the page which this element's window contains after the event
     */
    private Page doMouseEvent(final String eventType, final boolean shiftKey, final boolean ctrlKey,
        final boolean altKey, final int button) {
        if (this instanceof DisabledElement && ((DisabledElement) this).isDisabled()) {
            return getPage();
        }
        final HtmlPage page = (HtmlPage) getPage();
        final Event event = new MouseEvent(this, eventType, shiftKey, ctrlKey, altKey, button);
        final ScriptResult scriptResult = fireEvent(event);
        final Page currentPage;
        if (scriptResult == null) {
            currentPage = page;
        }
        else {
            currentPage = scriptResult.getNewPage();
        }
        return currentPage;
    }

    /**
     * Removes focus from this element.
     */
    public void blur() {
        ((HtmlPage) getPage()).setFocusedElement(null);
    }

    /**
     * Sets the focus on this element.
     */
    public void focus() {
        ((HtmlPage) getPage()).setFocusedElement(this);
        final WebClient webClient = getPage().getWebClient();
        if (webClient.getBrowserVersion().isIE()) {
            final HTMLElement jsElt = (HTMLElement) getScriptObject();
            jsElt.jsxFunction_setActive();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkChildHierarchy(final Node childNode) throws DOMException {
        if (!((childNode instanceof Element) || (childNode instanceof Text)
            || (childNode instanceof Comment) || (childNode instanceof ProcessingInstruction)
            || (childNode instanceof CDATASection) || (childNode instanceof EntityReference))) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                "The Element may not have a child of this type: " + childNode.getNodeType());
        }
        super.checkChildHierarchy(childNode);
    }

    void setOwningForm(final HtmlForm form) {
        owningForm_ = form;
    }

    /**
     * Gets notified that it has lost the focus
     */
    void removeFocus() {
        // nothing
    }

    /**
     * Indicates if the attribute names are case sensitive.
     * @return <code>false</code>
     */
    @Override
    protected boolean isAttributeCaseSensitive() {
        return false;
    }

    /**
     * Simulates clicking on this element, returning the page in the window that has the focus
     * after the element has been clicked. Note that the returned page may or may not be the same
     * as the original page, depending on the type of element being clicked, the presence of JavaScript
     * action listeners, etc.
     *
     * @param <P> the page type
     * @return the page contained in the current window as returned by {@link WebClient#getCurrentWindow()}
     * @exception IOException if an IO error occurs
     */
    @SuppressWarnings("unchecked")
    public <P extends Page> P click() throws IOException {
        return (P) click(false, false, false);
    }

    /**
     * Simulates clicking on this element, returning the page in the window that has the focus
     * after the element has been clicked. Note that the returned page may or may not be the same
     * as the original page, depending on the type of element being clicked, the presence of JavaScript
     * action listeners, etc.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the click
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the click
     * @param altKey <tt>true</tt> if ALT is pressed during the click
     * @param <P> the page type
     * @return the page contained in the current window as returned by {@link WebClient#getCurrentWindow()}
     * @exception IOException if an IO error occurs
     */
    @SuppressWarnings("unchecked")
    public <P extends Page> P click(final boolean shiftKey, final boolean ctrlKey, final boolean altKey)
        throws IOException {
        if (this instanceof DisabledElement && ((DisabledElement) this).isDisabled()) {
            return (P) getPage();
        }

        mouseDown(shiftKey, ctrlKey, altKey, MouseEvent.BUTTON_LEFT);
        if (this instanceof SubmittableElement) {
            ((HtmlPage) getPage()).setFocusedElement(this);
        }
        mouseUp(shiftKey, ctrlKey, altKey, MouseEvent.BUTTON_LEFT);

        final Event event = new MouseEvent(this, MouseEvent.TYPE_CLICK, shiftKey, ctrlKey, altKey,
                MouseEvent.BUTTON_LEFT);
        return (P) click(event);
    }

    /**
     * <span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span><br/>
     *
     * Simulates clicking on this element, returning the page in the window that has the focus
     * after the element has been clicked. Note that the returned page may or may not be the same
     * as the original page, depending on the type of element being clicked, the presence of JavaScript
     * action listeners, etc.
     *
     * @param event the click event used
     * @param <P> the page type
     * @return the page contained in the current window as returned by {@link WebClient#getCurrentWindow()}
     * @exception IOException if an IO error occurs
     */
    @SuppressWarnings("unchecked")
    public <P extends Page> P click(final Event event) throws IOException {
        if (this instanceof DisabledElement && ((DisabledElement) this).isDisabled()) {
            return (P) getPage();
        }

        final SgmlPage page = getPage();

        boolean stateUpdated = false;
        if (isStateUpdateFirst()) {
            doClickAction(page);
            stateUpdated = true;
        }
        final ScriptResult scriptResult = fireEvent(event);
        final Page currentPage;
        if (scriptResult == null) {
            currentPage = page;
        }
        else {
            currentPage = scriptResult.getNewPage();
        }

        if (!stateUpdated && !event.isAborted(scriptResult)) {
            doClickAction(currentPage);
        }
        return (P) getPage().getWebClient().getCurrentWindow().getEnclosedPage();
    }

    /**
     * Simulates double-clicking on this element, returning the page in the window that has the focus
     * after the element has been clicked. Note that the returned page may or may not be the same
     * as the original page, depending on the type of element being clicked, the presence of JavaScript
     * action listeners, etc. Note also that {@link #click()} is automatically called first.
     *
     * @param <P> the page type
     * @return the page that occupies this element's window after the element has been double-clicked
     * @exception IOException if an IO error occurs
     */
    @SuppressWarnings("unchecked")
    public <P extends Page> P dblClick() throws IOException {
        return (P) dblClick(false, false, false);
    }

    /**
     * Simulates double-clicking on this element, returning the page in the window that has the focus
     * after the element has been clicked. Note that the returned page may or may not be the same
     * as the original page, depending on the type of element being clicked, the presence of JavaScript
     * action listeners, etc. Note also that {@link #click(boolean, boolean, boolean)} is automatically
     * called first.
     *
     * @param shiftKey <tt>true</tt> if SHIFT is pressed during the double-click
     * @param ctrlKey <tt>true</tt> if CTRL is pressed during the double-click
     * @param altKey <tt>true</tt> if ALT is pressed during the double-click
     * @param <P> the page type
     * @return the page that occupies this element's window after the element has been double-clicked
     * @exception IOException if an IO error occurs
     */
    @SuppressWarnings("unchecked")
    public <P extends Page> P dblClick(final boolean shiftKey, final boolean ctrlKey, final boolean altKey)
        throws IOException {
        if (this instanceof DisabledElement && ((DisabledElement) this).isDisabled()) {
            return (P) getPage();
        }

        //call click event first
        final Page clickPage = click(shiftKey, ctrlKey, altKey);
        if (clickPage != getPage()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dblClick() is ignored, as click() loaded a different page.");
            }
            return (P) clickPage;
        }

        final Event event = new MouseEvent(this, MouseEvent.TYPE_DBL_CLICK, shiftKey, ctrlKey, altKey,
                MouseEvent.BUTTON_LEFT);
        final ScriptResult scriptResult = fireEvent(event);
        if (scriptResult == null) {
            return (P) clickPage;
        }
        return (P) scriptResult.getNewPage();
    }

    /**
     * <p>This method will be called if there either wasn't an <tt>onclick</tt> handler, or if
     * there was one, but the result of that handler wasn't <tt>false</tt>. This is the default
     * behavior of clicking the element.<p>
     *
     * <p>The default implementation returns the current page. Subclasses requiring different
     * behavior (like {@link HtmlSubmitInput}) will override this method.</p>
     *
     * @param defaultPage the default page to return if the action does not load a new page
     * @return the page that is currently loaded after execution of this method
     * @throws IOException if an IO error occurs
     */
    protected Page doClickAction(final Page defaultPage) throws IOException {
        return defaultPage;
    }

    /**
     * Returns the value of the attribute "lang". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "lang" or an empty string if that attribute isn't defined
     */
    public final String getLangAttribute() {
        return getAttribute("lang");
    }

    /**
     * Returns the value of the attribute "xml:lang". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "xml:lang" or an empty string if that attribute isn't defined
     */
    public final String getXmlLangAttribute() {
        return getAttribute("xml:lang");
    }

    /**
     * Returns the value of the attribute "dir". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "dir" or an empty string if that attribute isn't defined
     */
    public final String getTextDirectionAttribute() {
        return getAttribute("dir");
    }

    /**
     * Returns the value of the attribute "onclick". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onclick" or an empty string if that attribute isn't defined
     */
    public final String getOnClickAttribute() {
        return getAttribute("onclick");
    }

    /**
     * Returns the value of the attribute "ondblclick". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "ondblclick" or an empty string if that attribute isn't defined
     */
    public final String getOnDblClickAttribute() {
        return getAttribute("ondblclick");
    }

    /**
     * Returns the value of the attribute "onmousedown". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onmousedown" or an empty string if that attribute isn't defined
     */
    public final String getOnMouseDownAttribute() {
        return getAttribute("onmousedown");
    }

    /**
     * Returns the value of the attribute "onmouseup". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onmouseup" or an empty string if that attribute isn't defined
     */
    public final String getOnMouseUpAttribute() {
        return getAttribute("onmouseup");
    }

    /**
     * Returns the value of the attribute "onmouseover". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onmouseover" or an empty string if that attribute isn't defined
     */
    public final String getOnMouseOverAttribute() {
        return getAttribute("onmouseover");
    }

    /**
     * Returns the value of the attribute "onmousemove". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onmousemove" or an empty string if that attribute isn't defined
     */
    public final String getOnMouseMoveAttribute() {
        return getAttribute("onmousemove");
    }

    /**
     * Returns the value of the attribute "onmouseout". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onmouseout" or an empty string if that attribute isn't defined
     */
    public final String getOnMouseOutAttribute() {
        return getAttribute("onmouseout");
    }

    /**
     * Returns the value of the attribute "onkeypress". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onkeypress" or an empty string if that attribute isn't defined
     */
    public final String getOnKeyPressAttribute() {
        return getAttribute("onkeypress");
    }

    /**
     * Returns the value of the attribute "onkeydown". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onkeydown" or an empty string if that attribute isn't defined
     */
    public final String getOnKeyDownAttribute() {
        return getAttribute("onkeydown");
    }

    /**
     * Returns the value of the attribute "onkeyup". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "onkeyup" or an empty string if that attribute isn't defined
     */
    public final String getOnKeyUpAttribute() {
        return getAttribute("onkeyup");
    }

    /**
     * Returns <tt>true</tt> if state updates should be done before onclick event handling. This method
     * returns <tt>false</tt> by default, and is expected to be overridden to return <tt>true</tt> by
     * derived classes like {@link HtmlCheckBoxInput}.
     * @return <tt>true</tt> if state updates should be done before onclick event handling
     */
    protected boolean isStateUpdateFirst() {
        return false;
    }
}
