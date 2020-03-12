package com.example.transporttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class MainActivity extends AppCompatActivity
  implements
  ActivityCompat.OnRequestPermissionsResultCallback,
  MapFragment.OnFragmentInteractionListener {

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

  /**
   * Launching program
   * @param savedInstanceState Bundle
   */
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

  /**
   * initializing map
   */
  void initMap() {
    mapFragment.setOnMapReadyListener(() -> setMapListeners());
  }

  /**
   * Collapse Bottom Sheet
   */
  void collapseBottomSheet() {
    if (sheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
      sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
  }

  /**
   * set bottom sheet by type of marker
   */
  public void setMapListeners() {
    mapFragment.setOnMarkerClickListener(marker -> {

      if (marker.getTag() instanceof Stop) {
        setBottomSheetStopDetails(marker);
      } else {
        setBottomSheetTransportDetails(marker);
      }
      collapseBottomSheet();

      return false;
    });

    //adding bottom sheet callback
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

    //when camera stops show transport and stop markers
    mapFragment.setOnCameraIdleListener(() -> {
      doSubscription();
      doQuery();
      List<String> transportItemsRemoved = mapFragment.clearTransportMarkers();
      transportModel.removeItems(transportItemsRemoved);
    });

    //when clicking on map if bottom sheet not hided hide it
    mapFragment.setOnMapClickListener(latLng -> {
      if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      }
    });
  }

  /**
   * call subscription to show transport markers on visible region
   */
  void doSubscription() {
    VisibleRegion bounds = mapFragment.getMapBounds();

    transportModel.subscribeToTransportEvents(
      bounds.latLngBounds.northeast,
      bounds.latLngBounds.southwest,
      new TransportModel.Callback() {
        @Override
        public void onEvent(Transport transport) {
          handleTransportEvent(transport);
        }

        @Override
        public void onError(@NotNull ApolloException e) {

        }
      });
  }

  /**
   * add transport markers on the map
   * @param transport Transport class which contains data about transport
   */
  void handleTransportEvent(final Transport transport) {
    this.runOnUiThread(() -> mapFragment.addTransportMarker(transport));
  }

  /**
   * add stop markers on the map
   * @param stops List<Stop>
   */
  void handleStopsResponse(final List<Stop> stops) {
    MainActivity.this.runOnUiThread(() -> mapFragment.addStopMarkers(stops));
  }

  /**
   * Get arrays from Transport class and put it to transportDetailsListAdapter
   * @param data TransportDetailsFromMapQuery.Data (data from TransportDetailsFromMapQuery)
   */
  public void getTransportDetailsFromMap(final TransportDetailsFromMapQuery.Data data) {
    if (data == null) {
      return;
    }

    MainActivity.this.runOnUiThread(() -> {
      //Set additional info about transport
      name.setText(transport.getRouteName());
      recyclerView = findViewById(R.id.recycler_view);
      recyclerView.setAdapter(null);
      transportDetailsListAdapter = new TransportDetailsListAdapter(MainActivity.this,
        transport.getStopId(), transport.getStopCodes(), transport.getStopNames(), transport.getStopZones(),
        transport.getPlatformCodes(), transport.getRouteTime(), transport.getRouteDelay());
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
//      transportDetailsListAdapter.setItemClickListener(this);
      //recyclerView.setHasFixedSize(true);
      recyclerView.setAdapter(transportDetailsListAdapter);
      transportDetailsListAdapter.notifyDataSetChanged();

    });
  }

  /**
   * Get arrays from Transport class and put it to transportDetailsListAdapter (in future)
   * @param data TransportDetailsFromStopQuery.Data (data from TransportDetailsFromStopQuery)
   */
  public void getTransportDetailsFromStop(final TransportDetailsFromStopQuery.Data data) {
    if (data == null) {
      return;
    }

    MainActivity.this.runOnUiThread(() -> {
      //Set additional info about transport
      name.setText(transport.getRouteName());
      recyclerView = findViewById(R.id.recycler_view);
      recyclerView.setAdapter(null);
      transportDetailsListAdapter = new TransportDetailsListAdapter(MainActivity.this,
        transport.getStopId(), transport.getStopCodes(), transport.getStopNames(), transport.getStopZones(),
        transport.getPlatformCodes(), transport.getRouteTime(), transport.getRouteDelay());
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      //recyclerView.setHasFixedSize(true);
      recyclerView.setAdapter(transportDetailsListAdapter);

      transportDetailsListAdapter.notifyDataSetChanged();
    });
  }

  /**
   * get transport color by transport mode
   * @param mode String
   * @return int Color
   */
  int getTransportColor(String mode) {
    switch (transport.getRouteMode()) {
      default:
      case "bus":
        return R.color.busColor;
      case "train":
        return R.color.trainColor;
      case "tram":
        return R.color.tramColor;
      case "metro":
        return R.color.subwayColor;
      case "ferry":
        return R.color.ferryColor;
    }
  }

  /**
   * Set code background
   * @param colorToSet int
   * @param isColorResource boolean
   */
  void setCodeBackground(int colorToSet, boolean isColorResource) {
    Drawable background = code.getBackground();
    int color = isColorResource ? ContextCompat.getColor(this, colorToSet) : colorToSet;
    if (background instanceof ShapeDrawable) {
      ((ShapeDrawable) background).getPaint().setColor(color);
    } else if (background instanceof GradientDrawable) {
      ((GradientDrawable) background).setColor(color);
    } else if (background instanceof ColorDrawable) {
      ((ColorDrawable) background).setColor(color);
    }
  }

  /**
   * Set up transport bottom sheet
   * @param marker Marker
   */
  public void setBottomSheetTransportDetails(Marker marker) {
    transport = (Transport) marker.getTag();

    this.runOnUiThread(() -> {
      //put data into the header of bottom sheet
      setCodeBackground(getTransportColor(transport.getRouteMode()), true);
      code.setText(transport.getRouteDisplayName());
      zone.setBackgroundColor(Color.WHITE);
      zone.setText("");
      platform.setText("");
      recyclerView = findViewById(R.id.recycler_view);
      recyclerView.setAdapter(null);
      collapseBottomSheet();
    });

    //if handler not null remove it
    if (handler != null) handler.removeCallbacksAndMessages(null);
    //async task with recycler view. refresh data every 5 seconds
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Transport.getTransportDetailsFromMap(transport.getRouteDate(), transport.getRouteDirection(),
          transport.getRouteId(), transport.getRouteStart(), new Transport.Callback() {
            @Override
            public void onTransportFromMap(@NonNull TransportDetailsFromMapQuery.Data data) {
              getTransportDetailsFromMap(data);
            }

            @Override
            public void onTransportFromStop(@NonNull TransportDetailsFromStopQuery.Data data) {

            }

            @Override
            public void onError(@NotNull ApolloException e) {
            }
          });
        handler.postDelayed(this, 5000);
      }
    }, 1000);


  }

  /**
   * call query to set up list of stops
   */
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

  /**
   * Get arrays from Stop class and put it to stopDetailsListAdapter
   * @param data StopDetailsQuery.Data (data from StopDetailsQuery)
   */
  public void getStopDetails(final StopDetailsQuery.Data data) {
    if (data == null) {
      return;
    }


    MainActivity.this.runOnUiThread(() -> {
      // Set additional info about the stop
      stopDetailsListAdapter = new StopDetailsListAdapter(MainActivity.this, stop.getVehicleMode(),
        stop.getTripId(), stop.getRouteNums(), stop.getRouteNames(), stop.getRouteTime(), stop.getRouteDelay(), stop.getRouteDirections());
      recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
      //recyclerView.setHasFixedSize(true);
      recyclerView.setAdapter(stopDetailsListAdapter);

      stopDetailsListAdapter.setOnItemClickListener((tag) -> {
        Optional<Transport> transport = transportModel.findTransportByTag(tag);

        if (transport.isPresent()) {
          mapFragment.focusOnTransportMarker(transport.get());
        } else {
          Toast toast = Toast.makeText(
            this,
            "Could not find transport item :(",
            Toast.LENGTH_SHORT);
          toast.show();
        }
      });

      stopDetailsListAdapter.notifyDataSetChanged();
    });

  }

  /**
   * Set up stop bottom sheet
   * @param marker Marker
   */
  public void setBottomSheetStopDetails(Marker marker) {
    stop = (Stop) marker.getTag();

    this.runOnUiThread(() -> {
      //put data into the header of bottom sheet
      setCodeBackground(Color.GRAY, false);
      code.setText(stop.getCode());
      name.setText(stop.getName());
      zone.setBackground(getResources().getDrawable(R.drawable.stop_icon, getApplicationContext().getTheme()));
      zone.setTextColor(Color.WHITE);
      zone.setText(stop.getZoneId());
      platform.setText(stop.getPlatformCode());
      recyclerView = findViewById(R.id.recycler_view);
      collapseBottomSheet();
    });

    //if handler not null remove it
    if (handler != null) handler.removeCallbacksAndMessages(null);
    //async task with recycler view. refresh data every 5 seconds
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
        handler.postDelayed(this, 5000);
      }
    }, 1000);

  }

  /**
   * Required for MapFragment to work????????
   * @param uri Uri
   */
  @Override
  public void onFragmentInteraction(Uri uri) {

  }
}
