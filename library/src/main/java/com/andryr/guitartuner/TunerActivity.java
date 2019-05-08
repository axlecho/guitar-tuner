/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.guitartuner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

@SuppressWarnings("DefaultLocale")
public class TunerActivity extends AppCompatActivity {

    private static final String TAG = TunerActivity.class.getCanonicalName();

    public static final String STATE_NEEDLE_POS = "needle_pos";
    public static final String STATE_PITCH_INDEX = "pitch_index";
    public static final String STATE_LAST_FREQ = "last_freq";
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 443;


    private Tuning mTuning;
    private NeedleView mNeedleView;
    private TuningView mTuningView;
    private TextView mFrequencyView;
    private ImageView mGoodPitchView;

    private int mPitchIndex;
    private double mLastFreq;

    private AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);

    private void updateView() {
        mNeedleView =  findViewById(R.id.pitch_needle_view);
        mNeedleView.setTickLabel(-1.0F, "-100c");
        mNeedleView.setTickLabel(0.0F, String.format("%.02fHz", mTuning.pitches[0].frequency));
        mNeedleView.setTickLabel(1.0F, "+100c");

        mTuningView =  findViewById(R.id.tuning_view);
        mTuningView.setTuning(mTuning);

        mFrequencyView = findViewById(R.id.frequency_view);
        mFrequencyView.setText(String.format("%.02fHz", mTuning.pitches[0].frequency));

        int primaryTextColor = Utils.getAttrColor(this, android.R.attr.textColorPrimary);
        mGoodPitchView = findViewById(R.id.good_pitch_view);
        mGoodPitchView.setColorFilter(primaryTextColor);
    }

    private void updateUI(float freq) {
        final int index = mTuning.closestPitchIndex(freq);
        final Notes.Pitch pitch = mTuning.pitches[index];
        double interval = 1200 * Utils.log2(freq / pitch.frequency); // interval in cents
        final float needlePos = (float) (interval / 100);
        final boolean goodPitch = Math.abs(interval) < 5.0;

        mTuningView.setSelectedIndex(index, true);
        mNeedleView.setTickLabel(0.0F, String.format("%.02fHz", pitch.frequency));
        mNeedleView.animateTip(needlePos);
        mFrequencyView.setText(String.format("%.02fHz", freq));


        if (goodPitch) {
            if (mGoodPitchView.getVisibility() != View.VISIBLE) {
                Utils.reveal(mGoodPitchView);
            }
        } else if (mGoodPitchView.getVisibility() == View.VISIBLE) {
            Utils.hide(mGoodPitchView);
        }

        mPitchIndex = index;
        mLastFreq = freq;
    }

    private void startAudioProcessing() {
        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 44100, 2048, new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(pitchInHz > 0) {
                            updateUI(pitchInHz);
                        }
                    }
                });
            }
        }));

        new Thread(dispatcher, "Audio Dispatcher").start();
    }

    private void requestPermissions() {
        if (!Utils.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                DialogUtils.showPermissionDialog(this, getString(R.string.permission_record_audio), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(TunerActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                PERMISSION_REQUEST_RECORD_AUDIO);
                    }
                });

            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_RECORD_AUDIO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_RECORD_AUDIO: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startAudioProcessing();
                }
                break;
            }

        }
    }

    @Override
    protected void onResume() {
        if (Utils.checkPermission(this, Manifest.permission.RECORD_AUDIO)) {
            startAudioProcessing();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if(!dispatcher.isStopped()) {
            dispatcher.stop();
        }
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tuner);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mTuning = Tuning.getTuning(this, Preferences.getString(this, getString(R.string.pref_tuning_key), getString(R.string.common)));
        this.updateView();
        requestPermissions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat(STATE_NEEDLE_POS, mNeedleView.getTipPos());
        outState.putInt(STATE_PITCH_INDEX, mPitchIndex);
        outState.putDouble(STATE_LAST_FREQ, mLastFreq);
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mNeedleView.setTipPos(savedInstanceState.getFloat(STATE_NEEDLE_POS));
        int pitchIndex = savedInstanceState.getInt(STATE_PITCH_INDEX);
        mNeedleView.setTickLabel(0.0F, String.format("%.02fHz", mTuning.pitches[pitchIndex].frequency));
        mTuningView.setSelectedIndex(pitchIndex);
        mFrequencyView.setText(String.format("%.02fHz", savedInstanceState.getFloat(STATE_LAST_FREQ)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_tuner_settings) {
            NavUtils.showSettingsActivity(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tunner_menu, menu);
        return true;
    }


}
