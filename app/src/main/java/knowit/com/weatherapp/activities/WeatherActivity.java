package knowit.com.weatherapp.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import knowit.com.weatherapp.AsyncTaskCompleteListener;
import knowit.com.weatherapp.R;
import knowit.com.weatherapp.TaskFragment;
import knowit.com.weatherapp.WeatherDetails;
import knowit.com.weatherapp.utils.ServerUtility;
import knowit.com.weatherapp.utils.Utility;
import knowit.com.weatherapp.xml.Product;
import knowit.com.weatherapp.xml.Symbol;
import knowit.com.weatherapp.xml.Temperature;
import knowit.com.weatherapp.xml.Time;
import knowit.com.weatherapp.xml.WeatherData;

import static android.content.ContentValues.TAG;

public class WeatherActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, TaskFragment.TaskCallbacks {

    private static final int ZOOM_LEVEL = 15;
    private static final int ONE_HOUR = 3600000; // ms
    private static final int REQUEST_CODE = 200;
    private static final int RESOLUTION_REQUEST_CODE = 1000;
    private static final int WEATHER_REQUEST_INTERVAL = 5; // min
    private static final int WEATHER_REQUEST_DISTANCE = 500; // m
    private static final int LOCATION_REQUEST_INTERVAL = 30000; // ms
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL= 5000; // ms

    private static final String DASH = "-";
    private static final String DEGREES = " °";
    private static final String LOCATION = "location";
    private static final String LOCATION_TITLE = "Your current location";
    private static final String LATITUDE_PARAMETER = "?lat=";
    private static final String LONGITUDE_PARAMETER = ";lon=";
    private static final String WEATHER_FRAGMENT_TAG = "WEATHER_FRAGMENT";
    private static final String WEATHER = "weather";
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "REQUESTING_LOCATION_UPDATES";

    private ImageView mWeatherIcon;
    private TextView mTemperature;
    private TextView mDate;

    private FragmentManager mFragmentManager;
    private TaskFragment mTaskFragment;

    private GoogleMap mMap;
    private WeatherDetails mWeather;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private GoogleApiClient mGoogleApiClient = null;
    private LocationCallback mLocationCallback;
    private boolean mRequestingLocationUpdates;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDate = (TextView) findViewById(R.id.date);
        mTemperature = (TextView) findViewById(R.id.temperature);
        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);
        mFusedLocationClient = new FusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    updateMap(location);
                    checkWeatherUpdate(location);
                }
            };
        };

        if (savedInstanceState != null && savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    REQUESTING_LOCATION_UPDATES_KEY);
            mWeather = savedInstanceState.getParcelable(WEATHER);
            if (mWeather != null) {
                updateWeatherDetails(mWeather);
            }
        } else {
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        if (mWeather != null) {
            state.putParcelable(WEATHER, mWeather);
        }

        super.onSaveInstanceState(state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mWeather = state.getParcelable(WEATHER);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new WeatherLocationListener();
        mFragmentManager = getFragmentManager();
        mTaskFragment = (TaskFragment) mFragmentManager.findFragmentByTag(WEATHER_FRAGMENT_TAG);

        if (mWeather != null) {
            updateWeatherDetails(mWeather);
        } else {
            getLocation();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        mLocationListener = new WeatherLocationListener();
            mFragmentManager = getFragmentManager();
            mTaskFragment = (TaskFragment) mFragmentManager.findFragmentByTag(WEATHER_FRAGMENT_TAG);
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if(grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    askForGPS();
                    getLocation();
                }
            };
            break;
            default:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap(mLocation);
    }

    private void checkWeatherUpdate(Location location) {
        if (mLocation != null && Math.abs(mLocation.distanceTo(location)) <= WEATHER_REQUEST_DISTANCE) {
            if(mWeather == null || Utility.getDuration(mWeather.getDate()) > WEATHER_REQUEST_INTERVAL) {
                getWeather(location);
            } else if (mWeather != null) {
                updateWeatherDetails(mWeather);
            }
        } else {
            getWeather(location);
        }
    }

    private void updateMap(Location location) {
        if (location != null) {
            mLocation = location;
            LatLng position = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(position).title(LOCATION_TITLE));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_LEVEL));
        }
    }

    private void updateWeatherDetails(WeatherDetails details) {
        String temperature = details.getTemperature() + DEGREES + details.getUnits().toUpperCase().charAt(0);
        loadImage(mWeatherIcon, details.getIconId(), details.getIconNumber());
        mTemperature.setText(temperature);
        mDate.setText(mWeather.getDate());
    }

    private void getWeather(Location location) {
        mLocation = location;
        String query = LATITUDE_PARAMETER + location.getLatitude() +
                LONGITUDE_PARAMETER + location.getLongitude();
//        if (mTaskFragment == null) {
        mTaskFragment = TaskFragment.newInstance(query);
        mFragmentManager.beginTransaction().add(mTaskFragment, WEATHER_FRAGMENT_TAG).commit();
//        }
    }

    private void loadImage(final ImageView container, String icon, int number) {
        final String imageId = icon + number;
        Bitmap image = Utility.getBitmapFromMemCache(imageId);
        if (image != null) {
            container.setImageBitmap(image);
            container.refreshDrawableState();
        } else  {
            // if no usable bitmap is available, fetch image from server
            ServerUtility.getImageBitmap(new AsyncTaskCompleteListener() {
                @Override
                public void onSuccess(Bitmap image) {
                    Utility.addBitmapToMemoryCache(imageId, image);
                    container.setImageBitmap(image);
                    container.refreshDrawableState();
                }

                @Override
                public void onFailure() {
                    // use default image
                }
            }, number);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            return;
        } else {
            askForGPS();
//            if (mLocationManager != null) {
//                mLocationManager.requestLocationUpdates(
//                        LocationManager.GPS_PROVIDER, Utility.MINIMUM_LOCATION_UPDATE_DELAY,
//                        Utility.MINIMUM_LOCATION_UPDATE_DISTANCE, mLocationListener);
//            }
            mFusedLocationClient.requestLocationUpdates(LocationRequest.create(),
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    private void askForGPS() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    }).build();
            mGoogleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
            locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(
                                        WeatherActivity.this, RESOLUTION_REQUEST_CODE);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    private class WeatherLocationListener implements LocationListener {

        private static final String LONGITUDE = "Longitude: ";
        private static final String LATITUDE = "Latitude: ";

        @Override
        public void onLocationChanged(Location location) {
            updateMap(location);
            checkWeatherUpdate(location);
            Log.v(TAG, LONGITUDE + location.getLongitude());
            Log.v(TAG, LATITUDE + location.getLatitude());
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    // TaskFragment.TaskCallbacks implementation beyond this comment

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(final WeatherData result) {
        if(!WeatherActivity.this.isDestroyed()) {
            if (mTaskFragment.getException() == null) {
                if (result != null){
                    processResult(result);
                }
            }
        }
    }

    private void processResult(WeatherData result) {
        Product product = result.getProduct();
        List<Time> times = product.getList();
        Calendar calendar = Calendar.getInstance();
        long currentTemperatureTime, currentIconTime, currentTime, from, to;
        currentTemperatureTime = currentIconTime = from = to = 0;
        currentTime = calendar.getTimeInMillis();

        mWeather = new WeatherDetails();

        for (Time time : times) {
            to = Utility.getTimeFromDateString(time.getTo());
            from  = Utility.getTimeFromDateString(time.getFrom());
            if (to >= currentTime && time.getLocation().getSymbol() != null
                    && currentIconTime < from && calendar.getTimeInMillis() >= from) {
                currentIconTime = from;
                updateIcon(time.getLocation().getSymbol());
            }
            if (currentTemperatureTime < from && time.getLocation().getTemperature() != null &&
                    ((currentTime >= from && currentTime <= to)
                            || (from == to && currentTime-ONE_HOUR < from && currentTime > from))){
                currentTemperatureTime = from;
                updateTemperature(time.getLocation().getTemperature());
            }
        }

        updateDate(Utility.convertDateToString(new Date(currentTime)));
    }

    private void updateDate(String date) {
        if (date != null) {
            mWeather.setDate(date);
            mDate.setText(date);
        }
    }

    private void updateIcon(Symbol s) {
        if (s != null) {
            loadImage(mWeatherIcon, s.getId(), s.getNumber());
            mWeather.setIconNumber(s.getNumber());
            mWeather.setIconId(s.getId());
        }
    }

    private void updateTemperature(Temperature t) {
        String temperature = DASH;
        if (t != null) {
            temperature = t.getValue() + DEGREES + t.getUnit().toUpperCase().charAt(0);
            mWeather.setTemperature(t.getValue());
            mWeather.setUnits(t.getUnit());
        }
        mTemperature.setText(temperature);
    }
}
