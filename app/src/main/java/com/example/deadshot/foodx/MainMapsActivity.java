package com.example.deadshot.foodx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *  Created by Farrukh Shahid
 */
public class MainMapsActivity extends AppCompatActivity {

    private static final String TAG = "MainMapsActivity->>";
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,168), new LatLng(71,136));;
    //UI components
    private DrawerLayout drawerLayout = null;
    private ImageView maps_drawer_button = null;
    private NavigationView maps_navigation_View = null;
    private NavigationView maps_navigation_View_right = null;
    private AutoCompleteTextView PlaceSearchBox = null;
    private TextView PlaceNameTextBox = null;
    private TextView PlaceDetailsTextBox = null;
    private FloatingActionButton MapsDrawerButtonRight = null;


    //Google Maps
    private GoogleMap mMap;
    /**
     * below are all location based attributes
     */
    private CameraPosition mCameraPosition;
    private FusedLocationProviderClient mFusedLocationProviderClient = null;
    // A default location (Islamabad) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(33.668085, 73.015853);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = true;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    /**
     * below are all places based attributes
     */

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private ClassAutoCompleteAdapter PlacesAutoCompleteAdapter;
    private ClassPlaceInfoContainer mPlace;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // to make a transparent notification bar if version > lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            Log.d(TAG,"onCreate: build version >= lollipop: "+ Build.VERSION.SDK.toString());
        }
        else{
            Log.d(TAG,"onCreate: build version < lollipop: "+ Build.VERSION.SDK.toString());
        }

        // Retrieve location and camera position from saved instance state.
        // last detected location
        if (savedInstanceState != null && SignInScreen.check) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
            Log.d(TAG,"onCreate: savedInstanceState != null");
        }
        else {
            Log.d(TAG,"onCreate: savedInstanceState = null");
        }
        setContentView(R.layout.activity_main_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG,"onMapReady: called");
                if (SignInScreen.check)
                    MapReady(googleMap);
            }
        });
        initialize();
        assign_listeners();


        Log.d(TAG,"onCreate: completed execution");
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap!=null){
            // getting the last known position and focus of the maps
            outState.putParcelable(KEY_CAMERA_POSITION,mMap.getCameraPosition());
            // getting the last known location (saved)
            outState.putParcelable(KEY_LOCATION,mLastKnownLocation);
            Log.d(TAG,"onSaveInstance: "+outState.toString());
            super.onSaveInstanceState(outState);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    public void ProfileViewClick(View v) {
        startActivity(new Intent(this, InitialProfileSetupScreen.class));
    }
    private void initialize(){
        drawerLayout = findViewById(R.id.Maps_drawer_layout);
        maps_drawer_button = findViewById(R.id.maps_drawer_button);
        maps_navigation_View = findViewById(R.id.maps_drawer_navigationView);
        PlaceNameTextBox = findViewById(R.id.PlaceNameTextBox);
        PlaceDetailsTextBox = findViewById(R.id.PlaceDetailsTextBox);
        MapsDrawerButtonRight = findViewById(R.id.maps_drawer_button_right);
        if (!SignInScreen.check)
            return;
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG,"Google Api Client: Connection Failed");
                    }
                })
                .build();
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        PlaceSearchBox = (AutoCompleteTextView) findViewById(R.id.LocationSearchTextBox);
        PlacesAutoCompleteAdapter = new ClassAutoCompleteAdapter(this,mGeoDataClient,LAT_LNG_BOUNDS,null);

    }
    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }
    private void assign_listeners(){
        // the side button in the search bar to open the navigation drawer
        maps_drawer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    Log.d(TAG,"Drawer Closed");
                    drawerLayout.openDrawer(GravityCompat.START);

                }
            }
        });
        MapsDrawerButtonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!drawerLayout.isDrawerOpen(GravityCompat.END)){
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });
        if (!SignInScreen.check)
            return;
        PlaceSearchBox.setAdapter(PlacesAutoCompleteAdapter);
        PlaceSearchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                setPlaceSuggestionClick(i);
                hideKeyboard();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void MapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setPadding(0, 200, 0, 0);
        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        Log.d(TAG,"MapReady: complete execution");

    }
    private void updateLocationUI() {
        if (mMap == null) {
            Log.d(TAG,"updateUI: mMap = null");
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                /**
                 * To get my current location
                 * This call is connected with a permission if the permission check not implimented
                 * then this call gives an error and says to implement it
                 * so this check if(mLocationPermissionGranted) verifies that check
                 */
                mMap.setMyLocationEnabled(true);
                /**
                 * There will be a button just bellow the edit text in maps
                 * this function call enables that button = true
                 */
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                Log.d(TAG,"UpdateUI: my location enabled = true");
            } else {
                mMap.setMyLocationEnabled(false);
                /**
                 * There will be a button just bellow the edit text in maps
                 * this function call disables that button = false
                 */
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                /**
                 * if i have a last known location and i dont want the app to track me
                 * then i will delete that location as well for making no sense
                 */
                mLastKnownLocation = null;
                Log.d(TAG,"UpdateUI: my location enabled = false");
                // if location = disable: then this function asks to grant the permission
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e(TAG + "Exception: %s", e.getMessage());
        }
    }
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                // getting the last location
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();// saves that location for future
                            // moves the focus on map to the current location
                            // camera means focus
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            Log.d(TAG,"GetDeviceLocation: task successful");
                        } else {
                            Log.d(TAG, "GetDeviceLocation: Current location is null. Using defaults.");
                            Log.e(TAG, "GetDeviceLocation: Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (TermsAndConditionsActivity.allPermissionsGranted){
            Log.d(TAG,"getLocationPermission: check for all conditions = true");
            return;
        }
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * below are places based functions
     */

    private void setPlaceSuggestionClick(int index){
        final AutocompletePrediction item = PlacesAutoCompleteAdapter.getItem(index);
        final String placeId = item.getPlaceId();

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if(!places.getStatus().isSuccess()){
                    Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                    places.release();
                    return;
                }
                final Place place = places.get(0);

                try{
                    mPlace = new ClassPlaceInfoContainer();
                    mPlace.setName(place.getName().toString());
                    mPlace.setAddress(place.getAddress().toString());
                    mPlace.setId(place.getId());
                    mPlace.setLatlng(place.getLatLng());
                    mPlace.setRating(place.getRating());
                    mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                    mPlace.setWebsite(place.getWebsiteUri());
//                            LocationSelectionTextView.setText(mPlace.getName());
//                            DetailsOfLocationSelection.setText(mPlace.getAddress());
                    Log.d(TAG, "onResult: place: " + mPlace.toString());
                }catch (NullPointerException e){
                    Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
                }
                GoToSearchedLocation(place);
                places.release();
            }
        });
    }
    private void GoToSearchedLocation(Place place){
        // Add a marker for the selected place, with an info window
        // showing information about that place.
        PlaceNameTextBox.setText(mPlace.getName());
        PlaceDetailsTextBox.setText(mPlace.getAddress());
        PlaceNameTextBox.setVisibility(View.VISIBLE);
        PlaceDetailsTextBox.setVisibility(View.VISIBLE);
        mMap.addMarker(new MarkerOptions()
                .title(mPlace.getName())
                .position(mPlace.getLatlng())
                .snippet(mPlace.getAddress()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(place.getViewport().getCenter().latitude,
                place.getViewport().getCenter().longitude), DEFAULT_ZOOM));
    }
    /**
     * Adapter that handles Autocomplete requests from the Places Geo Data Client.
     * {@link AutocompletePrediction} results from the API are frozen and stored directly in this
     * adapter. (See {@link AutocompletePrediction#freeze()}.)
     */
    private class ClassAutoCompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {

        private final String TAG = "PlacesAdapter";
        private final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
        /**
         * Current results returned by this adapter.
         */
        private ArrayList<AutocompletePrediction> mResultList;

        /**
         * Handles autocomplete requests.
         */
        private GeoDataClient mGeoDataClient;

        /**
         * The bounds used for Places Geo Data autocomplete API requests.
         */
        private LatLngBounds mBounds;

        /**
         * The autocomplete filter used to restrict queries to a specific set of place types.
         */
        private AutocompleteFilter mPlaceFilter;

        /**
         * Initializes with a resource for text rows and autocomplete query bounds.
         *
         * @see android.widget.ArrayAdapter#ArrayAdapter(android.content.Context, int)
         */
        public ClassAutoCompleteAdapter(Context context, GeoDataClient geoDataClient,
                                        LatLngBounds bounds, AutocompleteFilter filter) {
            super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
            mGeoDataClient = geoDataClient;
            mBounds = bounds;
            mPlaceFilter = filter;
        }

        /**
         * Sets the bounds for all subsequent queries.
         */
        public void setBounds(LatLngBounds bounds) {
            mBounds = bounds;
        }

        /**
         * Returns the number of results received in the last autocomplete query.
         */
        @Override
        public int getCount() {
            return mResultList.size();
        }

        /**
         * Returns an item from the last autocomplete query.
         */
        @Override
        public AutocompletePrediction getItem(int position) {
            return mResultList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = super.getView(position, convertView, parent);

            // Sets the primary and secondary text for a row.
            // Note that getPrimaryText() and getSecondaryText() return a CharSequence that may contain
            // styling based on the given CharacterStyle.

            AutocompletePrediction item = getItem(position);

            TextView textView1 = (TextView) row.findViewById(android.R.id.text1);
            TextView textView2 = (TextView) row.findViewById(android.R.id.text2);
            textView1.setText(item.getPrimaryText(STYLE_BOLD));
            textView2.setText(item.getSecondaryText(STYLE_BOLD));

            return row;
        }

        /**
         * Returns the filter for the current set of autocomplete results.
         */
        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();

                    // We need a separate list to store the results, since
                    // this is run asynchronously.
                    ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                    // Skip the autocomplete query if no constraints are given.
                    if (constraint != null) {
                        // Query the autocomplete API for the (constraint) search string.
                        filterData = getAutocomplete(constraint);
                    }

                    results.values = filterData;
                    if (filterData != null) {
                        results.count = filterData.size();
                    } else {
                        results.count = 0;
                    }

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                    if (results != null && results.count > 0) {
                        // The API returned at least one result, update the data.
                        mResultList = (ArrayList<AutocompletePrediction>) results.values;
                        notifyDataSetChanged();
                    } else {
                        // The API did not return any results, invalidate the data set.
                        notifyDataSetInvalidated();
                    }
                }

                @Override
                public CharSequence convertResultToString(Object resultValue) {
                    // Override this method to display a readable result in the AutocompleteTextView
                    // when clicked.
                    if (resultValue instanceof AutocompletePrediction) {
                        return ((AutocompletePrediction) resultValue).getFullText(null);
                    } else {
                        return super.convertResultToString(resultValue);
                    }
                }
            };
        }

        /**
         * Submits an autocomplete query to the Places Geo Data Autocomplete API.
         * Results are returned as frozen AutocompletePrediction objects, ready to be cached.
         * Returns an empty list if no results were found.
         * Returns null if the API client is not available or the query did not complete
         * successfully.
         * This method MUST be called off the main UI thread, as it will block until data is returned
         * from the API, which may include a network request.
         *
         * @param constraint Autocomplete query string
         * @return Results from the autocomplete API or null if the query was not successful.
         * @see GeoDataClient#getAutocompletePredictions(String, LatLngBounds, AutocompleteFilter)
         * @see AutocompletePrediction#freeze()
         */
        private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
            Log.i(TAG, "Starting autocomplete query for: " + constraint);

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            Task<AutocompletePredictionBufferResponse> results =
                    mGeoDataClient.getAutocompletePredictions(constraint.toString(), mBounds,
                            mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for at most
            // 60s for a result from the API.
            try {
                Tasks.await(results, 60, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            }

            try {
                AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();

                Log.i(TAG, "Query completed. Received " + autocompletePredictions.getCount()
                        + " predictions.");

                // Freeze the results immutable representation that can be stored safely.
                return DataBufferUtils.freezeAndClose(autocompletePredictions);
            } catch (RuntimeExecutionException e) {
                // If the query did not complete successfully return null
                Toast.makeText(getContext(), "Error contacting API: " + e.toString(),
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting autocomplete prediction API call", e);
                return null;
            }
        }
    }
    private class ClassPlaceInfoContainer {
        private String Name;
        private String Address;
        private String PhoneNumber;
        private String Id;
        private Uri Website;
        private LatLng latlng;
        private float rating;
        private String Attribution;

        public ClassPlaceInfoContainer(String name,
                                       String address,
                                       String phoneNumber,
                                       String id,
                                       Uri website,
                                       LatLng latlng,
                                       float rating,
                                       String attribution) {
            Name = name;
            Address = address;
            PhoneNumber = phoneNumber;
            Id = id;
            Website = website;
            this.latlng = latlng;
            this.rating = rating;
            Attribution = attribution;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String getAddress() {
            return Address;
        }

        public void setAddress(String address) {
            Address = address;
        }

        public String getPhoneNumber() {
            return PhoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            PhoneNumber = phoneNumber;
        }

        public String getId() {
            return Id;
        }

        public void setId(String id) {
            Id = id;
        }

        public Uri getWebsite() {
            return Website;
        }

        public void setWebsite(Uri website) {
            Website = website;
        }

        public LatLng getLatlng() {
            return latlng;
        }

        public void setLatlng(LatLng latlng) {
            this.latlng = latlng;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public String getAttribution() {
            return Attribution;
        }

        public void setAttribution(String attribution) {
            Attribution = attribution;
        }

        public ClassPlaceInfoContainer(){

        }

        @Override
        public String toString() {
            return "ClassPlaceInfoContainer{" +
                    "Name='" + Name + '\'' +
                    ", Address='" + Address + '\'' +
                    ", PhoneNumber='" + PhoneNumber + '\'' +
                    ", Id='" + Id + '\'' +
                    ", Website=" + Website +
                    ", latlng=" + latlng +
                    ", rating=" + rating +
                    ", Attribution='" + Attribution + '\'' +
                    '}';
        }
    }

}



