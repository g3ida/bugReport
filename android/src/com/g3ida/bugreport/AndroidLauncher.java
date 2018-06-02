package com.g3ida.bugreport;

import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.g3ida.bugreport.BugReport;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import com.google.android.gms.ads.InterstitialAd;

import java.util.concurrent.CountDownLatch;

public class AndroidLauncher extends AndroidApplication implements SystemVolume, RewardedVideoAdListener {

    private RewardedVideoAd mRewardedVideoAd;
    private InterstitialAd mInterstitialAd;


    @Override
    public float getMusicVolume() {
        Context context = getApplicationContext();
        android.media.AudioManager audio = (android.media.AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(android.media.AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC);
        return (float) currentVolume / (float) maxVolume;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;


        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        initialize(new BugReport(this), config);
    }

    public void loadInterstitialAd() {
        runOnUiThread(new Runnable() {
            public void run() {

                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                } else {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }
        });
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        //loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        Settings.numDiamonds++;
        Settings.save();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    public void showVideoAd() {
        runOnUiThread(new Runnable() {
            public void run() {

                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    loadRewardedVideoAd();
                }
            }
        });
    }

    public boolean hasVideoReward() {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] value = new boolean[1];
        runOnUiThread(new Runnable() {
            public void run() {

                value[0] = mRewardedVideoAd.isLoaded();
                latch.countDown();
            }
        });

        try {
            latch.await();
            return value[0];

        } catch(Exception e) {
            return false;
        }
        //return mRewardedVideoAd.isLoaded();

    }

}