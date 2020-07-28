package com.ruoyi.common.utils.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML过滤器，用于去除XSS漏洞隐患。
 *
 * @author ruoyi
 */
public final class HTMLFilter
{
    /**
     * regex flag union representing /si modifiers in php
     **/
    private static final int REGEX_FLAGS_SI = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;
    private static final Pattern P_COMMENTS = Pattern.compile("<!--(.*?)-->", Pattern.DOTALL);
    private static final Pattern P_COMMENT = Pattern.compile("^!--(.*)--$", REGEX_FLAGS_SI);
    private static final Pattern P_TAGS = Pattern.compile("<(.*?)>", Pattern.DOTALL);
    private static final Pattern P_END_TAG = Pattern.compile("^/([a-z0-9]+)", REGEX_FLAGS_SI);
    private static final Pattern P_START_TAG = Pattern.compile("^([a-z0-9]+)(.*?)(/?)$", REGEX_FLAGS_SI);
    private static final Pattern P_QUOTED_ATTRIBUTES = Pattern.compile("([a-z0-9]+)=([\"'])(.*?)\\2", REGEX_FLAGS_SI);
    private static final Pattern P_UNQUOTED_ATTRIBUTES = Pattern.compile("([a-z0-9]+)(=)([^\"\\s']+)", REGEX_FLAGS_SI);
    private static final Pattern P_PROTOCOL = Pattern.compile("^([^:]+):", REGEX_FLAGS_SI);
    private static final Pattern P_ENTITY = Pattern.compile("&#(\\d+);?");
    private static final Pattern P_ENTITY_UNICODE = Pattern.compile("&#x([0-9a-f]+);?");
    private static final Pattern P_ENCODE = Pattern.compile("%([0-9a-f]{2});?");
    private static final Pattern P_VALID_ENTITIES = Pattern.compile("&([^&;]*)(?=(;|&|$))");
    private static final Pattern P_VALID_QUOTES = Pattern.compile("(>|^)([^<]+?)(<|$)", Pattern.DOTALL);
    private static final Pattern P_END_ARROW = Pattern.compile("^>");
    private static final Pattern P_BODY_TO_END = Pattern.compile("<([^>]*?)(?=<|$)");
    private static final Pattern P_XML_CONTENT = Pattern.compile("(^|>)([^<]*?)(?=>)");
    private static final Pattern P_STRAY_LEFT_ARROW = Pattern.compile("<([^>]*?)(?=<|$)");
    private static final Pattern P_STRAY_RIGHT_ARROW = Pattern.compile("(^|>)([^<]*?)(?=>)");
    private static final Pattern P_AMP = Pattern.compile("&");
    private static final Pattern P_QUOTE = Pattern.compile("\"");
    private static final Pattern P_LEFT_ARROW = Pattern.compile("<");
    private static final Pattern P_RIGHT_ARROW = Pattern.compile(">");
    private static final Pattern P_BOTH_ARROWS = Pattern.compile("<>");

    // @xxx could grow large... maybe use sesat's ReferenceMap
    private static final ConcurrentMap<String, Pattern> P_REMOVE_PAIR_BLANKS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Pattern> P_REMOVE_SELF_BLANKS = new ConcurrentHashMap<>();

    /**
     * set of allowed html elements, along with allowed attributes for each element
     **/
    private final Map<String, List<String>> vAllowed;
    /**
     * counts of open tags for each (allowable) html element
     **/
    private final Map<String, Integer> vTagCounts = new HashMap<>();

    /**
     * html elements which must always be self-closing (e.g. "<img />")
     **/
    private final String[] vSelfClosingTags;
    /**
     * html elements which must always have separate opening and closing tags (e.g. "<b></b>")
     **/
    private final String[] vNeedClosingTags;
    /**
     * set of disallowed html elements
     **/
    private final String[] vDisallowed;
    /**
     * attributes which should be checked for valid protocols
     **/
    private final String[] vProtocolAtts;
    /**
     * allowed protocols
     **/
    private final String[] vAllowedProtocols;
    /**
     * tags which should be removed if they contain no content (e.g. "<b></b>" or "<b />")
     **/
    private final String[] vRemoveBlanks;
    /**
     * entities allowed within html markup
     **/
    private final String[] vAllowedEntities;
    /**
     * flag determining whether comments are allowed in input String.
     */
    private final boolean stripComment;
    private final boolean encodeQuotes;
    /**
     * flag determining whether to try to make tags when presented with "unbalanced" angle brackets (e.g. "<b text </b>"
     * becomes "<b> text </b>"). If set to false, unbalanced angle brackets will be html escaped.
     */
    private final boolean alwaysMakeTags;

    /**
     * Default constructor.
     */
    public HTMLFilter()
    {
        vAllowed = new HashMap<>();

        final ArrayList<String> a_atts = new ArrayList<>();
        a_atts.add("href");
        a_atts.add("target");
        vAllowed.put("a", a_atts);

        final ArrayList<String> img_atts = new ArrayList<>();
        img_atts.add("src");
        img_atts.add("width");
        img_atts.add("height");
        img_atts.add("alt");
        vAllowed.put("img", img_atts);

        final ArrayList<String> no_atts = new ArrayList<>();
        vAllowed.put("b", no_atts);
        vAllowed.put("strong", no_atts);
        vAllowed.put("i", no_atts);
        vAllowed.put("em", no_atts);

        vSelfClosingTags = new String[] { "img" };
        vNeedClosingTags = new String[] { "a", "b", "strong", "i", "em" };
        vDisallowed = new String[] {};
        vAllowedProtocols = new String[] { "http", "mailto", "https" }; // no ftp.
        vProtocolAtts = new String[] { "src", "href" };
        vRemoveBlanks = new String[] { "a", "b", "strong", "i", "em" };
        vAllowedEntities = new String[] { "amp", "gt", "lt", "quot" };
        stripComment = true;
        encodeQuotes = true;
        alwaysMakeTags = false;
    }

    /**
     * Map-parameter configurable constructor.
     *
     * @param conf map containing configuration. keys match field names.
     */
    @SuppressWarnings("unchecked")
    public HTMLFilter(final Map<String, Object> conf)
    {

        assert conf.containsKey("vAllowed") : "configuration requires vAllowed";
        assert conf.containsKey("vSelfClosingTags") : "configuration requires vSelfClosingTags";
        assert conf.containsKey("vNeedClosingTags") : "configuration requires vNeedClosingTags";
        assert conf.containsKey("vDisallowed") : "configuration requires vDisallowed";
        assert conf.containsKey("vAllowedProtocols") : "configuration requires vAllowedProtocols";
        assert conf.containsKey("vProtocolAtts") : "configuration requires vProtocolAtts";
        assert conf.containsKey("vRemoveBlanks") : "configuration requires vRemoveBlanks";
        assert conf.containsKey("vAllowedEntities") : "configuration requires vAllowedEntities";

        vAllowed = Collections.unmodifiableMap((HashMap<String, List<String>>) conf.get("vAllowed"));
        vSelfClosingTags = (String[]) conf.get("vSelfClosingTags");
        vNeedClosingTags = (String[]) conf.get("vNeedClosingTags");
        vDisallowed = (String[]) conf.get("vDisallowed");
        vAllowedProtocols = (String[]) conf.get("vAllowedProtocols");
        vProtocolAtts = (String[]) conf.get("vProtocolAtts");
        vRemoveBlanks = (String[]) conf.get("vRemoveBlanks");
        vAllowedEntities = (String[]) conf.get("vAllowedEntities");
        stripComment = conf.containsKey("stripComment") ? (Boolean) conf.get("stripComment") : true;
        encodeQuotes = conf.containsKey("encodeQuotes") ? (Boolean) conf.get("encodeQuotes") : true;
        alwaysMakeTags = conf.containsKey("alwaysMakeTags") ? (Boolean) conf.get("alwaysMakeTags") : true;
    }

    private void reset()
    {
        vTagCounts.clear();
    }

    // ---------------------------------------------------------------
    // my versions of some PHP library functions
    public static String chr(final int decimal)
    {
        return String.valueOf((char) decimal);
    }

    public static String htmlSpecialChars(final String s)
    {
        String result = s;
        result = regexReplace(P_AMP, "&amp;", result);
        result = regexReplace(P_QUOTE, "&quot;", result);
        result = regexReplace(P_LEFT_ARROW, "&lt;", result);
        result = regexReplace(P_RIGHT_ARROW, "&gt;", result);
        return result;
    }

    // ---------------------------------------------------------------

    /**
     * given a user submitted input String, filter out any invalid or restricted html.
     *
     * @param input text (i.e. submitted by a user) than may contain html
     * @return "clean" version of input, with only valid, whitelisted html elements allowed
     */
    public String filter(final String input)
    {
        reset();
        String s = input;

        s = escapeComments(s);

        s = balanceHTML(s);

        s = checkTags(s);

        s = processRemoveBlanks(s);

        // s = validateEntities(s);

        return s;
    }

    public boolean isAlwaysMakeTags()
    {
        return alwaysMakeTags;
    }

    public boolean isStripComments()
    {
        return stripComment;
    }

    private String escapeComments(final String s)
    {
        final Matcher m = P_COMMENTS.matcher(s);
        final StringBuffer buf = new StringBuffer();
        if (m.find())
        {
            final String match = m.group(1); // (.*?)
            m.appendReplacement(buf, Matcher.quoteReplacement("<!--" + htmlSpecialChars(match) + "-->"));
        }
        m.appendTail(buf);

        return buf.toString();
    }

    private String balanceHTML(String s)
    {
        if (alwaysMakeTags)
        {
            //
            // try and form html
            //
            s = regexReplace(P_END_ARROW, "", s);
            // 不追加结束标签
            s = regexReplace(P_BODY_TO_END, "<$1>", s);
            s = regexReplace(P_XML_CONTENT, "$1<$2", s);

        }
        else
        {
            //
            // escape stray brackets
            //
            s = regexReplace(P_STRAY_LEFT_ARROW, "&lt;$1", s);
            s = regexReplace(P_STRAY_RIGHT_ARROW, "$1$2&gt;<", s);

            //
            // the last regexp causes '<>' entities to appear
            // (we need to do a lookahead assertion so that the last bracket can
            // be used in the next pass of the regexp)
            //
            s = regexReplace(P_BOTH_ARROWS, "", s);
        }

        return s;
    }

    private String checkTags(String s)
    {
        Matcher m = P_TAGS.matcher(s);

        final StringBuffer buf = new StringBuffer();
        while (m.find())
        {
            String replaceStr = m.group(1);
            replaceStr = processTag(replaceStr);
            m.appendReplacement(buf, Matcher.quoteReplacement(replaceStr));
        }
        m.appendTail(buf);

        // these get tallied in processTag
        // (remember to reset before subsequent calls to filter method)
        final StringBuilder sBuilder = new StringBuilder(buf.toString());
        for (String key : vTagCounts.keySet())
        {
            for (int ii = 0; ii < vTagCounts.get(key); ii++)
            {
                sBuilder.append("</").append(key).append(">");
            }
        }
        s = sBuilder.toString();

        return s;
    }

    private String processRemoveBlanks(final String s)
    {
        String result = s;
        for (String tag : vRemoveBlanks)
        {
            if (!P_REMOVE_PAIR_BLANKS.containsKey(tag))
            {
                P_REMOVE_PAIR_BLANKS.putIfAbsent(tag, Pattern.compile("<" + tag + "(\\s[^>]*)?></" + tag + ">"));
            }
            result = regexReplace(P_REMOVE_PAIR_BLANKS.get(tag), "", result);
            if (!P_REMOVE_SELF_BLANKS.containsKey(tag))
            {
                P_REMOVE_SELF_BLANKS.putIfAbsent(tag, Pattern.compile("<" + tag + "(\\s[^>]*)?/>"));
            }
            result = regexReplace(P_REMOVE_SELF_BLANKS.get(tag), "", result);
        }

        return result;
    }

    private static String regexReplace(final Pattern regex_pattern, final String replacement, final String s)
    {
        Matcher m = regex_pattern.matcher(s);
        return m.replaceAll(replacement);
    }

    private String processTag(final String s)
    {
        // ending tags
        Matcher m = P_END_TAG.matcher(s);
        if (m.find())
        {
            final String name = m.group(1).toLowerCase();
            if (allowed(name))
            {
                if (false == inArray(name, vSelfClosingTags))
                {
                    if (vTagCounts.containsKey(name))
                    {
                        vTagCounts.put(name, vTagCounts.get(name) - 1);
                        return "</" + name + ">";
                    }
                }
            }
        }

        // starting tags
        m = P_START_TAG.matcher(s);
        if (m.find())
        {
            final String name = m.group(1).toLowerCase();
            final String body = m.group(2);
            String ending = m.group(3);

            // debug( "in a starting tag, name='" + name + "'; body='" + body + "'; ending='" + ending + "'" );
            if (allowed(name))
            {
                final StringBuilder params = new StringBuilder();

                final Matcher m2 = P_QUOTED_ATTRIBUTES.matcher(body);
                final Matcher m3 = P_UNQUOTED_ATTRIBUTES.matcher(body);
                final List<String> paramNames = new ArrayList<>();
                final List<String> paramValues = new ArrayList<>();
                while (m2.find())
                {
                    paramNames.add(m2.group(1)); // ([a-z0-9]+)
                    paramValues.add(m2.group(3)); // (.*?)
                }
                while (m3.find())
                {
                    paramNames.add(m3.group(1)); // ([a-z0-9]+)
                    paramValues.add(m3.group(3)); // ([^\"\\s']+)
                }

                String paramName, paramValue;
                for (int ii = 0; ii < paramNames.size(); ii++)
                {
                    paramName = paramNames.get(ii).toLowerCase();
                    paramValue = paramValues.get(ii);

                    // debug( "paramName='" + paramName + "'" );
                    // debug( "paramValue='" + paramValue + "'" );
                    // debug( "allowed? " + vAllowed.get( name ).contains( paramName ) );

                    if (allowedAttribute(name, paramName))
                    {
                        if (inArray(paramName, vProtocolAtts))
                        {
                            paramValue = processParamProtocol(paramValue);
                        }
                        params.append(' ').append(paramName).append("=\"").append(paramValue).append("\"");
                    }
                }

                if (inArray(name, vSelfClosingTags))
                {
                    ending = " /";
                }

                if (inArray(name, vNeedClosingTags))
                {
                    ending = "";
                }

                if (ending == null || ending.length() < 1)
                {
                    if (vTagCounts.containsKey(name))
                    {
                        vTagCounts.put(name, vTagCounts.get(name) + 1);
                    }
                    else
                    {
                        vTagCounts.put(name, 1);
                    }
                }
                else
                {
                    ending = " /";
                }
                return "<" + name + params + ending + ">";
            }
            else
            {
                return "";
            }
        }

        // comments
        m = P_COMMENT.matcher(s);
        if (!stripComment && m.find())
        {
            return "<" + m.group() + ">";
        }

        return "";
    }

    private String processParamProtocol(String s)
    {
        s = decodeEntities(s);
        final Matcher m = P_PROTOCOL.matcher(s);
        if (m.find())
        {
            final String protocol = m.group(1);
            if (!inArray(protocol, vAllowedProtocols))
            {
                // bad protocol, turn into local anchor link instead
                s = "#" + s.substring(protocol.length() + 1);
                if (s.startsWith("#//"))
                {
                    s = "#" + s.substring(3);
                }
            }
        }

        return s;
    }

    private String decodeEntities(String s)
    {
        StringBuffer buf = new StringBuffer();

        Matcher m = P_ENTITY.matcher(s);
        while (m.find())
        {
            final String match = m.group(1);
            final int decimal = Integer.decode(match).intValue();
            m.appendReplacement(buf, Matcher.quoteReplacement(chr(decimal)));
        }
        m.appendTail(buf);
        s = buf.toString();

        buf = new StringBuffer();
        m = P_ENTITY_UNICODE.matcher(s);
        while (m.find())
        {
            final String match = m.group(1);
            final int decimal = Integer.valueOf(match, 16).intValue();
            m.appendReplacement(buf, Matcher.quoteReplacement(chr(decimal)));
        }
        m.appendTail(buf);
        s = buf.toString();

        buf = new StringBuffer();
        m = P_ENCODE.matcher(s);
        while (m.find())
        {
            final String match = m.group(1);
            final int decimal = Integer.valueOf(match, 16).intValue();
            m.appendReplacement(buf, Matcher.quoteReplacement(chr(decimal)));
        }
        m.appendTail(buf);
        s = buf.toString();

        s = validateEntities(s);
        return s;
    }

    private String validateEntities(final String s)
    {
        StringBuffer buf = new StringBuffer();

        // validate entities throughout the string
        Matcher m = P_VALID_ENTITIES.matcher(s);
        while (m.find())
        {
            final String one = m.group(1); // ([^&;]*)
            final String two = m.group(2); // (?=(;|&|$))
            m.appendReplacement(buf, Matcher.quoteReplacement(checkEntity(one, two)));
        }
        m.appendTail(buf);

        return encodeQuotes(buf.toString());
    }

    private String encodeQuotes(final String s)
    {
        if (encodeQuotes)
        {
            StringBuffer buf = new StringBuffer();
            Matcher m = P_VALID_QUOTES.matcher(s);
            while (m.find())
            {
                final String one = m.group(1); // (>|^)
                final String two = m.group(2); // ([^<]+?)
                final String three = m.group(3); // (<|$)
                // 不替换双引号为&quot;，防止json格式无效 regexReplace(P_QUOTE, "&quot;", two)
                m.appendReplacement(buf, Matcher.quoteReplacement(one + two + three));
            }
            m.appendTail(buf);
            return buf.toString();
        }
        else
        {
            return s;
        }
    }

    private String checkEntity(final String preamble, final String term)
    {

        return ";".equals(term) && isValidEntity(preamble) ? '&' + preamble : "&amp;" + preamble;
    }

    private boolean isValidEntity(final String entity)
    {
        return inArray(entity, vAllowedEntities);
    }

    private static boolean inArray(final String s, final String[] array)
    {
        for (String item : array)
        {
            if (item != null && item.equals(s))
            {
                return true;
            }
        }
        return false;
    }

    private boolean allowed(final String name)
    {
        return (vAllowed.isEmpty() || vAllowed.containsKey(name)) && !inArray(name, vDisallowed);
    }

    private boolean allowedAttribute(final String name, final String paramName)
    {
        return allowed(name) && (vAllowed.isEmpty() || vAllowed.get(name).contains(paramName));
    }
}