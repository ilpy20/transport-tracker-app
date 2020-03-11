package com.example.transporttracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
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

  /**
   *
   */
  public MapFragment() {
    transportMarkers = new HashMap<>();
    stopMarkers = new HashMap<>();
  }


  /**
   *
   * @param inflater
   * @param container
   * @param savedInstanceState
   * @return
   */
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

  /**
   *
   */
  public void clearTransportMarkers() {
    VisibleRegion bounds = getMapBounds();
    ArrayList<String> markersToRemove = new ArrayList<>();
    transportMarkers.forEach((String key, Marker marker) -> {
        LatLng markerLocation = marker.getPosition();
        if (!bounds.latLngBounds.contains(markerLocation)) {
          marker.remove();
          markersToRemove.add(key);
        }
      }
    );
    markersToRemove.forEach((String key) -> transportMarkers.remove(key));
  }

  /**
   *
   */
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

  /**
   *
   * @param requestCode
   * @param permissions
   * @param grantResults
   */
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

  /**
   *
   */
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

  /**
   *
   * @param mode
   * @return
   */
  int getTransportColor(String mode) {
    switch (mode) {
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
   *
   * @param transport
   * @return
   */
  Drawable getTransportIcon(Transport transport) {
    Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.transport_icon);
    Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(getContext(), getTransportColor(transport.getRouteMode())));
    return wrappedDrawable;
  }

  /**
   *
   * @param transport
   */
  public void addTransportMarker(final Transport transport) {
    LatLng position = transport.getLocation();

    Marker existingMarker = transportMarkers.get(transport.getId());

    if (existingMarker != null) {
      existingMarker.setPosition(position);
    } else {

      IconGenerator iconTransportMarker = new IconGenerator(getContext());
      iconTransportMarker.setBackground(getTransportIcon(transport));
      iconTransportMarker.setTextAppearance(R.style.amu_Bubble_TextAppearance_Light);

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

  /**
   *
   * @param stops
   */
  public void addStopMarkers(List<Stop> stops) {
    for (Stop stop : stops) {
      addStopMarker(stop);
    }
  }

  /**
   *
   * @return
   */
  Bitmap getStopIcon() {
    int height = 24;
    int width = 24;
    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.stop_icon, getContext().getTheme());
    Bitmap b = bitmapdraw.getBitmap();
    return Bitmap.createScaledBitmap(b, width, height, false);
  }

  /**
   *
   * @param stop
   */
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

  /**
   *
   * @param callback
   */
  public void setOnMapReadyListener(OnMapViewReadyCallback callback) {
    if (ready) {
      callback.onMapReady();
    } else {
      this.mapReadyCallback = callback;
    }
  }

  /**
   *
   * @param callback
   */
  public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener callback) {
    googleMap.setOnMarkerClickListener(callback);
  }

  /**
   *
   * @param callback
   */
  public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener callback) {
    googleMap.setOnCameraIdleListener(callback);
  }

  /**
   *
   * @return
   */
  public VisibleRegion getMapBounds() {
    return googleMap.getProjection().getVisibleRegion();
  }

  /**
   *
   * @param callback
   */
  public void setOnMapClickListener(GoogleMap.OnMapClickListener callback) {
    googleMap.setOnMapClickListener(callback);
  }

  /**
   *
   */
  public interface OnMapViewReadyCallback {
    void onMapReady();
  }

  /**
   *
   * @param context
   */
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

  /**
   *
   */
  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   *
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }
}
