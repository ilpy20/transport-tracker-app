package com.example.transporttracker;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
import com.example.stopmodel.StopModel;
import com.example.transportmodel.TransportModel;
import com.example.transporttracker.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hsl.StopsQuery;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
    implements
    OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {
  private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  private boolean mPermissionDenied = false;


  GoogleMap googleMap;
  TransportModel transportModel;
  StopModel stopModel;

  HashMap<String, Marker> transportMarkers;
  HashMap<String, Marker> stopMarkers;

  BottomSheetBehavior sheetBehavior;
  //private BottomSheetBehavior sheetBehavior_route;
  //private LinearLayout bottom_sheet_route;
  //private BottomSheetBehavior sheetBehavior_stops;
  //private LinearLayout bottom_sheet_stops;
  TextView someName;
  String title = "";


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initMap();

    transportModel = new TransportModel();
    stopModel = new StopModel();
    transportMarkers = new HashMap<>();
    stopMarkers = new HashMap<>();

    RelativeLayout bottom_sheet = findViewById(R.id.bottom_sheet);
    sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    someName = findViewById(R.id.some_name);

  }

  void initMap() {
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);

    mapFragment.getMapAsync(this);
  }


  public void onMapReady(final GoogleMap googleMap) {
    this.googleMap = googleMap;
    //enableMyLocation();
    LatLng home = new LatLng(60.206723, 24.667192);
    googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(home));


    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        title = marker.getTitle();
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
          sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
          someName.setText(title);
        } else {
          sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          someName.setText(title);
        }
        return false;
      }
    });
    sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        switch (newState) {
          case BottomSheetBehavior.STATE_HIDDEN:
            break;
          case BottomSheetBehavior.STATE_EXPANDED: {
            someName.setText(title);
          }
          break;
          case BottomSheetBehavior.STATE_COLLAPSED: {
            someName.setText(title);
          }
          break;
          case BottomSheetBehavior.STATE_DRAGGING:
            break;
          case BottomSheetBehavior.STATE_SETTLING:
            break;
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });
    googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
      @Override
      public void onCameraIdle() {
        doSubscription();
        doQuery();
      }
    });
    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
      @Override
      public void onMapClick(LatLng latLng) {
        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
          sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
      }
    });
  }

  /**
   * Enables the My Location layer if the fine location permission has been granted.
   */
  private void enableMyLocation() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED) {
      // Permission to access the location is missing.
      PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
          Manifest.permission.ACCESS_FINE_LOCATION, true);
    } else if (googleMap != null) {
      // Access to the location has been granted to the app.
      googleMap.setMyLocationEnabled(true);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
      return;
    }

    if (PermissionUtils.isPermissionGranted(permissions, grantResults,
        Manifest.permission.ACCESS_FINE_LOCATION)) {
      // Enable the my location layer if the permission has been granted.
      enableMyLocation();
    } else {
      // Display the missing permission error dialog when the fragments resume.
      mPermissionDenied = true;
    }
  }

  @Override
  protected void onResumeFragments() {
    super.onResumeFragments();
    if (mPermissionDenied) {
      // Permission was not granted, display error dialog.
      showMissingPermissionError();
      mPermissionDenied = false;
    }
  }

  /**
   * Displays a dialog with error message explaining that the location permission is missing.
   */
  private void showMissingPermissionError() {
    PermissionUtils.PermissionDeniedDialog
        .newInstance(true).show(getSupportFragmentManager(), "dialog");
  }


  void doSubscription() {
    LatLng farLeft = googleMap.getProjection().getVisibleRegion().farLeft;
    LatLng nearRight = googleMap.getProjection().getVisibleRegion().nearRight;

    transportModel.subscribeToTransportEvents(farLeft, nearRight, new TransportModel.Callback() {
      @Override
      public void onEvent(TransportSubscription.TransportEventsInArea transportEvent) {
        addTransportMarker(transportEvent);
      }

      @Override
      public void onError(@NotNull ApolloException e) {

      }
    });
  }

  private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
    Canvas canvas = new Canvas();
    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    canvas.setBitmap(bitmap);
    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    drawable.draw(canvas);
    return BitmapDescriptorFactory.fromBitmap(bitmap);
  }

  void addTransportMarker(final TransportSubscription.TransportEventsInArea transportEvent) {
    if (transportEvent == null) {
      return;
    }
    final LatLng position = new LatLng(
        transportEvent.lat(),
        transportEvent.lon()
    );


    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Marker existingMarker = transportMarkers.get(transportEvent.id());

        if (existingMarker != null) {
          existingMarker.setPosition(position);
        } else {
          Marker marker = googleMap.addMarker(
              new MarkerOptions()
                  .position(position)
                  .title(transportEvent.route())
                  .anchor(0.5f, 0.5f)
                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.transport_icon))
          );
          transportMarkers.put(transportEvent.id(), marker);
        }
      }
    });
  }

  void addStopMarker(final StopsQuery.StopsByBbox stop) {
    if (stop == null) {
      return;
    }
    final LatLng stopLocation = new LatLng(
        stop.lat(),
        stop.lon()
    );


    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Marker existingMarker = stopMarkers.get(stop.id());

        if (existingMarker != null) {
          existingMarker.setPosition(stopLocation);
        } else {
          Marker marker = googleMap.addMarker(
              new MarkerOptions()
                  .position(stopLocation)
                  .title(stop.name())
                  .anchor(0.5f, 0.5f)
                  .icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_icon))
          );
          stopMarkers.put(stop.id(), marker);
        }
      }
    });
  }


  void doQuery() {
    LatLng farLeft = googleMap.getProjection().getVisibleRegion().farLeft;
    LatLng nearRight = googleMap.getProjection().getVisibleRegion().nearRight;

    stopModel.makeStops(farLeft, nearRight, new StopModel.Callback() {
      @Override
      public void onStops(StopsQuery.StopsByBbox stops) {
        addStopMarker(stops);
      }

      @Override
      public void onError(@NotNull ApolloException e) {

      }
    });
  }
}
