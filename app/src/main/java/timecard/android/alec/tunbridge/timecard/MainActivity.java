package timecard.android.alec.tunbridge.timecard;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Handler handler = new Handler();
    private long startTime;
    private TextView timeLabel;
    private TextView startDistanceLabel;
    private TextView finishDistanceLabel;
    private Button startStopButton;
    private Button resetButton;
    private boolean running;
    private BeaconManager beaconManager;
    private Region startRegion;
    private Region finishRegion;
    private double startDistance;
    private double finishDistance;

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            timeLabel.setText(String.format("%6.2f", millis / 1000.0));
            handler.postAtTime(this,
                    SystemClock.uptimeMillis() + 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        timeLabel = (TextView) findViewById(R.id.time);
        startDistanceLabel = (TextView) findViewById(R.id.start_distance);
        finishDistanceLabel = (TextView) findViewById(R.id.finish_distance);
        startStopButton = (Button) findViewById(R.id.button_start_stop);
        resetButton = (Button) findViewById(R.id.button_reset);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        timeLabel.setText(String.format("%6.2f", 0.0));

        //Beacon stuff
        beaconManager = new BeaconManager(this);
        beaconManager.setForegroundScanPeriod(100,0);
        startRegion = new Region("startRegion", UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 16498, 12508);
        finishRegion = new Region("finishRegion", UUID.fromString("b9407f30-f5f8-466e-aff9-25556b57fe6d"), 35227, 55488);
    }

    @Override
    protected void onStart() {
        super.onStart();
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                // Note that results are not delivered on UI thread.
                if (region == startRegion) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Just in case if there are multiple beacons with the same uuid, major, minor.
                            // TODO we should really check these are our beacons here (also Start or Finish?)
                            Log.d(TAG, String.format("Found %d start beacons.", rangedBeacons.size()));
                            if (rangedBeacons.size() != 0) {
                                startDistance = Utils.computeAccuracy(rangedBeacons.get(0));
                                startDistanceLabel.setText(String.format("%3.2f", startDistance));
                            }
                        }
                    });
                }
                if (region == finishRegion){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Just in case if there are multiple beacons with the same uuid, major, minor.
                            // TODO we should really check these are our beacons here (also Start or Finish?)
                            Log.d(TAG, String.format("Found %d finish beacons.", rangedBeacons.size()));
                            if (rangedBeacons.size() != 0) {
                                finishDistance = Utils.computeAccuracy(rangedBeacons.get(0));
                                finishDistanceLabel.setText(String.format("%3.2f", finishDistance));
                            }
                        }
                    });
                }
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(startRegion);
                beaconManager.startRanging(finishRegion);
            }
        });
    }

    @Override
    protected void onStop() {
        beaconManager.disconnect();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(getString(R.string.start_time), startTime);
        savedInstanceState.putBoolean(getString(R.string.running),running);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        startTime = savedInstanceState.getLong(getString(R.string.start_time));
        running = savedInstanceState.getBoolean(getString(R.string.running));
        if(running){
            start();
        }
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
            startTime = 0;
            timeLabel.setText(String.format("%6.2f", 0.0));
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
        resetButton.setEnabled(false);
        if(startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        handler.removeCallbacks(mUpdateTimeTask);
        handler.post(mUpdateTimeTask);

    }

    private void stop() {
        resetButton.setEnabled(true);
        handler.removeCallbacks(mUpdateTimeTask);
    }
}
