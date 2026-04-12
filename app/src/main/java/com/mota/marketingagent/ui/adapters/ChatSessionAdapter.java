package com.mota.marketingagent.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.models.ChatSessionItem;
import java.util.List;

public class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.SessionHolder> {
    public interface SessionClickListener {
        void onSessionClicked(ChatSessionItem item);
    }

    private final List<ChatSessionItem> items;
    private final SessionClickListener listener;

    public ChatSessionAdapter(List<ChatSessionItem> items, SessionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
        return new SessionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionHolder holder, int position) {
        ChatSessionItem item = items.get(position);
        holder.title.setText(item.title == null || item.title.trim().isEmpty() ? "Chat Session" : item.title);
        String meta = "Messages: " + item.messageCount;
        if (item.updatedAt != null && !item.updatedAt.trim().isEmpty()) {
            meta = meta + "  •  " + item.updatedAt;
        }
        holder.meta.setText(meta);
        holder.itemView.setOnClickListener(v -> listener.onSessionClicked(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SessionHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView meta;

        SessionHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.sessionTitle);
            meta = itemView.findViewById(R.id.sessionMeta);
        }
    }
}
