package com.example.transporttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apollographql.apollo.exception.ApolloException;
import com.example.stopmodel.Stop;
import com.example.stopmodel.StopModel;
import com.example.transportmodel.Transport;
import com.example.transportmodel.TransportModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hsl.StopDetailsQuery;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity
  implements
  ActivityCompat.OnRequestPermissionsResultCallback,
  MapFragment.OnFragmentInteractionListener {

  MapFragment mapFragment;

  TransportModel transportModel;
  StopModel stopModel;

  BottomSheetBehavior sheetBehavior;
  TextView name;
  TextView code;
  TextView zone;
  TextView platform;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    View decorView = getWindow().getDecorView();
//    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//    decorView.setSystemUiVisibility(uiOptions);
    setContentView(R.layout.activity_main);

    mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
    initMap();

    transportModel = new TransportModel();
    stopModel = new StopModel();

    RelativeLayout bottom_sheet = findViewById(R.id.bottom_sheet);
    sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    name = findViewById(R.id.name);
    code = findViewById(R.id.code);
    zone = findViewById(R.id.zone);
    platform = findViewById(R.id.platform);
  }

  void initMap() {
    mapFragment.setOnMapReadyListener(() -> setMapListeners());
  }


  public void setMapListeners() {
    mapFragment.setOnMarkerClickListener(marker -> {
      if (marker.getSnippet().equals("stop")) {
        setBottomSheetStopDetails(marker);

        if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
          sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
          sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }


      }
      return false;
    });

    sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        /*switch (newState) {
          case BottomSheetBehavior.STATE_HIDDEN:
            break;
          case BottomSheetBehavior.STATE_EXPANDED: {
            stopName.setText(stopDetails.getStopName());
            break;
          }
          case BottomSheetBehavior.STATE_COLLAPSED: {
            stopName.setText(stopDetails.getStopName());
            break;
          }
          case BottomSheetBehavior.STATE_DRAGGING:
            break;
          case BottomSheetBehavior.STATE_SETTLING:
            break;
        }*/
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });

    mapFragment.setOnCameraIdleListener(() -> {
      doSubscription();
      doQuery();
    });

    mapFragment.setOnMapClickListener(latLng -> {
      if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      }
    });
  }

  void doSubscription() {
    VisibleRegion bounds = mapFragment.getMapBounds();
    LatLng farLeft = bounds.farLeft;
    LatLng nearRight = bounds.nearRight;

    transportModel.subscribeToTransportEvents(farLeft, nearRight, new TransportModel.Callback() {
      @Override
      public void onEvent(Transport transport) {
        handleTransportEvent(transport);
      }

      @Override
      public void onError(@NotNull ApolloException e) {

      }
    });
  }

  void handleTransportEvent(final Transport transport) {
    this.runOnUiThread(() -> mapFragment.addTransportMarker(transport));
  }

  void handleStopsResponse(final List<Stop> stops) {
    MainActivity.this.runOnUiThread(() -> mapFragment.addStopMarkers(stops));
  }

  void doQuery() {
    VisibleRegion bounds = mapFragment.getMapBounds();
    LatLng farLeft = bounds.farLeft;
    LatLng nearRight = bounds.nearRight;

    stopModel.makeStops(farLeft, nearRight, new StopModel.Callback() {
      @Override
      public void onStops(List<Stop> stops) {
        handleStopsResponse(stops);
      }

      @Override
      public void onError(@NotNull ApolloException e) {
      }
    });
  }

  void getStopDetails(final StopDetailsQuery.Data stop) {
    if (stop == null) {
      return;
    }

    MainActivity.this.runOnUiThread(() -> {
      // Set additional info about the stop
    });

  }

  void setBottomSheetStopDetails(Marker marker) {
    Stop stop = (Stop) marker.getTag();

    this.runOnUiThread(() -> {
      name.setText(stop.getName());
      code.setText(Integer.toString(stop.getCode()));
      zone.setText(stop.getZoneId());
      platform.setText(stop.getPlatformCode());
    });

    Stop.makeStop(stop.getId(), new Stop.Callback() {
      @Override
      public void onStop(StopDetailsQuery.Data stop) {
        getStopDetails(stop);
      }

      @Override
      public void onError(@NotNull ApolloException e) {
      }
    });
  }

  @Override
  public void onFragmentInteraction(Uri uri) {
    // Required for MapFragment to work 🤷‍
  }
}
