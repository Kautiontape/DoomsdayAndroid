package kautiontape.doomsday;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {
    static final String STATE_CURRENT_DATE = "current_date";
    static final String STATE_WIN_SPREE = "win_spree";
    static final String STATE_WRONG_GUESSES = "wrong_guesses";
    static final String STATE_START_TIME = "start_time";
    static final String STATE_SCORE = "score";

    private GregorianCalendar currentDate;
    private int win_spree;
    private int wrong_guesses;
    private long start_time;
    private int score;

    private SimpleDateFormat dFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.currentDate = new GregorianCalendar();
        this.dFormat = new SimpleDateFormat(getString(R.string.date_format), Locale.getDefault());

        Timer timer = new Timer();
        timer.schedule(new UpdateTime(), 0, 1000);

        if(savedInstanceState != null) {
            try {
                this.currentDate.setTime(dFormat.parse(savedInstanceState.getString(STATE_CURRENT_DATE)));
                this.win_spree = savedInstanceState.getInt(STATE_WIN_SPREE);
                this.wrong_guesses = savedInstanceState.getInt(STATE_WRONG_GUESSES);
                this.start_time = savedInstanceState.getLong(STATE_START_TIME);
                this.score = savedInstanceState.getInt(STATE_SCORE);
            } catch (ParseException e) {
                this.newRound();
            }
        } else {
            this.win_spree = 0;
            this.wrong_guesses = 0;
            this.score = 0;
            this.newRound();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_CURRENT_DATE, dFormat.format(this.currentDate.getTime()));
        savedInstanceState.putInt(STATE_WIN_SPREE, win_spree);
        savedInstanceState.putInt(STATE_WRONG_GUESSES, wrong_guesses);
        savedInstanceState.putLong(STATE_START_TIME, start_time);
        savedInstanceState.putInt(STATE_SCORE, score);

        super.onSaveInstanceState(savedInstanceState);
    }

    private void newRound() {
        this.currentDate = this.randDate();
        ((TextView) findViewById(R.id.dateText)).setText(dFormat.format(this.currentDate.getTime()));

        ((Button)findViewById(R.id.buttonSun)).setEnabled(true);
        ((Button)findViewById(R.id.buttonMon)).setEnabled(true);
        ((Button)findViewById(R.id.buttonTue)).setEnabled(true);
        ((Button)findViewById(R.id.buttonWed)).setEnabled(true);
        ((Button)findViewById(R.id.buttonThu)).setEnabled(true);
        ((Button)findViewById(R.id.buttonFri)).setEnabled(true);
        ((Button)findViewById(R.id.buttonSat)).setEnabled(true);

        wrong_guesses = 0;
        start_time = System.currentTimeMillis();
        updateTextView();
    }

    private GregorianCalendar randDate() {
        GregorianCalendar gc = new GregorianCalendar();

        int year = randBetween(1700, 2500);
        gc.set(GregorianCalendar.YEAR, year);

        int dayOfYear = randBetween(1, gc.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
        gc.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);

        return gc;
    }

    private int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }

    private void win() {
        // Score update
        win_spree++;
        this.deltaScore(50);

        // Visual updates
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        this.flashBG(Color.parseColor(getString(R.string.win_color)));
        updateTextView();

        // Win prompt
        TextView statusText = (TextView)findViewById(R.id.statusText);
        statusText.setTextColor(Color.parseColor(getString(R.string.win_color)));
        statusText.setText(getString(R.string.win_prompt));

        this.newRound();
    }

    private void wrong() {
        wrong_guesses++;
        win_spree = 0;
        this.deltaScore(-25);

        // Visual updates
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);
        this.flashBG(Color.parseColor(getString(R.string.wrong_color)));
        updateTextView();

        // Wrong prompt
        TextView statusText = (TextView)findViewById(R.id.statusText);
        statusText.setTextColor(Color.parseColor(getString(R.string.wrong_color)));
        statusText.setText(getString(R.string.wrong_prompt));
    }

    public void onClickDay(View v) {
        int day = -1;
        Button selectedButton = null;

        switch(v.getId()) {
            case R.id.buttonSun:
                selectedButton = (Button)findViewById(R.id.buttonSun);
                day = Calendar.SUNDAY;
                break;
            case R.id.buttonMon:
                selectedButton = (Button)findViewById(R.id.buttonMon);
                day = Calendar.MONDAY;
                break;
            case R.id.buttonTue:
                selectedButton = (Button)findViewById(R.id.buttonTue);
                day = Calendar.TUESDAY;
                break;
            case R.id.buttonWed:
                selectedButton = (Button)findViewById(R.id.buttonWed);
                day = Calendar.WEDNESDAY;
                break;
            case R.id.buttonThu:
                selectedButton = (Button)findViewById(R.id.buttonThu);
                day = Calendar.THURSDAY;
                break;
            case R.id.buttonFri:
                selectedButton = (Button)findViewById(R.id.buttonFri);
                day = Calendar.FRIDAY;
                break;
            case R.id.buttonSat:
                selectedButton = (Button)findViewById(R.id.buttonSat);
                day = Calendar.SATURDAY;
                break;
        }

        if(day == currentDate.get(GregorianCalendar.DAY_OF_WEEK)) {
            this.win();
        } else {
            this.wrong();
            if (selectedButton != null) {
                selectedButton.setEnabled(false);
            }
        }
    }

    public void onClickSkip(View v) {
        TextView statusText = (TextView)findViewById(R.id.statusText);
        statusText.setTextColor(Color.parseColor(getString(R.string.skip_color)));
        statusText.setText(getString(R.string.skip_prompt));
        this.flashBG(Color.parseColor(getString(R.string.skip_color)));
        win_spree = 0;
        this.deltaScore(-50);
        this.updateTextView();
        this.newRound();
    }

    private void updateTextView() {
        ((TextView)findViewById(R.id.winText)).setText(String.valueOf(win_spree));
        ((TextView)findViewById(R.id.wrongText)).setText(String.valueOf(wrong_guesses));
        ((TextView)findViewById(R.id.scoreText)).setText(String.valueOf(score));
    }

    private int deltaScore(int delta) {
        this.score = this.score + delta;
        this.updateTextView();
        return this.score;
    }

    private void flashBG(int fc) {
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(findViewById(R.id.layout), "backgroundColor", fc, Color.TRANSPARENT);
        bgAnim.setDuration(1000);
        bgAnim.setEvaluator(new ArgbEvaluator());
        bgAnim.start();
    }

    private class UpdateTime extends TimerTask {
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    long time_elapsed = (System.currentTimeMillis() - start_time) / 1000;
                    ((TextView) findViewById(R.id.timeText)).setText(String.valueOf(time_elapsed));
                }
            });
        }
    }
}
