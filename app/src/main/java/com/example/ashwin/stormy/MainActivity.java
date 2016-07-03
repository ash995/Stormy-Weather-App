package com.example.ashwin.stormy;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends ActionBarActivity {

    public static  final  String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;
    

    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipitationValue) TextView mPrecipitationValue;
    @BindView(R.id.summaryText) TextView mSummary;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.locationLabel) TextView mLocationLabel;
    @BindView(R.id.iconView) ImageView mIconView;
    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 37.8267;
        final double longitude = -122.423;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(latitude,longitude);
            }
        });



        getForecast(latitude,longitude);

    }

    private void getForecast(double latitude,double longitude) {
        String apiKey = "d0e1dd21273dac6ef385281a041822ae";
        String forecastUrl = "https://api.forecast.io/forecast/"+ apiKey +"/" + latitude + "," + longitude ;
        if(isNetworkAvaialable()) {
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
                            Log.v(TAG, jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.INVISIBLE);
                                    mRefreshImageView.setVisibility(View.VISIBLE);
                                }
                            });
                            mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            Log.v(TAG, jsonData);
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG,"EXCEPTION CAUGHT",e);
                    }

                }
            });
        }
        else{
            Toast.makeText(MainActivity.this,"Some Problem with the Network",Toast.LENGTH_LONG).show();
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it is");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipitationValue.setText(mCurrentWeather.getPrecipChance() + "");
        mSummary.setText(mCurrentWeather.getSummary());
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconView.setImageDrawable(drawable);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather mCurrentWeather = new CurrentWeather();

        mCurrentWeather.setTime(currently.getLong("time"));
        mCurrentWeather.setHumidity(currently.getDouble("humidity"));
        mCurrentWeather.setTemperature(currently.getDouble("temperature"));
        mCurrentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        mCurrentWeather.setSummary(currently.getString("summary"));
        mCurrentWeather.setIcon(currently.getString("icon"));
        mCurrentWeather.setTimeZone(forecast.getString("timezone"));

        return mCurrentWeather;
    }

    private boolean isNetworkAvaialable() {
        ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");

    }

}
