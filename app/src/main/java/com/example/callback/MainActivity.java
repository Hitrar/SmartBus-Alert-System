package com.example.callback;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private PlacesClient placesClient;
    public static GoogleMap busMap;
    private CameraPosition cameraPosition;
    private TextView txtLocation;
    private final com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create();
    private LocationCallback locationCallback;
    private final int locationRequestCode = 1000; //ACCESS_FINE_LOCATION
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private LatLng selectedLocation;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private boolean locationPermissionGranted;
    public static List<Address> destinations = new ArrayList<>();
    public static boolean This_is_a_Destination;
    Context context;
    DatabaseReference journeyDatabase;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        journeyDatabase = FirebaseDatabase.getInstance().getReference("Journeys");
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
//        toolbar.setSubtitle("Options");
        toolbar.inflateMenu(R.menu.main_menu);

        Places.initialize(getApplicationContext(), String.valueOf(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        SupportMapFragment mapFragment = ((SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync((OnMapReadyCallback) MainActivity.this);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
        this.txtLocation = (TextView) findViewById(R.id.searchText);

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        locationCallback = new LocationCallback() {
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                txtLocation.setText("Here");
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    if (location != null) {
//                        float wayLatitude = (float) location.getLatitude();
//                        float wayLongitude = (float) location.getLongitude();
//                        txtLocation.setText(String.format(Locale.US, "%s -- %s", wayLatitude, wayLongitude));
//
//
//                    }
//                    if (mFusedLocationClient != null) {
//                        Log.i("flushing", "onLocationResult: 222");
//                        mFusedLocationClient.removeLocationUpdates(locationCallback);
//                    }
//                }
//            }
//        };

    }//End of onCreate

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "onCreate: Launched7");
        if (busMap != null) {
            Log.i(TAG, "onCreate: Launched78");
            outState.putParcelable(KEY_CAMERA_POSITION, busMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onCreate: Launched79");
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0

                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    break;
                }
                break;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        Log.i(TAG, "onOptionsItemSelected: ");
//        switch (item.getItemId()) {
//            case R.id.Login:
////                addSomething();
//                return true;
//            case R.id.Logout:
////                startSettings();
//                return true;
//            case R.id.ConnectDisplay:
//                Log.i(TAG, "ConnecttedDispaly: Launched10");
//
//
//                return true;
//            case R.id.Feedback:
////                startSettings();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        busMap = googleMap;
        Log.i(TAG, "onCreate: Launched9");
        busMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        busMap.getUiSettings().setZoomControlsEnabled(true);
        busMap.getUiSettings().setCompassEnabled(true);
        busMap.getUiSettings().setMyLocationButtonEnabled(true);
        busMap.getUiSettings().setZoomGesturesEnabled(true);
        busMap.getUiSettings().setRotateGesturesEnabled(true);
        busMap.getUiSettings().setMapToolbarEnabled(false);


        busMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            //            Log.i(TAG, "onCreate: Launched10");
            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(@NonNull Marker arg0) {
                Log.i(TAG, "onCreate: Launched10");
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                marker.setDraggable(true);
                Log.i(TAG, "onCreate: Launched111");
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info,
                        findViewById(R.id.custom), false);
                Log.i(TAG, "onCreate: Launched12");

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());
                Log.i(TAG, "getInfoContents: " + marker.getTitle());
                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getPosition().toString());

                selectedLocation = marker.getPosition();
                return infoWindow;

            }
        });
        busMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                selectedLocation = marker.getPosition();
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
        });

        if (!locationPermissionGranted){
            getLocationPermission();
        }
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Log.i(TAG, "onCreate: Launched13");
                FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                Log.i(TAG, "onCreate: Launched14");
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        Log.i(TAG, "onCreate: Launched15");
                        if (lastKnownLocation != null) {
                            Log.i(TAG, "onCreate: Launched16");
                            Log.i(TAG, String.valueOf(lastKnownLocation.getLongitude()));
                            busMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                                busMap.addMarker(new MarkerOptions()
//                                        .position(new LatLng(lastKnownLocation.getLatitude(),
//                                                lastKnownLocation.getLongitude()))
//                                        .title("Marker "));
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        busMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                        busMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
    public void startLogin(MenuItem menuItem) {
        Intent login = new Intent(this, Login.class);
        startActivity(login);

    }




    private void updateLocationUI() {
        if (busMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                busMap.setMyLocationEnabled(true);
                busMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                busMap.setMyLocationEnabled(false);
                busMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    public void getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request for permission to access location
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    locationRequestCode);
        } else {
            Log.i("Requesting updates", "Permissions are already givens ");
            locationPermissionGranted = true;
//            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
        updateLocationUI();
    }



    public void onMapSearch(@NonNull View view) {
        context = view.getContext();
        EditText locationSearch = (EditText) findViewById(R.id.searchText);
        String locationText = locationSearch.getText().toString();
        Log.i(TAG, "onMapSearch: " + locationText);
        List<Address> addressList = new ArrayList<>();
        SearchLocation Query = new SearchLocation(context);
        Query.execute(locationText);
        Log.i(TAG, "onMapSearch: "+ findViewById(R.id.confirmAddButton).isClickable());
        findViewById(R.id.confirmAddButton).setEnabled(true);

    }

    public void launchDevices(MenuItem item) {
        Intent i = new Intent(this, SelectDeviceActivity.class);
        Log.i(TAG, "onCreate: Launched11111");
        startActivity(i);
        Log.i(TAG, "onCreate: Launched10");
            }
//  generates the dialog box to confirm a destination
    public void drawDialogBox(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User confirmed the destination
                sendConfirmation(selectedLocation);


            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the confirmation
                dialog.dismiss();
                busMap.clear();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void sendConfirmation(LatLng selectedLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.send_location, null))
                // Add action buttons
                .setPositiveButton(R.string.send, (dialog, id) -> {
                    // send details to firebase/vehicle
                    EditText codeTxt = findViewById(R.id.editTextTextVehicleCode);
                    String code = codeTxt.getText().toString();
                    EditText locationTagTxt = findViewById(R.id.editTextLocationTag);
                    String locationTag = codeTxt.getText().toString();

                    String myId = journeyDatabase.push().child("Journeys\\"+code).getKey();
                    Destination newDestination = new Destination(myId, code, selectedLocation, locationTag);
                    journeyDatabase.child("Journeys\\"+code).setValue(newDestination);



                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    static class SearchLocation extends AsyncTask<String, Address, String> {
        List<Address> addressList = new ArrayList<>();
        Context ctx;

        public SearchLocation(Context ct) {//Constructor
            this.ctx = ct;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(String... strings) {
            String location = strings[0];
            Log.i(TAG, "doInBackground: " + ctx);
            if (location != null || !location.equals("")) {
                if (Geocoder.isPresent()) {
                    Log.i(TAG, "doInBackground: " + Geocoder.isPresent());
                    Geocoder geocoder = new Geocoder(ctx, new Locale("english", "Kenya"));
                    try {
                        while (addressList.size() < 1) {
                            Log.i(TAG, location + " geocoder.getFromLocationName11");
                            addressList = geocoder.getFromLocationName(location, 1);
                            Log.i(TAG, "geocoder.getFromLocationName22");
                            publishProgress(addressList.get(0));
                            if (isCancelled()) break;

                        }
                    } catch (IOException e) {
                        Log.i(TAG, "doInBackground: " + addressList);
                        e.printStackTrace();
                    }
                    Log.i(TAG, "onMapSearch: got an address");

                }
            } else {
                Log.i(TAG, "onMapSearch: Geocoder service absent");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Address... addresses) {
            super.onProgressUpdate(addresses);
            Log.i(TAG, "ProgressUpdate" + addresses[0].getLatitude());
            Address[] address = addresses;
            LatLng latLng = new LatLng(address[0].getLatitude(), address[0].getLongitude());
            MainActivity.busMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            MainActivity.busMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

            destinations.add(addresses[0]);
            Log.i(TAG, "onProgressUpdate: added a destination " + destinations.get(0).getLatitude());

        }

        @Override
        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
            if (This_is_a_Destination) {
                Log.i(TAG, "onProgressUpdate: confirmed");
//                destinations.add([0]);
                This_is_a_Destination = false;

            }
            for (int i = 0; i < destinations.size(); i++) {
                Log.i(TAG, "onPostExecute: " + destinations.get(i).getLatitude());
            }
            Log.i(TAG, "onPostExecute: Completed");
        }
    }
}



