package com.mota.marketingagent.ui.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.models.ChatItem;
import com.mota.marketingagent.ui.helpers.MarkdownHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mota.marketingagent.activities.PublishDraftActivity;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OptionClickListener {
        void onOptionClicked(String optionText);
    }

    private static final int VIEW_USER = 1;
    private static final int VIEW_ASSISTANT = 2;
    private static final int VIEW_STATUS = 3;
    private static final int VIEW_WELCOME_OPTIONS = 4;
    private static final int VIEW_TOPIC_SELECTOR = 5;
    private static final int VIEW_FORMAT_SELECTOR = 6;
    private static final int VIEW_CONTENT_DRAFTS = 7;
    private static final int VIEW_SOCIAL_POSTS = 8;
    private static final int VIEW_INFLUENCERS = 9;
    private static final int VIEW_NEXT_STEPS = 10;
    private static final int VIEW_SOURCES = 11;
    private static final int VIEW_POST_OPTIONS = 12;
    private static final int VIEW_IMAGE_RESULTS = 13;
    private static final int VIEW_METRICS = 14;

    private final List<ChatItem> items;
    private final OptionClickListener listener;

    public ChatAdapter(List<ChatItem> items, OptionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatItem item = items.get(position);
        switch (item.type) {
            case USER: return VIEW_USER;
            case ASSISTANT: return VIEW_ASSISTANT;
            case STATUS: return VIEW_STATUS;
            case WELCOME_OPTIONS: return VIEW_WELCOME_OPTIONS;
            case TOPIC_SELECTOR: return VIEW_TOPIC_SELECTOR;
            case FORMAT_SELECTOR: return VIEW_FORMAT_SELECTOR;
            case CONTENT_DRAFTS: return VIEW_CONTENT_DRAFTS;
            case SOCIAL_POSTS: return VIEW_SOCIAL_POSTS;
            case INFLUENCERS: return VIEW_INFLUENCERS;
            case NEXT_STEPS: return VIEW_NEXT_STEPS;
            case SOURCES: return VIEW_SOURCES;
            case POST_OPTIONS: return VIEW_POST_OPTIONS;
            case IMAGE_RESULTS: return VIEW_IMAGE_RESULTS;
            case METRICS: return VIEW_METRICS;
            default: return VIEW_ASSISTANT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_USER:
                return new TextHolder(inf.inflate(R.layout.item_chat_user, parent, false));
            case VIEW_ASSISTANT:
            case VIEW_STATUS:
                return new TextHolder(inf.inflate(R.layout.item_chat_agent, parent, false));
            case VIEW_WELCOME_OPTIONS:
                return new WelcomeOptionsHolder(inf.inflate(R.layout.item_card_welcome_options, parent, false));
            case VIEW_TOPIC_SELECTOR:
                return new TopicSelectorHolder(inf.inflate(R.layout.item_card_topic_selector, parent, false));
            case VIEW_FORMAT_SELECTOR:
                return new FormatSelectorHolder(inf.inflate(R.layout.item_card_format_selector, parent, false));
            case VIEW_CONTENT_DRAFTS:
                return new ContentDraftHolder(inf.inflate(R.layout.item_card_content_draft, parent, false));
            case VIEW_SOCIAL_POSTS:
                return new SocialPostHolder(inf.inflate(R.layout.item_card_social_post, parent, false));
            case VIEW_INFLUENCERS:
                return new InfluencerListHolder(inf.inflate(R.layout.item_card_influencer_list, parent, false));
            case VIEW_NEXT_STEPS:
                return new NextStepsHolder(inf.inflate(R.layout.item_card_next_steps, parent, false));
            case VIEW_SOURCES:
                return new SourcesHolder(inf.inflate(R.layout.item_card_sources, parent, false));
            case VIEW_POST_OPTIONS:
                return new PostOptionsHolder(inf.inflate(R.layout.item_card_post_options, parent, false));
            case VIEW_IMAGE_RESULTS:
                return new ImageResultsHolder(inf.inflate(R.layout.item_card_social_post, parent, false));
            case VIEW_METRICS:
                return new MetricsHolder(inf.inflate(R.layout.item_card_metrics, parent, false));
            default:
                return new TextHolder(inf.inflate(R.layout.item_chat_agent, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = items.get(position);
        if (holder instanceof TextHolder) {
            TextHolder th = (TextHolder) holder;
            if (item.type == ChatItem.Type.ASSISTANT) {
                th.message.setText(MarkdownHelper.render(item.text));
            } else {
                th.message.setText(item.text);
            }
        } else if (holder instanceof WelcomeOptionsHolder) {
            ((WelcomeOptionsHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof TopicSelectorHolder) {
            ((TopicSelectorHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof FormatSelectorHolder) {
            ((FormatSelectorHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof ContentDraftHolder) {
            ((ContentDraftHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof SocialPostHolder) {
            ((SocialPostHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof InfluencerListHolder) {
            ((InfluencerListHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof NextStepsHolder) {
            ((NextStepsHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof SourcesHolder) {
            ((SourcesHolder) holder).bind(item.rawPayload);
        } else if (holder instanceof PostOptionsHolder) {
            ((PostOptionsHolder) holder).bind(item.rawPayload, listener);
        } else if (holder instanceof ImageResultsHolder) {
            ((ImageResultsHolder) holder).bindImages(item.rawPayload);
        } else if (holder instanceof MetricsHolder) {
            ((MetricsHolder) holder).bind(item.html);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    private static int dp(Context ctx, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, ctx.getResources().getDisplayMetrics());
    }

    private static GradientDrawable roundedBg(int color, int radiusDp, Context ctx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(color);
        gd.setCornerRadius(dp(ctx, radiusDp));
        return gd;
    }

    private static GradientDrawable roundedStrokeBg(int fillColor, int strokeColor, int radiusDp, Context ctx) {
        GradientDrawable gd = roundedBg(fillColor, radiusDp, ctx);
        gd.setStroke(dp(ctx, 1), strokeColor);
        return gd;
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        return obj != null && obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : fallback;
    }

    private static int getInt(JsonObject obj, String key, int fallback) {
        try {
            return obj != null && obj.has(key) ? obj.get(key).getAsInt() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double getDouble(JsonObject obj, String key, double fallback) {
        try {
            return obj != null && obj.has(key) ? obj.get(key).getAsDouble() : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String formatNumber(long num) {
        if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0).replace(".0M", "M");
        if (num >= 1_000) return String.format("%.1fK", num / 1_000.0).replace(".0K", "K");
        return String.valueOf(num);
    }

    private static void copyToClipboard(Context ctx, String text) {
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("content", text));
        Toast.makeText(ctx, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    // ─── Color palettes for cards ─────────────────────────────────────────────

    private static final int[][] OPTION_COLORS = {
            {0xFFF0F9FF, 0xFFBAE6FD, 0xFF0891B2}, // show_posts - cyan
            {0xFFF5F3FF, 0xFFDDD6FE, 0xFF7C3AED}, // find_influencers - violet
            {0xFFF0F9FF, 0xFFBAE6FD, 0xFF0284C7}, // manage_ads - sky
            {0xFFF0FDF4, 0xFFBBF7D0, 0xFF059669}, // create_content - emerald
            {0xFFFFF7ED, 0xFFFED7AA, 0xFFEA580C}, // analyze_metrics - orange
    };

    private static final int[][] TOPIC_COLORS = {
            {0xFFF5F3FF, 0xFFDDD6FE, 0xFF7C3AED},
            {0xFFF0F9FF, 0xFFBAE6FD, 0xFF0891B2},
            {0xFFF0FDF4, 0xFFBBF7D0, 0xFF059669},
            {0xFFFFF7ED, 0xFFFED7AA, 0xFFEA580C},
            {0xFFFDF2F8, 0xFFFBCFE8, 0xFFDB2777},
    };

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    static class TextHolder extends RecyclerView.ViewHolder {
        TextView message;
        TextHolder(@NonNull View v) {
            super(v);
            message = v.findViewById(R.id.messageText);
            message.setLineSpacing(0f, 1.3f);
        }
    }

    // ─── Welcome Options ──────────────────────────────────────────────────────

    static class WelcomeOptionsHolder extends RecyclerView.ViewHolder {
        TextView greeting;
        LinearLayout container;

        WelcomeOptionsHolder(@NonNull View v) {
            super(v);
            greeting = v.findViewById(R.id.greetingText);
            container = v.findViewById(R.id.optionsContainer);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();

            greeting.setText(getString(data, "greeting", ""));
            greeting.setVisibility(TextUtils.isEmpty(greeting.getText()) ? View.GONE : View.VISIBLE);
            container.removeAllViews();

            JsonArray options = data.has("options") && data.get("options").isJsonArray()
                    ? data.getAsJsonArray("options") : new JsonArray();

            for (int i = 0; i < options.size(); i++) {
                JsonObject opt = options.get(i).getAsJsonObject();
                int[] colors = OPTION_COLORS[i % OPTION_COLORS.length];
                String title = getString(opt, "title", "Option");
                String desc = getString(opt, "description", "");
                String actionMsg = getString(opt, "action_message", title);

                // Card container
                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(ctx, 14), dp(ctx, 14), dp(ctx, 14), dp(ctx, 14));
                card.setBackground(roundedStrokeBg(colors[0], colors[1], 12, ctx));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dp(ctx, 8);
                card.setLayoutParams(lp);
                card.setClickable(true);
                card.setFocusable(true);
                card.setOnClickListener(v -> listener.onOptionClicked(actionMsg));

                // Icon badge
                TextView icon = new TextView(ctx);
                icon.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36)));
                icon.setGravity(Gravity.CENTER);
                icon.setBackground(roundedBg(colors[2], 10, ctx));
                icon.setTextColor(Color.WHITE);
                icon.setTextSize(14);
                String[] icons = {"👥", "🔍", "📢", "✏️", "📊"};
                icon.setText(icons[i % icons.length]);
                card.addView(icon);

                // Text column
                LinearLayout textCol = new LinearLayout(ctx);
                textCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                textLp.leftMargin = dp(ctx, 12);
                textCol.setLayoutParams(textLp);

                TextView titleTv = new TextView(ctx);
                titleTv.setText(title);
                titleTv.setTextSize(13);
                titleTv.setTypeface(null, Typeface.BOLD);
                titleTv.setTextColor(0xFF1E293B);
                textCol.addView(titleTv);

                if (!TextUtils.isEmpty(desc)) {
                    TextView descTv = new TextView(ctx);
                    descTv.setText(desc);
                    descTv.setTextSize(11);
                    descTv.setTextColor(0xFF64748B);
                    descTv.setMaxLines(2);
                    descTv.setEllipsize(TextUtils.TruncateAt.END);
                    textCol.addView(descTv);
                }
                card.addView(textCol);

                // Arrow
                TextView arrow = new TextView(ctx);
                arrow.setText("→");
                arrow.setTextSize(16);
                arrow.setTextColor(colors[2]);
                card.addView(arrow);

                container.addView(card);
            }
        }
    }

    // ─── Topic Selector ───────────────────────────────────────────────────────

    static class TopicSelectorHolder extends RecyclerView.ViewHolder {
        TextView greeting;
        LinearLayout container;

        TopicSelectorHolder(@NonNull View v) {
            super(v);
            greeting = v.findViewById(R.id.greetingText);
            container = v.findViewById(R.id.topicsContainer);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();

            greeting.setText(getString(data, "greeting", ""));
            greeting.setVisibility(TextUtils.isEmpty(greeting.getText()) ? View.GONE : View.VISIBLE);
            container.removeAllViews();

            JsonArray topics = data.has("topics") && data.get("topics").isJsonArray()
                    ? data.getAsJsonArray("topics") : new JsonArray();

            for (int i = 0; i < topics.size(); i++) {
                JsonElement el = topics.get(i);
                String title, desc = "";
                if (el.isJsonObject()) {
                    title = getString(el.getAsJsonObject(), "title", "Topic " + (i + 1));
                    desc = getString(el.getAsJsonObject(), "description", "");
                } else {
                    title = el.getAsString();
                }

                int[] colors = TOPIC_COLORS[i % TOPIC_COLORS.length];

                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
                card.setBackground(roundedStrokeBg(colors[0], colors[1], 12, ctx));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dp(ctx, 6);
                card.setLayoutParams(lp);
                card.setClickable(true);
                card.setFocusable(true);
                String finalTitle = title;
                card.setOnClickListener(v -> listener.onOptionClicked("Topic: " + finalTitle));

                // Number badge
                TextView num = new TextView(ctx);
                num.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 28), dp(ctx, 28)));
                num.setGravity(Gravity.CENTER);
                num.setBackground(roundedBg(colors[2], 8, ctx));
                num.setTextColor(Color.WHITE);
                num.setTextSize(12);
                num.setTypeface(null, Typeface.BOLD);
                num.setText(String.valueOf(i + 1));
                card.addView(num);

                // Title
                LinearLayout textCol = new LinearLayout(ctx);
                textCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                tLp.leftMargin = dp(ctx, 10);
                textCol.setLayoutParams(tLp);

                TextView titleTv = new TextView(ctx);
                titleTv.setText(title);
                titleTv.setTextSize(12);
                titleTv.setTypeface(null, Typeface.BOLD);
                titleTv.setTextColor(0xFF1E293B);
                titleTv.setMaxLines(2);
                textCol.addView(titleTv);

                if (!TextUtils.isEmpty(desc)) {
                    TextView descTv = new TextView(ctx);
                    descTv.setText(desc);
                    descTv.setTextSize(10);
                    descTv.setTextColor(0xFF64748B);
                    descTv.setMaxLines(2);
                    descTv.setEllipsize(TextUtils.TruncateAt.END);
                    LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dLp.topMargin = dp(ctx, 2);
                    descTv.setLayoutParams(dLp);
                    textCol.addView(descTv);
                }
                card.addView(textCol);

                // Arrow
                TextView arrow = new TextView(ctx);
                arrow.setText("›");
                arrow.setTextSize(20);
                arrow.setTextColor(0xFFCBD5E1);
                card.addView(arrow);

                container.addView(card);
            }
        }
    }

    // ─── Format Selector ──────────────────────────────────────────────────────

    static class FormatSelectorHolder extends RecyclerView.ViewHolder {
        ChipGroup chipGroup;
        EditText themeInput;
        MaterialButton btnCreate;

        FormatSelectorHolder(@NonNull View v) {
            super(v);
            chipGroup = v.findViewById(R.id.formatChipGroup);
            themeInput = v.findViewById(R.id.themeInput);
            btnCreate = v.findViewById(R.id.btnCreate);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();
            chipGroup.removeAllViews();

            String topic = getString(data, "topic", "");
            JsonArray formats = data.has("formats") && data.get("formats").isJsonArray()
                    ? data.getAsJsonArray("formats") : new JsonArray();

            String[][] formatMeta = {
                    {"one_liner", "⚡ One-Liner", "#7C3AED"},
                    {"short_post", "📝 Short Post", "#0891B2"},
                    {"article", "📰 Article", "#059669"},
                    {"short_video", "🎬 Video Script", "#DC2626"},
                    {"instagram_post", "📸 Instagram", "#DB2777"},
            };

            Set<String> selected = new HashSet<>();

            for (String[] meta : formatMeta) {
                boolean found = false;
                for (int i = 0; i < formats.size(); i++) {
                    if (formats.get(i).getAsString().equals(meta[0])) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;

                Chip chip = new Chip(ctx);
                chip.setText(meta[1]);
                chip.setCheckable(true);
                chip.setCheckedIconVisible(true);
                chip.setChipBackgroundColorResource(R.color.slate_50);
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) selected.add(meta[0]); else selected.remove(meta[0]);
                    btnCreate.setEnabled(!selected.isEmpty());
                    btnCreate.setText("Create " + (selected.isEmpty() ? "drafts" : selected.size() + " format" + (selected.size() > 1 ? "s" : "")));
                });
                chipGroup.addView(chip);
            }

            btnCreate.setOnClickListener(v -> {
                StringBuilder labels = new StringBuilder();
                for (String s : selected) {
                    for (String[] m : formatMeta) {
                        if (m[0].equals(s)) {
                            if (labels.length() > 0) labels.append(" + ");
                            labels.append(m[1].substring(2).trim());
                        }
                    }
                }
                String query = "Create " + labels + " for: " + topic;
                String theme = themeInput.getText().toString().trim();
                if (!theme.isEmpty()) query += "\nTheme: " + theme;
                listener.onOptionClicked(query);
            });
        }
    }

    // ─── Content Drafts ───────────────────────────────────────────────────────

    static class ContentDraftHolder extends RecyclerView.ViewHolder {
        TextView topicHeader, researchSummary;
        LinearLayout draftsContainer;

        ContentDraftHolder(@NonNull View v) {
            super(v);
            topicHeader = v.findViewById(R.id.topicHeader);
            researchSummary = v.findViewById(R.id.researchSummary);
            draftsContainer = v.findViewById(R.id.draftsContainer);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();

            String topic = getString(data, "topic", "");
            String summary = getString(data, "research_summary", "");
            topicHeader.setText(topic.isEmpty() ? "Content Drafts" : "📝 " + topic);
            topicHeader.setVisibility(View.VISIBLE);

            if (!summary.isEmpty()) {
                researchSummary.setText(summary);
                researchSummary.setVisibility(View.VISIBLE);
            } else {
                researchSummary.setVisibility(View.GONE);
            }

            draftsContainer.removeAllViews();
            JsonArray drafts = data.has("drafts") && data.get("drafts").isJsonArray()
                    ? data.getAsJsonArray("drafts") : new JsonArray();

            for (int i = 0; i < drafts.size(); i++) {
                JsonObject draft = drafts.get(i).getAsJsonObject();
                String title = getString(draft, "title", "Draft " + (i + 1));
                String content = getString(draft, "content", "");
                String format = getString(draft, "format", "short_post");

                // Draft card
                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12));
                card.setBackground(roundedStrokeBg(Color.WHITE, 0xFFE2E8F0, 12, ctx));
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clp.bottomMargin = dp(ctx, 8);
                card.setLayoutParams(clp);
                card.setElevation(dp(ctx, 2));

                // Format badge + title row
                LinearLayout header = new LinearLayout(ctx);
                header.setOrientation(LinearLayout.HORIZONTAL);
                header.setGravity(Gravity.CENTER_VERTICAL);

                // Format badge
                TextView badge = new TextView(ctx);
                badge.setText(format.replace("_", " ").toUpperCase());
                badge.setTextSize(9);
                badge.setTypeface(null, Typeface.BOLD);
                badge.setTextColor(getFormatColor(format));
                badge.setPadding(dp(ctx, 8), dp(ctx, 3), dp(ctx, 8), dp(ctx, 3));
                badge.setBackground(roundedBg(getFormatBg(format), 6, ctx));
                header.addView(badge);

                // Title
                TextView titleTv = new TextView(ctx);
                titleTv.setText(title);
                titleTv.setTextSize(13);
                titleTv.setTypeface(null, Typeface.BOLD);
                titleTv.setTextColor(0xFF1E293B);
                LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                titleLp.leftMargin = dp(ctx, 8);
                titleTv.setLayoutParams(titleLp);
                header.addView(titleTv);
                card.addView(header);

                // Content
                TextView contentTv = new TextView(ctx);
                contentTv.setText(MarkdownHelper.render(content));
                contentTv.setTextSize(13);
                contentTv.setTextColor(0xFF334155);
                contentTv.setLineSpacing(0f, 1.4f);
                LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                contentLp.topMargin = dp(ctx, 8);
                contentTv.setLayoutParams(contentLp);
                card.addView(contentTv);

                // Action buttons row
                LinearLayout actions = new LinearLayout(ctx);
                actions.setOrientation(LinearLayout.HORIZONTAL);
                actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams actLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                actLp.topMargin = dp(ctx, 8);
                actions.setLayoutParams(actLp);

                // Copy button
                TextView copyBtn = createActionButton(ctx, "📋 Copy", 0xFF64748B);
                String finalContent = content;
                copyBtn.setOnClickListener(v -> copyToClipboard(ctx, finalContent));
                actions.addView(copyBtn);

                // LinkedIn button
                TextView linkedinBtn = createActionButton(ctx, "in  LinkedIn", 0xFFFFFFFF);
                linkedinBtn.setBackground(roundedBg(0xFF0A66C2, 8, ctx)); // LinkedIn blue
                linkedinBtn.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams linLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, dp(ctx, 32));
                linLp.leftMargin = dp(ctx, 6);
                linkedinBtn.setLayoutParams(linLp);
                String pubContent = content;
                linkedinBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(ctx, PublishDraftActivity.class);
                    // For now, the PublishDraftActivity acts as the gateway to the platform editor
                    intent.putExtra("draft_content", pubContent);
                    ctx.startActivity(intent);
                });
                actions.addView(linkedinBtn);

                // Twitter button
                TextView twitterBtn = createActionButton(ctx, "𝕏 Twitter", 0xFFFFFFFF);
                twitterBtn.setBackground(roundedBg(0xFF0F1419, 8, ctx)); // Twitter Black
                twitterBtn.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams twpLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, dp(ctx, 32));
                twpLp.leftMargin = dp(ctx, 6);
                twitterBtn.setLayoutParams(twpLp);
                twitterBtn.setOnClickListener(v -> {
                    Intent intent = new Intent(ctx, PublishDraftActivity.class);
                    intent.putExtra("draft_content", pubContent);
                    ctx.startActivity(intent);
                });
                actions.addView(twitterBtn);

                card.addView(actions);
                draftsContainer.addView(card);
            }
        }

        private int getFormatColor(String format) {
            switch (format) {
                case "one_liner": return 0xFF7C3AED;
                case "short_post": return 0xFF0891B2;
                case "article": return 0xFF059669;
                case "short_video": return 0xFFDC2626;
                case "instagram_post": return 0xFFDB2777;
                default: return 0xFF64748B;
            }
        }

        private int getFormatBg(String format) {
            switch (format) {
                case "one_liner": return 0xFFF5F3FF;
                case "short_post": return 0xFFF0F9FF;
                case "article": return 0xFFF0FDF4;
                case "short_video": return 0xFFFEF2F2;
                case "instagram_post": return 0xFFFDF2F8;
                default: return 0xFFF1F5F9;
            }
        }
    }

    // ─── Social Posts Carousel ─────────────────────────────────────────────────

    static class SocialPostHolder extends RecyclerView.ViewHolder {
        LinearLayout postsContainer;

        SocialPostHolder(@NonNull View v) {
            super(v);
            postsContainer = v.findViewById(R.id.postsContainer);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();
            postsContainer.removeAllViews();

            JsonArray posts = data.has("posts") && data.get("posts").isJsonArray()
                    ? data.getAsJsonArray("posts") : new JsonArray();

            for (int i = 0; i < posts.size(); i++) {
                JsonObject post = posts.get(i).getAsJsonObject();
                String contentType = getString(post, "content_type", "linkedin_post");
                boolean isTwitter = contentType.contains("twitter");
                boolean isLinkedin = contentType.contains("linkedin") || !isTwitter;

                String content = getString(post, "content", getString(post, "text", ""));

                // Extract metadata sub-object (matches web LinkedinPostCard.js structure)
                JsonObject metadata = post.has("metadata") && post.get("metadata").isJsonObject()
                        ? post.getAsJsonObject("metadata") : null;

                // Author: check metadata.author → top-level author
                String author = getString(metadata, "author", getString(post, "author", ""));
                if (author.isEmpty()) author = getString(metadata, "author_name", "");

                // Author description: metadata.author_description → top-level
                String authorDesc = getString(metadata, "author_description", getString(post, "author_description", ""));

                // Followers count from metadata
                String followers = getString(metadata, "followers", "");

                // Date: metadata.published_date → top-level date
                String date = getString(metadata, "published_date", getString(post, "date", getString(post, "published_date", "")));

                String url = getString(post, "url", "");

                // Engagement
                JsonObject engagement = post.has("engagement") && post.get("engagement").isJsonObject()
                        ? post.getAsJsonObject("engagement") : null;
                int likes = getInt(engagement, "likes", 0);
                int comments = getInt(engagement, "comments", 0);
                int shares = getInt(engagement, "shares", 0);

                // Card
                CardView cardView = new CardView(ctx);
                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(dp(ctx, 300), LinearLayout.LayoutParams.WRAP_CONTENT);
                cardLp.rightMargin = dp(ctx, 10);
                cardView.setLayoutParams(cardLp);
                cardView.setRadius(dp(ctx, 12));
                cardView.setCardElevation(dp(ctx, 3));
                cardView.setCardBackgroundColor(Color.WHITE);
                cardView.setContentPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12));

                LinearLayout innerLayout = new LinearLayout(ctx);
                innerLayout.setOrientation(LinearLayout.VERTICAL);
                innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                // Author row
                LinearLayout authorRow = new LinearLayout(ctx);
                authorRow.setOrientation(LinearLayout.HORIZONTAL);
                authorRow.setGravity(Gravity.CENTER_VERTICAL);

                // Avatar initial
                TextView avatar = new TextView(ctx);
                avatar.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36)));
                avatar.setGravity(Gravity.CENTER);
                avatar.setText(author.isEmpty() ? "?" : String.valueOf(author.charAt(0)).toUpperCase());
                avatar.setTextSize(14);
                avatar.setTypeface(null, Typeface.BOLD);
                int avatarBg = isTwitter ? 0xFF0F1419 : 0xFF0A66C2;
                avatar.setTextColor(Color.WHITE);
                avatar.setBackground(roundedBg(avatarBg, 18, ctx));
                authorRow.addView(avatar);

                // Author info column
                LinearLayout authorInfo = new LinearLayout(ctx);
                authorInfo.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams aiLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                aiLp.leftMargin = dp(ctx, 10);
                authorInfo.setLayoutParams(aiLp);

                TextView authorName = new TextView(ctx);
                authorName.setText(author.isEmpty() ? "Unknown Author" : author);
                authorName.setTextSize(13);
                authorName.setTypeface(null, Typeface.BOLD);
                authorName.setTextColor(0xFF0F172A);
                authorName.setSingleLine(true);
                authorName.setEllipsize(TextUtils.TruncateAt.END);
                authorInfo.addView(authorName);

                // Author description (headline)
                if (!authorDesc.isEmpty()) {
                    TextView descTv = new TextView(ctx);
                    descTv.setText(authorDesc);
                    descTv.setTextSize(11);
                    descTv.setTextColor(0xFF64748B);
                    descTv.setSingleLine(true);
                    descTv.setEllipsize(TextUtils.TruncateAt.END);
                    authorInfo.addView(descTv);
                }

                // Date + followers line
                StringBuilder subLine = new StringBuilder();
                if (!date.isEmpty()) subLine.append(date);
                if (!followers.isEmpty()) {
                    if (subLine.length() > 0) subLine.append(" · ");
                    subLine.append(followers).append(" followers");
                }
                if (subLine.length() > 0) {
                    TextView dateTv = new TextView(ctx);
                    dateTv.setText(subLine.toString());
                    dateTv.setTextSize(11);
                    dateTv.setTextColor(0xFF94A3B8);
                    dateTv.setSingleLine(true);
                    authorInfo.addView(dateTv);
                }
                authorRow.addView(authorInfo);

                // Platform icon
                TextView platformIcon = new TextView(ctx);
                platformIcon.setText(isTwitter ? "𝕏" : "in");
                platformIcon.setTextSize(isTwitter ? 16 : 14);
                platformIcon.setTypeface(null, Typeface.BOLD);
                platformIcon.setTextColor(isTwitter ? 0xFF0F1419 : 0xFF0A66C2);
                authorRow.addView(platformIcon);

                innerLayout.addView(authorRow);

                // Content
                TextView contentTv = new TextView(ctx);
                contentTv.setText(content.replace("\\n", "\n"));
                contentTv.setTextSize(13);
                contentTv.setTextColor(0xFF1E293B);
                contentTv.setLineSpacing(0f, 1.4f);
                contentTv.setMaxLines(10);
                contentTv.setEllipsize(TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams ctLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ctLp.topMargin = dp(ctx, 10);
                contentTv.setLayoutParams(ctLp);
                innerLayout.addView(contentTv);

                // Engagement row
                if (likes > 0 || comments > 0 || shares > 0) {
                    LinearLayout engRow = new LinearLayout(ctx);
                    engRow.setOrientation(LinearLayout.HORIZONTAL);
                    engRow.setGravity(Gravity.CENTER_VERTICAL);
                    LinearLayout.LayoutParams eLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    eLp.topMargin = dp(ctx, 10);
                    engRow.setLayoutParams(eLp);

                    if (likes > 0) engRow.addView(engStat(ctx, "❤️ " + formatNumber(likes)));
                    if (comments > 0) engRow.addView(engStat(ctx, "💬 " + formatNumber(comments)));
                    if (shares > 0) engRow.addView(engStat(ctx, "🔄 " + formatNumber(shares)));

                    innerLayout.addView(engRow);
                }

                // Action bar
                LinearLayout actionBar = new LinearLayout(ctx);
                actionBar.setOrientation(LinearLayout.HORIZONTAL);
                actionBar.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                LinearLayout.LayoutParams abLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                abLp.topMargin = dp(ctx, 8);
                actionBar.setLayoutParams(abLp);

                // Copy
                TextView copyBtn = createActionButton(ctx, "📋 Copy", 0xFF64748B);
                String fc = content;
                copyBtn.setOnClickListener(v -> copyToClipboard(ctx, fc));
                actionBar.addView(copyBtn);

                // Open in browser
                if (!url.isEmpty()) {
                    TextView openBtn = createActionButton(ctx, "🔗 Open", 0xFF3B82F6);
                    LinearLayout.LayoutParams olp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, dp(ctx, 30));
                    olp.leftMargin = dp(ctx, 6);
                    openBtn.setLayoutParams(olp);
                    String finalUrl = url;
                    openBtn.setOnClickListener(v -> {
                        try {
                            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)));
                        } catch (Exception ignored) {}
                    });
                    actionBar.addView(openBtn);
                }

                innerLayout.addView(actionBar);
                cardView.addView(innerLayout);
                postsContainer.addView(cardView);
            }
        }

        private TextView engStat(Context ctx, String text) {
            TextView tv = new TextView(ctx);
            tv.setText(text);
            tv.setTextSize(11);
            tv.setTextColor(0xFF94A3B8);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = dp(ctx, 14);
            tv.setLayoutParams(lp);
            return tv;
        }
    }

    // ─── Influencer List ──────────────────────────────────────────────────────

    static class InfluencerListHolder extends RecyclerView.ViewHolder {
        TextView headerTitle, headerMeta;
        LinearLayout influencerContainer, platformTabs;
        HorizontalScrollView platformTabsScroll;

        InfluencerListHolder(@NonNull View v) {
            super(v);
            headerTitle = v.findViewById(R.id.headerTitle);
            headerMeta = v.findViewById(R.id.headerMeta);
            influencerContainer = v.findViewById(R.id.influencerContainer);
            platformTabs = v.findViewById(R.id.platformTabs);
            platformTabsScroll = v.findViewById(R.id.platformTabsScroll);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();

            String query = getString(data, "query", "");
            headerTitle.setText("Top Influencers" + (query.isEmpty() ? "" : " in \"" + query + "\""));

            JsonArray influencers = data.has("influencers") && data.get("influencers").isJsonArray()
                    ? data.getAsJsonArray("influencers") : new JsonArray();

            // Multi-platform support
            JsonArray platforms = data.has("platforms") && data.get("platforms").isJsonArray()
                    ? data.getAsJsonArray("platforms") : null;

            if (platforms != null && platforms.size() > 0) {
                platformTabsScroll.setVisibility(View.VISIBLE);
                platformTabs.removeAllViews();
                for (int i = 0; i < platforms.size(); i++) {
                    JsonObject pData = platforms.get(i).getAsJsonObject();
                    String pName = getString(pData, "platform", "linkedin");
                    TextView tab = new TextView(ctx);
                    tab.setText(pName.substring(0, 1).toUpperCase() + pName.substring(1));
                    tab.setTextSize(12);
                    tab.setTypeface(null, Typeface.BOLD);
                    tab.setPadding(dp(ctx, 12), dp(ctx, 6), dp(ctx, 12), dp(ctx, 6));
                    tab.setBackground(roundedBg(i == 0 ? 0xFFEEF3F8 : 0xFFF1F5F9, 8, ctx));
                    tab.setTextColor(i == 0 ? 0xFF0A66C2 : 0xFF64748B);
                    LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    tlp.rightMargin = dp(ctx, 6);
                    tab.setLayoutParams(tlp);
                    platformTabs.addView(tab);
                }
                // Show first platform's influencers
                if (platforms.get(0).getAsJsonObject().has("influencers")) {
                    influencers = platforms.get(0).getAsJsonObject().getAsJsonArray("influencers");
                }
            } else {
                platformTabsScroll.setVisibility(View.GONE);
            }

            headerMeta.setText(influencers.size() + " found");
            influencerContainer.removeAllViews();

            int[][] rankColors = {
                    {0xFFFFFBEB, 0xFFFDE68A, 0xFFF59E0B}, // Gold
                    {0xFFF8FAFC, 0xFFCBD5E1, 0xFF94A3B8}, // Silver
                    {0xFFFFF7ED, 0xFFFED7AA, 0xFFEA580C}, // Bronze
            };

            for (int i = 0; i < influencers.size(); i++) {
                JsonObject inf = influencers.get(i).getAsJsonObject();
                String name = getString(inf, "name", "Unknown");
                String desc = getString(inf, "description", "");
                long followers = (long) getDouble(inf, "followers", 0);
                long totalLikes = (long) getDouble(inf, "total_likes", 0);
                double engRate = getDouble(inf, "engagement_rate", 0);
                String profileUrl = getString(inf, "profile_url", "");

                int[] colors = i < 3 ? rankColors[i] : new int[]{0xFFF8FAFC, 0xFFE2E8F0, 0xFF6366F1};

                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(ctx, 12), dp(ctx, 12), dp(ctx, 12), dp(ctx, 12));
                card.setBackground(roundedStrokeBg(colors[0], colors[1], 12, ctx));
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clp.bottomMargin = dp(ctx, 6);
                card.setLayoutParams(clp);

                // Rank badge
                TextView rank = new TextView(ctx);
                rank.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 28), dp(ctx, 28)));
                rank.setGravity(Gravity.CENTER);
                rank.setBackground(roundedBg(colors[2], 8, ctx));
                rank.setTextColor(Color.WHITE);
                rank.setTextSize(11);
                rank.setTypeface(null, Typeface.BOLD);
                rank.setText(String.valueOf(i + 1));
                card.addView(rank);

                // Avatar
                TextView avatarTv = new TextView(ctx);
                avatarTv.setLayoutParams(new LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36)));
                avatarTv.setGravity(Gravity.CENTER);
                avatarTv.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
                avatarTv.setTextSize(14);
                avatarTv.setTextColor(0xFF64748B);
                avatarTv.setBackground(roundedBg(0xFFE2E8F0, 18, ctx));
                LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(dp(ctx, 36), dp(ctx, 36));
                avLp.leftMargin = dp(ctx, 8);
                avatarTv.setLayoutParams(avLp);
                card.addView(avatarTv);

                // Info column
                LinearLayout infoCol = new LinearLayout(ctx);
                infoCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams iclp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                iclp.leftMargin = dp(ctx, 10);
                infoCol.setLayoutParams(iclp);

                TextView nameTv = new TextView(ctx);
                nameTv.setText(name);
                nameTv.setTextSize(13);
                nameTv.setTypeface(null, Typeface.BOLD);
                nameTv.setTextColor(0xFF1E293B);
                nameTv.setSingleLine(true);
                nameTv.setEllipsize(TextUtils.TruncateAt.END);
                infoCol.addView(nameTv);

                if (!desc.isEmpty()) {
                    TextView descTv = new TextView(ctx);
                    descTv.setText(desc);
                    descTv.setTextSize(10);
                    descTv.setTextColor(0xFF64748B);
                    descTv.setMaxLines(1);
                    descTv.setEllipsize(TextUtils.TruncateAt.END);
                    infoCol.addView(descTv);
                }
                card.addView(infoCol);

                // Stats column
                LinearLayout statsCol = new LinearLayout(ctx);
                statsCol.setOrientation(LinearLayout.VERTICAL);
                statsCol.setGravity(Gravity.END);

                if (followers > 0) {
                    TextView fTv = new TextView(ctx);
                    fTv.setText("👥 " + formatNumber(followers));
                    fTv.setTextSize(10);
                    fTv.setTextColor(0xFF64748B);
                    statsCol.addView(fTv);
                }
                if (totalLikes > 0) {
                    TextView lTv = new TextView(ctx);
                    lTv.setText("❤️ " + formatNumber(totalLikes));
                    lTv.setTextSize(10);
                    lTv.setTextColor(0xFFFB7185);
                    statsCol.addView(lTv);
                }
                if (engRate > 0) {
                    TextView eTv = new TextView(ctx);
                    eTv.setText(String.format("%.1f%%", engRate));
                    eTv.setTextSize(10);
                    eTv.setTypeface(null, Typeface.BOLD);
                    eTv.setTextColor(engRate > 1 ? 0xFF059669 : 0xFF64748B);
                    eTv.setPadding(dp(ctx, 6), dp(ctx, 2), dp(ctx, 6), dp(ctx, 2));
                    eTv.setBackground(roundedBg(engRate > 1 ? 0xFFD1FAE5 : 0xFFF1F5F9, 10, ctx));
                    statsCol.addView(eTv);
                }
                card.addView(statsCol);

                // Click to view posts
                if (!profileUrl.isEmpty()) {
                    card.setClickable(true);
                    card.setFocusable(true);
                    card.setOnClickListener(v -> {
                        try {
                            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl)));
                        } catch (Exception ignored) {}
                    });
                } else {
                    String fName = name;
                    card.setClickable(true);
                    card.setOnClickListener(v -> listener.onOptionClicked("Show posts by " + fName));
                }

                influencerContainer.addView(card);
            }
        }
    }

    // ─── Next Steps ───────────────────────────────────────────────────────────

    static class NextStepsHolder extends RecyclerView.ViewHolder {
        LinearLayout stepsContainer;

        NextStepsHolder(@NonNull View v) {
            super(v);
            stepsContainer = v.findViewById(R.id.stepsContainer);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();
            stepsContainer.removeAllViews();

            JsonArray steps = null;
            if (data.has("next_steps") && data.get("next_steps").isJsonArray()) {
                steps = data.getAsJsonArray("next_steps");
            } else if (data.has("steps") && data.get("steps").isJsonArray()) {
                steps = data.getAsJsonArray("steps");
            }
            if (steps == null) return;

            for (int i = 0; i < steps.size(); i++) {
                String step = steps.get(i).getAsString();

                TextView chip = new TextView(ctx);
                chip.setText("💡 " + step);
                chip.setTextSize(12);
                chip.setTextColor(0xFF1E293B);
                chip.setPadding(dp(ctx, 14), dp(ctx, 10), dp(ctx, 14), dp(ctx, 10));
                chip.setBackground(roundedStrokeBg(0xFFF8FAFC, 0xFFE2E8F0, 10, ctx));
                chip.setMaxLines(2);
                chip.setEllipsize(TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                clp.rightMargin = dp(ctx, 8);
                chip.setLayoutParams(clp);
                chip.setClickable(true);
                chip.setFocusable(true);
                chip.setOnClickListener(v -> listener.onOptionClicked(step));

                stepsContainer.addView(chip);
            }
        }
    }

    // ─── Sources ──────────────────────────────────────────────────────────────

    static class SourcesHolder extends RecyclerView.ViewHolder {
        LinearLayout sourcesContainer;

        SourcesHolder(@NonNull View v) {
            super(v);
            sourcesContainer = v.findViewById(R.id.sourcesContainer);
        }

        void bind(JsonObject data) {
            if (data == null) return;
            Context ctx = itemView.getContext();
            sourcesContainer.removeAllViews();

            JsonArray sources = data.has("sources") && data.get("sources").isJsonArray()
                    ? data.getAsJsonArray("sources") : new JsonArray();

            for (int i = 0; i < sources.size(); i++) {
                JsonObject src = sources.get(i).getAsJsonObject();
                String title = getString(src, "title", "Source");
                String url = getString(src, "url", "");

                String hostname = url;
                try {
                    hostname = Uri.parse(url).getHost();
                    if (hostname == null) hostname = url;
                } catch (Exception e) { /* ignore */ }

                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));
                card.setBackground(roundedStrokeBg(Color.WHITE, 0xFFE2E8F0, 10, ctx));
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(ctx, 180), LinearLayout.LayoutParams.WRAP_CONTENT);
                clp.rightMargin = dp(ctx, 8);
                card.setLayoutParams(clp);

                // Icon + host
                TextView hostTv = new TextView(ctx);
                hostTv.setText("🔗 " + hostname);
                hostTv.setTextSize(10);
                hostTv.setTextColor(0xFF3B82F6);
                hostTv.setSingleLine(true);
                hostTv.setEllipsize(TextUtils.TruncateAt.END);
                card.addView(hostTv);

                // Title
                TextView titleTv = new TextView(ctx);
                titleTv.setText(title);
                titleTv.setTextSize(12);
                titleTv.setTypeface(null, Typeface.BOLD);
                titleTv.setTextColor(0xFF1E293B);
                titleTv.setMaxLines(2);
                titleTv.setEllipsize(TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tlp.topMargin = dp(ctx, 4);
                titleTv.setLayoutParams(tlp);
                card.addView(titleTv);

                if (!url.isEmpty()) {
                    card.setClickable(true);
                    card.setFocusable(true);
                    String finalUrl = url;
                    card.setOnClickListener(v -> {
                        try {
                            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)));
                        } catch (Exception ignored) {}
                    });
                }

                sourcesContainer.addView(card);
            }
        }
    }

    // ─── Post Options ─────────────────────────────────────────────────────────

    static class PostOptionsHolder extends RecyclerView.ViewHolder {
        TextView greeting;
        LinearLayout container;

        PostOptionsHolder(@NonNull View v) {
            super(v);
            greeting = v.findViewById(R.id.greetingText);
            container = v.findViewById(R.id.optionsContainer);
        }

        void bind(JsonObject data, OptionClickListener listener) {
            if (data == null) return;
            Context ctx = itemView.getContext();

            greeting.setText(getString(data, "greeting", ""));
            greeting.setVisibility(TextUtils.isEmpty(greeting.getText()) ? View.GONE : View.VISIBLE);
            container.removeAllViews();

            JsonArray options = data.has("options") && data.get("options").isJsonArray()
                    ? data.getAsJsonArray("options") : new JsonArray();

            for (int i = 0; i < options.size(); i++) {
                JsonObject opt = options.get(i).getAsJsonObject();
                String title = getString(opt, "title", "Option");
                String desc = getString(opt, "description", "");
                String actionMsg = getString(opt, "action_message", title);
                int[] colors = OPTION_COLORS[i % OPTION_COLORS.length];

                LinearLayout card = new LinearLayout(ctx);
                card.setOrientation(LinearLayout.HORIZONTAL);
                card.setGravity(Gravity.CENTER_VERTICAL);
                card.setPadding(dp(ctx, 14), dp(ctx, 12), dp(ctx, 14), dp(ctx, 12));
                card.setBackground(roundedStrokeBg(colors[0], colors[1], 12, ctx));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = dp(ctx, 6);
                card.setLayoutParams(lp);
                card.setClickable(true);
                card.setFocusable(true);
                card.setOnClickListener(v -> listener.onOptionClicked(actionMsg));

                // Title + Desc
                LinearLayout textCol = new LinearLayout(ctx);
                textCol.setOrientation(LinearLayout.VERTICAL);
                textCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView titleTv = new TextView(ctx);
                titleTv.setText(title);
                titleTv.setTextSize(13);
                titleTv.setTypeface(null, Typeface.BOLD);
                titleTv.setTextColor(0xFF1E293B);
                textCol.addView(titleTv);

                if (!TextUtils.isEmpty(desc)) {
                    TextView descTv = new TextView(ctx);
                    descTv.setText(desc);
                    descTv.setTextSize(11);
                    descTv.setTextColor(0xFF64748B);
                    textCol.addView(descTv);
                }
                card.addView(textCol);

                TextView arrow = new TextView(ctx);
                arrow.setText("→");
                arrow.setTextSize(16);
                arrow.setTextColor(colors[2]);
                card.addView(arrow);

                container.addView(card);
            }
        }
    }

    // ─── Image Results ────────────────────────────────────────────────────────

    static class ImageResultsHolder extends RecyclerView.ViewHolder {
        LinearLayout postsContainer;

        ImageResultsHolder(@NonNull View v) {
            super(v);
            postsContainer = v.findViewById(R.id.postsContainer);
        }

        void bindImages(JsonObject data) {
            if (data == null) return;
            Context ctx = itemView.getContext();
            postsContainer.removeAllViews();

            String bestImage = getString(data, "best_image", "");
            JsonArray others = data.has("other_options") && data.get("other_options").isJsonArray()
                    ? data.getAsJsonArray("other_options") : new JsonArray();

            List<String> allImages = new ArrayList<>();
            if (!bestImage.isEmpty()) allImages.add(bestImage);
            for (int i = 0; i < others.size(); i++) {
                allImages.add(others.get(i).getAsString());
            }

            for (int i = 0; i < allImages.size(); i++) {
                String url = allImages.get(i);
                CardView card = new CardView(ctx);
                LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(ctx, 200), dp(ctx, 150));
                clp.rightMargin = dp(ctx, 8);
                card.setLayoutParams(clp);
                card.setRadius(dp(ctx, 12));
                card.setCardElevation(dp(ctx, 2));

                ImageView img = new ImageView(ctx);
                img.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                try {
                    Glide.with(ctx).load(url).centerCrop().into(img);
                } catch (Exception ignored) {}

                card.addView(img);

                // Best image badge
                if (i == 0) {
                    TextView badge = new TextView(ctx);
                    badge.setText("⭐ Best");
                    badge.setTextSize(9);
                    badge.setTextColor(Color.WHITE);
                    badge.setPadding(dp(ctx, 6), dp(ctx, 2), dp(ctx, 6), dp(ctx, 2));
                    badge.setBackground(roundedBg(0xFF7C3AED, 6, ctx));
                    FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    blp.setMargins(dp(ctx, 8), dp(ctx, 8), 0, 0);
                    badge.setLayoutParams(blp);
                    card.addView(badge);
                }

                postsContainer.addView(card);
            }
        }
    }

    // ─── Metrics (HTML WebView) ───────────────────────────────────────────────

    static class MetricsHolder extends RecyclerView.ViewHolder {
        WebView webView;

        MetricsHolder(@NonNull View v) {
            super(v);
            webView = v.findViewById(R.id.metricsWebView);
        }

        void bind(String html) {
            if (TextUtils.isEmpty(html)) {
                webView.loadData("<html><body>No metrics available</body></html>", "text/html", "UTF-8");
            } else {
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            }
        }
    }

    // ─── Shared helper for action buttons ─────────────────────────────────────

    private static TextView createActionButton(Context ctx, String text, int textColor) {
        TextView btn = new TextView(ctx);
        btn.setText(text);
        btn.setTextSize(11);
        btn.setTextColor(textColor);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setPadding(dp(ctx, 10), dp(ctx, 6), dp(ctx, 10), dp(ctx, 6));
        btn.setBackground(roundedStrokeBg(Color.WHITE, 0xFFE2E8F0, 8, ctx));
        btn.setGravity(Gravity.CENTER);
        btn.setClickable(true);
        btn.setFocusable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(ctx, 30));
        btn.setLayoutParams(lp);
        return btn;
    }
}
