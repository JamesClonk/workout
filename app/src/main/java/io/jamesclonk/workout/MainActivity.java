package io.jamesclonk.workout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Scanner;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static final String M_WORKOUT = "io.jamesclonk.workout.M_WORKOUT";
    public static final String M_CARDIO = "io.jamesclonk.workout.M_CARDIO";
    public static final String M_INTERVAL = "io.jamesclonk.workout.M_INTERVAL";
    private EditText editWorkout;
    private EditText editCardio;
    private EditText editIntverval;
    private InputWatcher watcher;
    private int workout;
    private int cardio;
    private int interval;

    private class InputWatcher implements TextWatcher {
        private final MainActivity parentActivity;

        public InputWatcher(MainActivity p) {
            parentActivity = p;
        }

        @Override
        public void afterTextChanged(Editable s) {
            parentActivity.computeWorkoutTime();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editWorkout = (EditText) findViewById(R.id.edit_workout);
        editCardio = (EditText) findViewById(R.id.edit_cardio);
        editIntverval = (EditText) findViewById(R.id.edit_interval);

        watcher = new InputWatcher(this);
        editWorkout.addTextChangedListener(watcher);
        editCardio.addTextChangedListener(watcher);
        editIntverval.addTextChangedListener(watcher);

        restoreSettings();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public void workoutRun(View view) {
        try {
            convertSettings();
        } catch (NumberFormatException ex) {
            restoreSettings();
            return;
        }
        saveSettings();

        final Intent intent = new Intent(this, WorkoutActivity.class);
        intent.putExtra(M_WORKOUT, workout);
        intent.putExtra(M_CARDIO, cardio);
        intent.putExtra(M_INTERVAL, interval);
        startActivity(intent);
    }

    private void convertSettings() throws NumberFormatException {
        workout = Math.max(Integer.parseInt(editWorkout.getText().toString()), 5);
        cardio = Math.max(Integer.parseInt(editCardio.getText().toString()), 5);
        interval = Math.max(Integer.parseInt(editIntverval.getText().toString()), 1);
    }

    private void saveSettings() {
        final String values = String.format("%s:%s:%s"
                , editWorkout.getText().toString()
                , editCardio.getText().toString()
                , editIntverval.getText().toString()
        );
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("io.jamesclonk.workout", values);
        edit.commit();
    }

    private void restoreSettings() {
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        final String defaultSettings = String.format("%s:%s:%s", "45", "12", "8");
        final String values = prefs.getString("io.jamesclonk.workout", defaultSettings);

        final Scanner scanner = new Scanner(values);
        scanner.useDelimiter(Pattern.compile(":"));

        editWorkout.setText(scanner.next());
        editCardio.setText(scanner.next());
        editIntverval.setText(scanner.next());
        scanner.close();
    }

    private void computeWorkoutTime() {
        try {
            convertSettings();
        } catch (NumberFormatException ex) {
            return;
        }

        final int totalTime = interval * (workout + cardio);
        ((TextView) findViewById(R.id.workout_time)).setText(String.format("%d:%02d", totalTime / 60, totalTime % 60));
    }
}
