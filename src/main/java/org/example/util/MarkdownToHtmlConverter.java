package org.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownToHtmlConverter {

    private static final Pattern ALLOWED_TAGS_PATTERN = Pattern.compile(
            "</?(b|i|u|s|code|pre|tg-spoiler|a\\s+href=[\"'][^\"']*[\"'])>");

    public static String convert(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        String htmlText = standardMarkdownToHtml(markdown);
        return removeUnsupportedTags(htmlText);
    }

    private static String standardMarkdownToHtml(String markdown) {
        String result = markdown;

        Pattern codeBlockPattern = Pattern.compile("```(\\w*\\n)?([\\s\\S]+?)```", Pattern.DOTALL);
        Matcher codeMatcher = codeBlockPattern.matcher(result);
        StringBuffer sb = new StringBuffer();
        while (codeMatcher.find()) {
            String codeContent = escapeHtml(codeMatcher.group(2));
            codeMatcher.appendReplacement(sb, "<pre><code>" + Matcher.quoteReplacement(codeContent) + "</code></pre>");
        }
        codeMatcher.appendTail(sb);
        result = sb.toString();

        result = result.replaceAll("`(.*?)`", "<code>$1</code>");
        result = result.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        result = result.replaceAll("\\*(.*?)\\*", "<i>$1</i>");

        return result;
    }

    private static String removeUnsupportedTags(String htmlText) {
        Pattern unsupportedTagsPattern = Pattern.compile("</?[a-zA-Z0-9]+[^>]*>");
        Matcher matcher = unsupportedTagsPattern.matcher(htmlText);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String tag = matcher.group(0);
            if (!ALLOWED_TAGS_PATTERN.matcher(tag).matches()) {
                matcher.appendReplacement(sb, "");
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(tag));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
