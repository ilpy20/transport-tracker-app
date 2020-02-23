package com.example.transporttracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.stopmodel.Stop;
import com.example.transportmodel.Transport;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment {
  private OnFragmentInteractionListener mListener;
  private GoogleMap googleMap;
  private MapView mMapView;

  private boolean ready = false;

  private OnMapViewReadyCallback mapReadyCallback;

  private HashMap<String, Marker> transportMarkers;
  private HashMap<String, Marker> stopMarkers;

  public MapFragment() {
    transportMarkers = new HashMap<>();
    stopMarkers = new HashMap<>();
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Log.d("debug", "createView");
    // Inflate the layout for this fragment
    View rootView = inflater.inflate(R.layout.fragment_map, container, false);

    mMapView = rootView.findViewById(R.id.mapView);
    mMapView.onCreate(savedInstanceState);

    mMapView.onResume(); // needed to get the map to display immediately

    try {
      MapsInitializer.initialize(getActivity().getApplicationContext());
    } catch (Exception e) {
      e.printStackTrace();
    }

    mMapView.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(GoogleMap mMap) {
        googleMap = mMap;

        // For showing a move to my location button
//        googleMap.setMyLocationEnabled(true);

        ready = true;
        init();
        if (mapReadyCallback != null) {
          mapReadyCallback.onMapReady();
        }
      }
    });

    return rootView;

  }

  void init() {
    LatLng home = new LatLng(60.206723, 24.667192);
    googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));
    googleMap.moveCamera(CameraUpdateFactory.newLatLng(home));
  }

  public void addTransportMarker(final Transport transport) {
    LatLng position = transport.getLocation();

    Marker existingMarker = transportMarkers.get(transport.getId());

    if (existingMarker != null) {
      existingMarker.setPosition(position);
    } else {
      Marker marker = googleMap.addMarker(
        new MarkerOptions()
          .position(position)
          .title(transport.getRouteDisplayName())
          .snippet("route")
          .anchor(0.5f, 0.5f)
          .icon(BitmapDescriptorFactory.fromResource(R.drawable.transport_icon))
      );

      marker.setTag(transport);
      transportMarkers.put(transport.getId(), marker);
    }
  }

  public void addStopMarkers(List<Stop> stops) {
    for(Stop stop: stops) {
      addStopMarker(stop);
    }
  }

  Bitmap getStopIcon() {
    int height = 24;
    int width = 24;
    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.stop_icon, getContext().getTheme());
    Bitmap b = bitmapdraw.getBitmap();
    return Bitmap.createScaledBitmap(b, width, height, false);
  }

  public void addStopMarker(Stop stop) {
        Marker existingMarker = stopMarkers.get(stop.getId());

        if (existingMarker != null) {
          // Update info
        } else {
          Marker marker = googleMap.addMarker(
              new MarkerOptions()
                  .position(stop.getLocation())
                  .title(stop.getName())
                  .snippet("stop")
                  .anchor(0.5f, 0.5f)
                  .icon(BitmapDescriptorFactory.fromBitmap(getStopIcon()))
          );

          marker.setTag(stop);
          stopMarkers.put(stop.getId(), marker);
        }
  }

  public void setOnMapReadyListener(OnMapViewReadyCallback callback) {
    if (ready) {
      callback.onMapReady();
    } else {
      this.mapReadyCallback = callback;
    }
  }

  public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener callback) {
    googleMap.setOnMarkerClickListener(callback);
  }

  public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener callback) {
    googleMap.setOnCameraIdleListener(callback);
  }

  public VisibleRegion getMapBounds() {
    return googleMap.getProjection().getVisibleRegion();
  }

  public void setOnMapClickListener(GoogleMap.OnMapClickListener callback) {
    googleMap.setOnMapClickListener(callback);
  }

  public interface OnMapViewReadyCallback {
    void onMapReady();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
        + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }
}
