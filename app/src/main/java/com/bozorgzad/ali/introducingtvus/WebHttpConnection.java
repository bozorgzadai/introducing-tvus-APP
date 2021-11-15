package com.bozorgzad.ali.introducingtvus;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*
 * Created by Ali_Dev on 7/9/2017.
 */

/*
 *
 *
 *
 *
 *           IBazar WebHttpConnection IS THE BEST
 *
 *
 *
 *
 */

class WebHttpConnection extends AsyncTask<String, Void, String> {
    private URL url = null;
    private Uri.Builder builder = null;
    //private String method;

    WebHttpConnection(String urlConnect, Uri.Builder builderConnect) {
        try {
            url = new URL(urlConnect);
            //method = sendMethod;

            if (builderConnect != null) {
                builder = builderConnect;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected String doInBackground(String... sUrl) {
        try {
            //seller httpConnection
            /*if (builder != null && method.equals("GET")) {
                url = new URL(url.toString() + builder.toString());
            }*/

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(13000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoInput(true);

            if (builder != null) {
                urlConnection.setDoOutput(true);
                String query = builder.build().getEncodedQuery();

                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(outputStream, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                outputStream.close();
            }

            //// WE use cookie for transferring session between user and server for when a user not signIn yet (For example for paymentCart)
            // Check the sharedPreferences for set previous session or not
            // get session form cookie
            /*// Fetch and set cookies in requests
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(urlConnection.getURL().toString());
            if (cookie != null) {
                urlConnection.setRequestProperty("Cookie", cookie);
            }*/

            urlConnection.connect();

            // set session form cookie
            /*// Get cookies from responses and save into the cookie manager
            List cookieList = urlConnection.getHeaderFields().get("Set-Cookie");
            if (cookieList != null) {
                for (Object cookieTemp : cookieList) {
                    cookieManager.setCookie(urlConnection.getURL().toString(), (String) cookieTemp);
                }
            }*/

            InputStream inputStream = urlConnection.getInputStream();
            return convertInputStreamToString(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private static String convertInputStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}

