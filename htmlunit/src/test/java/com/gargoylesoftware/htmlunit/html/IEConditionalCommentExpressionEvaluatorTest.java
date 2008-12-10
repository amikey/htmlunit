/*
 * Copyright (c) 2002-2008 Gargoyle Software Inc.
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

import static com.gargoylesoftware.htmlunit.BrowserVersion.INTERNET_EXPLORER_6;
import static com.gargoylesoftware.htmlunit.BrowserVersion.INTERNET_EXPLORER_7;
import static com.gargoylesoftware.htmlunit.html.IEConditionalCommentExpressionEvaluator.evaluate;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Tests for {@link IEConditionalCommentExpressionEvaluator}.
 * Due to current implementation, conditional comment expressions get evaluated only when the simulated browser is IE.
 * @version $Revision$
 * @author Marc Guillemot
 */
public class IEConditionalCommentExpressionEvaluatorTest {

    /**
     * Test for expression [if IE].
     */
    @Test
    public void IE() {
        doTest("IE", true, true);
    }

    /**
     * Test for expressions like [if IE 7].
     */
    @Test
    public void IE_X() {
        doTest("IE 5", false, false);
        doTest("IE 6", true, false);
        doTest("IE 7", false, true);
        doTest("IE 8", false, false);
    }

    /**
     * Test for expression [if !IE].
     */
    @Test
    public void notIE() {
        doTest("!IE", false, false);
    }

    /**
     * Test for expressions like [if lt IE 5.5].
     */
    @Test
    public void lt_IE_X() {
        doTest("lt IE 5.5", false, false);
        doTest("lt IE 6", false, false);
        doTest("lt IE 7", true, false);
        doTest("lt IE 8", true, true);
    }

    /**
     * Test for expressions like [if lte IE 6].
     */
    @Test
    public void lte_IE_X() {
        doTest("lte IE 5.5", false, false);
        doTest("lte IE 6", true, false);
        doTest("lte IE 7", true, true);
        doTest("lte IE 8", true, true);
    }

    /**
     * Test for expressions like [if gt IE 5].
     */
    @Test
    public void gt_IE_X() {
        doTest("gt IE 5.5", true, true);
        doTest("gt IE 6", false, true);
        doTest("gt IE 7", false, false);
        doTest("gt IE 8", false, false);
    }

    /**
     * Test for expressions like [if gte IE 7].
     */
    @Test
    public void gte_IE_X() {
        doTest("gte IE 5.5", true, true);
        doTest("gte IE 6", true, true);
        doTest("gte IE 7", false, true);
        doTest("gte IE 8", false, false);
    }

    /**
     * Test for expressions like [if !(IE 7)].
     */
    @Test
    public void parenthese() {
        doTest("!(IE 5)", true, true);
        doTest("!(IE 6)", false, true);
        doTest("!(IE 7)", true, false);
        doTest("!(IE 8)", true, true);
    }

    /**
     * Test for expressions like if [(gt IE 5)&(lt IE 7)].
     */
    @Test
    public void and() {
        doTest("(gt IE 5)&(lt IE 7)", true, false);
        doTest("(gt IE 6)&(lt IE 8)", false, true);
    }

    /**
     * Test for expressions like if [if (IE 6)|(IE 7)].
     */
    @Test
    public void or() {
        doTest("(IE 6)|(IE 7)", true, true);
        doTest("(IE 5)|(IE 7)", false, true);
    }

    /**
     * Test for expressions like if [if true].
     */
    @Test
    public void true_false() {
        doTest("true", true, true);
        doTest("false", false, false);
    }

    private void doTest(final String expression, final boolean expectedIE6, final boolean expectedIE7) {
        doTest(expectedIE6, expression, INTERNET_EXPLORER_6);
        doTest(expectedIE7, expression, INTERNET_EXPLORER_7);
    }

    private void doTest(final boolean b, final String expression, final BrowserVersion browserVersion) {
        assertEquals(expression + " for " + browserVersion.getNickname(), b, evaluate(expression, browserVersion));
    }
}
