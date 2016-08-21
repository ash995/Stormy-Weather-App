package com.example.ashwin.stormy;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ashwin.stormy.weather.Current;
import com.example.ashwin.stormy.weather.Day;
import com.example.ashwin.stormy.weather.Forecast;
import com.example.ashwin.stormy.weather.Hour;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    public static final String TAG = MainActivity.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public Location location = null;

    private LocationRequest mLocationRequest;

    private String addr ;

    public static String DAILY_FORECAST = "DAILY_FORECAST";

    public static String HOURLY_FORECAST = "HOURLY_FORECAST";

    //private double latitude;
    //private double longitude;

    private Forecast mForecast;
    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipitationValue) TextView mPrecipitationValue;
    @BindView(R.id.summaryText) TextView mSummary;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.locationLabel) TextView mLocationLabel;
    @BindView(R.id.iconView) ImageView mIconView;
    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.dailyButton) Button mDailyButton;
    @BindView(R.id.hourlyButton) Button mHourlyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setInterval(10*1000)
                .setFastestInterval(1*1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mProgressBar.setVisibility(View.INVISIBLE);

        //final double latitude = location.getLatitude();
        //final double longitude = location.getLongitude();

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(location.getLatitude(), location.getLongitude());
            }
        });


       // getForecast(latitude, longitude);



    }

    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "d0e1dd21273dac6ef385281a041822ae";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;
        if (isNetworkAvaialable()) {
            mRefreshImageView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.INVISIBLE);
                            mRefreshImageView.setVisibility(View.VISIBLE);
                        }
                    });
                    alertUserAboutError();

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                           // Log.v(TAG, jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    mRefreshImageView.setVisibility(View.VISIBLE);
                                }
                            });
                            mForecast = getForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                           // Log.v(TAG, jsonData);
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "EXCEPTION CAUGHT", e);
                    }

                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Some Problem with the Network", Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mForecast.getCurrent().getTemperature() + "");
        mTimeLabel.setText("At " + mForecast.getCurrent().getFormattedTime() + " it is");
        mHumidityValue.setText(mForecast.getCurrent().getHumidity() + "");
        mPrecipitationValue.setText(mForecast.getCurrent().getPrecipChance() + "");
        mSummary.setText(mForecast.getCurrent().getSummary());
        Drawable drawable = getResources().getDrawable(mForecast.getCurrent().getIconId());
        mIconView.setImageDrawable(drawable);
        mLocationLabel.setText(addr);

    }


    private Forecast getForecastDetails(String jsonData) throws JSONException{

        Forecast forecast = new Forecast();

        forecast.setDays(getDailyDetails(jsonData));
        forecast.setHours(getHourlyDetails(jsonData));

        forecast.setCurrent(getCurrentDetails(jsonData));
        return forecast;
    }

    private Hour[] getHourlyDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");


        Hour[] hours = new Hour[data.length()];

        for(int i=0;i<data.length();i++)
        {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }
        return hours;
    }

    private Day[] getDailyDetails(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");


        Day[] days = new Day[data.length()];

        for(int i=0;i<data.length();i++)
        {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;
        }
        return days;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");

        Current mCurrent = new Current();

        mCurrent.setTime(currently.getLong("time"));
        mCurrent.setHumidity(currently.getDouble("humidity"));
        mCurrent.setTemperature(currently.getDouble("temperature"));
        mCurrent.setPrecipChance(currently.getDouble("precipProbability"));
        mCurrent.setSummary(currently.getString("summary"));
        mCurrent.setIcon(currently.getString("icon"));
        mCurrent.setTimeZone(forecast.getString("timezone"));

        return mCurrent;
    }

    private boolean isNetworkAvaialable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location Services connected.");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(location==null){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
        else{
            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0)
                addr = addresses.get(0).getLocality();
            getForecast(location.getLatitude(),location.getLongitude());
        }
    }

    private void handleNewLocation(Location location) {
       // Log.d(TAG,location.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
       // Log.i(TAG,"Location Services suspended. Please reconnect");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        if(connectionResult.hasResolution()){
            try{
                connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);
            }
            catch(IntentSender.SendIntentException e){
                e.printStackTrace();
            }
        }
        else {
          //  Log.i(TAG,"Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }


    @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view)
    {
        Intent intent = new Intent(this,DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST,mForecast.getDays());
        startActivity(intent);
    }

    @OnClick (R.id.hourlyButton)
    public void startHourlyActivity(View view)
    {
        Intent intent = new Intent(this,HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST,mForecast.getHours());
        startActivity(intent);
    }
}

