package com.mota.marketingagent.ui.helpers;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts basic markdown text into Android Spannable for rich text rendering.
 * Supports: bold, italic, bold+italic, headers, bullet lists, numbered lists,
 * inline code, and line breaks.
 */
public final class MarkdownHelper {

    private MarkdownHelper() {}

    public static CharSequence render(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        // Clean up common artifacts from backend
        String cleaned = markdown
                .replace("\\n", "\n")
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("\\[PREVIOUS CONTEXT SUMMARY\\]:[\\s\\S]*?\\[END SUMMARY\\]", "")
                .replaceAll("```assistant_meta[\\s\\S]*?```", "")
                .replaceAll(":::[a-zA-Z_]+:::", "")
                .trim();

        String[] lines = cleaned.split("\n");
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (i > 0) {
                builder.append("\n");
            }

            // Headers
            if (line.startsWith("### ")) {
                appendStyled(builder, line.substring(4), Typeface.BOLD, 1.1f);
                continue;
            }
            if (line.startsWith("## ")) {
                appendStyled(builder, line.substring(3), Typeface.BOLD, 1.2f);
                continue;
            }
            if (line.startsWith("# ")) {
                appendStyled(builder, line.substring(2), Typeface.BOLD, 1.35f);
                continue;
            }

            // Bullet lists
            if (line.matches("^\\s*[-*]\\s+.*")) {
                String content = line.replaceFirst("^\\s*[-*]\\s+", "");
                int start = builder.length();
                appendInlineFormatted(builder, "  • " + content);
                continue;
            }

            // Numbered lists
            if (line.matches("^\\s*\\d+\\.\\s+.*")) {
                String content = line.replaceFirst("^\\s*\\d+\\.\\s+", "");
                String num = line.replaceFirst("^\\s*(\\d+)\\.\\s+.*", "$1");
                appendInlineFormatted(builder, "  " + num + ". " + content);
                continue;
            }

            // Regular text
            appendInlineFormatted(builder, line);
        }

        return builder;
    }

    private static void appendStyled(SpannableStringBuilder builder, String text, int style, float sizeMultiplier) {
        int start = builder.length();
        appendInlineFormatted(builder, text);
        int end = builder.length();
        builder.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sizeMultiplier != 1.0f) {
            builder.setSpan(new RelativeSizeSpan(sizeMultiplier), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Handles inline formatting: ***bold italic***, **bold**, *italic*, `code`
     */
    private static void appendInlineFormatted(SpannableStringBuilder builder, String text) {
        // Process inline patterns
        Pattern pattern = Pattern.compile(
                "(\\*\\*\\*(.+?)\\*\\*\\*)" +  // Group 1,2: bold italic
                "|(\\*\\*(.+?)\\*\\*)" +        // Group 3,4: bold
                "|(\\*(.+?)\\*)" +              // Group 5,6: italic
                "|(`([^`]+)`)"                   // Group 7,8: inline code
        );

        Matcher matcher = pattern.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // Append text before this match
            if (matcher.start() > lastEnd) {
                builder.append(text, lastEnd, matcher.start());
            }

            int start = builder.length();

            if (matcher.group(2) != null) {
                // Bold italic
                builder.append(matcher.group(2));
                builder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (matcher.group(4) != null) {
                // Bold
                builder.append(matcher.group(4));
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (matcher.group(6) != null) {
                // Italic
                builder.append(matcher.group(6));
                builder.setSpan(new StyleSpan(Typeface.ITALIC), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (matcher.group(8) != null) {
                // Inline code
                builder.append(matcher.group(8));
                builder.setSpan(new TypefaceSpan("monospace"), start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            lastEnd = matcher.end();
        }

        // Append remaining text
        if (lastEnd < text.length()) {
            builder.append(text, lastEnd, text.length());
        }
    }
}
