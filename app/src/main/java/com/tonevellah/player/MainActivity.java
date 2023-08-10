package com.tonevellah.player;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chibde.visualizer.BarVisualizer;
import com.chibde.visualizer.CircleBarVisualizer;
import com.chibde.visualizer.LineBarVisualizer;
import com.chibde.visualizer.LineVisualizer;
import com.jgabrielfreitas.core.BlurImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

@UnstableApi public class MainActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }

RecyclerView recyclerView;
    SongAdapter songAdapter;
    List<Song> allSongs = new ArrayList<>();
    ActivityResultLauncher<String> storagePermissionLauncher;
    final String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    ExoPlayer player;

    ActivityResultLauncher<String> recordAudioPermissionLauncher;
    final String recordAudioPermission = Manifest.permission.RECORD_AUDIO;
    ConstraintLayout playerView;
    TextView playerCloseBtn;

    //controls
    TextView songNameView,skipPreviousBtn, skipNextBtn, playedPauseBtn, repeatModeBtn, playlistBtn;
    TextView homeSongNameView, homeSkipPreviousBtn, homePlayPauseBtn, homeSkipNextBtn;

    //wrappers
    ConstraintLayout homeControlWrapper, headWrapper, artworkWrapper, seekbarWrapper, controlWrapper,audioVisualizerWrapper;

    //artwork
    CircleImageView artworkView;

    //seek bar
    SeekBar seekBar;
    TextView progressView, durationView;

    //audio visuaizer
    BarVisualizer audioVisualizer;
    //LineBarVisualizer audioVisualizer;

    //blur image  view
    BlurImageView blurImageView;

    // status bar & navigation color
    int defaultStatusColor;

    //repeat mode
    int repeatMode = 1; //reapeat all 1,repeat one 2,shuffle all = 3



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Felhasználói felület elemek inicializálása

        defaultStatusColor = getWindow().getStatusBarColor();
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));

        // Tárolási engedély kérése és dalok lekérése

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name));

        recyclerView = findViewById(R.id.recyclerview);
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                fetchSongs();
            } else {
                userResponses();
            }
        });

        // ExoPlayer és lejátszóvezérlők beállítása

        storagePermissionLauncher.launch(permission);

        recordAudioPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted->{
            if(granted && player.isPlaying()){
                activateAudioVisualizer();
            }else{
                userResponsesOnRecordAudioPerm();
            }
        });

        player = new ExoPlayer.Builder(this).build();

        playerView = findViewById(R.id.playerView);
        playerCloseBtn = findViewById(R.id.playerClose);
        songNameView = findViewById(R.id.songNameView);
        skipPreviousBtn = findViewById(R.id.skipPreviousBtn);
        skipNextBtn = findViewById(R.id.skipNextBtn);
        playedPauseBtn = findViewById(R.id.playPauseBtn);
        repeatModeBtn = findViewById(R.id.repeatModeBtn);
        playlistBtn = findViewById(R.id.playlistBtn);

        homeSongNameView = findViewById(R.id.homeSongNameView);
        homeSkipPreviousBtn = findViewById(R.id.homeSkipPreviousBtn);
        homeSkipNextBtn = findViewById(R.id.homeSkipNextBtn);
        homePlayPauseBtn = findViewById(R.id.homePlayPauseBtn);


        homeControlWrapper = findViewById(R.id.homeControlWrapper);
        headWrapper = findViewById(R.id.headWrapper);
        artworkWrapper = findViewById(R.id.artworkWrapper);
        seekbarWrapper = findViewById(R.id.seekbarWrapper);
        controlWrapper = findViewById(R.id.controlWrapper);
        audioVisualizerWrapper = findViewById(R.id.audioVisualizerWrapper);


        artworkView = findViewById(R.id.artworkView);


        seekBar = findViewById(R.id.seekbar);


        progressView = findViewById(R.id.progressView);
        durationView = findViewById(R.id.durationView);

        audioVisualizer = findViewById(R.id.visualizer);

        blurImageView = findViewById(R.id.blurImagenView);
        //atlathatosag


        playerControl();
    }


    @Override
    public void onBackPressed() {
        if(playerView.getVisibility() == View.VISIBLE){
            exitPlayerView();
        } else{
            super.onBackPressed();
        }
    }

    private void playerControl() {
        // Kattintásfigyelők beállítása gombokhoz
        songNameView.setSelected(true);
        homeSongNameView.setSelected(true);

        playerCloseBtn.setOnClickListener(view -> exitPlayerView());
        playlistBtn.setOnClickListener(view -> exitPlayerView());

        homeControlWrapper.setOnClickListener(view -> showPlayerView());

        player.addListener(new Player.Listener() {
            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                Player.Listener.super.onMediaItemTransition(mediaItem, reason);
                assert mediaItem != null;
                songNameView.setText(mediaItem.mediaMetadata.title);
                homeSongNameView.setText(mediaItem.mediaMetadata.title);

                progressView.setText(getReadableTime((int) player.getCurrentPosition()));

                seekBar.setProgress((int) player.getCurrentPosition());

                seekBar.setMax((int) player.getDuration());

                // Ismétlési mód gomb figyelőjének beállítása

                durationView.setText(getReadableTime((int) player.getDuration()));

                playedPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline,0,0,0);

                homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause,0,0,0);

                showCurentArtwork();

                updatePlayerPositionProgres();

                artworkView.setAnimation(loadRotation());

                activateAudioVisualizer();

                updatePlayerColors();

                if(!player.isPlaying()){
                    player.play();
                }

            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                if(playbackState == ExoPlayer.STATE_READY){
                    songNameView.setText(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.title);
                    homeSongNameView.setText(player.getCurrentMediaItem().mediaMetadata.title);
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    durationView.setText(getReadableTime((int) player.getDuration()));
                    seekBar.setMax((int) player.getDuration());
                    seekBar.setProgress((int) player.getCurrentPosition());

                    playedPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause,0,0,0);

                    showCurentArtwork();

                    updatePlayerPositionProgres();

                    artworkView.setAnimation(loadRotation());

                    activateAudioVisualizer();

                    updatePlayerColors();

                }
                else{
                    playedPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_outline,0,0,0);
                    homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_arrow,0,0,0);
                }
            }
        });
        //skip a kovetkezore
        skipNextBtn.setOnClickListener(view -> skipToNextSong());
        homeSkipNextBtn.setOnClickListener(view -> skipToNextSong());

        //skip az elozore
        skipPreviousBtn.setOnClickListener(view -> skipToPreviousSong());
        homePlayPauseBtn.setOnClickListener(view -> skipToPreviousSong());


        //play or pause
        playedPauseBtn.setOnClickListener(view -> playOnPausePlayer());
        homePlayPauseBtn.setOnClickListener(view -> playOnPausePlayer());

        //seek bar listener

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progressValue = seekBar.getProgress();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player.getPlaybackState() == ExoPlayer.STATE_READY){
                    seekBar.setProgress(progressValue);
                    progressView.setText(getReadableTime(progressValue));
                    player.seekTo(progressValue);
                }
            }
        });

        //repeat mode
        repeatModeBtn.setOnClickListener(view -> {
            if(repeatMode == 1){
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ONE);
                repeatMode = 2;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_repeat_one,0,0,0);
            }
            else if(repeatMode == 2){
                //shuffle all
                player.setShuffleModeEnabled(true);
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                repeatMode = 3;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_shuffle,0,0,0);
            }
            else if(repeatMode == 3){
                player.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);
                player.setShuffleModeEnabled(false);
                repeatMode = 1;
                repeatModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_shuffle,0,0,0);
            }
            updatePlayerColors();
        });

    }

    // A lejatszás/szunet gomb kattintasanak kezelase

    private void playOnPausePlayer() {
        if(player.isPlaying()){
            player.pause();
            playedPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_circle_outline,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_play_arrow,0,0,0);
            artworkView.clearAnimation();
        }else{
            player.play();
            playedPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause_circle_outline,0,0,0);
            homePlayPauseBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_pause,0,0,0);
            artworkView.startAnimation(loadRotation());
        }
        // update colors
        updatePlayerColors();
    }

    // Az előző dalra ugrás kezelése
    private void skipToPreviousSong() {
        if(player.hasPreviousMediaItem()){
            player.seekToPrevious();
        }
    }

    // A következő dalra ugrás kezelése
    private void skipToNextSong() {
        if(player.hasNextMediaItem()){
            player.seekToNext();
        }
    }

    // A lejatszo helyzetenek frissítese
    private void updatePlayerPositionProgres() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(player.isPlaying()){
                    progressView.setText(getReadableTime((int) player.getCurrentPosition()));
                    seekBar.setProgress((int) player.getCurrentPosition());
                }

                //ismetles ujrahivas
                updatePlayerPositionProgres();
                //animacio betoltese
                artworkView.setAnimation(loadRotation());
            }
        }, 1000);
    }

    // Forgas animacio betoltese
    private Animation loadRotation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
//        rotateAnimation.setInterpolator(new LinearInterpolator());
//        rotateAnimation.setDuration(10000);
//        rotateAnimation.setRepeatCount(Animation.INFINITE);
        //A kis kepnek a forgatasa de most nem kell
       return rotateAnimation;
    }

    // Az idotartamot olvashato idoformatumba
    //A masik helyet
    String getReadableTime(int duration){
        String time;
        int hrs = duration/(1000*60*60);
        int min = (duration%(1000*60*60))/(1000*60);
        int secs = (((duration%(1000*60*60))%(1000*60*60))%(1000*60))/1000;

        if(hrs<1){ time = min +":"+secs;}
        else{
            time = hrs + ":" + min + ":" + secs;
        }
        return time;
   }

    // Az aktualis kinezet jeleníti meg
    private void showCurentArtwork() {
        artworkView.setImageURI(Objects.requireNonNull(player.getCurrentMediaItem()).mediaMetadata.artworkUri);

        if(artworkView.getDrawable() == null){
            artworkView.setImageResource(R.drawable.df);
        }
    }

    // A lejatszó nezetet megjeleníti es szin
    private void showPlayerView() {
        playerView.setVisibility(View.VISIBLE);
        updatePlayerColors();
    }

    private void updatePlayerColors() {
        // only player view is visible

        if(playerView.getVisibility() == View.GONE)
            return;

        BitmapDrawable bitmapDrawable = (BitmapDrawable) artworkView.getDrawable();
        if(bitmapDrawable == null){
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this,R.drawable.df);
        }

        assert bitmapDrawable != null;
        Bitmap bmp = bitmapDrawable.getBitmap();

       // set bitmap to blur image view
        blurImageView.setImageBitmap(bmp);
        blurImageView.setBlur(3);

        //player control colors
        Palette.from(bmp).generate(palette -> {
           if(palette != null){
               Palette.Swatch swatch = palette.getDarkVibrantSwatch();
               if(swatch == null) {
                   swatch = palette.getMutedSwatch();
                   if(swatch == null){
                       swatch = palette.getDominantSwatch();
                   }
               }
               //extract text colors
               assert swatch != null;
               int titleTextColor = swatch.getTitleTextColor();
               int bodyTextColor = swatch.getBodyTextColor();
               int rgbColor = swatch.getRgb();

               // set colors to  player views
               // status & navigation bar colors

               getWindow().setStatusBarColor(rgbColor);
               getWindow().setNavigationBarColor(rgbColor);


               songNameView.setTextColor(titleTextColor);
               playerCloseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
               progressView.setTextColor(bodyTextColor);
               durationView.setTextColor(bodyTextColor);


               repeatModeBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
               skipPreviousBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
               skipNextBtn.getCompoundDrawables()[0].setTint(bodyTextColor);
               playedPauseBtn.getCompoundDrawables()[0].setTint(titleTextColor);
               playlistBtn.getCompoundDrawables()[0].setTint(bodyTextColor);

           }
        });


    }

    private void exitPlayerView() {
        playerView.setVisibility(View.GONE);
        getWindow().setStatusBarColor(defaultStatusColor);
        getWindow().setNavigationBarColor(ColorUtils.setAlphaComponent(defaultStatusColor,199));
    }

    private void userResponsesOnRecordAudioPerm() {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(shouldShowRequestPermissionRationale(recordAudioPermission)){
                    new AlertDialog.Builder(this)
                            .setTitle("Requesting to show Audio Visualizer")
                            .setMessage("Allow this app to display audio visualizer when music is playing")
                            .setPositiveButton("Allow", (dialogInterface, i) -> {
                                recordAudioPermissionLauncher.launch(recordAudioPermission);
                            })

                            .setNegativeButton("No", (dialogInterface, i) -> {
                                Toast.makeText(getApplicationContext(),"You denied to show the audio visualizer",Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            })
                            .show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"You denied to show the audio visualizer",Toast.LENGTH_SHORT).show();
                }
            }
    }

    private void activateAudioVisualizer() {
      if (ContextCompat.checkSelfPermission(this,recordAudioPermission) != PackageManager.PERMISSION_GRANTED){
          return;
      }
      audioVisualizer.setColor(ContextCompat.getColor(this,R.color.secondary_color));
      audioVisualizer.setDensity(60);
      audioVisualizer.setPlayer(player.getAudioSessionId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player.isPlaying()){
            player.stop();
        }
        player.release();
    }

    private void userResponses() {

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            fetchSongs();
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(shouldShowRequestPermissionRationale(permission)){
                new AlertDialog.Builder(this)
                        .setTitle("Requesting Permission")
                        .setMessage("Allow us to fetch songs on your device")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                storagePermissionLauncher.launch(permission);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "You denied us to show songs", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "You denied us to show songs", Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchSongs() {

        List<Song> songs = new ArrayList<>();
        Uri mediaStoreUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaStoreUri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.ALBUM_ID,
        };

        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";


        try (Cursor cursor = getContentResolver().query(mediaStoreUri, projection, null, null, sortOrder)) {

            assert cursor != null;
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
            int durationColum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
            int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

            while (cursor.moveToNext()) {

                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                int duration = cursor.getInt(durationColum);
                int size = cursor.getInt(sizeColumn);
                long albumId = cursor.getLong(albumIdColumn);

                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);


                Uri albumArtworkUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);

                name = name.substring(0, name.lastIndexOf("."));

                Song song = new Song(name, uri, albumArtworkUri, size, duration);

                songs.add(song);
            }


            showSong(songs);
        }
    }

    private void showSong(List<Song> songs) {

        if (songs.size() == 0) {
            Toast.makeText(this, "NO SONG", Toast.LENGTH_SHORT).show();
            return;
        }
        allSongs.clear();
        allSongs.addAll(songs);

        String title = getResources().getString(R.string.app_name) + " - " + songs.size();
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        songAdapter = new SongAdapter(this, songs,player,playerView);

        recyclerView.setAdapter(songAdapter);

        //recyclerview animators

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(songAdapter);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator());
        scaleInAnimationAdapter.setFirstOnly(false);
        recyclerView.setAdapter(scaleInAnimationAdapter);

        for (Song song : songs) {
            Log.d("AllSongs", "Title: " + song.getTitle() + ", URI: " + song.getUri());
        }

    }


    //opcios menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_btn, menu);

        MenuItem menuItem = menu.findItem(R.id.searchBtn);
        SearchView searchView = (SearchView) menuItem.getActionView();

        SearchSong(Objects.requireNonNull(searchView));

        return super.onCreateOptionsMenu(menu);
    }

    //kereses
    private void SearchSong(SearchView searchView) {


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText.toLowerCase());
                return true;
            }
        });

    }

    //szures lekerdezes alapjan
    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();

        if (allSongs.size() > 0) {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(query)) {
                    filteredList.add(song);
                }
            }

            if (songAdapter != null) {
                songAdapter.filterSongs(filteredList);
            }

        }
    }
}