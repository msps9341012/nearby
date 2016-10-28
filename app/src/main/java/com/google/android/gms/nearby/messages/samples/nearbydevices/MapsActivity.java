package com.google.android.gms.nearby.messages.samples.nearbydevices;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import android.os.AsyncTask;
import java.util.ArrayList;
import org.json.JSONArray;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import com.readystatesoftware.viewbadger.BadgeView;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;

    //*****暘哥加這邊↓↓↓↓

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int TTL_IN_SECONDS = 3 * 60; // Three minutes.
    private static final String KEY_UUID = "key_uuid";
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder().setTtlSeconds(TTL_IN_SECONDS).build();
    private static String getUUID(SharedPreferences sharedPreferences) {
        String uuid = sharedPreferences.getString(KEY_UUID, "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_UUID, uuid).apply();
        }
        return uuid;
    }
    private SwitchCompat mSubscribeSwitch;
    private Message mPubMessage;
    private MessageListener mMessageListener;
    public static int length = 1;
    private GoogleApiClient mGoogleApiClient;

    //*****暘哥加這邊↑↑↑↑↑





    BadgeView badge1;
    Button btnPosition;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnPosition = (Button) findViewById(R.id.test);
        badge1 = new BadgeView(this, btnPosition);
        badge1.setText("12");
        badge1.show();


        //*****暘哥加這邊↓↓↓↓

        mSubscribeSwitch = (SwitchCompat) findViewById(R.id.subscribe_switch1);
        mPubMessage = DeviceMessage.newNearbyMessage(getUUID(getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE)));
        doSomethingRepeatedly();
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                testNotification(DeviceMessage.fromNearbyMessage(message).getMessageBody());
            }

            @Override
            public void onLost(final Message message) {

            }
        };

        mSubscribeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If GoogleApiClient is connected, perform sub actions in response to user action.
                // If it isn't connected, do nothing, and perform sub actions when it connects (see
                // onConnected()).
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    if (isChecked) {
                        subscribe();
                        publish();
                    } else {
                        unsubscribe();
                        unpublish();
                    }
                }
            }
        });
        buildGoogleApiClient();

        //*****暘哥加這邊↑↑↑↑↑



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }


    //*****暘哥加這邊↓↓↓↓

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logAndShowSnackbar("Exception while connecting to Google Play services: " +
                connectionResult.getErrorMessage());
    }

    @Override
    public void onConnectionSuspended(int i) {
        logAndShowSnackbar("Connection suspended. Error code: " + i);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");

        if (mSubscribeSwitch.isChecked()) {
            publish();
            subscribe();
        }

    }


    private void subscribe() {
        Log.i(TAG, "Subscribing");
        SubscribeOptions options = new SubscribeOptions.Builder().setStrategy(PUB_SUB_STRATEGY).build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options);
    }


    private void publish() {
        Log.i(TAG, "Publishing");
        PublishOptions options = new PublishOptions.Builder().build();

        Nearby.Messages.publish(mGoogleApiClient, mPubMessage, options);
    }

    private void unsubscribe() {
        Log.i(TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }


    private void unpublish() {
        Log.i(TAG, "Unpublishing.");
        Nearby.Messages.unpublish(mGoogleApiClient, mPubMessage);
    }


    private void logAndShowSnackbar(final String text) {
        Log.w(TAG, text);
        View container = findViewById(R.id.map);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    public class AsyncTaskParseJson extends AsyncTask<String, String, String>  {

        String yourJsonStringUrl ="http://140.113.72.12:8089/photo/lilybon.php";
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... arg0) {

                JSONParser jParser = new JSONParser();

                length = jParser. getJSONFromUrl(yourJsonStringUrl);


            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            if (length>1)
            {
                testNotification();
                badge1.show();
            }

        }
    }
    private void doSomethingRepeatedly() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate( new TimerTask() {
            public void run() {

                try{

                    new AsyncTaskParseJson().execute();

                }
                catch (Exception e) {
                    // TODO: handle exception
                }
                if (length>1)
                {
                    cancel();
                }


            }
        }, 0, 2000);
    }
    public void testNotification(String s)
    {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.transfer2)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("擦肩而過")
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setColor(Color.WHITE)
                .setLights(Color.GREEN, 2000, 2000)
                .setStyle(new Notification.BigTextStyle().bigText(s));


        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
    public void testNotification()
    {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.iconrocket)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("任務通知")
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setColor(Color.WHITE)
                .setLights(Color.GREEN, 2000, 2000)
                .setContentText("測試測試");

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());


    }


    //*****暘哥加這邊↑↑↑↑↑

}
