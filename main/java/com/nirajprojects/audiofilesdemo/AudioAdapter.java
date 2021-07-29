package com.nirajprojects.audiofilesdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.viewHolder>{

    ArrayList<ModelAudio> songList;
    Context context;
    public OnItemClickListener onClickItemListener;

    public AudioAdapter(ArrayList<ModelAudio> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @NonNull
    @NotNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_style, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull AudioAdapter.viewHolder holder, int position) {
        ModelAudio model = songList.get(position);
        holder.songTitle.setText(model.getSongTitle());
        holder.songArtist.setText(model.getSongArtist());

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView songTitle;
        TextView songArtist;
        ImageView delete, edit;
        public viewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        songTitle = itemView.findViewById(R.id.songTitle);
        songArtist = itemView.findViewById(R.id.songArtist);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickItemListener.onClickItem(getItemCount(),v);
            }
        });
        }
    }

    public void setOnClickItemListener(OnItemClickListener onClickItemListener){
        this.onClickItemListener = onClickItemListener;
    }

    public interface OnItemClickListener{
        void onClickItem(int pos, View v);
    }
}
