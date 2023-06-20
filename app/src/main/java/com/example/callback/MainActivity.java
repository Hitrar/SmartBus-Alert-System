package com.example.callback;

//import static com.example.callback.Login.userDatabase;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static GoogleMap busMap;
    private final com.google.android.gms.location.LocationRequest locationRequest = com.google.android.gms.location.LocationRequest.create();
    private final int locationRequestCode = 1000; //ACCESS_FINE_LOCATION
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 14;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private static Location lastKnownLocation;
    private LatLng selectedLocation;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    TextToSpeech textToSpeech = null;
    private boolean locationPermissionGranted;
    public static List<Address> destinations = new ArrayList<>();
    public static boolean This_is_a_Destination;
    Context context;
//    static DatabaseReference journeyDatabase;
    //    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener mAuthListener;
    Users thisUser;
    String userid;

    public static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public static DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("Users");
    public static DatabaseReference vehicleDatabase = FirebaseDatabase.getInstance().getReference("Vehicles").child("vehicleDetails");
    public static DatabaseReference journeyDatabase = FirebaseDatabase.getInstance().getReference("Journeys");
    private static String thisVehicleCode;
    private static String finalDestination;
    static ArrayList<Destination> stopOvers = new ArrayList<>();
    public static final int bluetoothrequestcode = 11;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate: " + userid);
        Log.i(TAG, "onCreate: ");


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.i(TAG, "onCreate: FirebaseAuth.getInstance().getCurrentUser() is null");
            startActivity(new Intent(MainActivity.this, Login.class));
        }
        else {
            Log.i(TAG, "onCreate: FirebaseAuth.getInstance().getCurrentUser() is not null");
            userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            getUser(userid);
        }
        setContentView(R.layout.activity_main);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        SupportMapFragment mapFragment = ((SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map));
        assert mapFragment != null;
        mapFragment.getMapAsync(MainActivity.this);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userid!=null)
            getUser(userid);
    }

    private void getUser(String userid) {
        Log.i(TAG, "getUser:onCreate Tuko hapa");
        userDatabase.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                thisUser = snapshot.getValue(Users.class);
                assert thisUser != null;
                Log.i(TAG, "onDataChange: successful retrieval " + thisUser.email);
                Log.i(TAG, "onDataChange: successful retrieval " + thisUser.name);
                Log.i(TAG, "onDataChange: successful retrieval " + thisUser.vehicleCode);
                Log.i(TAG, "onDataChange: successful retrieval " + thisUser.vehicleRegistration);
                Log.i(TAG, "onDataChange: successful retrieval " + thisUser.vehicle);


                if (thisUser.vehicle) {
                    findViewById(R.id.floatingActionButton).setEnabled(true);
                    findViewById(R.id.floatingActionButton).setClickable(true);
                    String vehicleReg = thisUser.vehicleRegistration;
                    ArrayList<MyVehicle> vehicles = new ArrayList<>();
                    vehicleDatabase.child(thisUser.vehicleCode).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            System.out.println(String.valueOf(snapshot==null));
                            vehicles.add(snapshot.getValue(MyVehicle.class));
                            Log.i(TAG, "onDataChange: "+ vehicles.size());
                            if (vehicles.size() > 0) {
                                for (int i = 0; i < vehicles.size(); i++) {
                                    if (vehicleReg.equals(vehicles.get(i).vehiclePlate)) {
                                        thisVehicleCode = vehicles.get(i).vehicleCode;
                                        MyVehicle thisVehicle = vehicles.get(i);
                                        Log.i(TAG, "Next step in reading databases: ");
                                        Log.i(TAG, "onDataChange: "+ thisVehicleCode);
                                        journeyDatabase.child(thisVehicleCode).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                stopOvers.clear();
                                                long v =0;
                                                Log.i(TAG, "onDataChange reading journey: fdatabase");
                                                for(DataSnapshot stops : snapshot.getChildren()){
                                                    Log.i(TAG, "inside here last steps");
                                                    Destination stop = stops.getValue(Destination.class);
                                                    stopOvers.add(stop);
                                                    v++;
                                                }
//                                                ArrayList<Destination>
                                                v = snapshot.getChildrenCount();

                                                System.out.println("num,ber of stop overs is "+v);
                                                if (!(stopOvers.size() == 0)) {
                                                    Log.i(TAG, "onDataChange: stopOvers "+ stopOvers.get(0).longitude);
                                                    Log.i(TAG, "onDataChange: "+ stopOvers.get(0).getId());
                                                    Log.i(TAG, "onDataChange: "+ stopOvers.get(0).tagLocation);
                                                    Log.i(TAG, "onDataChange: "+ stopOvers.get(0).vehicleCode);
                                                    System.out.println(thisUser.UserId);
                                                for (int i = 0; i < stopOvers.size(); i++) {
                                                    if (thisUser.UserId.equals(thisVehicle.id)) {
                                                        finalDestination = stopOvers.get(i).tagLocation;
                                                        Log.i(TAG, "onDataChange:addedfinal dest " + finalDestination);
                                                    }
                                                }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.i(TAG, "onCancelled: " + error.getMessage());
                                            }
                                        });

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else{
                    setContentView(R.layout.activity_main_user);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Log.i(TAG, "onCancelled: there is an error " + error.getMessage());
            }
        });
        Log.i(TAG, "getUser: Tushapita");
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
        if (requestCode == 1000) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                }// TODO: Consider calling
//    ActivityCompat#requestPermissions

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
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
                return null;
            }

            @Override
            public View getInfoContents(@NonNull Marker marker) {
                marker.setDraggable(true);
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info,
                        findViewById(R.id.custom), false);
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

        if (!locationPermissionGranted) {
            getLocationPermission();
        }
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

//        Log.i(TAG, "onMapReady: " + thisUser.name);
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
//                getLocationPermission();
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
        }
        updateLocationUI();
    }


    public void onMapSearch(@NonNull View view) {
        busMap.clear();
        context = view.getContext();
        if (thisUser.isVehicle()) {
            EditText locationSearch = findViewById(R.id.searchText);
            String locationText = locationSearch.getText().toString();
            Log.i(TAG, "onMapSearch: " + locationText);
            SearchLocation query = new SearchLocation(context);
            query.execute(locationText);
        }
        else{
            EditText locationSearch = findViewById(R.id.searchText2);
            String locationText = locationSearch.getText().toString();
            Log.i(TAG, "onMapSearch: " + locationText);
            SearchLocation query = new SearchLocation(context);
            query.execute(locationText);
        }

        Log.i(TAG, "onMapSearch: " + findViewById(R.id.confirmAddButton).isClickable());
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
                if (selectedLocation == null)
                    selectedLocation = new LatLng(destinations.get(destinations.size()-1).getLatitude(), destinations.get(destinations.size()-1).getLongitude());
                Log.i(TAG, "onClick: selected location" + String.valueOf(selectedLocation.latitude));
                if (destinations.size() > 1){
                    Log.i(TAG, "onClick: adding the second selected");
                    selectedLocation = new LatLng(destinations.get(destinations.size()-1).getLatitude(), destinations.get(destinations.size()-1).getLongitude());}
                sendConfirmation(selectedLocation);


            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            // User cancelled the confirmation
            dialog.dismiss();
            busMap.clear();
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void sendConfirmation(LatLng selectedLocation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        View view = inflater.inflate(R.layout.send_location, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.send, (dialog, id) -> {
                    // send details to firebase/vehicle
                    EditText codeTxt = view.findViewById(R.id.editTextVehicleCode);
                    String code = codeTxt.getText().toString().trim();
                    EditText locationTagTxt = view.findViewById(R.id.editTextLocationTag);
                    String locationTag = locationTagTxt.getText().toString();

                    String myId = journeyDatabase.push().getKey();
                    Log.i(TAG, "sendConfirmation: " + myId);
                    Destination newDestination = new Destination( myId,selectedLocation.latitude,selectedLocation.longitude, locationTag, code);
                    journeyDatabase.child(code).child(myId).setValue(newDestination);
                    Log.i(TAG, "sendConfirmation: " + newDestination.id);
                    Log.i(TAG, "sendConfirmation: " + newDestination.longitude);
                    Log.i(TAG, "sendConfirmation: " + newDestination.tagLocation);
                    Log.i(TAG, "sendConfirmation: " + newDestination.id);
                    Toast.makeText(MainActivity.this, "Sent successfully", Toast.LENGTH_SHORT).show();


                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());


        builder.create().show();

    }

    public void setFinalDestination(View view) {
        //read vehicle code(using plate) clear previous destinations, set final destination then
//        start the loop, to check current location, compute distance, send appropriate notifications
    }

    public void signOut(MenuItem item) {
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this, Login.class));
    }

    public void startJourneyTracking(View view) {
//        Context ct = view.getContext();
        StartJourney startJourney = new StartJourney(this);
        startJourney.execute(thisVehicleCode);

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
            if (Geocoder.isPresent()) {
                Log.i(TAG, "doInBackground: " + Geocoder.isPresent());
                Geocoder geocoder = new Geocoder(ctx, new Locale("english", "Kenya"));
                try {
                    while (addressList.size() < 1) {
                        Log.i(TAG, location + " geocoder.getFromLocationName11");
                        addressList = geocoder.getFromLocationName(location, 1);
                        Log.i(TAG, "geocoder.getFromLocationName22");
                        if (addressList.size()>0){publishProgress(addressList.get(0));}
                        if (isCancelled()) break;

                    }
                } catch (IOException e) {
                    Log.i(TAG, "doInBackground: " + addressList);
                    e.printStackTrace();
                }
                Log.i(TAG, "onMapSearch: got an address");

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Address... addresses) {
            super.onProgressUpdate(addresses);
            Log.i(TAG, "ProgressUpdate" + addresses[0].getLatitude());
            LatLng latLng = new LatLng(addresses[0].getLatitude(), addresses[0].getLongitude());
            MainActivity.busMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            MainActivity.busMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

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

    //TODO here below
    class StartJourney extends AsyncTask<String, String, String> {
        Context ctx;
        String deviceAddress;
        BluetoothDevice myBluetoothDevice;
        BluetoothSocket bluetoothSocket;
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//bluetoothDevice.getUuids()[0].getUuid();

        public StartJourney(Context ct) {
            this.ctx = ct;
        }

//        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getDeviceLocation();
            BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.i(TAG, "onPreExecute: "+ ctx);
            Log.i(TAG, "onPreExecute: "+ Manifest.permission.BLUETOOTH_CONNECT);
            Log.i(TAG, "onPreExecute: "+ PackageManager.PERMISSION_GRANTED);
            if(bluetoothAdapter==null) {
                System.out.println("Bluetooth service is missing");
            }
            assert bluetoothAdapter != null;
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, bluetoothrequestcode);
            }
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                /*
                 TODO: Consider calling
                    ActivityCompat#requestPermissions
                 here to request the missing permissions, and then overriding
                   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                          int[] grantResults)
                 to handle the case where the user grants the permission. See the documentation
                 for ActivityCompat#requestPermissions for more details.
                */
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, locationRequestCode);
                return;
            }
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
//            bluetoothAdapter.getState();

            System.out.println("bonded devices"+bondedDevices);
            Log.i(TAG, "onPreExecute: bonded "+ bondedDevices);
            for (BluetoothDevice d : bondedDevices) {
                if (Objects.equals(d.getName(), "HC-05")) {
                    Log.i(TAG, "onPreExecute: "+ d.getAddress());
                    deviceAddress = d.getAddress();
                    myBluetoothDevice = bluetoothAdapter.getRemoteDevice(d.getAddress());
                    break;
                }
            }


        }

        @Override
        protected String doInBackground(String... strings) {
            // read database,
//            ArrayList<Destination> myDestinations = new ArrayList<>();
            String vehicleCode = strings[0];
//            myDestinations = stopOvers;

            String nextPlace = "";
            LatLng currentLocation1 = new LatLng(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude());
            Log.i(TAG, "background: "+ lastKnownLocation.getLatitude()+ "  Long "+ lastKnownLocation.getLongitude());
            int loops = stopOvers.size();
            System.out.println("Final destination is "+finalDestination);
            for  (int z =0;z<loops; z++) {
                System.out.println("final destination is "+ finalDestination);
                double distance = 0;
                if (stopOvers.size() > 0) {
                    for (int i = 0; i < stopOvers.size(); i++) {
                        Log.i(TAG, "doInBackground: stop over " + stopOvers.get(i).tagLocation);
                        Log.i(TAG, "doInBackground: stop over " + stopOvers.get(i).tagLocation + stopOvers.get(i).longitude);
                        Log.i(TAG, "doInBackground: stop over " + stopOvers.get(i).tagLocation + stopOvers.get(i).latitude);
                        if (distance == 0) {
                            //nextLocationDistance = CalculationByDistance(currentLocation1, new LatLng(stopOvers.get(i).getLatitude(), stopOvers.get(i).getLongitude()));
                            distance = computeDistanceBetween(currentLocation1, new LatLng(stopOvers.get(i).getLatitude(), stopOvers.get(i).getLongitude()))/1000;
                            Log.i(TAG, "doInBackground:  distance on spherical " + distance);
                            nextPlace = stopOvers.get(i).tagLocation;
                            Log.i(TAG, "doInBackground: distance " + String.valueOf(distance));
                            Log.i(TAG, "doInBackground: " + nextPlace);
                            if (distance < 40) {
                                Log.i(TAG, "now at " + nextPlace);
                                publishProgress(nextPlace);
                            }


                        } else {

                            if (distance < computeDistanceBetween(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), new LatLng(stopOvers.get(i).getLatitude(), stopOvers.get(i).getLongitude()))/1000) {
                                distance = computeDistanceBetween(currentLocation1, new LatLng(stopOvers.get(i).getLatitude(), stopOvers.get(i).getLongitude()))/1000;
                                Log.i(TAG, "doInBackground:  distance on spheri " + distance);
                                //nextLocationDistance = CalculationByDistance(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), new LatLng(stopOvers.get(i).getLatitude(), stopOvers.get(i).getLongitude()));
                                nextPlace = stopOvers.get(i).tagLocation;
                                if (distance < 40) {
                                    Log.i(TAG, "doInBackground: kopoisss");
                                    Log.i(TAG, "now at " + nextPlace);
                                    publishProgress(nextPlace);
                                }
                            }

                        }
                    }
                }
            }
            Log.i(TAG, "premature");
            return null;
        }

        public double CalculationByDistance(LatLng StartP, LatLng EndP) {
            double lon1 = StartP.longitude;
            double lon2 = EndP.longitude;
            double lat1 = StartP.latitude;
            double lat2 = EndP.latitude;
            double theta = lon1 - lon2;
            double dist = Math.sin(deg2rad(lat1))
                    * Math.sin(deg2rad(lat2))
                    + Math.cos(deg2rad(lat1))
                    * Math.cos(deg2rad(lat2))
                    * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
//            return (dist/0.62137);

            Location loc1 = new Location("");
            loc1.setLatitude(lat1);
            loc1.setLongitude(lon1);

            Location loc2 = new Location("");
            loc2.setLatitude(lat2);
            loc2.setLongitude(lon2);

            float distanceInMeters = loc1.distanceTo(loc2);
            return  distanceInMeters/1000;
        }

        private double deg2rad(double deg) {
            return (deg * Math.PI / 180.0);
        }

        private double rad2deg(double rad) {
            return (rad * 180.0 / Math.PI);
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);
            System.out.println("we done here");
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // closely
            String nextPlace = values[0];
            Log.i(TAG, "onProgressUpdate: next place is " + nextPlace);
            int counter = 0;
            do {
                try {
                    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    //if (myBluetoothDevice)
                    bluetoothSocket = myBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    bluetoothSocket.connect();
                    Log.i(TAG, "onProgressUpdate: bluetooth connected " + bluetoothSocket.isConnected());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                counter ++;
            }

            while (!bluetoothSocket.isConnected() && counter < 5);


            try {
                System.out.println(bluetoothSocket==null);
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                System.out.println("sending to device: "+ nextPlace);
                byte[] bytes = nextPlace.getBytes(StandardCharsets.UTF_8);
//                byte[] bytes = "Z".getBytes(StandardCharsets.UTF_8);

                outputStream.write(bytes);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                }, 2000);

                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if(i != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(Locale.UK);
                            textToSpeech.speak(nextPlace, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

            //close connection.
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //end

        }
    }


}




