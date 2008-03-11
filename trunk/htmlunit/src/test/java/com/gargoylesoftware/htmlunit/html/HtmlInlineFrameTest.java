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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebTestCase;

/**
 * Unit tests for {@link HtmlInlineFrame}.
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Ahmed Ashour
 * @author Marc Guillemot
 */
public class HtmlInlineFrameTest extends WebTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testSetSrcAttribute() throws Exception {
        final String firstContent
            = "<html><head><title>First</title></head><body>\n"
            + "<iframe id='iframe1' src='" + URL_SECOND + "'>\n"
            + "</body></html>";
        final String secondContent = "<html><head><title>Second</title></head><body></body></html>";
        final String thirdContent = "<html><head><title>Third</title></head><body></body></html>";
        final WebClient client = new WebClient();

        final MockWebConnection webConnection = new MockWebConnection(client);
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        webConnection.setResponse(URL_THIRD, thirdContent);

        client.setWebConnection(webConnection);

        final HtmlPage page = (HtmlPage) client.getPage(URL_FIRST);
        assertEquals("First", page.getTitleText());

        final HtmlInlineFrame iframe = (HtmlInlineFrame) page.getHtmlElementById("iframe1");
        assertEquals(URL_SECOND.toExternalForm(), iframe.getSrcAttribute());
        assertEquals("Second", ((HtmlPage) iframe.getEnclosedPage()).getTitleText());

        iframe.setSrcAttribute(URL_THIRD.toExternalForm());
        assertEquals(URL_THIRD.toExternalForm(), iframe.getSrcAttribute());
        assertEquals("Third", ((HtmlPage) iframe.getEnclosedPage()).getTitleText());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testSetSrcAttributeWithWhiteSpaces() throws Exception {
        final String firstContent
            = "<html><head><title>First</title></head><body>\n"
            + "<iframe id='iframe1' src='\n" + URL_SECOND + "\n'>\n"
            + "</body></html>";
        final String secondContent = "<html><head><title>Second</title></head><body></body></html>";
        final String thirdContent = "<html><head><title>Third</title></head><body></body></html>";
        final WebClient client = new WebClient();

        final MockWebConnection webConnection = new MockWebConnection(client);
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        webConnection.setResponse(URL_THIRD, thirdContent);

        client.setWebConnection(webConnection);

        final HtmlPage page = (HtmlPage) client.getPage(URL_FIRST);
        assertEquals("First", page.getTitleText());

        final HtmlInlineFrame iframe = (HtmlInlineFrame) page.getHtmlElementById("iframe1");
        assertEquals(URL_SECOND.toExternalForm(), iframe.getSrcAttribute());
        assertEquals("Second", ((HtmlPage) iframe.getEnclosedPage()).getTitleText());

        iframe.setSrcAttribute(URL_THIRD.toExternalForm());
        assertEquals(URL_THIRD.toExternalForm(), iframe.getSrcAttribute());
        assertEquals("Third", ((HtmlPage) iframe.getEnclosedPage()).getTitleText());
    }

    /**
     * Tests that a recursive src attribute (i.e. src="#xyz") doesn't result in an
     * infinite loop (bug 1699125).
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testRecursiveSrcAttribute() throws Exception {
        final String html = "<html><body><iframe id='a' src='#abc'></body></html>";
        final HtmlPage page = loadPage(html);
        final HtmlInlineFrame iframe = (HtmlInlineFrame) page.getHtmlElementById("a");
        assertNotNull(iframe.getEnclosedPage());
    }

    /**
     * Tests that a recursive src is prevented.
     * @throws Exception if an error occurs
     */
    @Test
    public void testRecursiveNestedFrames() throws Exception {
        final String firstContent
            = "<html><head><title>First</title></head><body>\n"
            + "<iframe id='iframe1' src='" + URL_SECOND + "'>\n"
            + "</body></html>";
        final String secondContent = "<html><head><title>Second</title></head>\n"
            + "<body><iframe id='iframe2_1' src='" + URL_FIRST + "'></iframe></body></html>";
        final WebClient client = new WebClient();
    
        final MockWebConnection webConnection = new MockWebConnection(client);
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
    
        client.setWebConnection(webConnection);
    
        final HtmlPage page = (HtmlPage) client.getPage(URL_FIRST);
        assertEquals("First", page.getTitleText());
    
        final HtmlInlineFrame iframe = (HtmlInlineFrame) page.getHtmlElementById("iframe1");
        assertEquals(URL_SECOND.toExternalForm(), iframe.getSrcAttribute());
        final HtmlPage iframePage = (HtmlPage) iframe.getEnclosedPage();
        assertEquals("Second", iframePage.getTitleText());
    
        // the nested frame should not have been loaded
        final HtmlInlineFrame iframeIn2 = (HtmlInlineFrame) iframePage.getHtmlElementById("iframe2_1");
        assertEquals(URL_FIRST.toExternalForm(), iframeIn2.getSrcAttribute());
        assertEquals("about:blank", iframeIn2.getEnclosedPage().getWebResponse().getUrl());
    }

    /**
     * Tests that an invalid src attribute (i.e. src="foo://bar") doesn't result
     * in a NPE (bug 1699119).
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void testInvalidSrcAttribute() throws Exception {
        final String html = "<html><body><iframe id='a' src='foo://bar'></body></html>";
        final HtmlPage page = loadPage(html);
        final HtmlInlineFrame iframe = (HtmlInlineFrame) page.getHtmlElementById("a");
        assertNotNull(iframe.getEnclosedPage());
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void testSetSrcAttribute_ViaJavaScript() throws Exception {
        final String firstContent
            = "<html><head><title>First</title></head><body>\n"
            + "<iframe id='iframe1' src='" + URL_SECOND + "'></iframe>\n"
            + "<script type='text/javascript'>document.getElementById('iframe1').src = '" + URL_THIRD + "';\n"
            + "</script></body></html>";
        final String secondContent = "<html><head><title>Second</title></head><body></body></html>";
        final String thirdContent = "<html><head><title>Third</title></head><body></body></html>";
        final WebClient client = new WebClient();

        final MockWebConnection webConnection = new MockWebConnection(client);
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        webConnection.setResponse(URL_THIRD, thirdContent);

        client.setWebConnection(webConnection);

        final HtmlPage page = (HtmlPage) client.getPage(URL_FIRST);
        assertEquals("First", page.getTitleText());

        final HtmlInlineFrame iframe = (HtmlInlineFrame) page.getHtmlElementById("iframe1");
        assertEquals(URL_THIRD.toExternalForm(), iframe.getSrcAttribute());
        assertEquals("Third", ((HtmlPage) iframe.getEnclosedPage()).getTitleText());
    }

    /**
     *
     * @throws Exception if the test fails
     */
    @Test
    public void testScriptUnderIFrame() throws Exception {
        final String firstContent
            = "<html><body>\n"
            + "<iframe src='" + URL_SECOND + "'>\n"
            + "  <div><script>alert(1);</script></div>\n"
            + "  <script src='" + URL_THIRD + "'></script>\n"
            + "</iframe>\n"
            + "</body></html>";
        final String secondContent
            = "<html><body><script>alert(2);</script></body></html>";
        final String thirdContent
            = "alert('3');";
        final WebClient client = new WebClient();

        final MockWebConnection webConnection = new MockWebConnection(client);
        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        webConnection.setResponse(URL_THIRD, thirdContent, "text/javascript");

        client.setWebConnection(webConnection);
        
        final String[] expectedAlerts = {"2"};
        final List<String> collectedAlerts = new ArrayList<String>();
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
        
        client.getPage(URL_FIRST);
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails.
     */
    @Test
    public void testSimpleScriptable() throws Exception {
        final String html = "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    alert(document.getElementById('myId'));\n"
            + "  }\n"
            + "</script>\n"
            + "</head><body onload='test()'>\n"
            + "  <iframe id='myId'>\n"
            + "</body></html>";

        final String[] expectedAlerts = {"[object HTMLIFrameElement]"};
        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(BrowserVersion.FIREFOX_2, html, collectedAlerts);
        assertTrue(HtmlInlineFrame.class.isInstance(page.getHtmlElementById("myId")));
        assertEquals(expectedAlerts, collectedAlerts);
    }
}
