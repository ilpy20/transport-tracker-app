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
import com.google.android.gms.maps.CameraUpdate;
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
import java.util.stream.Collectors;

/**
 * MapFragment works with map
 * @author Sergey Ushakov, Ilya Pyshkin, Mahamudul Alam
 * @version 1.0
 * @since 2020-03-12
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
   *Initializing hashmaps for markers
   */
  public MapFragment() {
    transportMarkers = new HashMap<>();
    stopMarkers = new HashMap<>();
  }


  /**
   *Initialize map
   * @param inflater The LayoutInflater object used to inflate map in the MapFragment.
   * @param container This is the parent view that the fragment_map should be attached to.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
   * @return Return the rootView for MapFragment
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
   *Clean transport markers
   * @return markersToRemove
   */
  public List<String> clearTransportMarkers() {
    VisibleRegion bounds = getMapBounds();

    List<String> markersToRemove = transportMarkers
      .entrySet()
      .stream()
      .filter(markerEntry -> !bounds.latLngBounds.contains(markerEntry.getValue().getPosition()))
      .map(markerEntry -> markerEntry.getKey())
      .collect(Collectors.toList());

    markersToRemove.forEach(marker -> transportMarkers.remove(marker).remove());

    return markersToRemove;
  }

  /**
   *Ask permission for user location with PermissionUtils
   */
  private void accessUserLocation() {
    MainActivity mContext = (MainActivity) getActivity();
    if (ContextCompat.checkSelfPermission(mContext.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
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
   *Gives result of permission
   * @param requestCode code of request
   * @param permissions array of permissions
   * @param grantResults result
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
   *Moves to user location if have permission. Otherwise, moves to some location.
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
   *Set up color for every mode of transport
   * @param mode mode of transport
   * @return int transport color
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
   *Draw transport icon
   * @param transport class which contains data about transport
   * @return wrappedDrawable transport icon background
   */
  Drawable getTransportIcon(Transport transport) {
    Drawable unwrappedDrawable = AppCompatResources.getDrawable(getContext(), R.drawable.transport_icon);
    Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(getContext(), getTransportColor(transport.getRouteMode())));
    return wrappedDrawable;
  }

  /**
   *Put transport marker to the map and transportMarkers hashmap
   * @param transport Transport class which contains data about transport
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
   *Put stop markers to the map
   * @param stops List<Stop>
   */
  public void addStopMarkers(List<Stop> stops) {
    for (Stop stop : stops) {
      addStopMarker(stop);
    }
  }

  /**
   *Generate stop icon
   * @return Bitmap as a stop icon
   */
  Bitmap getStopIcon() {
    int height = 24;
    int width = 24;
    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.stop_icon, getContext().getTheme());
    Bitmap b = bitmapdraw.getBitmap();
    return Bitmap.createScaledBitmap(b, width, height, false);
  }

  /**
   *Focusing on transport marker
   * @param transport Transport class which contains data about transport
   */
  public void focusOnTransportMarker(Transport transport) {
    Marker marker = transportMarkers.get(transport.getId());

    if(marker != null) {
      CameraUpdate center=
        CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 15);
      googleMap.animateCamera(center);
    }

  }

  /**
   *Put stop marker to the map and stopMarkers hashmap
   * @param stop Stop class which contains data about stop
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
   *Callback for onMapReady function
   * @param callback OnMapViewReadyCallback callback
   */
  public void setOnMapReadyListener(OnMapViewReadyCallback callback) {
    if (ready) {
      callback.onMapReady();
    } else {
      this.mapReadyCallback = callback;
    }
  }

  /**
   *Callback for setOnMarkerListener(set listener on the marker click)
   * @param callback GoogleMap.OnMarkerClickListener callback
   */
  public void setOnMarkerClickListener(GoogleMap.OnMarkerClickListener callback) {
    googleMap.setOnMarkerClickListener(callback);
  }

  /**
   *Callback for setOnCameraIdleListener(set listener when camera movement has ended)
   * @param callback GoogleMap.OnCameraIdleListener callback
   */
  public void setOnCameraIdleListener(GoogleMap.OnCameraIdleListener callback) {
    googleMap.setOnCameraIdleListener(callback);
  }

  /**
   *Get visible region
   * @return visible region
   */
  public VisibleRegion getMapBounds() {
    return googleMap.getProjection().getVisibleRegion();
  }

  /**
   *Callback for setOnMapClickListener(set listener on the map click)
   * @param callback GoogleMap.OnMapClickListener callback
   */
  public void setOnMapClickListener(GoogleMap.OnMapClickListener callback) {
    googleMap.setOnMapClickListener(callback);
  }

  /**
   *Callback interface for onMapReady function
   */
  public interface OnMapViewReadyCallback {
    void onMapReady();
  }

  /**
   *Called when a fragment is first attached to its context.
   * @param context context
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
   * Called when the fragment is no longer attached to its activity.
   */
  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * Required for MapFragment to work???????
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }
}
