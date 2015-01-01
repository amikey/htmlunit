/*
 * Copyright (c) 2002-2015 Gargoyle Software Inc.
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
package com.gargoylesoftware.htmlunit.javascript.host.html;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.SimpleWebTestCase;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * Tests for {@link HTMLInputElement} and buttons.
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Marc Guillemot
 * @author Chris Erskine
 * @author Ahmed Ashour
 */
@RunWith(BrowserRunner.class)
public class HTMLInputElement2Test extends SimpleWebTestCase {

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = { "hello", "me te" },
            IE8 = { "input.setSelectionRange not available", "" })
    public void selectionRange() throws Exception {
        final String html
            = "<html><head><title>foo</title><script>\n"
            + "function test() {\n"
            + "    var input = document.getElementById('myInput');\n"
            + "    if (!input.setSelectionRange) { alert('input.setSelectionRange not available'); return };\n"
            + "    input.setSelectionRange(2, 7);\n"
            + "    alert('hello');"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='myInput' value='some test'>\n"
            + "</body></html>";

        final String[] expected = getExpectedAlerts();
        setExpectedAlerts(new String[] {expected[0]});
        final HtmlPage page = loadPageWithAlerts(html);
        final HtmlTextInput input = page.getHtmlElementById("myInput");

        assertEquals(expected[1], input.getSelectedText());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    @Alerts("initial")
    public void focus() throws Exception {
        final String html = "<html><body>\n"
            + "<iframe name='theFrame' src='" + URL_SECOND + "'></iframe>\n"
            + "</body></html>";
        final String frame = "<html><body>\n"
            + "<input id='input' value='initial' onfocus='alert(this.value)'>\n"
            + "<div id='div'>click me</div>\n"
            + "</body></html>";

        getMockWebConnection().setResponse(URL_SECOND, frame);

        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(html, collectedAlerts);
        final HtmlPage framePage = (HtmlPage) page.getFrames().get(0).getEnclosedPage();

        framePage.getHtmlElementById("input").type("foo");

        framePage.getHtmlElementById("div").click();
        assertEquals(getExpectedAlerts(), collectedAlerts);
    }
}
