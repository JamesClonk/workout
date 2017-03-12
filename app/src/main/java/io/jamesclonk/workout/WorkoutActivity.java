package io.jamesclonk.workout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.TextView;

public class WorkoutActivity extends AppCompatActivity {

    private SoundManager soundManager;
    private final static int zeroSound = R.raw.zero;
    private final static int oneSound = R.raw.one;
    private final static int twoSound = R.raw.two;
    private final static int threeSound = R.raw.three;
    private final static int fourSound = R.raw.four;
    private final static int fiveSound = R.raw.five;
    private final static int sixSound = R.raw.six;
    private final static int sevenSound = R.raw.seven;
    private final static int eightSound = R.raw.eight;
    private final static int nineSound = R.raw.nine;
    private final static int tenSound = R.raw.ten;
    private final static int elevenSound = R.raw.eleven;
    private final static int twelveSound = R.raw.twelve;
    private final static int moveSound = R.raw.move;
    private final static int endSound = R.raw.end;
    private final static int handSound = R.raw.hand;
    private final static int problemoSound = R.raw.problemo;
    private int zero;
    private int one;
    private int two;
    private int three;
    private int four;
    private int five;
    private int six;
    private int seven;
    private int eight;
    private int nine;
    private int ten;
    private int eleven;
    private int twelve;
    private int move;
    private int end;
    private int hand;
    private int problemo;
    private LinearLayout layout;
    private TextView viewSeconds;
    private TextView viewIntervals;
    private Handler uiHandler;
    private WorkoutRunner workoutRunner;
    private int workoutSeconds;
    private int cardioSeconds;
    private int intervalCount;
    private WakeLock screenUnlock;

    private enum WorkoutState {CARDIO, WORKOUT}

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        uiHandler = new UIHandler(Looper.getMainLooper());

        soundManager = new SoundManager(this, uiHandler);
        zero = soundManager.loadSound(zeroSound);
        one = soundManager.loadSound(oneSound);
        two = soundManager.loadSound(twoSound);
        three = soundManager.loadSound(threeSound);
        four = soundManager.loadSound(fourSound);
        five = soundManager.loadSound(fiveSound);
        six = soundManager.loadSound(sixSound);
        seven = soundManager.loadSound(sevenSound);
        eight = soundManager.loadSound(eightSound);
        nine = soundManager.loadSound(nineSound);
        ten = soundManager.loadSound(tenSound);
        eleven = soundManager.loadSound(elevenSound);
        twelve = soundManager.loadSound(twelveSound);
        move = soundManager.loadSound(moveSound);
        end = soundManager.loadSound(endSound);
        hand = soundManager.loadSound(handSound);
        problemo = soundManager.loadSound(problemoSound);

        Intent it = getIntent();
        workoutSeconds = it.getIntExtra(MainActivity.M_WORKOUT, Integer.parseInt("45"));
        cardioSeconds = it.getIntExtra(MainActivity.M_CARDIO, Integer.parseInt("12"));
        intervalCount = it.getIntExtra(MainActivity.M_INTERVAL, Integer.parseInt("8"));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        screenUnlock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE
                , "io.jamesclonk.workout.WorkoutActivity.scrUnLock"
        );

        uiHandler.obtainMessage(0, cardioSeconds, intervalCount - 1).sendToTarget();

        workoutRunner = new WorkoutRunner();
        workoutRunner.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!screenUnlock.isHeld() & workoutRunner.isAlive()) screenUnlock.acquire();

        workoutRunner.resumeRunner();
        synchronized (this) {
            notify();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        workoutRunner.pauseRunner();
        workoutRunner.interrupt();

        if (screenUnlock.isHeld()) screenUnlock.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workoutRunner.resumeRunner();
        workoutRunner.stopRunner();
        synchronized (this) {
            notify();
        }
    }

    @SuppressLint("HandlerLeak")
    private class UIHandler extends Handler {
        UIHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    setContentView(R.layout.activity_workout);
                    layout = (LinearLayout) findViewById(R.id.activity_workout);
                    viewSeconds = (TextView) findViewById(R.id.workout_seconds);
                    viewIntervals = (TextView) findViewById(R.id.workout_intervals);

                    layout.setBackgroundColor(Color.YELLOW);
                    viewSeconds.setText(String.format("%d", msg.arg1));
                    viewIntervals.setText(String.format("%d", msg.arg2));
                    break;

                case 1:
                    layout.setBackgroundColor(Color.GREEN);
                    viewSeconds.setText(String.format("%d", msg.arg1));
                    viewIntervals.setText(String.format("%d", msg.arg2));
                    break;

                case 2:
                    viewSeconds.setText(String.format("%d", msg.arg1));
                    break;

                case 3:
                    setContentView(R.layout.activity_workout_done);
                    layout = (LinearLayout) findViewById(R.id.activity_workout_done);
                    viewSeconds = (TextView) findViewById(R.id.workout_time_done);

                    int workoutTime = intervalCount * (workoutSeconds + cardioSeconds);
                    layout.setBackgroundColor(Color.CYAN);
                    viewSeconds.setText(String.format("%d:%02d", workoutTime / 60, workoutTime % 60));
                    if (screenUnlock.isHeld()) screenUnlock.release();
                    break;
            }
        }
    }

    private class WorkoutRunner extends Thread {
        private volatile boolean runLoop;
        private volatile boolean continueLoop;

        void stopRunner() {
            continueLoop = false;
        }

        void pauseRunner() {
            runLoop = false;
        }

        void resumeRunner() {
            runLoop = true;
        }

        private void hangThread() {
            synchronized (WorkoutActivity.this) {
                while (!runLoop)
                    try {
                        WorkoutActivity.this.wait();
                    } catch (InterruptedException ex) {
                        continue;
                    }
            }
        }

        @Override
        public void run() {
            runLoop = true;
            continueLoop = true;
            int timeRemaining = WorkoutActivity.this.cardioSeconds;
            int intervalRemaining;
            WorkoutState workoutState = WorkoutState.CARDIO;

            final long sleepTarget = 1000000000L;
            long sleepDelay = sleepTarget;
            long wakeupError;
            long lastWakeup;
            long thisWakeup;


            WorkoutActivity.this.soundManager.pauseUntilLoaded(
                    WorkoutActivity.this.zero +
                            WorkoutActivity.this.one +
                            WorkoutActivity.this.two +
                            WorkoutActivity.this.three +
                            WorkoutActivity.this.four +
                            WorkoutActivity.this.five +
                            WorkoutActivity.this.six +
                            WorkoutActivity.this.seven +
                            WorkoutActivity.this.eight +
                            WorkoutActivity.this.nine +
                            WorkoutActivity.this.ten +
                            WorkoutActivity.this.eleven +
                            WorkoutActivity.this.twelve +
                            WorkoutActivity.this.move +
                            WorkoutActivity.this.end +
                            WorkoutActivity.this.hand +
                            WorkoutActivity.this.problemo);

            // start out with countdown from 10, then the cardio begins
            for (intervalRemaining = 10; (intervalRemaining > 0); intervalRemaining--) {
                if (!runLoop) hangThread();
                if (!continueLoop) return;

                try {
                    switch (intervalRemaining) {
                        case 10:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.ten);
                            break;
                        case 9:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.nine);
                            break;
                        case 8:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.eight);
                            break;
                        case 7:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.seven);
                            break;
                        case 6:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.six);
                            break;
                        case 5:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.five);
                            break;
                        case 4:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.four);
                            break;
                        case 3:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.three);
                            break;
                        case 2:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.two);
                            break;
                        case 1:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.one);
                            break;
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    continue;
                }
            }
            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.hand);

            intervalRemaining = WorkoutActivity.this.intervalCount - 1;
            lastWakeup = System.nanoTime() - sleepTarget;
            while (true) {
                if (!runLoop) {
                    hangThread();
                    lastWakeup = System.nanoTime() - sleepTarget;
                }
                if (!continueLoop) return;

                thisWakeup = System.nanoTime();
                if (timeRemaining <= 1) {
                    switch (workoutState) {
                        case WORKOUT:
                            // WORKOUT transition, either to CARDIO or done
                            if (intervalRemaining <= 0) { // done
                                WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.end);
                                WorkoutActivity.this.uiHandler.obtainMessage(3, 0, 0).sendToTarget();
                                return;
                            } else { // back to CARDIO
                                workoutState = WorkoutState.CARDIO;
                                timeRemaining = WorkoutActivity.this.cardioSeconds;
                                intervalRemaining--;
                                WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.problemo);
                                WorkoutActivity.this.uiHandler.obtainMessage(0, timeRemaining, intervalRemaining).sendToTarget();
                            }
                            break;

                        case CARDIO:
                            // CARDIO transition to WORKOUT
                            workoutState = WorkoutState.WORKOUT;
                            timeRemaining = WorkoutActivity.this.workoutSeconds;
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.zero);
                            WorkoutActivity.this.uiHandler.obtainMessage(1, timeRemaining, intervalRemaining).sendToTarget();
                            break;
                    }
                } else { // no transition, just update the timer display and possibly play the countdown sounds
                    timeRemaining--;
                    WorkoutActivity.this.uiHandler.obtainMessage(2, timeRemaining, intervalRemaining).sendToTarget();
                    switch (timeRemaining) {
                        case 10:
                            if (workoutState == WorkoutState.WORKOUT)
                                WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.ten);
                            break;
                        case 5:
                            if (workoutState == WorkoutState.WORKOUT)
                                WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.five);
                            break;
                        case 4:
                            if (workoutState == WorkoutState.WORKOUT)
                                WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.four);
                            break;
                        case 3:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.three);
                            break;
                        case 2:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.two);
                            break;
                        case 1:
                            WorkoutActivity.this.soundManager.playSound(WorkoutActivity.this.one);
                            break;
                    }
                }

                wakeupError = sleepTarget - (thisWakeup - lastWakeup);
                sleepDelay = sleepDelay + (wakeupError / 5);
                wakeupError = sleepDelay + (wakeupError / 3);
                lastWakeup = thisWakeup;

                try {
                    Thread.sleep(wakeupError / 1000000L);
                } catch (InterruptedException ex) {
                    continue;
                }
            }
        }
    }
}
