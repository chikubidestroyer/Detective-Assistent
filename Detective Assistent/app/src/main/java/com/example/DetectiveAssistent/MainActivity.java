package com.example.DetectiveAssistent;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 100;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private List<LatLng> pathPoints;
    private Polyline polyline;

    private Marker currentLocationMarker;
    private Marker permanentPin; // Added for permanent pin

    private List<Marker> permanentPins = new ArrayList<>();

    private static final String PREF_PERMANENT_PINS = "pref_permanent_pins";

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private FrameLayout videoContainer; // Add a layout container for the VideoFragment



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pathPoints = new ArrayList<>();

        // Initialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create location request
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds

        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateMap(location);
                }
            }
        };
        // Add floating chat fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.chatFragmentContainer, new ChatFragment())
                    .commit();
        }

        // Load saved path points
        loadPathPoints();

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        videoContainer = findViewById(R.id.video_container);
    }
    // Method to display video fragment when needed
    public void showVideoFragment(String videoPath) {
        // Create a new instance of VideoFragment
        VideoFragment videoFragment = new VideoFragment();

        // Pass the video path to the fragment
        Bundle args = new Bundle();
        args.putString("videoPath", videoPath);
        videoFragment.setArguments(args);

        // Add VideoFragment to the container
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.video_container, videoFragment)
                .commit();
    }

    private void savePermanentPins() {
        // Get SharedPreferences editor
        SharedPreferences.Editor editor = getSharedPreferences(PREF_PERMANENT_PINS, MODE_PRIVATE).edit();
        // Serialize and save the positions, titles, and video paths of permanent pins
        for (int i = 0; i < permanentPins.size(); i++) {
            Marker marker = permanentPins.get(i);
            LatLng position = marker.getPosition();
            editor.putFloat("pin_" + i + "_lat", (float) position.latitude);
            editor.putFloat("pin_" + i + "_lng", (float) position.longitude);
            editor.putString("pin_" + i + "_title", marker.getTitle());

            Log.d("Save Pins", "Saving Pins: " + i);
        }
        Log.d("Save Pins", "Saving Pins");
        // Commit the changes
        editor.apply();
    }


    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager();
            }
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }


    private void loadPermanentPins() {

        // Proceed with loading pins
        // Get SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREF_PERMANENT_PINS, MODE_PRIVATE);
        Log.d("loadPins", "done requesting permission");
        // Load the positions, titles, and video paths of permanent pins and add them to the map
        for (int i = 0; ; i++) {
            // Retrieve the position, title, and video path of each pin
            float lat = prefs.getFloat("pin_" + i + "_lat", Float.MIN_VALUE);
            float lng = prefs.getFloat("pin_" + i + "_lng", Float.MIN_VALUE);
            String title = prefs.getString("pin_" + i + "_title", null);
            if (lat == Float.MIN_VALUE || lng == Float.MIN_VALUE || title == null) {
                // No more pins stored
                break;
            }
            LatLng position = new LatLng(lat, lng);

            // Add the pin to the map with its title
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(title);
            Marker permanentPin = mMap.addMarker(markerOptions);
            permanentPins.add(permanentPin);


            // Associate the video path with the marker if available
//            if (videoPath != null) {
//                permanentPin.setTag(videoPath);
//            }
        }
    }






    @Override
    protected void onStart() {
        super.onStart();
        // Check permissions and request location updates
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);
        Log.d("onstop", "Saving Pins");
        // Save path points
        savePathPoints();
        savePermanentPins();
    }

    private void startLocationUpdates() {
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
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null /* Looper */);
    }




    public void erasePath(View view) {
        // Clear the pathPoints list
        pathPoints.clear();

        // Remove polyline from map
        if (polyline != null) {
            polyline.remove();
        }

        // Remove marker from map
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
            currentLocationMarker = null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadPermanentPins();

        // Draw polyline for loaded path points
        if (!pathPoints.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(pathPoints)
                    .color(Color.BLUE)
                    .width(5);
            polyline = mMap.addPolyline(polylineOptions);

            // Move camera to the last location in the path
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.get(pathPoints.size() - 1), 15));
        }
        // Add long click listener for setting permanent pin
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addPermanentPin(latLng);
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Check if the clicked marker is a permanent pin
                if (permanentPins.contains(marker)) {
                    // Handle marker click (e.g., show dialog to add video or rename)
                    showOptionsDialog(marker);
                    return true; // Consume the event
                }
                return false; // Let the default behavior occur
            }
        });
    }

    private void showOptionsDialog(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(marker.getTitle());
        final List<String> options = new ArrayList<>();
        options.add("Rename");

        // Check if the marker has a video associated with it
        if (marker.getTag() != null && marker.getTag() instanceof String) {
            options.add("View Video");
        } else {
            options.add("Add Video");
        }

        options.add("Delete Pin");

        builder.setItems(options.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Rename pin
                        showRenameDialog(marker);
                        break;
                    case 1:
                        if ("View Video".equals(options.get(1))) {
                            // View video
                            viewVideo(marker);
                        } else {
                            // Add video
                            addVideoToPin(marker);
                        }
                        break;
                    case 2:
                        // Delete pin
                        deletePermanentPin(marker);
                        break;
                }
            }
        });
        builder.show();
    }

    private void deletePermanentPin(Marker marker) {
        // Remove the marker from the map
        marker.remove();
        // Remove the marker from the list of permanent pins
        permanentPins.remove(marker);
        // Remove the marker's data from shared preferences
        removePermanentPinFromSharedPreferences(marker);
    }

    private void removePermanentPinFromSharedPreferences(Marker marker) {
        // Get SharedPreferences editor
        SharedPreferences.Editor editor = getSharedPreferences(PREF_PERMANENT_PINS, MODE_PRIVATE).edit();

        // Remove the marker's data based on its position
        for (int i = 0; i < permanentPins.size(); i++) {
            if (permanentPins.get(i).equals(marker)) {
                editor.remove("pin_" + i + "_lat");
                editor.remove("pin_" + i + "_lng");
                editor.remove("pin_" + i + "_title");
                break;
            }
        }

        // Commit the changes
        assert(editor.commit());
    }



    private void viewVideo(Marker marker) {
        // Retrieve the video path associated with the marker
        String videoPath = (String) marker.getTag();

        if (videoPath != null && !videoPath.isEmpty()) {

            Log.d("VideoPath", "Video path for marker : " + videoPath);
            // Create a new instance of VideoFragment
            VideoFragment videoFragment = new VideoFragment();

            // Set the video path to the VideoFragment

            videoFragment.setVideoPath(videoPath);

            // Get the FragmentManager and start a FragmentTransaction
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.video_container, videoFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            // Display a message indicating that no video is associated with the marker
            Toast.makeText(this, "No video associated with this marker", Toast.LENGTH_SHORT).show();
        }
    }







    private void showRenameDialog(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Pin");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(marker.getTitle());
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                marker.setTitle(newName); // Update marker title
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    // Declare a variable to hold the marker
    Marker[] selectedMarker = new Marker[1];

    // Register ActivityResultLauncher
    private ActivityResultLauncher<Intent> videoCaptureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // Check if data is not null
                    if (data != null) {
                        // Get the video file URI
                        Uri videoUri = data.getData();
                        // Resolve the video file path from the URI
                        String videoPath = getVideoPathFromUri(videoUri);
                        if (videoPath != null) {
                            // Save video path with the marker, assuming selectedMarker is not null
                            if (selectedMarker[0] != null) {
                                selectedMarker[0].setTag(videoPath);
                            } else {
                                Log.e("VideoCaptureLauncher", "Selected marker is null");
                            }
                        } else {
                            Log.e("VideoCaptureLauncher", "Video path is null");
                        }
                        // Handle the video data as needed
                    }
                }
            });

    // Method to retrieve the video file path from the URI
    private String getVideoPathFromUri(Uri uri) {
        String videoPath = null;
        try {
            // Resolve the actual file path from the URI
            String[] projection = {MediaStore.Video.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                videoPath = cursor.getString(columnIndex);
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoPath;
    }



    // Method to add video to pin
    private void addVideoToPin(Marker marker) {
        // Set the selectedMarker to the current marker
        selectedMarker[0] = marker;

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        videoCaptureLauncher.launch(intent);
    }



    private void requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }





    private void addPermanentPin(LatLng latLng) {
        // Add new permanent pin marker at the long-clicked location
        Marker permanentPin = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Permanent Pin"));

        // Add the marker to the list of permanent pins
        permanentPins.add(permanentPin);
    }

    private void updateMap(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        pathPoints.add(latLng);

        // Draw polyline
        if (polyline != null) {
            polyline.remove();
        }
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(pathPoints)
                .color(Color.BLUE)
                .width(5);
        polyline = mMap.addPolyline(polylineOptions);

        // Update marker for current location
        if (currentLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("Current Location");
            currentLocationMarker = mMap.addMarker(markerOptions);
        } else {
            currentLocationMarker.setPosition(latLng);
        }

        // Move camera to current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private void savePathPoints() {
        try {
            File file = new File(getFilesDir(), "path_points.txt");
            FileOutputStream fos = new FileOutputStream(file);
            for (LatLng latLng : pathPoints) {
                String line = latLng.latitude + "," + latLng.longitude + "\n";
                fos.write(line.getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPathPoints() {
        try {
            File file = new File(getFilesDir(), "path_points.txt");
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    double latitude = Double.parseDouble(parts[0]);
                    double longitude = Double.parseDouble(parts[1]);
                    LatLng latLng = new LatLng(latitude, longitude);
                    pathPoints.add(latLng);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
