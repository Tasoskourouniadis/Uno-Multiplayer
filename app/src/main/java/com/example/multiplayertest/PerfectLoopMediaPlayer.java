package com.example.multiplayertest;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class PerfectLoopMediaPlayer {


    private static final String TAG = "PerfectLoopMediaPlayer";
    private Context mContext;
    private int mResId = 0;
    MediaPlayerService playerService;
    public MediaPlayer mCurrentPlayer = null;
    public MediaPlayer mNextPlayer = null;

    private float leftvolume= 0.5f;
    private float rightvolume= 0.5f;

    boolean isLoopMedia;


    public static PerfectLoopMediaPlayer create(Context context, int resId, Boolean loopMedia) {
        return new PerfectLoopMediaPlayer(context, resId, loopMedia);
    }


    public PerfectLoopMediaPlayer(Context context, int resId, Boolean loopMedia) {
        mContext = context;
        mResId = resId;
        try {
            AssetFileDescriptor afd = context.getResources().openRawResourceFd(mResId);
            isLoopMedia = loopMedia;
            mCurrentPlayer = new MediaPlayer();
            mCurrentPlayer.setVolume(leftvolume,rightvolume);
            mCurrentPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mCurrentPlayer.setOnCompletionListener(playerService);
            mCurrentPlayer.setOnErrorListener(playerService);
            mCurrentPlayer.setOnPreparedListener(playerService);
            mCurrentPlayer.setOnBufferingUpdateListener(playerService);
            mCurrentPlayer.setOnSeekCompleteListener(playerService);
            mCurrentPlayer.setOnInfoListener(playerService);
            mCurrentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    start();
                    mCurrentPlayer.setVolume(leftvolume,rightvolume);
                }
            });
            mCurrentPlayer.prepareAsync();
            if(loopMedia) {
                createNextMediaPlayerRaw();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void createNextMediaPlayerRaw() {
        AssetFileDescriptor afd = mContext.getResources().openRawResourceFd(mResId);
        mNextPlayer = new MediaPlayer();
        mNextPlayer.setVolume(leftvolume,rightvolume);

        try {
            mNextPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mNextPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
                    mCurrentPlayer.setOnCompletionListener(onCompletionListener);
                }
            });
            mNextPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private final MediaPlayer.OnCompletionListener onCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(isLoopMedia) {
                        mediaPlayer.release();
                        mCurrentPlayer = mNextPlayer;
                        createNextMediaPlayerRaw();
                    }else{
                        mediaPlayer.release();
                    }

                }
            };


    public boolean isPlaying() throws IllegalStateException {
        if (mCurrentPlayer != null) {
            return mCurrentPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void setVolume(float leftVolume, float rightVolume) {
        leftvolume = leftVolume;
        rightvolume = rightVolume;
        if (mCurrentPlayer != null) {
            mCurrentPlayer.setVolume(leftVolume, rightVolume);
            mNextPlayer.setVolume(leftVolume, rightVolume);
        } else {
            Log.d(TAG, "setVolume()");
        }

    }





    public void start() throws IllegalStateException {
        if (mCurrentPlayer != null) {
            Log.d(TAG, "start()");
            mCurrentPlayer.start();
        } else {
            Log.d(TAG, "start() | mCurrentPlayer is NULL");
        }

    }

    public void stop() throws IllegalStateException {
        if (mCurrentPlayer != null && mCurrentPlayer.isPlaying()) {
            Log.d(TAG, "stop()");
            mCurrentPlayer.stop();


        } else {
            Log.d(TAG, "stop() | mCurrentPlayer " +
                    "is NULL or not playing");
        }

    }

    public void pause() throws IllegalStateException {
        if (mCurrentPlayer != null && mCurrentPlayer.isPlaying()) {
            Log.d(TAG, "pause()");
            mCurrentPlayer.pause();
        } else {
            Log.d(TAG, "pause() | mCurrentPlayer " +
                    "is NULL or not playing");
        }

    }


    public void release() {
        Log.d(TAG, "release()");
        mCurrentPlayer.stop();
        mCurrentPlayer.reset();
        mNextPlayer.stop();
        mNextPlayer.reset();
        mCurrentPlayer.release();
        mNextPlayer.release();
        mContext = null;
        mCurrentPlayer.setOnCompletionListener(null);
        mCurrentPlayer.setOnPreparedListener(null);
        mNextPlayer.setOnCompletionListener(null);
        mNextPlayer.setOnPreparedListener(null);
        mCurrentPlayer = null;
        mNextPlayer = null;

        Log.e(TAG, "Media Player Successfully released!: " );
    }


    public void setAudioStreamType() {
        mCurrentPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
        mNextPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
    }


}