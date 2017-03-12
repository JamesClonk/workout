package io.jamesclonk.workout;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

public class SoundManager {
    private final SoundPool soundPool;
    private final Activity context;
    private int soundsLoaded;
    private final float maxVolume;
    private final AudioManager audioManager;
    private Handler handler;

    public SoundManager(Activity appContext, Handler handler) {
        this.handler = handler;
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool pool, int sndId, int status) {
                SoundManager.this.soundsLoaded += sndId;
            }
        });
        context = appContext;
        context.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void pauseUntilLoaded(int soundsExpected) {
        while (this.soundsLoaded != soundsExpected) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    public int loadSound(int soundID) {
        return soundPool.load(context, soundID, 1);
    }

    public void playSound(final int soundID) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                float pVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / maxVolume;
                soundPool.play(soundID, pVol, pVol, 1, 0, 1.0f);
            }
        });
    }
}
