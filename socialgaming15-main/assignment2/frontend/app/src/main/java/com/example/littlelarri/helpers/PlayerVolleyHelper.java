package com.example.littlelarri.helpers;


import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.littlelarri.IPConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class PlayerVolleyHelper {
    public RequestQueue requestQueue;
    // TODO: Add your IP address of your local computer instead of localhost (ipconfig /all -> copy ipv4-adress)
    private final String baseURL = "http://" + IPConfig.IP + ":8080";

    public PlayerVolleyHelper(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void getPlayerByFirebaseUID(String firebaseUID, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        // Rest API endpoint
        String url = baseURL + "/player/" + firebaseUID;
        // Create the GET request
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);
        //objectRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(objectRequest);
    }

    public void getAllPlayers(Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/";
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        requestQueue.add(arrayRequest);
    }

    public void findAllByOrderByLevelDesc(Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/rank";
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        requestQueue.add(arrayRequest);
    }

    public void findPlayersByNicknameContaining(String name_part, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/nickname/" + name_part;
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        requestQueue.add(arrayRequest);
    }

    public void findPlayersByFirebaseUIDBefore(String uid_part, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/uid/" + uid_part;
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        requestQueue.add(arrayRequest);
    }

    public void getFriendsOfFirebaseUID(String uid_part, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/friends/" + uid_part;
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        requestQueue.add(arrayRequest);
    }

    public void getFriendRequestsOfFirebaseUID(String uid_part, Response.Listener<JSONArray> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/friendRequests/" + uid_part;
        JsonArrayRequest arrayRequest = new JsonArrayRequest(Request.Method.GET, url, null, responseListener, errorListener);
        requestQueue.add(arrayRequest);
    }

    public void postRegisterNewUser(JSONObject jsonObject, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        // Rest API endpoint
        String url = baseURL + "/player";

        // Create the POST request
        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, responseListener, errorListener);
        //objRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(objRequest);
    }

    public void updatePlayer(String firebaseUID, JSONObject jsonObject,
                               Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/" + firebaseUID;
        JsonObjectRequest objRequest = new MyJsonObjectRequest(Request.Method.PUT, url, jsonObject, responseListener, errorListener);
        requestQueue.add(objRequest);
    }

    public void updateLarri(String firebaseUID,JSONObject jsonObject, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        String url = baseURL + "/player/" + firebaseUID;

        // Create the POST request
        JsonObjectRequest objRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject, responseListener, errorListener);
        //objRequest.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(objRequest);
    }

    // Needed if server sends no response
    private static class MyJsonObjectRequest extends JsonObjectRequest {
        public MyJsonObjectRequest(String url, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
            super(url, listener, errorListener);
        }

        public MyJsonObjectRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
            super(method, url, jsonRequest, listener, errorListener);
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            if (response.data.length == 0) {
                response = new NetworkResponse(response.statusCode, "{}".getBytes(StandardCharsets.UTF_8), response.notModified, response.networkTimeMs, response.allHeaders);
            }
            return super.parseNetworkResponse(response);
        }
    }
}
