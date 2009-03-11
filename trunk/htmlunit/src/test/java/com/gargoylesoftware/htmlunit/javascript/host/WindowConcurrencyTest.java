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
package com.gargoylesoftware.htmlunit.javascript.host;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.BaseFunction;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebTestCase;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;

/**
 * Tests for {@link Window} that use background jobs.
 *
 * @version $Revision$
 * @author Brad Clarke
 */
public class WindowConcurrencyTest extends WebTestCase {

    private long startTime_;

    private void startTimedTest() {
        startTime_ = System.currentTimeMillis();
    }

    private void assertMaxTestRunTime(final long maxRunTimeMilliseconds) {
        final long endTime = System.currentTimeMillis();
        final long runTime = endTime - startTime_;
        assertTrue("\nTest took too long to run and results may not be accurate. Please try again. "
            + "\n  Actual Run Time: "
            + runTime
            + "\n  Max Run Time: "
            + maxRunTimeMilliseconds, runTime < maxRunTimeMilliseconds);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void setTimeout() throws Exception {
        final String content
            = "<html><body><script language='JavaScript'>window.setTimeout('alert(\"Yo!\")',1);\n"
            + "</script></body></html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(content, collectedAlerts);
        final JavaScriptJobManager mgr = page.getEnclosingWindow().getJobManager();
        assertTrue("thread failed to stop in 1 second", mgr.waitForAllJobsToFinish(1000));
        assertEquals(new String[] {"Yo!"}, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void setTimeoutByReference() throws Exception {
        final String content = "<html><body><script language='JavaScript'>\n"
            + "function doTimeout() {alert('Yo!');}\n"
            + "window.setTimeout(doTimeout,1);\n"
            + "</script></body></html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(content, collectedAlerts);
        final JavaScriptJobManager mgr = page.getEnclosingWindow().getJobManager();
        assertTrue("thread failed to stop in 1 second", mgr.waitForAllJobsToFinish(1000));
        assertEquals(new String[] {"Yo!"}, collectedAlerts);
    }

    /**
     * Just tests that setting and clearing an interval doesn't throw an exception.
     * @throws Exception if the test fails
     */
    @Test
    public void setAndClearInterval() throws Exception {
        final String content
            = "<html><body>\n"
            + "<script>\n"
            + "window.setInterval('alert(\"Yo!\")', 500);\n"
            + "function foo() { alert('Yo2'); }\n"
            + "var i = window.setInterval(foo, 500);\n"
            + "window.clearInterval(i);\n"
            + "</script></body></html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        loadPage(content, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void setIntervalFunctionReference() throws Exception {
        final String content = "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    var threadID;\n"
            + "    function test() {\n"
            + "      threadID = setInterval(doAlert, 100);\n"
            + "    }\n"
            + "    var iterationNumber=0;\n"
            + "    function doAlert() {\n"
            + "      alert('blah');\n"
            + "      if (++iterationNumber >= 3) {\n"
            + "        clearInterval(threadID);\n"
            + "      }\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body>\n"
            + "</html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(content, collectedAlerts);
        final JavaScriptJobManager jobManager = page.getEnclosingWindow().getJobManager();
        jobManager.waitForAllJobsToFinish(1000);
        assertEquals(0, jobManager.getJobCount());
        assertEquals(Collections.nCopies(3, "blah"), collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void clearInterval() throws Exception {
        final String html = "<html><body onload='test()'><script>\n"
            + "  var count;\n"
            + "  var id;\n"
            + "  function test() {\n"
            + "    count = 0;\n"
            + "    id = setInterval(callback, 100);\n"
            + "  };\n"
            + "  function callback() {\n"
            + "    count++;\n"
            + "    clearInterval(id);\n"
            + "    // Give the callback time to show its ugly face.\n"
            + "    // If it fires between now and then, we'll know.\n"
            + "    setTimeout('alert(count)', 500);\n"
            + "  }\n"
            + "</script></body></html>";
        final String[] expected = {"1"};
        final List<String> actual = Collections.synchronizedList(new ArrayList<String>());
        startTimedTest();
        final HtmlPage page = loadPage(html, actual);
        final JavaScriptJobManager jobManager = page.getEnclosingWindow().getJobManager();
        jobManager.waitForAllJobsToFinish(10000);
        assertEquals(0, jobManager.getJobCount());
        assertEquals(expected, actual);
        assertMaxTestRunTime(5000);
    }

    /**
     * Test that a script started by a timer is stopped if the page that started it
     * is not loaded anymore.
     * @throws Exception if the test fails
     */
    @Test
    public void setTimeoutStopped() throws Exception {
        final String firstContent
            = "<html><head>\n"
            + "<script language='JavaScript'>window.setTimeout('alert(\"Yo!\")', 10000);</script>\n"
            + "</head><body onload='document.location.replace(\"" + URL_SECOND + "\")'></body></html>";
        final String secondContent = "<html><head><title>Second</title></head><body></body></html>";

        final WebClient webClient = new WebClient();
        final MockWebConnection webConnection = new MockWebConnection();
        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());

        webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        webConnection.setResponse(URL_FIRST, firstContent);
        webConnection.setResponse(URL_SECOND, secondContent);
        webClient.setWebConnection(webConnection);

        final HtmlPage page = webClient.getPage(URL_FIRST);
        page.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(2000);
        assertEquals("Second", page.getTitleText());
        Assert.assertEquals("no thread should be running",
                0, page.getEnclosingWindow().getJobManager().getJobCount());
        assertEquals(Collections.EMPTY_LIST, collectedAlerts);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void clearTimeout() throws Exception {
        final String content =
              "<html>\n"
            + "<head>\n"
            + "  <title>test</title>\n"
            + "  <script>\n"
            + "    function test() {\n"
            + "      var id = setTimeout('doAlert()', 2000);\n"
            + "      clearTimeout(id);\n"
            + "    }\n"
            + "    function doAlert() {\n"
            + "      alert('blah');\n"
            + "    }\n"
            + "  </script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body>\n"
            + "</html>";
        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(content, collectedAlerts);
        page.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(2000);
        assertEquals(Collections.EMPTY_LIST, collectedAlerts);
    }

    /**
     * Verifies that calling clearTimeout() on a callback which has already fired
     * does not affect said callback.
     * @throws Exception if the test fails
     */
    @Test
    public void clearTimeout_DoesNotStopExecutingCallback() throws Exception {
        final String html = "<html><body onload='test()'><script>\n"
            + "  var id;\n"
            + "  function test() {\n"
            + "    id = setTimeout(callback, 1);\n"
            + "  };\n"
            + "  function callback() {\n"
            + "    alert(id != 0);\n"
            + "    clearTimeout(id);\n"
            + "    // Make sure we weren't stopped.\n"
            + "    alert('completed');\n"
            + "  }\n"
            + "</script><div id='a'></div></body></html>";
        final String[] expected = {"true", "completed"};
        final List<String> actual = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(html, actual);
        page.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(5000);
        assertEquals(expected, actual);
    }

    /**
     * Tests that nested setTimeouts that are deeper than Thread.MAX_PRIORITY
     * do not cause an exception.
     * @throws Exception if the test fails
     */
    @Test
    public void nestedSetTimeoutAboveMaxPriority() throws Exception {
        final int max = Thread.MAX_PRIORITY + 1;
        final String content = "<html><body><script language='JavaScript'>\n"
            + "var depth = 0;\n"
            + "var maxdepth = " + max + ";\n"
            + "function addAnother() {\n"
            + "  if (depth < maxdepth) {\n"
            + "    window.alert('ping');\n"
            + "    depth++;\n"
            + "    window.setTimeout('addAnother();', 1);\n"
            + "  }\n"
            + "}\n"
            + "addAnother();\n"
            + "</script></body></html>";

        final List<String> collectedAlerts = Collections.synchronizedList(new ArrayList<String>());
        final HtmlPage page = loadPage(content, collectedAlerts);
        assertTrue("threads did not stop in time", page.getEnclosingWindow()
                .getJobManager().waitForAllJobsToFinish((max + 1) * 1000));
        assertEquals(Collections.nCopies(max, "ping"), collectedAlerts);
    }

    /**
     * Regression test for bug #2093370 with clearInterval.
     * @see <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=2093370&group_id=47038&atid=448266">
     * bug details</a>
     * @throws Exception if the test fails
     */
    @Test
    public void clearInterval_threadInterrupt() throws Exception {
        doTestClearX_threadInterrupt("Interval");
    }

    /**
     * Regression test for bug #2093370 with clearTimeout.
     * @see <a href="http://sourceforge.net/tracker/index.php?func=detail&aid=2093370&group_id=47038&atid=448266">
     * bug details</a>
     * @throws Exception if the test fails
     */
    @Test
    public void clearTimeout_threadInterrupt() throws Exception {
        doTestClearX_threadInterrupt("Timeout");
    }

    private void doTestClearX_threadInterrupt(final String x) throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function f() {\n"
            + "    alert('started');\n"
            + "    clear" + x + "(window.timeoutId);\n"
            + "    mySpecialFunction();\n"
            + "    alert('finished');\n"
            + "  }\n"
            + "  function test() {\n"
            + "    window.timeoutId = set" + x + "(f, 10);\n"
            + "  }\n"
            + "</script></head><body>\n"
            + "<span id='clickMe' onclick='test()'>click me</span>"
            + "</body></html>";

        final String[] expectedAlerts = {"started", "finished"};

        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadPage(html, collectedAlerts);
        final Function mySpecialFunction = new BaseFunction() {
            private static final long serialVersionUID = -2445994102698852899L;

            @Override
            public Object call(final Context cx, final Scriptable scope,
                    final Scriptable thisObj, final Object[] args) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new RuntimeException("My thread is already interrupted");
                }
                return null;
            }
        };
        final Window window = (Window) page.getEnclosingWindow().getScriptObject();
        ScriptableObject.putProperty(window, "mySpecialFunction", mySpecialFunction);
        page.<HtmlElement>getHtmlElementById("clickMe").click();
        page.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(5000);
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Verifies that when all windows are closed, background JS jobs are stopped (see bug 2127419).
     * @throws Exception if the test fails
     */
    @Test
    public void verifyCloseAllWindowsStopsJavaScript() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function f() {\n"
            + "    alert('Oh no!');\n"
            + "  }\n"
            + "  function test() {\n"
            + "    window.timeoutId = setInterval(f, 1000);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();

        final WebClient client = new WebClient();
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final MockWebConnection webConnection = new MockWebConnection();
        webConnection.setDefaultResponse(html);
        client.setWebConnection(webConnection);

        final HtmlPage page = client.getPage(URL_FIRST);
        client.closeAllWindows();
        page.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(5000);
        assertEquals(0, collectedAlerts.size());
    }

    /**
     * Verifies that when you go to a new page, background JS jobs are stopped (see bug 2127419).
     * @throws Exception if the test fails
     */
    @Test
    public void verifyGoingToNewPageStopsJavaScript() throws Exception {
        final String html1 = "<html><head><title>foo</title><script>\n"
            + "  function f() {\n"
            + "    alert('Oh no!');\n"
            + "  }\n"
            + "  function test() {\n"
            + "    window.timeoutId = setInterval(f, 1000);\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";
        final String html2 = "<html></html>";

        final List<String> collectedAlerts = new ArrayList<String>();

        final WebClient client = new WebClient();
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final MockWebConnection conn = new MockWebConnection();
        conn.setResponse(URL_FIRST, html1);
        conn.setResponse(URL_SECOND, html2);
        client.setWebConnection(conn);

        final HtmlPage page1 = client.getPage(URL_FIRST);
        final HtmlPage page2 = client.getPage(URL_SECOND);

        page1.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(5000);
        page2.getEnclosingWindow().getJobManager().waitForAllJobsToFinish(5000);

        assertEquals(0, collectedAlerts.size());
    }

    /**
     * Our Window proxy causes troubles.
     * @throws Exception if the test fails
     */
    @Test
    public void setTimeoutOnFrameWindow() throws Exception {
        if (notYetImplemented()) {
            return;
        }
        final String html = "<html><head><title>foo</title><script>\n"
            + "  function test() {\n"
            + "    frames[0].setTimeout(f, 0);\n"
            + "  }\n"
            + "  function f() {\n"
            + "    alert('in f');\n"
            + "  }\n"
            + "</script></head><body onload='test()'>\n"
            + "<iframe src='about:blank'></iframe>\n"
            + "</body></html>";

        final List<String> collectedAlerts = new ArrayList<String>();

        final WebClient client = new WebClient();
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        final MockWebConnection conn = new MockWebConnection();
        conn.setDefaultResponse(html);
        client.setWebConnection(conn);

        client.getPage(URL_FIRST);
        client.waitForBackgroundJavaScript(1000);

        final String[] expectedAlerts = {"in f"};
        assertEquals(expectedAlerts, collectedAlerts);
    }
}
