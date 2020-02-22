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
import com.example.stopmodel.Stop;
import com.example.stopmodel.StopModel;
import com.example.transportmodel.Transport;
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
import com.hsl.StopDetailsQuery;
import com.hsl.StopsQuery;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
    implements
    OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {
  //private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
  //private boolean mPermissionDenied = false;


  GoogleMap googleMap;
  TransportModel transportModel;
  StopModel stopModel;
  Stop stopDetails;
  Transport transportDetails;

  HashMap<String, Marker> transportMarkers;
  HashMap<String, Marker> stopMarkers;

  BottomSheetBehavior sheetBehavior;
  //BottomSheetBehavior sheetBehaviorRoute;
  //private LinearLayout bottom_sheet_route;
  //BottomSheetBehavior sheetBehaviorStop;
  //private LinearLayout bottom_sheet_stops;
  TextView name;
  TextView code;
  TextView zone;
  //TextView mode;
  TextView platform;
  //String title = "";




  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View decorView = getWindow().getDecorView();
    int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    decorView.setSystemUiVisibility(uiOptions);
    setContentView(R.layout.activity_main);
    initMap();

    transportModel = new TransportModel();
    stopModel = new StopModel();
    transportMarkers = new HashMap<>();
    stopMarkers = new HashMap<>();
    stopDetails = new Stop();
    transportDetails = new Transport();

    RelativeLayout bottom_sheet = findViewById(R.id.bottom_sheet);
    sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
    //RelativeLayout bottom_sheet_route = findViewById(R.id.bottom_sheet_route);
    //sheetBehaviorRoute = BottomSheetBehavior.from(bottom_sheet_route);
    //
    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    //
    //sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    name = findViewById(R.id.name);
    code = findViewById(R.id.code);
    zone = findViewById(R.id.zone);
    //mode = findViewById(R.id.mode);
    platform = findViewById(R.id.platform);
    //routeName = findViewById(R.id.route_name);

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
        if(marker.getSnippet().equals("stop")){
          doStopDetailsQuery(marker);
          //BottomSheetBehavior sheetBehaviorStop;
          //stopDetails.setStopName(marker.getTitle());
          //stopDetails.setStopCode();
          //stopDetails.setZoneId();
          //stopDetails.setVehicleMode();
          //stopDetails.setPlatformCode();
          //sheetBehavior = sheetBehaviorStop;
          if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            //name.setText();
          } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //name.setText(stopDetails.getStopName());
          }
        }
        /*else{
          //BottomSheetBehavior sheetBehaviorRoute;
          transportDetails.setRouteName(marker.getTitle());
          //sheetBehavior = sheetBehaviorRoute;
          if (sheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            name.setText(transportDetails.getRouteName());
          } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            name.setText(transportDetails.getRouteName());
          }
        }*/
        return false;
      }
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
                  .title(transportEvent.id())
                  .snippet("route")
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
                  .title(stop.gtfsId())
                  .snippet("stop")
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

  void getStopDetails(final StopDetailsQuery.Data stop){
    if(stop==null){
      return;
    }
    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        name.setText(stop.stop().name());
        code.setText(stop.stop().code());
        zone.setText(stop.stop().zoneId());
        //mode.setText(stop.stop().vehicleMode().rawValue());
        if (stop.stop().platformCode() != null) {
          platform.setText(stop.stop().platformCode());
        }
      }
    });

  }
  void doStopDetailsQuery(Marker marker){
    String id = marker.getTitle();
    Stop.makeStop(id, new Stop.Callback() {
      @Override
      public void onStop(StopDetailsQuery.Data stop) {
        getStopDetails(stop);
      }

      @Override
      public void onError(@NotNull ApolloException e) {

      }
    });
  }
}
