package com.example.transporttracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.ApolloSubscriptionCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hsl.TestQuery;
import com.hsl.TransportSubscription;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String BASE_URL = "https://transport-tracker-graphql.herokuapp.com/graphql";
    private static final String WEBSOCKET_URL = "wss://transport-tracker-graphql.herokuapp.com/graphql";

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
                .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(WEBSOCKET_URL, okHttpClient))
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
//                doQuery(googleMap.getCameraPosition().target);
                doSubscription();

            }
        });

//        this.doQuery(new LatLng(60.206723, 24.667192));
    }

    void doSubscription() {
        LatLng farLeft = googleMap.getProjection().getVisibleRegion().farLeft;
        LatLng nearRight = googleMap.getProjection().getVisibleRegion().nearRight;

        TransportSubscription transportSubscription = TransportSubscription.builder()
                .minLat(nearRight.latitude)
                .minLon(farLeft.longitude)
                .maxLat(farLeft.latitude)
                .maxLon(nearRight.longitude)
                .build();

        apolloClient.subscribe(transportSubscription).execute(new ApolloSubscriptionCall.Callback<TransportSubscription.Data>() {
            @Override
            public void onResponse(@NotNull Response<TransportSubscription.Data> response) {
                System.out.println(response.data().transportEventsInArea().routeNumber());
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                System.out.println(e);

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onTerminated() {

            }

            @Override
            public void onConnected() {
                System.out.println("Connected to subscription");
            }
        });

//        TransportSubscription.builder()
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
