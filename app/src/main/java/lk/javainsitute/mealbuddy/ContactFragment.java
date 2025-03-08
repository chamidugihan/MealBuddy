package lk.javainsitute.mealbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ContactFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng userLocation;
    private LatLng destinationLocation = new LatLng(6.9802728697938, 79.8639107130015); // Destination coordinates

    public ContactFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        checkLocationPermission();

        Button whatsapp = view.findViewById(R.id.sendbutton);
        Button contactus = view.findViewById(R.id.contactus);
        Button sms = view.findViewById(R.id.sms);

        contactus.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_DIAL);
            Uri u = Uri.parse("tel:xxxxxxxxxx");
            i.setData(u);
            startActivity(i);
        });

        whatsapp.setOnClickListener(v -> sendMessageToWhatsApp("xxxxxxxxxx", "Hello! Buddy"));

        sms.setOnClickListener(v -> {
            String phoneNumber = "xxxxxxxxxx";
            String message = "Hello! Buddy";

            Uri uri = Uri.parse("smsto:" + phoneNumber);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", message);
            Toast.makeText(requireContext(), "Message Sent", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        });

        return view;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getUserLocation() {
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                initializeMap();
            } else {
                Log.e("App30", "Failed to get user location.");
            }
        });
    }

    private void initializeMap() {
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout2, supportMapFragment);
        fragmentTransaction.commit();

        supportMapFragment.getMapAsync(gMap -> {
            googleMap = gMap;
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
            googleMap.addMarker(new MarkerOptions().position(destinationLocation).title("MealBuddy"));

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            }

            if (userLocation != null) {
                drawRoute();
            } else {
                Log.e("App30", "User location is null. Cannot draw route.");
            }
        });
    }

    private void drawRoute() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String url = getDirectionsUrl(userLocation, destinationLocation);
                Log.d("App30", "Requesting Directions API: " + url);

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                String jsonData = response.body().string();

                Log.d("App30", "Directions API Response: " + jsonData);

                parseAndDrawRoute(jsonData);
            } catch (IOException e) {
                Log.e("App30", "Error fetching route: " + e.getMessage());
            }
        });
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String originParam = "origin=" + origin.latitude + "," + origin.longitude;
        String destinationParam = "destination=" + destination.latitude + "," + destination.longitude;
        String apiKey = "ENTERS GOOGLE MAPS API KEY";
        return "https://maps.googleapis.com/maps/api/directions/json?" + originParam + "&" + destinationParam + "&mode=driving&key=" + apiKey;
    }

    private void parseAndDrawRoute(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray routes = jsonObject.getJSONArray("routes");

            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String points = overviewPolyline.getString("points");
                List<LatLng> decodedPath = decodePolyline(points);

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        googleMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.BLUE).width(12));
                        Log.d("App30", "Route drawn successfully!");
                    });
                }
            } else {
                Log.e("App30", "No route found in response.");
            }
        } catch (JSONException e) {
            Log.e("App30", "Error parsing Directions API response: " + e.getMessage());
        }
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1F) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            polyline.add(new LatLng(lat / 1E5, lng / 1E5));
        }

        Log.d("App30", "Decoded Polyline Points: " + polyline.toString());
        return polyline;
    }

    private void sendMessageToWhatsApp(String phoneNumber, String message) {
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
