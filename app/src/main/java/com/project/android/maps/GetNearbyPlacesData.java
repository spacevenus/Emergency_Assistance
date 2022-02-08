package com.project.android.maps;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap mMap;
    String url;
    List<HashMap<String, String>> placeDetailsList = null;
    int totalPlacesCount = 0;
    static int count = 0;

    @Override
    protected String doInBackground(Object... params) {
        try {
            Log.d("GetNearbyPlacesData", "doInBackground entered");
            mMap = (GoogleMap) params[0];
            url = (String) params[1];
            DownloadUrl downloadUrl = new DownloadUrl();
            googlePlacesData = downloadUrl.readUrl(url);
            Log.d("GooglePlacesReadTask", "doInBackground Exit");
        } catch (Exception e) {
            Log.d("GooglePlacesReadTask", e.toString());
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("GooglePlacesReadTask", "onPostExecute Entered");
        List<String> nearbyPlacesList = null;
        DataParser dataParser = new DataParser();
        nearbyPlacesList =  dataParser.parse(result);
        count = 0;
        totalPlacesCount = nearbyPlacesList.size();
        for (int i = 0; i<nearbyPlacesList.size();i++)
        {
            ParserTask parserTask = new ParserTask();
            String[] DataTransfer = new String[1];
            DataTransfer[0] = getUrl(nearbyPlacesList.get(i));

            parserTask.execute(DataTransfer);
        }

    }

    TaskComplete taskComplete = new TaskComplete() {

        @Override
        public void taskDone(HashMap<String,String> hPlaceDetails){
            // TODO Auto-generated method stub
            if (placeDetailsList == null)
            {
                placeDetailsList = new ArrayList<>();
            }
            placeDetailsList.add(hPlaceDetails);
            count++;
            if (count == totalPlacesCount)
            {
                ShowNearbyPlaces(placeDetailsList);
            }
        }
    };
    private void ShowNearbyPlaces(List<HashMap<String, String>> nearbyPlacesList) {
        for (int i = 0; i < nearbyPlacesList.size(); i++) {
            Log.d("onPostExecute","Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlacesList.get(i);
            if (googlePlace != null) {
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                String placeName = googlePlace.get("name");
                String phone = googlePlace.get("formatted_phone");

                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + phone);
                mMap.addMarker(markerOptions);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        }
    }

    private String getUrl(String placeID) {

        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?place_id=");
        googlePlacesUrl.append(placeID);
        googlePlacesUrl.append("&key=" + "AIzaSyAMY50OPT8pMbRwVnQHlKUxv73AcCOEu9Y");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    /** A class to parse the Google Place Details in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, HashMap<String,String>>{

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected HashMap<String,String> doInBackground(String... jsonData) {

            HashMap<String, String> hPlaceDetails = null;
            String data = null;
            DownloadUrl downloadUrl = new DownloadUrl();
            try {
               data = downloadUrl.readUrl(jsonData[0]);
               if (data != null)
               {
                   PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();


                   jObject = new JSONObject(data);
                   hPlaceDetails = placeDetailsJsonParser.parse(jObject);

               }


            } catch (Exception e1) {
                e1.printStackTrace();
            }

            // Start parsing Google place details in JSON format


            return hPlaceDetails;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(HashMap<String,String> hPlaceDetails){
            taskComplete.taskDone(hPlaceDetails);

        }
    }




}

