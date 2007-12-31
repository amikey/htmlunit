/*
 * Copyright (c) 2002-2008 Gargoyle Software Inc. All rights reserved.
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
package com.gargoylesoftware.htmlunit.html;

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.WebTestCase;

/**
 * Tests for elements with onblur and onfocus attributes.
 *
 * @version $Revision$
 * @author David D. Kilzer
 * @author Marc Guillemot
 */
public class FocusableElementTest extends WebTestCase {

    private static final String COMMON_ID = " id='focusId'";
    private static final String COMMON_EVENTS = " onblur=\"alert('foo onblur')\" onfocus=\"alert('foo onfocus')\"";
    private static final String COMMON_ATTRIBUTES = COMMON_ID + COMMON_EVENTS;

    /**
     * Create an instance.
     * @param name The name of the test.
     */
    public FocusableElementTest(final String name) {
        super(name);
    }

    /**
     * Full page driver for onblur and onfocus tests.
     *
     * @param htmlContent HTML fragment for body of page with a focusable element identified by a focusId ID attribute.
     * Must have onfocus event that raises an alert of "foo1 onfocus" and an onblur event that raises an alert of "foo
     * onblur" on the second element.
     * @throws Exception if the test fails
     */
    private void onClickPageTest(final String htmlContent) throws Exception {
        final List collectedAlerts = new ArrayList();
        final HtmlPage page = loadPage(htmlContent, collectedAlerts);
        final HtmlElement element = (HtmlElement) page.getHtmlElementById("focusId");

        element.focus();
        element.blur();

        final String[] expectedAlerts = {"foo onfocus", "foo onblur", "foo onfocus", "foo onblur"};
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Body driver for onblur and onfocus tests.
     *
     * @param htmlBodyContent HTML tag name for simple tag with text body.
     * @throws Exception if the test fails
     */
    private void onClickBodyTest(final String htmlBodyContent) throws Exception {
        onClickPageTest("<html><head><title>foo</title></head><body>\n"
                + htmlBodyContent
                + "<script type=\"text/javascript\" language=\"JavaScript\">\n"
                + "document.getElementById('focusId').focus();\n"
                + "document.getElementById('focusId').blur();\n"
                + "</script></body></html>");
    }

    /**
     * Simple tag name driver for onblur and onfocus tests.
     *
     * @param tagName HTML tag name for simple tag with text body.
     * @param tagAttributes Additional attribute(s) to add to the generated tag.
     * @throws Exception if the test fails
     */
    private void onClickSimpleTest(final String tagName, final String tagAttributes) throws Exception {
        onClickBodyTest("<" + tagName + COMMON_ATTRIBUTES
                + " " + tagAttributes + ">Text</" + tagName + ">");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of anchor element.
     *
     * @throws Exception if the test fails
     */
    public void testAnchor_onblur_onfocus() throws Exception {
        onClickSimpleTest("a", "href=\".\"");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of area element.
     *
     * @throws Exception if the test fails
     */
    public void testArea_onblur_onfocus() throws Exception {
        onClickBodyTest("<map><area " + COMMON_ATTRIBUTES
                + " shape=\"rect\" coords=\"0,0,1,1\" href=\".\">\n"
                + "</area></map>");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of button element.
     *
     * @throws Exception if the test fails
     */
    public void testButton_onblur_onfocus() throws Exception {
        onClickSimpleTest("button", "name=\"foo\" value=\"bar\" type=\"button\"");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of label element surrounding input element.
     *
     * @throws Exception if the test fails
     */
    public void testLabelContainsInput_onblur_onfocus() throws Exception {
        onClickBodyTest("<form><label " + COMMON_ID + ">"
                + "Foo<input type=\"text\" name=\"foo\"" + COMMON_EVENTS + "></label></form>\n");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of label element referencing an input element.
     *
     * @throws Exception if the test fails
     */
    public void testLabelReferencesInput_onblur_onfocus() throws Exception {
        onClickBodyTest("<form><label " + COMMON_ID + " for=\"fooId\">Foo</label>\n"
                + "<input type=\"text\" name=\"foo\" id=\"fooId\"" + COMMON_EVENTS + "></form>\n");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of select element.
     *
     * @throws Exception if the test fails
     */
    public void testSelect_onblur_onfocus() throws Exception {
        onClickBodyTest("<form><select " + COMMON_ATTRIBUTES + "><option>1</option></select></form>\n");
    }

    /**
     * Test onblur and onfocus handlers and blur() and focus() methods of textarea element.
     *
     * @throws Exception if the test fails
     */
    public void testTextarea_onblur_onfocus() throws Exception {
        onClickBodyTest("<form><textarea " + COMMON_ATTRIBUTES + ">Text</textarea></form>\n");
    }
    
    /**
     * Regression test for bug
     * https://sourceforge.net/tracker/?func=detail&atid=448266&aid=1161705&group_id=47038
     * @throws Exception if the test fails
     */
    public void testOnBlurWith2Pages() throws Exception {
        final String html =
            "<html>\n"
            + "<head>\n"
            + "<script>\n"
            + "var bCalled = false;\n"
            + "function testOnBlur()\n"
            + "{\n"
            + "  if (bCalled)\n"
            + "    throw 'problem!'; // to get the error immediately rather than an infinite loop\n"
            + "  bCalled = true;\n"
            + "  document.getElementById('text2').focus();\n"
            + "}\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onLoad='document.getElementById(\"text1\").focus()'>\n"
            + "<input type='text' id='text1' onblur='testOnBlur()'>\n"
            + "<input type='text' id='text2'>\n"
            + "<a href='foo'>this page again</a>\n"
            + "</body></html>";

        final HtmlPage page = loadPage(html);
        ((HtmlAnchor) page.getAnchors().get(0)).click();
    }
    
    /**
     * Test focus on all types of elements
     * @throws Exception if the test fails
     */
    public void testOnAllElements() throws Exception {
        testHTMLFile("FocusableElementTest_onAllElements.html");
    }
}
