package timecard.android.alec.tunbridge.timecard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.*;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private long mStartTime;
    private TextView mTimeLabel;
    private boolean running;

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long millis = System.currentTimeMillis() - mStartTime;
            mTimeLabel.setText(String.format("%6.2f", millis/1000.0));
            mHandler.postAtTime(this,
                    SystemClock.uptimeMillis() + 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTimeLabel = (TextView) findViewById(R.id.time);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mTimeLabel.setText(String.format("%6.2f", 0.0));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(getString(R.string.start_time),mStartTime);
        editor.commit();
        super.onPause();
    }

    @Override
    protected void onResume() {
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        mStartTime = sharedPreferences.getLong(getString(R.string.start_time),0);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reset(View view){
        if(!running) {
            mStartTime = 0;
            mTimeLabel.setText(String.format("%6.2f", 0.0));
        }
    }


    public void startStop(View view) {
        if (running) {
            running = false;
            stop();
        } else {
            running = true;
            start();
        }
    }

    private void start() {
        if(mStartTime==0) {
            mStartTime = System.currentTimeMillis();
        }
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.post(mUpdateTimeTask);

    }

    private void stop() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
}
