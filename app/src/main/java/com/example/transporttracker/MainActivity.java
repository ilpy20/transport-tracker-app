package com.example.transporttracker;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.network.Networking;
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
import com.hsl.TestQuery;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
  GoogleMap googleMap;
  TransportModel transportModel;

  HashMap<String, Marker> transportMarkers;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initMap();

    transportModel = new TransportModel();
    transportMarkers = new HashMap<>();
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

    googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
      @Override
      public void onCameraIdle() {
        doSubscription();
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
    if(transportEvent == null) {
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

  void addStopMarker(final TestQuery.Stop stop) {
    final LatLng stopLocation = new LatLng(
      stop.lat(),
      stop.lon()
    );


    MainActivity.this.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        googleMap.addMarker(
          new MarkerOptions().position(stopLocation).title(stop.name())
        );
      }
    });
  }

  void doQuery(LatLng latLng) {
    TestQuery testQuery = TestQuery.builder().lat(latLng.latitude).lon(latLng.longitude).build();

    Networking.apollo().query(testQuery).enqueue(new ApolloCall.Callback<TestQuery.Data>() {


      @Override
      public void onResponse(@NotNull Response<TestQuery.Data> response) {
        for (TestQuery.Edge edge : response.data().stopsByRadius().edges()) {
          addStopMarker(edge.node().stop());
        }

      }

      @Override
      public void onFailure(@NotNull ApolloException e) {
        System.out.println(e);
      }
    });
  }
}
