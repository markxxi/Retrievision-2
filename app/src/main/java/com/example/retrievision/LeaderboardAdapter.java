package com.example.retrievision;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    // Use typed ArrayLists to avoid casting issues
    private ArrayList<String> boardImg;
    private ArrayList<String> uniqueName;
    private ArrayList<String> countName;

    public LeaderboardAdapter(ArrayList<String> boardImg, ArrayList<String> uniqueName, ArrayList<String> countName) {
        this.boardImg = boardImg;
        this.uniqueName = uniqueName;
        this.countName = countName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String profilePicUrl = String.valueOf(boardImg.get(position));
        Glide.with(holder.images.getContext())
                .load(profilePicUrl)
                .placeholder(R.drawable.logo_ret_rev)
                .into(holder.images);

        holder.text.setText(uniqueName.get(position));
        holder.text1.setText(countName.get(position));
    }

    @Override
    public int getItemCount() {
        return boardImg.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView images;
        TextView text, text1;

        public ViewHolder(View view) {
            super(view);
            images = view.findViewById(R.id.boardImg);
            text = view.findViewById(R.id.uniqueName);
            text1 = view.findViewById(R.id.countName);
        }
    }
}
