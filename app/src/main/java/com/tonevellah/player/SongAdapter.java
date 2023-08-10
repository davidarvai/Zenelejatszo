package com.tonevellah.player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Context context;

    List<Song> songs;

    ExoPlayer player;

    ConstraintLayout playerView;

    public SongAdapter(Context context, List<Song> songs,ExoPlayer player, ConstraintLayout playerView) {
        this.context = context;
        this.songs = songs;
        this.player =  player;
        this.playerView = playerView;
    }

// Uj nezet letrehozasa a listaelemek megjelenítesehez

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item,parent,false);

        return new SongViewHolder(view);
    }

    // Adatok megjelenítése a nézetben
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

// cim es adatok megjelenitese

        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;

        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(formatDuration(song.getDuration()));
        viewHolder.sizeHolder.setText(formatSize(song.getSize()));

        Uri artworkUri = song.getArtworkUri();

        //hater hasznalata

        if(artworkUri!=null){
            viewHolder.artworkHolder.setImageURI(artworkUri);

            if(viewHolder.artworkHolder.getDrawable()==null){
                viewHolder.artworkHolder.setImageResource(R.drawable.df);
            }
        }


   //katintaskezelo
        viewHolder.itemView.setOnClickListener(view ->{
            if(!player.isPlaying()){
                player.setMediaItems(getMediaItems(),position,0);
            }else{
                player.pause();
                player.seekTo(position,0);
            }

            player.prepare();
            player.play();
            Toast.makeText(context,song.getTitle(),Toast.LENGTH_SHORT).show();


            playerView.setVisibility(View.VISIBLE);

            //elenorzes hogy a zene audio engedejezese bisztositott
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                //request the  record
               // ((MainActivity)context).recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                //Verzio hiba
            }
        });

    }

    //adatokat tartalmazo lista
    private List<MediaItem> getMediaItems() {
        List<MediaItem> mediaItems = new ArrayList<>();

        for(Song song : songs){
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUri())
                    .setMediaMetadata(getMetadata(song))
                    .build();
            mediaItems.add(mediaItem);
        }
        return mediaItems;
    }

    //adatok lekerese
    private MediaMetadata getMetadata(Song song) {
        return new MediaMetadata.Builder()
                .setTitle(song.getTitle())
                .setArtworkUri(song.getArtworkUri())
                .build();
    }

    //dalnezet
    public static class SongViewHolder extends RecyclerView.ViewHolder{
        ImageView artworkHolder;

        TextView titleHolder,durationHolder,sizeHolder;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);


            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);


        }


    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterSongs(List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }


    private String formatDuration(int duration) {
        int minutes = duration / 1000 / 60;
        int seconds = (duration / 1000) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private String formatSize(int size) {
        float sizeInMB = (float) size / (1024 * 1024);
        return String.format(Locale.getDefault(), "%.2f MB", sizeInMB);
    }
}
