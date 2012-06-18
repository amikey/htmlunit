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
package com.gargoylesoftware.htmlunit.javascript.host.css;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.Browser;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;

/**
 * Tests for {@link CSSStyleDeclaration}.
 *
 * @version $Revision$
 * @author Ahmed Ashour
 */
@RunWith(BrowserRunner.class)
public class CSSStyleDeclaration2Test extends WebDriverTestCase {

    /*
     Below is a page to see the different elements behavior
<html>
  <head>
    <script>
      function test() {
        //all properties of CSSStyleDeclaration in JavaScriptConfiguration.xml
        var properties = ['azimuth','background','backgroundAttachment','backgroundColor','backgroundImage',
        'backgroundPosition','backgroundPositionX','backgroundPositionY','backgroundRepeat','behavior','border',
        'borderBottom','borderBottomColor','borderBottomStyle','borderBottomWidth','borderCollapse','borderColor',
        'borderLeft','borderLeftColor','borderLeftStyle','borderLeftWidth','borderRight','borderRightColor',
        'borderRightStyle','borderRightWidth','borderSpacing','borderStyle','borderTop','borderTopColor',
        'borderTopStyle','borderTopWidth','borderWidth','bottom','captionSide','clear','clip','color','content',
        'counterIncrement','counterReset','cssFloat','cssText','cue','cueAfter','cueBefore','cursor','direction',
        'display','elevation','emptyCells','filter','font','fontFamily','fontSize','fontSizeAdjust','fontStretch',
        'fontStyle','fontVariant','fontWeight','height','imeMode','layoutFlow','layoutGrid','layoutGridChar',
        'layoutGridLine','layoutGridMode','layoutGridType','left','letterSpacing','lineBreak','lineHeight',
        'listStyle','listStyleImage','listStylePosition','listStyleType','margin','marginBottom','marginLeft',
        'marginRight','marginTop','markerOffset','marks','maxHeight','maxWidth','minHeight','minWidth',
        'MozAppearance','MozBackgroundClip','MozBackgroundInlinePolicy','MozBackgroundOrigin','MozBinding',
        'MozBorderBottomColors','MozBorderLeftColors','MozBorderRadius','MozBorderRadiusBottomleft',
        'MozBorderRadiusBottomright','MozBorderRadiusTopleft','MozBorderRadiusTopright','MozBorderRightColors',
        'MozBorderTopColors','MozBoxAlign','MozBoxDirection','MozBoxFlex','MozBoxOrdinalGroup','MozBoxOrient',
        'MozBoxPack','MozBoxSizing','MozColumnCount','MozColumnGap','MozColumnWidth','MozFloatEdge',
        'MozForceBrokenImageIcon','MozImageRegion','MozMarginEnd','MozMarginStart','MozOpacity','MozOutline',
        'MozOutlineColor','MozOutlineOffset','MozOutlineRadius','MozOutlineRadiusBottomleft',
        'MozOutlineRadiusBottomright','MozOutlineRadiusTopleft','MozOutlineRadiusTopright','MozOutlineStyle',
        'MozOutlineWidth','MozPaddingEnd','MozPaddingStart','MozUserFocus','MozUserInput','MozUserModify',
        'MozUserSelect','msInterpolationMode','opacity','orphans','outline','outlineColor','outlineOffset',
        'outlineStyle','outlineWidth','overflow','overflowX','overflowY','padding','paddingBottom','paddingLeft',
        'paddingRight','paddingTop','page','pageBreakAfter','pageBreakBefore','pageBreakInside','pause',
        'pauseAfter','pauseBefore','pitch','pitchRange','pixelBottom','pixelLeft','pixelRight','pixelTop',
        'posBottom','posHeight','position','posLeft','posRight','posTop','posWidth','quotes','richness',
        'right','rubyAlign','rubyOverhang','rubyPosition','scrollbar3dLightColor','scrollbarArrowColor',
        'scrollbarBaseColor','scrollbarDarkShadowColor','scrollbarFaceColor','scrollbarHighlightColor',
        'scrollbarShadowColor','scrollbarTrackColor','size','speak','speakHeader','speakNumeral',
        'speakPunctuation','speechRate','stress','styleFloat','tableLayout','textAlign','textAlignLast',
        'textAutospace','textDecoration','textDecorationBlink','textDecorationLineThrough','textDecorationNone',
        'textDecorationOverline','textDecorationUnderline','textIndent','textJustify','textJustifyTrim',
        'textKashida','textKashidaSpace','textOverflow','textShadow','textTransform','textUnderlinePosition',
        'top','unicodeBidi','verticalAlign','visibility','voiceFamily','volume','whiteSpace','widows','width',
        'wordBreak','wordSpacing','wordWrap','writingMode','zIndex','zoom'];

    var ta = document.getElementById('myTextarea');
    for (var prop in properties) {
      prop = properties[prop];
      var node = document.createElement('div');
      var buffer = prop + ':';
      try {
        buffer += node.style[prop];
        node.style[prop] = '42.0';
        buffer += ',' + node.style[prop];
        node.style[prop] = '42.7';
        buffer += ',' + node.style[prop];
        node.style[prop] = '42';
        buffer += ',' + node.style[prop];
      } catch (e) {
          buffer += ',' + 'error';
      }
      ta.value += buffer + '\n';
    }
}
</script></head>
<body onload='test()'>
  <textarea id='myTextarea' cols='120' rows='40'></textarea>
</body></html>
     */

    /**
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented(Browser.IE6)
    //TODO: This test fails with WebDriver with real FF3, but succeed if test is done outside WebDriver
    @Alerts(FF = "success", IE = "success",
            IE7 = "error: outlineWidth-error: outlineWidth-error: outlineWidth-error: outlineWidth-",
            IE6 = "error: maxHeight-error: maxHeight-error: maxHeight-error: maxHeight-error: maxWidth-error: "
                + "maxWidth-error: maxWidth-error: maxWidth-error: minWidth-error: minWidth-error: minWidth-error: "
                + "minWidth-error: outlineWidth-error: outlineWidth-error: outlineWidth-error: outlineWidth-")
    public void width_like_properties() throws Exception {
        final String html
            = "<html><head><script>\n"
            + "function test() {\n"
            + "  var properties = ['borderBottomWidth','borderLeftWidth','borderRightWidth','borderTopWidth',\n"
            + "      'bottom', 'fontSize','height','left','letterSpacing','marginBottom','marginLeft',\n"
            + "      'marginRight','marginTop','maxHeight','maxWidth','minHeight','minWidth',\n"
            + "      'outlineWidth','paddingBottom','paddingLeft','paddingRight','paddingTop','right',\n"
            + "      'textIndent','top','verticalAlign','width','wordSpacing'];\n"
            + "\n"
            + "  var result = '';\n"
            + "  for (var prop in properties) {\n"
            + "    prop = properties[prop];\n"
            + "    var node = document.createElement('div');\n"
            + "    if (node.style[prop] != '')\n"
            + "      result += 'error: ' + prop + '-';\n"
            + "    node.style[prop] = '42.0';\n"
            + "    if (node.style[prop] != '42px')\n"
            + "      result += 'error: ' + prop + '-';\n"
            + "    node.style[prop] = '42.7';\n"
            + "    var expected = document.all ? '42px' : '42.7px';\n"
            + "    if (node.style[prop] != expected)\n"
            + "      result += 'error: ' + prop + '-';\n"
            + "    node.style[prop] = '42';\n"
            + "    if (node.style[prop] != '42px')\n"
            + "      result += 'error: ' + prop + '-';\n"
            + "  }\n"
            + "  alert(result == '' ? 'success' : result);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'></body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Expected values are missing for IE7.
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented({ Browser.IE7 })
    public void properties() throws Exception {
        final Map<BrowserVersion, String[]> properties = new HashMap<BrowserVersion, String[]>();
        properties.put(BrowserVersion.INTERNET_EXPLORER_6, new String[]{
            "background",
            "backgroundAttachment",
            "backgroundColor",
            "backgroundImage",
            "backgroundPosition",
            "backgroundPositionX",
            "backgroundPositionY",
            "backgroundRepeat",
            "behavior",
            "border",
            "borderBottom",
            "borderBottomColor",
            "borderBottomStyle",
            "borderBottomWidth",
            "borderCollapse",
            "borderColor",
            "borderLeft",
            "borderLeftColor",
            "borderLeftStyle",
            "borderLeftWidth",
            "borderRight",
            "borderRightColor",
            "borderRightStyle",
            "borderRightWidth",
            "borderStyle",
            "borderTop",
            "borderTopColor",
            "borderTopStyle",
            "borderTopWidth",
            "borderWidth",
            "bottom",
            "clear",
            "clip",
            "color",
            "cssText",
            "cursor",
            "direction",
            "display",
            "filter",
            "font",
            "fontFamily",
            "fontSize",
            "fontStyle",
            "fontVariant",
            "fontWeight",
            "height",
            "imeMode",
            "layoutFlow",
            "layoutGrid",
            "layoutGridChar",
            "layoutGridLine",
            "layoutGridMode",
            "layoutGridType",
            "left",
            "letterSpacing",
            "lineBreak",
            "lineHeight",
            "listStyle",
            "listStyleImage",
            "listStylePosition",
            "listStyleType",
            "margin",
            "marginBottom",
            "marginLeft",
            "marginRight",
            "marginTop",
            "maxHeight",
            "maxWidth",
            "minHeight",
            "minWidth",
            "msInterpolationMode",
            "overflow",
            "overflowX",
            "overflowY",
            "padding",
            "paddingBottom",
            "paddingLeft",
            "paddingRight",
            "paddingTop",
            "pageBreakAfter",
            "pageBreakBefore",
            "pixelBottom",
            "pixelLeft",
            "pixelRight",
            "pixelTop",
            "position",
            "posBottom",
            "posHeight",
            "posLeft",
            "posRight",
            "posTop",
            "posWidth",
            "right",
            "rubyAlign",
            "rubyOverhang",
            "rubyPosition",
            "scrollbar3dLightColor",
            "scrollbarArrowColor",
            "scrollbarBaseColor",
            "scrollbarDarkShadowColor",
            "scrollbarFaceColor",
            "scrollbarHighlightColor",
            "scrollbarShadowColor",
            "scrollbarTrackColor",
            "styleFloat",
            "tableLayout",
            "textAlign",
            "textAlignLast",
            "textAutospace",
            "textDecoration",
            "textDecorationBlink",
            "textDecorationLineThrough",
            "textDecorationNone",
            "textDecorationOverline",
            "textDecorationUnderline",
            "textIndent",
            "textJustify",
            "textJustifyTrim",
            "textKashida",
            "textKashidaSpace",
            "textOverflow",
            "textTransform",
            "textUnderlinePosition",
            "top",
            "unicodeBidi",
            "verticalAlign",
            "visibility",
            "whiteSpace",
            "width",
            "wordBreak",
            "wordSpacing",
            "wordWrap",
            "writingMode",
            "zIndex",
            "zoom"
        });

        properties.put(BrowserVersion.INTERNET_EXPLORER_8, new String[]{
            "background",
            "backgroundAttachment",
            "backgroundColor",
            "backgroundImage",
            "backgroundPosition",
            "backgroundPositionX",
            "backgroundPositionY",
            "backgroundRepeat",
            "behavior",
            "border",
            "borderBottom",
            "borderBottomColor",
            "borderBottomStyle",
            "borderBottomWidth",
            "borderCollapse",
            "borderColor",
            "borderLeft",
            "borderLeftColor",
            "borderLeftStyle",
            "borderLeftWidth",
            "borderRight",
            "borderRightColor",
            "borderRightStyle",
            "borderRightWidth",
            "borderSpacing",
            "borderStyle",
            "borderTop",
            "borderTopColor",
            "borderTopStyle",
            "borderTopWidth",
            "borderWidth",
            "bottom",
            "boxSizing",
            "captionSide",
            "clear",
            "clip",
            "color",
            "cssText",
            "content",
            "counterIncrement",
            "counterReset",
            "cursor",
            "direction",
            "display",
            "emptyCells",
            "filter",
            "font",
            "fontFamily",
            "fontSize",
            "fontStyle",
            "fontVariant",
            "fontWeight",
            "height",
            "imeMode",
            "layoutFlow",
            "layoutGrid",
            "layoutGridChar",
            "layoutGridLine",
            "layoutGridMode",
            "layoutGridType",
            "left",
            "letterSpacing",
            "lineBreak",
            "lineHeight",
            "listStyle",
            "listStyleImage",
            "listStylePosition",
            "listStyleType",
            "margin",
            "marginBottom",
            "marginLeft",
            "marginRight",
            "marginTop",
            "maxHeight",
            "maxWidth",
            "minHeight",
            "minWidth",
            "msBlockProgression",
            "msInterpolationMode",
            "orphans",
            "outline",
            "outlineColor",
            "outlineStyle",
            "outlineWidth",
            "overflow",
            "overflowX",
            "overflowY",
            "padding",
            "paddingBottom",
            "paddingLeft",
            "paddingRight",
            "paddingTop",
            "pageBreakAfter",
            "pageBreakBefore",
            "pageBreakInside",
            "pixelBottom",
            "pixelLeft",
            "pixelRight",
            "pixelTop",
            "position",
            "posBottom",
            "posHeight",
            "posLeft",
            "posRight",
            "posTop",
            "posWidth",
            "quotes",
            "right",
            "rubyAlign",
            "rubyOverhang",
            "rubyPosition",
            "scrollbar3dLightColor",
            "scrollbarArrowColor",
            "scrollbarBaseColor",
            "scrollbarDarkShadowColor",
            "scrollbarFaceColor",
            "scrollbarHighlightColor",
            "scrollbarShadowColor",
            "scrollbarTrackColor",
            "styleFloat",
            "tableLayout",
            "textAlign",
            "textAlignLast",
            "textAutospace",
            "textDecoration",
            "textDecorationBlink",
            "textDecorationLineThrough",
            "textDecorationNone",
            "textDecorationOverline",
            "textDecorationUnderline",
            "textIndent",
            "textJustify",
            "textJustifyTrim",
            "textKashida",
            "textKashidaSpace",
            "textOverflow",
            "textTransform",
            "textUnderlinePosition",
            "top",
            "unicodeBidi",
            "verticalAlign",
            "visibility",
            "whiteSpace",
            "widows",
            "width",
            "wordBreak",
            "wordSpacing",
            "wordWrap",
            "writingMode",
            "zIndex",
            "zoom"
        });

        properties.put(BrowserVersion.FIREFOX_3, new String[]{
            "MozAppearance",
            "MozBackgroundClip",
            "MozBackgroundInlinePolicy",
            "MozBackgroundOrigin",
            "MozBinding",
            "MozBorderBottomColors",
            "MozBorderEnd",
            "MozBorderEndColor",
            "MozBorderEndStyle",
            "MozBorderEndWidth",
            "MozBorderLeftColors",
            "MozBorderRadius",
            "MozBorderRadiusBottomleft",
            "MozBorderRadiusBottomright",
            "MozBorderRadiusTopleft",
            "MozBorderRadiusTopright",
            "MozBorderRightColors",
            "MozBorderStart",
            "MozBorderStartColor",
            "MozBorderStartStyle",
            "MozBorderStartWidth",
            "MozBorderTopColors",
            "MozBoxAlign",
            "MozBoxDirection",
            "MozBoxFlex",
            "MozBoxOrdinalGroup",
            "MozBoxOrient",
            "MozBoxPack",
            "MozBoxSizing",
            "MozColumnCount",
            "MozColumnGap",
            "MozColumnWidth",
            "MozFloatEdge",
            "MozForceBrokenImageIcon",
            "MozImageRegion",
            "MozMarginEnd",
            "MozMarginStart",
            "MozOpacity",
            "MozOutline",
            "MozOutlineColor",
            "MozOutlineOffset",
            "MozOutlineRadius",
            "MozOutlineRadiusBottomleft",
            "MozOutlineRadiusBottomright",
            "MozOutlineRadiusTopleft",
            "MozOutlineRadiusTopright",
            "MozOutlineStyle",
            "MozOutlineWidth",
            "MozPaddingEnd",
            "MozPaddingStart",
            "MozUserFocus",
            "MozUserInput",
            "MozUserModify",
            "MozUserSelect",
            "azimuth",
            "background",
            "backgroundAttachment",
            "backgroundColor",
            "backgroundImage",
            "backgroundPosition",
            "backgroundRepeat",
            "border",
            "borderBottom",
            "borderBottomColor",
            "borderBottomStyle",
            "borderBottomWidth",
            "borderCollapse",
            "borderColor",
            "borderLeft",
            "borderLeftColor",
            "borderLeftStyle",
            "borderLeftWidth",
            "borderRight",
            "borderRightColor",
            "borderRightStyle",
            "borderRightWidth",
            "borderSpacing",
            "borderStyle",
            "borderTop",
            "borderTopColor",
            "borderTopStyle",
            "borderTopWidth",
            "borderWidth",
            "bottom",
            "captionSide",
            "clear",
            "clip",
            "color",
            "content",
            "counterIncrement",
            "counterReset",
            "cssFloat",
            "cssText",
            "cue",
            "cueAfter",
            "cueBefore",
            "cursor",
            "direction",
            "display",
            "elevation",
            "emptyCells",
            "font",
            "fontFamily",
            "fontSize",
            "fontSizeAdjust",
            "fontStretch",
            "fontStyle",
            "fontVariant",
            "fontWeight",
            "height",
            "imeMode",
            "left",
            "length",
            "letterSpacing",
            "lineHeight",
            "listStyle",
            "listStyleImage",
            "listStylePosition",
            "listStyleType",
            "margin",
            "marginBottom",
            "marginLeft",
            "marginRight",
            "marginTop",
            "markerOffset",
            "marks",
            "maxHeight",
            "maxWidth",
            "minHeight",
            "minWidth",
            "opacity",
            "orphans",
            "outline",
            "outlineColor",
            "outlineOffset",
            "outlineStyle",
            "outlineWidth",
            "overflow",
            "overflowX",
            "overflowY",
            "padding",
            "paddingBottom",
            "paddingLeft",
            "paddingRight",
            "paddingTop",
            "page",
            "pageBreakAfter",
            "pageBreakBefore",
            "pageBreakInside",
            "pause",
            "pauseAfter",
            "pauseBefore",
            "pitch",
            "pitchRange",
            "position",
            "quotes",
            "richness",
            "right",
            "size",
            "speak",
            "speakHeader",
            "speakNumeral",
            "speakPunctuation",
            "speechRate",
            "stress",
            "tableLayout",
            "textAlign",
            "textDecoration",
            "textIndent",
            "textShadow",
            "textTransform",
            "top",
            "unicodeBidi",
            "verticalAlign",
            "visibility",
            "voiceFamily",
            "volume",
            "whiteSpace",
            "widows",
            "width",
            "wordSpacing",
            "zIndex",
        });

        properties.put(BrowserVersion.FIREFOX_3_6, new String[]{
            "MozAppearance",
            "MozBackgroundClip",
            "MozBackgroundInlinePolicy",
            "MozBackgroundOrigin",
            "MozBackgroundSize",
            "MozBinding",
            "MozBorderBottomColors",
            "MozBorderEnd",
            "MozBorderEndColor",
            "MozBorderEndStyle",
            "MozBorderEndWidth",
            "MozBorderImage",
            "MozBorderLeftColors",
            "MozBorderRadius",
            "MozBorderRadiusBottomleft",
            "MozBorderRadiusBottomright",
            "MozBorderRadiusTopleft",
            "MozBorderRadiusTopright",
            "MozBorderRightColors",
            "MozBorderStart",
            "MozBorderStartColor",
            "MozBorderStartStyle",
            "MozBorderStartWidth",
            "MozBorderTopColors",
            "MozBoxAlign",
            "MozBoxDirection",
            "MozBoxFlex",
            "MozBoxOrdinalGroup",
            "MozBoxOrient",
            "MozBoxPack",
            "MozBoxShadow",
            "MozBoxSizing",
            "MozColumnCount",
            "MozColumnGap",
            "MozColumnRule",
            "MozColumnRuleColor",
            "MozColumnRuleStyle",
            "MozColumnRuleWidth",
            "MozColumnWidth",
            "MozFloatEdge",
            "MozForceBrokenImageIcon",
            "MozImageRegion",
            "MozMarginEnd",
            "MozMarginStart",
            "MozOpacity",
            "MozOutline",
            "MozOutlineColor",
            "MozOutlineOffset",
            "MozOutlineRadius",
            "MozOutlineRadiusBottomleft",
            "MozOutlineRadiusBottomright",
            "MozOutlineRadiusTopleft",
            "MozOutlineRadiusTopright",
            "MozOutlineStyle",
            "MozOutlineWidth",
            "MozPaddingEnd",
            "MozPaddingStart",
            "MozStackSizing",
            "MozTransform",
            "MozTransformOrigin",
            "MozUserFocus",
            "MozUserInput",
            "MozUserModify",
            "MozUserSelect",
            "MozWindowShadow",
            "azimuth",
            "background",
            "backgroundAttachment",
            "backgroundColor",
            "backgroundImage",
            "backgroundPosition",
            "backgroundRepeat",
            "border",
            "borderBottom",
            "borderBottomColor",
            "borderBottomStyle",
            "borderBottomWidth",
            "borderCollapse",
            "borderColor",
            "borderLeft",
            "borderLeftColor",
            "borderLeftStyle",
            "borderLeftWidth",
            "borderRight",
            "borderRightColor",
            "borderRightStyle",
            "borderRightWidth",
            "borderSpacing",
            "borderStyle",
            "borderTop",
            "borderTopColor",
            "borderTopStyle",
            "borderTopWidth",
            "borderWidth",
            "bottom",
            "captionSide",
            "clear",
            "clip",
            "color",
            "content",
            "counterIncrement",
            "counterReset",
            "cssFloat",
            "cssText",
            "cue",
            "cueAfter",
            "cueBefore",
            "cursor",
            "direction",
            "display",
            "elevation",
            "emptyCells",
            "font",
            "fontFamily",
            "fontSize",
            "fontSizeAdjust",
            "fontStretch",
            "fontStyle",
            "fontVariant",
            "fontWeight",
            "height",
            "imeMode",
            "left",
            "length",
            "letterSpacing",
            "lineHeight",
            "listStyle",
            "listStyleImage",
            "listStylePosition",
            "listStyleType",
            "margin",
            "marginBottom",
            "marginLeft",
            "marginRight",
            "marginTop",
            "markerOffset",
            "marks",
            "maxHeight",
            "maxWidth",
            "minHeight",
            "minWidth",
            "opacity",
            "orphans",
            "outline",
            "outlineColor",
            "outlineOffset",
            "outlineStyle",
            "outlineWidth",
            "overflow",
            "overflowX",
            "overflowY",
            "padding",
            "paddingBottom",
            "paddingLeft",
            "paddingRight",
            "paddingTop",
            "page",
            "pageBreakAfter",
            "pageBreakBefore",
            "pageBreakInside",
            "pause",
            "pauseAfter",
            "pauseBefore",
            "pitch",
            "pitchRange",
            "position",
            "pointerEvents",
            "quotes",
            "richness",
            "right",
            "size",
            "speak",
            "speakHeader",
            "speakNumeral",
            "speakPunctuation",
            "speechRate",
            "stress",
            "tableLayout",
            "textAlign",
            "textDecoration",
            "textIndent",
            "textShadow",
            "textTransform",
            "top",
            "unicodeBidi",
            "verticalAlign",
            "visibility",
            "voiceFamily",
            "volume",
            "whiteSpace",
            "widows",
            "width",
            "wordSpacing",
            "wordWrap",
            "zIndex",
        });

        final String[] expectedProperties = properties.get(getBrowserVersion());
        properties(expectedProperties);
    }

    private void properties(final String[] expectedProperties) throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function test() {\n"
            + "  var style = document.getElementById('myDiv').style;\n"
            + "  var s = '';\n"
            + "  for (var i in style) {\n"
            + "    if (eval('style.' + i) == '')\n"
            + "      s += i + ' ';\n"
            + "  }\n"
            + "  document.getElementById('myTextarea').value = s;\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div id='myDiv'><br>\n"
            + "  <textarea id='myTextarea' cols='120' rows='20'></textarea>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final List<String> expectedStyles = Arrays.asList(expectedProperties);
        Collections.sort(expectedStyles);

        final List<String> collectedStyles =
            Arrays.asList(driver.findElement(By.id("myTextarea")).getText().split(" "));
        Collections.sort(collectedStyles);

        assertEquals(expectedStyles, collectedStyles);
    }

    /**
     * Test types of properties.
     * Expected values are missing for FF3, IE7 and IE8.
     * @throws Exception if the test fails
     */
    @Test
    @NotYetImplemented({ Browser.FF, Browser.IE7, Browser.IE8 })
    public void properties2() throws Exception {
        final Map<BrowserVersion, String[]> properties = new HashMap<BrowserVersion, String[]>();
        properties.put(BrowserVersion.INTERNET_EXPLORER_6, new String[]{
            "background",
            "backgroundAttachment",
            "backgroundColor",
            "backgroundImage",
            "backgroundPosition",
            "backgroundPositionX",
            "backgroundPositionY",
            "backgroundRepeat",
            "behavior",
            "border",
            "borderBottom",
            "borderBottomColor",
            "borderBottomStyle",
            "borderBottomWidth",
            "borderCollapse",
            "borderColor",
            "borderLeft",
            "borderLeftColor",
            "borderLeftStyle",
            "borderLeftWidth",
            "borderRight",
            "borderRightColor",
            "borderRightStyle",
            "borderRightWidth",
            "borderStyle",
            "borderTop",
            "borderTopColor",
            "borderTopStyle",
            "borderTopWidth",
            "borderWidth",
            "bottom",
            "clear",
            "clip",
            "color",
            "cssText",
            "cursor",
            "direction",
            "display",
            "filter",
            "font",
            "fontFamily",
            "fontSize",
            "fontStyle",
            "fontVariant",
            "fontWeight",
            "height",
            "imeMode",
            "layoutFlow",
            "layoutGrid",
            "layoutGridChar",
            "layoutGridLine",
            "layoutGridMode",
            "layoutGridType",
            "left",
            "letterSpacing",
            "lineBreak",
            "lineHeight",
            "listStyle",
            "listStyleImage",
            "listStylePosition",
            "listStyleType",
            "margin",
            "marginBottom",
            "marginLeft",
            "marginRight",
            "marginTop",
            "maxHeight",
            "maxWidth",
            "minHeight",
            "minWidth",
            "msInterpolationMode",
            "overflow",
            "overflowX",
            "overflowY",
            "padding",
            "paddingBottom",
            "paddingLeft",
            "paddingRight",
            "paddingTop",
            "pageBreakAfter",
            "pageBreakBefore",
            "position",
            "right",
            "rubyAlign",
            "rubyOverhang",
            "rubyPosition",
            "scrollbar3dLightColor",
            "scrollbarArrowColor",
            "scrollbarBaseColor",
            "scrollbarDarkShadowColor",
            "scrollbarFaceColor",
            "scrollbarHighlightColor",
            "scrollbarShadowColor",
            "scrollbarTrackColor",
            "styleFloat",
            "tableLayout",
            "textAlign",
            "textAlignLast",
            "textAutospace",
            "textDecoration",
            "textIndent",
            "textJustify",
            "textJustifyTrim",
            "textKashida",
            "textKashidaSpace",
            "textOverflow",
            "textTransform",
            "textUnderlinePosition",
            "top",
            "unicodeBidi",
            "verticalAlign",
            "visibility",
            "whiteSpace",
            "width",
            "wordBreak",
            "wordSpacing",
            "wordWrap",
            "writingMode",
            "zoom"
        });

        final String[] expectedProperties = properties.get(getBrowserVersion());
        properties2(expectedProperties);
    }

    private void properties2(final String[] expectedProperties) throws Exception {
        final String html
            = "<html><head><title>First</title><script>\n"
            + "function test() {\n"
            + "  var style = document.getElementById('myDiv').style;\n"
            + "  var s = '';\n"
            + "  for (var i in style) {\n"
            + "    if (eval('style.' + i) === '')\n"
            + "      s += i + ' ';\n"
            + "  }\n"
            + "  document.getElementById('myTextarea').value = s;\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <div id='myDiv'><br>\n"
            + "  <textarea id='myTextarea' cols='120' rows='20'></textarea>\n"
            + "</body></html>";

        final WebDriver driver = loadPage2(html);
        final List<String> expectedStyles = Arrays.asList(expectedProperties);
        Collections.sort(expectedStyles);

        final List<String> collectedStyles =
            Arrays.asList(driver.findElement(By.id("myTextarea")).getText().split(" "));
        Collections.sort(collectedStyles);

        assertEquals(expectedStyles, collectedStyles);
    }
}
