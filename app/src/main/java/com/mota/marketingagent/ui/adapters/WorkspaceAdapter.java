package com.mota.marketingagent.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mota.marketingagent.R;
import com.mota.marketingagent.data.models.WorkspaceItem;
import java.util.List;

public class WorkspaceAdapter extends RecyclerView.Adapter<WorkspaceAdapter.WorkspaceViewHolder> {
    public interface WorkspaceClickListener {
        void onWorkspaceClick(WorkspaceItem item);
    }

    private final List<WorkspaceItem> items;
    private final WorkspaceClickListener listener;

    public WorkspaceAdapter(List<WorkspaceItem> items, WorkspaceClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, int position) {
        WorkspaceItem item = items.get(position);
        holder.name.setText(item.workspaceName);
        holder.role.setText("Role: " + item.userRole);
        holder.itemView.setOnClickListener(v -> listener.onWorkspaceClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WorkspaceViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView role;

        WorkspaceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.workspaceName);
            role = itemView.findViewById(R.id.workspaceRole);
        }
    }
}
