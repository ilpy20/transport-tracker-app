package com.example.transporttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hsl.TestQuery;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String BASE_URL = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql";

    ApolloClient apolloClient;
    GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initApolloClient();
        initMap();

    }

    void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    void initApolloClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        apolloClient = ApolloClient.builder()
                .serverUrl(BASE_URL)
                .okHttpClient(okHttpClient)
                .build();
    }

    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng home = new LatLng(60.206723, 24.667192);


        googleMap.moveCamera(CameraUpdateFactory.zoomTo(14));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(home));

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                doQuery(googleMap.getCameraPosition().target);
            }
        });

        this.doQuery(new LatLng(60.206723, 24.667192));
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

        apolloClient.query(testQuery).enqueue(new ApolloCall.Callback<TestQuery.Data>() {


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
