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
package com.gargoylesoftware.htmlunit.httpclient;

import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTTP_COOKIE_EXTENDED_DATE_PATTERNS;
import static com.gargoylesoftware.htmlunit.BrowserVersionFeatures.HTTP_COOKIE_START_DATE_1970;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieAttributeHandler;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookiePathComparator;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicHeader;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * Customized BrowserCompatSpec for HtmlUnit.
 *
 * Workaround for <a href="https://issues.apache.org/jira/browse/HTTPCLIENT-1006">HttpClient bug 1006</a>:
 * quotes are wrongly removed in cookie's values.
 *
 * @version $Revision$
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author Noboru Sinohara
 * @author David D. Kilzer
 * @author Marc Guillemot
 * @author Brad Clarke
 * @author Ahmed Ashour
 * @author Nicolas Belisle
 * @author Ronald Brill
 * @author John J Murdoch
 */
public class HtmlUnitBrowserCompatCookieSpec extends BrowserCompatSpec {

    /** The cookie name used for cookies with no name (HttpClient doesn't like empty names). */
    public static final String EMPTY_COOKIE_NAME = "HTMLUNIT_EMPTY_COOKIE";

    /** Workaround for domain of local files. */
    public static final String LOCAL_FILESYSTEM_DOMAIN = "LOCAL_FILESYSTEM";

    /**
     * Comparator for sending cookies in right order.
     * See specification:
     * - RFC2109 (#4.3.4) http://www.ietf.org/rfc/rfc2109.txt
     * - RFC2965 (#3.3.4) http://www.ietf.org/rfc/rfc2965.txt http://www.ietf.org/rfc/rfc2109.txt
     */
    private static final Comparator<Cookie> COOKIE_COMPARATOR = new CookiePathComparator();

    private static final Date DATE_1_1_1970;

    static {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(DateUtils.GMT);
        calendar.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        DATE_1_1_1970 = calendar.getTime();
    }

    // simplified patterns from BrowserCompatSpec, with yy patterns before similar yyyy patterns
    private static final String[] DEFAULT_DATE_PATTERNS = new String[] {
        "EEE dd MMM yy HH mm ss zzz",
        "EEE dd MMM yyyy HH mm ss zzz",
        "EEE MMM d HH mm ss yyyy",
        "EEE dd MMM yy HH mm ss z ",
        "EEE dd MMM yyyy HH mm ss z ",
        "EEE dd MM yy HH mm ss z ",
        "EEE dd MM yyyy HH mm ss z ",
    };
    private static final String[] EXTENDED_DATE_PATTERNS = new String[] {
        "EEE dd MMM yy HH mm ss zzz",
        "EEE dd MMM yyyy HH mm ss zzz",
        "EEE MMM d HH mm ss yyyy",
        "EEE dd MMM yy HH mm ss z ",
        "EEE dd MMM yyyy HH mm ss z ",
        "EEE dd MM yy HH mm ss z ",
        "EEE dd MM yyyy HH mm ss z ",
        "d/M/yyyy",
    };

    /**
     * Constructor.
     *
     * @param browserVersion the {@link BrowserVersion} to simulate
     */
    public HtmlUnitBrowserCompatCookieSpec(final BrowserVersion browserVersion) {
        super();

        registerAttribHandler(ClientCookie.DISCARD_ATTR, new HtmlUnitDomainHandler());
        registerAttribHandler(ClientCookie.PATH_ATTR, new HtmlUnitPathHandler(browserVersion));

        final CookieAttributeHandler originalExpiresHandler = getAttribHandler(ClientCookie.EXPIRES_ATTR);
        final CookieAttributeHandler wrapperExpiresHandler = new CookieAttributeHandler() {

            public void validate(final Cookie cookie, final CookieOrigin origin) throws MalformedCookieException {
                originalExpiresHandler.validate(cookie, origin);
            }

            public void parse(final SetCookie cookie, String value) throws MalformedCookieException {
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                value = value.replaceAll("[ ,:-]+", " ");

                Date startDate = null;
                if (browserVersion.hasFeature(HTTP_COOKIE_START_DATE_1970)) {
                    startDate = DATE_1_1_1970;
                }

                String[] datePatterns = DEFAULT_DATE_PATTERNS;
                if (browserVersion.hasFeature(HTTP_COOKIE_EXTENDED_DATE_PATTERNS)) {
                    datePatterns = EXTENDED_DATE_PATTERNS;
                }

                final Date date = DateUtils.parseDate(value, datePatterns, startDate);
                cookie.setExpiryDate(date);
            }

            public boolean match(final Cookie cookie, final CookieOrigin origin) {
                return originalExpiresHandler.match(cookie, origin);
            }
        };
        registerAttribHandler(ClientCookie.EXPIRES_ATTR, wrapperExpiresHandler);

        registerAttribHandler("httponly", new HtmlUnitHttpOnlyHandler());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Cookie> parse(Header header, final CookieOrigin origin) throws MalformedCookieException {
        // first a hack to support empty headers
        final String text = header.getValue();
        int endPos = text.indexOf(';');
        if (endPos < 0) {
            endPos = text.indexOf('=');
        }
        else {
            final int pos = text.indexOf('=');
            if (pos > endPos) {
                endPos = -1;
            }
            else {
                endPos = pos;
            }
        }
        if (endPos < 0) {
            header = new BasicHeader(header.getName(), EMPTY_COOKIE_NAME + "=" + header.getValue());
        }
        else if (endPos == 0 || StringUtils.isBlank(text.substring(0, endPos))) {
            header = new BasicHeader(header.getName(), EMPTY_COOKIE_NAME + header.getValue());
        }

        final List<Cookie> cookies = super.parse(header, origin);
        for (final Cookie c : cookies) {
            // re-add quotes around value if parsing as incorrectly trimmed them
            if (header.getValue().contains(c.getName() + "=\"" + c.getValue())) {
                ((BasicClientCookie) c).setValue('"' + c.getValue() + '"');
            }
        }
        return cookies;
    }

    @Override
    public List<Header> formatCookies(final List<Cookie> cookies) {
        Collections.sort(cookies, COOKIE_COMPARATOR);

        return super.formatCookies(cookies);
    }
}
