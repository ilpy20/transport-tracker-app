package com.example.transporttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.hsl.TransportDetailsQuery;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.jetbrains.annotations.NotNull;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity
  implements
  ActivityCompat.OnRequestPermissionsResultCallback,
  MapFragment.OnFragmentInteractionListener {



  MapFragment mapFragment;

  TransportModel transportModel;
  StopModel stopModel;
  Stop stop;

  BottomSheetBehavior sheetBehavior;
  TextView name;
  TextView code;
  TextView zone;
  TextView platform;

  RecyclerView recyclerView;
  ListAdapter listAdapter;


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

    NestedScrollView bottom_sheet = findViewById(R.id.bottom_sheet);
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

  void bottomSheetChecker(){
    if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
      sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    } else {
      sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  public void setMapListeners() {
    mapFragment.setOnMarkerClickListener(marker -> {

      if (marker.getSnippet().equals("stop")) {
        setBottomSheetStopDetails(marker);
        bottomSheetChecker();
      }
      else{
        setBottomSheetTransportDetails(marker);
        bottomSheetChecker();
      }
      return false;
    });

    sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
          sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
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

  void getTransportDetails(final TransportDetailsQuery.Data transport){
    if(transport==null){
      return;
    }

    MainActivity.this.runOnUiThread(()->{
      //Set additional info about transport
    });
  }

  void setBottomSheetTransportDetails(Marker marker){
    Transport transport = (Transport) marker.getTag();

    this.runOnUiThread(()->{
      code.setText(transport.getRouteDisplayName());
      name.setText(transport.getRouteName());
      zone.setText("");
    });
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

  void getStopDetails(final StopDetailsQuery.Data data) {
    if (data == null) {
      return;
    }

    long unixTime = Instant.now().getEpochSecond();

    stop.makeStopDetailsArrays(data, unixTime);

    MainActivity.this.runOnUiThread(() -> {
      // Set additional info about the stop
      recyclerView = findViewById(R.id.recycler_view);
      listAdapter = new ListAdapter(MainActivity.this,stop.getRouteNums(),stop.getRouteNames(),stop.getRouteTime(),stop.getRouteDelay());
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      //recyclerView.setHasFixedSize(true);
      recyclerView.setAdapter(listAdapter);

    });

  }

  void setBottomSheetStopDetails(Marker marker) {
    stop = (Stop) marker.getTag();

    this.runOnUiThread(() -> {
      code.setText(stop.getCode());
      name.setText(stop.getName());
      zone.setText(stop.getZoneId());
      platform.setText(stop.getPlatformCode());
    });


    Stop.makeStop(stop.getGtfsId(), stop, new Stop.Callback() {
      @Override
      public void onStop(@NonNull StopDetailsQuery.Data data) {
        getStopDetails(data);
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
