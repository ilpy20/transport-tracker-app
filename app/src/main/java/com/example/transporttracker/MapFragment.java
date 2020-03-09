package com.example.transporttracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.stopmodel.Stop;
import com.example.transportmodel.Transport;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.ui.IconGenerator;

import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment {
  private static final int MY_LOCATION_REQUEST_CODE = 1337;
  private FusedLocationProviderClient fusedLocationClient;
  private OnFragmentInteractionListener mListener;
  private GoogleMap googleMap;
  private MapView mMapView;

  private boolean ready = false;

  private OnMapViewReadyCallback mapReadyCallback;

  private HashMap<String, Marker> transportMarkers;
  private HashMap<String, Marker> stopMarkers;
  //private CameraPosition cameraPosition;

  public MapFragment() {
    transportMarkers = new HashMap<>();
    stopMarkers = new HashMap<>();
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
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

    mMapView.getMapAsync(mMap -> {
      googleMap = mMap;
      accessUserLocation();

      ready = true;
      init();
      if (mapReadyCallback != null) {
        mapReadyCallback.onMapReady();
      }
    });

    return rootView;

  }

  private void accessUserLocation() {
    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
      == PackageManager.PERMISSION_GRANTED) {
      Log.d("MapFragment", "Permission granted already");

      googleMap.setMyLocationEnabled(true);
    } else {

      PermissionUtils.requestPermission(
        (MainActivity) getActivity(),
        MY_LOCATION_REQUEST_CODE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        false);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    Log.d("MapFragment", "Permission result");
    if (requestCode == MY_LOCATION_REQUEST_CODE) {
      if (permissions.length == 1 &&
        permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        googleMap.setMyLocationEnabled(true);
      } else {
        Toast.makeText(getContext(), "Could not get location", Toast.LENGTH_SHORT);
      }
    }
  }

  void init() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
      if (location != null) {
        LatLng home = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(home));
      } else {
        LatLng home = new LatLng(60.206723, 24.667192);
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(home));
      }

    });
  }

  public void addTransportMarker(final Transport transport) {
    LatLng position = transport.getLocation();

    Marker existingMarker = transportMarkers.get(transport.getId());

    if (existingMarker != null) {
      existingMarker.setPosition(position);
    } else {
      //IconGenerator icg = new IconGenerator(getContext());
      //icg.setColor(Color.GREEN); // green background
      //icg.setTextAppearance(R.style.amu_Bubble_TextAppearance_Dark); // black text
      //Bitmap bm = icg.makeIcon(transport.getRouteDisplayName());
      IconGenerator iconTransportMarker = new IconGenerator(getContext());
      iconTransportMarker.setBackground(getResources().getDrawable(R.drawable.transport_icon));
      Marker marker = googleMap.addMarker(
        new MarkerOptions()
          .position(position)
          .anchor(0.5f, 0.5f)
          .icon(BitmapDescriptorFactory.fromBitmap(iconTransportMarker.makeIcon(transport.getRouteDisplayName())))
      );

      marker.setTag(transport);
      transportMarkers.put(transport.getId(), marker);
    }
  }

  public void addStopMarkers(List<Stop> stops) {
    for (Stop stop : stops) {
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
          .anchor(0.5f, 0.5f)
          .icon(BitmapDescriptorFactory.fromBitmap(getStopIcon()))
      );
      //googleMap.setOnCameraMoveListener(() -> marker.setVisible(googleMap.getCameraPosition().zoom>15));
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
