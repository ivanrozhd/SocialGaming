package com.example.littlelarri.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.example.littlelarri.trading.Trading;
import com.example.littlelarri.helpers.MenuBarHelper;
import com.example.littlelarri.helpers.PlayerVolleyHelper;
import com.example.littlelarri.R;
import com.example.littlelarri.helpers.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapActivity extends AppCompatActivity {
    private static final double MAX_TRADE_DISTANCE = 100;// meter
    private static final String TAG = "MapActivity";

    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 0;

    // Attributes for items spawning
    private static final int ITEMS_SPAWN_AMOUNT = 40;
    private static final float ITEMS_SPAWN_RADIUS = 500f;
    private static final float ITEMS_SPAWN_REGENERATE_DISTANCE = 400f;
    private static final float ITEMS_SPAWN_DISTANCE_TO_PLAYER = 100f;

    private static final float ITEM_VISIBLE_DISTANCE = 70f;

    private static final String[] ITEMS = {"food", "pills", "pizza", "book", "toy", "coins"};
    private static final int[] ITEMS_IMAGES = {
            R.drawable.image_water,
            R.drawable.image_medicine,
            R.drawable.image_food,
            R.drawable.image_book,
            R.drawable.image_toy,
            R.drawable.image_coins};
    private static final int ITEM_ICONS_SIZE = 150;
    private Drawable[] item_icons;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference activePlayers;
    private SharedPreferences sharedPreferences;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ValueEventListener locationDBListener;
    private final long LOCATION_REQUEST_INTERVAL = 5000;

    private MapView mapView;
    private MyLocationNewOverlay playerLocationOverlay;

    private Switch tradeSwitch;
    private Button tradeButton;
    private String tradePartnerUID;
    private String tradePartnerNickname;

    private PlayerVolleyHelper playerVolleyHelper;

    private Set<Marker> activePlayerMarkers = new HashSet<>();
    private Set<Marker> itemMarkers = new HashSet<>();
    private Location itemGenerationLocation;

    // TODO generals TODOs:
    // TODO prevent animation from playing towards marker

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playerVolleyHelper = new PlayerVolleyHelper(this);

        sharedPreferences = getSharedPreferences("map_data", MODE_PRIVATE);
        loadLastLocation();
        initializeItemIcons(ITEM_ICONS_SIZE, ITEM_ICONS_SIZE);

        requestPermissionsIfNecessary();
        firebaseAuth = FirebaseAuth.getInstance();
        // TODO: Add String to your Firebase Realtime Database
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(getResources().getString(R.string.firebase_realtime_database_url));
        activePlayers = firebaseDatabase.getReference().child("activePlayers");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //setting up OSM (should be done before setContentView)
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_map);

        MenuBarHelper.setup(this, MapActivity.this, R.id.menuToMap);

        tradeButton = findViewById(R.id.tradeButton);
        tradeButton.setVisibility(View.INVISIBLE);
        tradeButton.setOnClickListener(view -> initiateTradeRequest(firebaseDatabase));
        // TODO check if location is activated
        tradeSwitch = findViewById(R.id.tradeSwitch);
        tradeSwitch.setChecked(true);//Trading is on by default// TODO: save state of the switch
        tradeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String toastText;
            if (isChecked) {
                enableTracking();
                toastText = "location now visible";
            }
            else {
                disableTracking();
                toastText = "location now invisible";
            }
            Toast.makeText(MapActivity.this, toastText, Toast.LENGTH_SHORT).show();
        });

        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.getController().setZoom(15.0);

        // The trade button is hidden again if something else is pressed
        mapView.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                tradeButton.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));

        locationDBListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the new data from Firebase realtime database
                Gson gson = new Gson();
                JsonElement activePlayers = gson.toJsonTree(snapshot.getValue());

                // Clear all old markers
                removeActivePlayerMarker();

                // Add new markers
                for(Map.Entry<String, JsonElement> activePlayer : activePlayers.getAsJsonObject().entrySet()) {
                    // If FirebaseUID is equal to current user -> skip
                    if(firebaseAuth.getCurrentUser() != null && activePlayer.getKey().equals(firebaseAuth.getCurrentUser().getUid()))
                        continue;
                    activePlayerMarkers.add(addOtherPlayerMarkersToMap(activePlayer));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read data from Firebase realtime database!", error.toException());
            }
        };

        checkLocationIsEnabled((LocationManager)getSystemService(Context.LOCATION_SERVICE));

        if (tradeSwitch.isChecked())
            enableTracking();

        addPlayerMarkerToMap();
        requestLocationUpdate();
        setupScaleMapGesture();
    }

    private void checkLocationIsEnabled(LocationManager locationManager) {
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            new AlertDialog.Builder(MapActivity.this)
                    .setTitle(R.string.alert_title_location_disabled)
                    .setMessage(R.string.alert_message_location_disabled)
                    .setPositiveButton(R.string.alert_understood, (dialog, which) -> {
                        checkLocationIsEnabled(locationManager);
                    })
                    .create().show();
        }
    }

    private void initiateTradeRequest(FirebaseDatabase firebaseDatabase) {
        // Send trade request to other user and start new activity
        String tradingID = Trading.createTradeRequest(firebaseAuth.getCurrentUser().getUid(), tradePartnerUID, firebaseDatabase);
        QRcodeActivity.start(MapActivity.this, true, tradePartnerNickname, tradingID);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupScaleMapGesture() {
        // Pinch/spread two fingers to de-/increase zoom distance of the map
        ScaleGestureDetector.SimpleOnScaleGestureListener scaleListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = (detector.getScaleFactor() - 1f) * 0.1f;
                mapView.getController().setZoom(mapView.getZoomLevelDouble() * (scale+1f));
                return true;
            }
        };
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(MapActivity.this, scaleListener);
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void enableTracking() {
        initialiseLocationOfPlayerInDatabase();
        activePlayers.addValueEventListener(locationDBListener);
    }

    private void disableTracking() {
        activePlayers.removeEventListener(locationDBListener);
        removeActivePlayerMarker();
        activePlayers.child(firebaseAuth.getCurrentUser().getUid()).removeValue();
    }

    private void removeActivePlayerMarker() {
        mapView.getOverlays().removeAll(activePlayerMarkers);
        activePlayerMarkers.clear();
    }

    private void requestPermissionsIfNecessary() {
        // The required permissions
        String[] permissions = new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
        };

        // Save all permissions that the user has not granted yet
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, add it to the list
                permissionsToRequest.add(permission);
            }
        }

        // If there are permissions missing
        if (permissionsToRequest.size() > 0) {
            // Request those permissions
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Save all permissions that the user has not granted yet
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));

        // If there are permissions missing
        if (permissionsToRequest.size() > 0) {
            // Request those permissions
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdate() {
        locationCallback =  new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (tradeSwitch.isChecked())
                    setLocationOfPlayerInDatabase(locationResult.getLastLocation());

                if(playerLocationOverlay.getMyLocation() != null){
                    for (Marker marker : itemMarkers)
                        if (marker.getPosition().distanceToAsDouble(playerLocationOverlay.getMyLocation()) <= ITEM_VISIBLE_DISTANCE) {
                            if (!marker.getTitle().equals("collected"))
                                marker.setVisible(true);
                        }
                }

                // When user travels to far new items have to be generated on the map
                if (locationResult.getLastLocation().distanceTo(itemGenerationLocation) >= ITEMS_SPAWN_REGENERATE_DISTANCE)
                    regenerateItems(false, locationResult.getLastLocation());
            }
        };

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void setLocationOfPlayerInDatabase(@NonNull Location location) {
        if(firebaseAuth.getCurrentUser() != null) {
            activePlayers.child(firebaseAuth.getCurrentUser().getUid()).child("latitude").setValue(location.getLatitude());
            activePlayers.child(firebaseAuth.getCurrentUser().getUid()).child("longitude").setValue(location.getLongitude());
        }
    }

    @SuppressLint("MissingPermission")
    private void initialiseLocationOfPlayerInDatabase() {
        fusedLocationClient.getLastLocation().addOnCompleteListener(result -> {
            boolean canBeInFront = true;
            Location location;
            if (result.isSuccessful() && result.getResult() != null) {
                location = result.getResult();
                canBeInFront = location.distanceTo(itemGenerationLocation) >= ITEMS_SPAWN_REGENERATE_DISTANCE;
            }
            else
                location = itemGenerationLocation;

            regenerateItems(canBeInFront, location);

            setLocationOfPlayerInDatabase(location);
            mapView.getController().setCenter(new GeoPoint(location));
        });
    }

    private Marker addOtherPlayerMarkersToMap(Map.Entry<String, JsonElement> activePlayer) {
        // Get information from the Firebase realtime database
        String firebaseUID = activePlayer.getKey();
        JsonElement latitude = activePlayer.getValue().getAsJsonObject().get("latitude");
        JsonElement longitude = activePlayer.getValue().getAsJsonObject().get("longitude");

        if(firebaseUID != null && latitude != null && longitude != null) {
            // For every other user add a marker to map
            Marker playerMarker = new Marker(mapView);
            playerMarker.setPosition(new GeoPoint(latitude.getAsDouble(), longitude.getAsDouble()));
            playerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            playerMarker.setIcon(ContextCompat.getDrawable(MapActivity.this, R.drawable.mapmarkergreen));
            playerMarker.setTitle(firebaseUID);
            playerMarker.setOnMarkerClickListener(this::onMarkerClicked);
            mapView.getOverlays().add(playerMarker);
            return playerMarker;
        }
        return null;
    }

    private void addPlayerMarkerToMap() {
        playerLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        // TODO: call on resume and on pause
        playerLocationOverlay.enableMyLocation();
        playerLocationOverlay.enableFollowLocation();
        playerLocationOverlay.setPersonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.mapmarkerblue));
        playerLocationOverlay.setDirectionIcon(BitmapFactory.decodeResource(getResources(), R.drawable.arrowblue));
        playerLocationOverlay.setDrawAccuracyEnabled(false);
        mapView.getOverlays().add(playerLocationOverlay);
    }

    @Override
    public void onResume(){
        super.onResume();
        loadLastLocation();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause(){
        super.onPause();
        storeLastLocation();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        storeLastLocation();

        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean onMarkerClicked(Marker marker, MapView mapView) {
        Log.d(TAG, marker.getTitle() + " was clicked");
        double dist = marker.getPosition().distanceToAsDouble(playerLocationOverlay.getMyLocation());
        boolean isInRange = dist <= MAX_TRADE_DISTANCE;
        tradeButton.setEnabled(isInRange);
        tradePartnerUID = marker.getTitle();
        playerVolleyHelper.getPlayerByFirebaseUID(marker.getTitle(), response -> {
            try {
                this.tradePartnerNickname = response.getString("nickname");
                String buttonText = getResources().getString(isInRange ? R.string.trade_button_default_text : R.string.trade_button_not_in_range_text);
                tradeButton.setText(String.format(buttonText, tradePartnerNickname));
                tradeButton.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                Log.e(TAG, "onMarkerClick: Could not get nickname by UID!");
                e.printStackTrace();
            }
        }, error -> {
            Log.e(TAG, "onMarkerClick: Could not get nickname by UID!");
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        Util.logoutUser(firebaseAuth, MapActivity.this);
    }

    private void loadLastLocation() {
        itemGenerationLocation = new Location("");
        itemGenerationLocation.setLatitude(sharedPreferences.getFloat("last_location_lat", 0));
        itemGenerationLocation.setLongitude(sharedPreferences.getFloat("last_location_long", 0));
    }

    private void storeLastLocation() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("center_lat", (float)itemGenerationLocation.getLatitude());
        editor.putFloat("center_long", (float)itemGenerationLocation.getLongitude());
        editor.apply();
    }

    private void regenerateItems(boolean canBeInFront, Location center) {
        Drawable[] drawables = new Drawable[ITEMS_IMAGES.length];
        for (int i = 0; i < drawables.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ITEMS_IMAGES[i]);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
            drawables[i] = new BitmapDrawable(getResources(), scaled);
        }
        mapView.getOverlays().removeAll(itemMarkers);
        itemMarkers.clear();
        GeoPoint[] geoPoints = generateRandomGeoPoints(canBeInFront, center);
        for (GeoPoint point : geoPoints) {
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setVisible(false);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setOnMarkerClickListener((m, mapView) -> {
                Toast.makeText(MapActivity.this, "Item collected", Toast.LENGTH_SHORT).show();
                increaseItemCountInDB(new String(m.getTitle()));
                m.setTitle("collected");
                m.setVisible(false);
                return true;
            });
            // Random item
            int itemType = Math.min((int)(Math.random() * ITEMS.length), ITEMS.length-1);
            marker.setTitle(ITEMS[itemType]);
            marker.setIcon(drawables[itemType]);
            itemMarkers.add(marker);
        }
        mapView.getOverlays().addAll(itemMarkers);
    }

    private void increaseItemCountInDB(String itemKey) {// this could be done cleaner (more in the backend)
        String uid = firebaseAuth.getCurrentUser().getUid();
        playerVolleyHelper.getPlayerByFirebaseUID(uid, response -> {
            try {
                int oldAmount = (int) response.get(itemKey);
                Log.d("JJ", oldAmount + "");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(itemKey, oldAmount + 1);
                playerVolleyHelper.updatePlayer(uid, jsonObject, r -> {}, e -> Log.e(TAG, "increaseItemCountInDB: could not update value in DB!"));
            } catch (JSONException e) {
                Log.e(TAG, "increaseItemCountInDB: JSON exception!");
                e.printStackTrace();
            }
        }, error -> Log.e(TAG, "increaseItemCountInDB: Could not retrieve player!"));
    }

    private GeoPoint[] generateRandomGeoPoints(boolean canBeInFront, Location center) {
        itemGenerationLocation = new Location(center);
        GeoPoint[] geoPoints = new GeoPoint[ITEMS_SPAWN_AMOUNT];
        for (int i = 0; i < ITEMS_SPAWN_AMOUNT; i++) {
            GeoPoint geopoint = new GeoPoint(itemGenerationLocation);
            double[] point = getRandomPointInCircle(canBeInFront ? 0 : ITEMS_SPAWN_DISTANCE_TO_PLAYER/(float)ITEMS_SPAWN_RADIUS);
            for (int j = 0; j < point.length; j++)
                point[j] *= ITEMS_SPAWN_RADIUS;

            // Roughly convert point offset to lat, long offset
            point[1] /= 111_111;
            point[0] /= 111_111 * Math.cos(Math.toRadians(geopoint.getLatitude()));

            geopoint.setCoords(geopoint.getLatitude() + point[1], geopoint.getLongitude() + point[0]);
            geoPoints[i] = geopoint;
        }
        return geoPoints;
    }

    // Returns a random point inside a unit circle.
    // If an inner circle radius (0-1) is given the points will not lye within this circle
    private double[] getRandomPointInCircle(float innerCircleRadius) {
        double angle = Math.random() * 2 * Math.PI;
        double hyp = Math.sqrt(Math.random());
        // Offset points to be outside of inner circle
        hyp = hyp * (1-innerCircleRadius) + innerCircleRadius;
        double x = Math.cos(angle) * hyp;
        double y = Math.sin(angle) * hyp;
        return new double[]{x, y};
    }

    // Creates rescaled drawables from resources
    private void initializeItemIcons(int width, int height) {
        item_icons = new Drawable[ITEMS_IMAGES.length];
        for (int i = 0; i < item_icons.length; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), ITEMS_IMAGES[i]);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
            item_icons[i] = new BitmapDrawable(getResources(), scaled);
        }
    }
}