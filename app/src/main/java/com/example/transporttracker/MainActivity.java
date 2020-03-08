package com.example.transporttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.apollographql.apollo.exception.ApolloException;
import com.example.stopmodel.Stop;
import com.example.stopmodel.StopDetailsListAdapter;
import com.example.stopmodel.StopModel;
import com.example.transportmodel.Transport;
import com.example.transportmodel.TransportDetailsListAdapter;
import com.example.transportmodel.TransportModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.hsl.StopDetailsQuery;
import com.hsl.TransportDetailsFromMapQuery;
import com.hsl.TransportDetailsFromStopQuery;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import static android.graphics.Color.parseColor;

public class MainActivity extends AppCompatActivity
  implements
  ActivityCompat.OnRequestPermissionsResultCallback,
  MapFragment.OnFragmentInteractionListener
  {

    private Handler handler = new Handler();

  MapFragment mapFragment;

  TransportModel transportModel;
  StopModel stopModel;
  Stop stop;
  Transport transport;

  BottomSheetBehavior sheetBehavior;
  TextView name;
  TextView code;
  TextView zone;
  TextView platform;

  RecyclerView recyclerView;
  StopDetailsListAdapter stopDetailsListAdapter;
  TransportDetailsListAdapter transportDetailsListAdapter;

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
    if (sheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
      sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  public void setMapListeners() {
    mapFragment.setOnMarkerClickListener(marker -> {

      if (marker.getTag() instanceof Stop) {
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

  public void getTransportDetailsFromMap(final TransportDetailsFromMapQuery.Data data){
    if(data==null){
      return;
    }

    MainActivity.this.runOnUiThread(()->{
      //Set additional info about transport
      name.setText(transport.getRouteName());
      recyclerView = findViewById(R.id.recycler_view);
      recyclerView.setAdapter(null);
      transportDetailsListAdapter = new TransportDetailsListAdapter(MainActivity.this,
          transport.getStopId(),transport.getStopCodes(),transport.getStopNames(),transport.getStopZones(),
          transport.getPlatformCodes(),transport.getRouteTime(),transport.getRouteDelay());
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      //recyclerView.setHasFixedSize(true);
      recyclerView.setAdapter(transportDetailsListAdapter);
      transportDetailsListAdapter.notifyDataSetChanged();
    });
  }

    public void getTransportDetailsFromStop(final TransportDetailsFromStopQuery.Data data){
      if(data==null){
        return;
      }

      MainActivity.this.runOnUiThread(()->{
        //Set additional info about transport
        name.setText(transport.getRouteName());
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(null);
        transportDetailsListAdapter = new TransportDetailsListAdapter(MainActivity.this,
            transport.getStopId(),transport.getStopCodes(),transport.getStopNames(),transport.getStopZones(),
            transport.getPlatformCodes(),transport.getRouteTime(),transport.getRouteDelay());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(transportDetailsListAdapter);
        transportDetailsListAdapter.notifyDataSetChanged();
      });
    }

  public void setBottomSheetTransportDetails(Marker marker){
    transport = (Transport) marker.getTag();

    this.runOnUiThread(()->{
      if(transport.getRouteMode().equals("bus")){
        code.setBackgroundColor(Color.BLUE);
        code.setTextColor(Color.WHITE);
      }
      if(transport.getRouteMode().equals("train")){
        code.setBackgroundColor(Color.MAGENTA);
        code.setTextColor(Color.WHITE);
      }
      if(transport.getRouteMode().equals("tram")){
        code.setBackgroundColor(Color.GREEN);
        code.setTextColor(Color.WHITE);
      }
      if(transport.getRouteMode().equals("metro")){
        code.setBackgroundColor(parseColor("#FFA500"));
        code.setTextColor(Color.WHITE);
      }
      if(transport.getRouteMode().equals("ferry")){
        code.setBackgroundColor(parseColor("#ADD8E6"));
        code.setTextColor(Color.WHITE);
      }
      code.setText(transport.getRouteDisplayName());
      zone.setBackgroundColor(Color.WHITE);
      zone.setText("");
      platform.setText("");
    });

    if(handler!=null)handler.removeCallbacksAndMessages(null);
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Transport.getTransportDetailsFromMap(transport.getRouteDate(),transport.getRouteDirection(),
            transport.getRouteId(),transport.getRouteStart(),new Transport.Callback(){
              @Override
              public void onTransportFromMap(@NonNull TransportDetailsFromMapQuery.Data data){
                getTransportDetailsFromMap(data);
              }

              @Override
              public void onTransportFromStop(@NonNull TransportDetailsFromStopQuery.Data data) {

              }

              @Override
              public void onError(@NotNull ApolloException e) {
              }
            });
        handler.postDelayed(this,5000);
      }
    },1000);

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

  public void getStopDetails(final StopDetailsQuery.Data data) {
    if (data == null) {
      return;
    }


    MainActivity.this.runOnUiThread(() -> {
      // Set additional info about the stop
      recyclerView = findViewById(R.id.recycler_view);
      recyclerView.setAdapter(null);
      stopDetailsListAdapter = new StopDetailsListAdapter(MainActivity.this,stop.getVehicleMode(),
          stop.getTripId(),stop.getRouteNums(),stop.getRouteNames(),stop.getRouteTime(),stop.getRouteDelay());
      recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
      //recyclerView.setHasFixedSize(true);
      recyclerView.setAdapter(stopDetailsListAdapter);

      stopDetailsListAdapter.notifyDataSetChanged();
    });

  }

  public void setBottomSheetStopDetails(Marker marker) {
    stop = (Stop) marker.getTag();

    this.runOnUiThread(() -> {
      code.setBackgroundColor(Color.GRAY);
      code.setTextColor(Color.WHITE);
      code.setText(stop.getCode());
      name.setText(stop.getName());
      zone.setBackground(getResources().getDrawable(R.drawable.stop_icon,getApplicationContext().getTheme()));
      zone.setTextColor(Color.WHITE);
      zone.setText(stop.getZoneId());
      platform.setText(stop.getPlatformCode());
    });
    if(handler!=null)handler.removeCallbacksAndMessages(null);
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Stop.makeStop(stop.getGtfsId(), new Stop.Callback() {
          @Override
          public void onStop(@NonNull StopDetailsQuery.Data data) {
            getStopDetails(data);
          }

          @Override
          public void onError(@NotNull ApolloException e) {
          }
        });
        handler.postDelayed(this,5000);
      }
    },1000);


  }

  @Override
  public void onFragmentInteraction(Uri uri) {
    // Required for MapFragment to work 🤷‍
  }
}
