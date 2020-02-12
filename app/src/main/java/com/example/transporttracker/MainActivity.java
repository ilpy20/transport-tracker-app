package com.example.transporttracker;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apollographql.apollo.exception.ApolloException;
import com.example.stopmodel.StopModel;
import com.example.transportmodel.TransportModel;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
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

    /*bottom_sheet_route = findViewById(R.id.bottom_sheet_route);
    sheetBehavior_route = BottomSheetBehavior.from(bottom_sheet_route);
    bottom_sheet_stops = findViewById(R.id.bottom_sheet_stop);
    sheetBehavior_stops = BottomSheetBehavior.from(bottom_sheet_stops);*/
    ConstraintLayout bottom_sheet = findViewById(R.id.bottom_sheet);
    sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
    someName = findViewById(R.id.some_name);
  }

  void initMap() {
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);

    mapFragment.getMapAsync(this);
  }


  public void onMapReady(final GoogleMap googleMap) {
    this.googleMap = googleMap;
    LatLng home = new LatLng(60.206723, 24.667192);


    googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(home));

    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(Marker marker) {
        title = marker.getTitle();
        //if(marker.)
        //Marker transportMarker = transportMarkers.get(marker);
        //Marker stopMarker = stopMarkers.get(marker);
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
        switch (newState){
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

  PorterDuffColorFilter getFilter(String mode) {
    @ColorInt Integer color;

    switch (mode) {
      default:
      case "bus":
        color = 0x4287f5;
        break;
      case "tram":
        color = 0x54b835;
        break;
      case "train":
        color = 0xb02eb0;
        break;
      case "metro":
        color = 0xff8c00;
        break;
    }

    return new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
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
                  .title(transportEvent.routeNumber())
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

    stopModel.getStops(farLeft, nearRight, new StopModel.Callback() {
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
