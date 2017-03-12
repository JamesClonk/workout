package io.jamesclonk.workout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.widget.EditText;

import java.util.Scanner;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    public static final String M_WORKOUT = "io.jamesclonk.workout.M_WORKOUT";
    public static final String M_CARDIO = "io.jamesclonk.workout.M_CARDIO";
    public static final String M_INTERVAL = "io.jamesclonk.workout.M_INTERVAL";
    private EditText eWorkout;
    private EditText eCardio;
    private EditText eInterval;
    private int iWorkout;
    private int iCardio;
    private int iInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eWorkout = (EditText) findViewById(R.id.edit_workout);
        eCardio = (EditText) findViewById(R.id.edit_cardio);
        eInterval = (EditText) findViewById(R.id.edit_interval);

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
        intent.putExtra(M_WORKOUT, iWorkout);
        intent.putExtra(M_CARDIO, iCardio);
        intent.putExtra(M_INTERVAL, iInterval);
        startActivity(intent);
    }

    private void convertSettings() throws NumberFormatException {
        iWorkout = Math.max(Integer.parseInt(eWorkout.getText().toString()), 5);
        iCardio = Math.max(Integer.parseInt(eCardio.getText().toString()), 5);
        iInterval = Math.max(Integer.parseInt(eInterval.getText().toString()), 1);
    }

    private void saveSettings() {
        final String values = String.format("%s:%s:%s"
                , eWorkout.getText().toString()
                , eCardio.getText().toString()
                , eInterval.getText().toString()
        );
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("io.jamesclonk.workout", values);
        edit.commit();
    }

    private void restoreSettings() {
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        final String defaultSettings = String.format("%s:%s:%s", "45", "12", "8");
        // retrieve the requested
        final String values = prefs.getString("io.jamesclonk.workout", defaultSettings);

        final Scanner scanner = new Scanner(values);
        scanner.useDelimiter(Pattern.compile(":"));

        eWorkout.setText(scanner.next());
        eCardio.setText(scanner.next());
        eInterval.setText(scanner.next());
        scanner.close();
    }
}
