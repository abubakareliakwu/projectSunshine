package com.example.hp.projectsunshine;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toolbar;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IllegalFormatException;

public class MainActivity extends Activity {



    private static String TAG = MainActivity.class.getSimpleName();

    private ArrayAdapter<String> weatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        /**
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         */

        ListView listView = (ListView) findViewById(R.id.listview);
        setUpList(listView);
    }

    private void setUpList(ListView listView) {
        String[] weatherString = {"Lagos", "Accra", "Makurdi", "Lokoja"};
        ArrayList<String> weatherList = new ArrayList<>(Arrays.asList(weatherString));

        weatherAdapter = new ArrayAdapter<>(this, R.layout.weather_list, R.id.list_item, weatherList);
        listView.setAdapter(weatherAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            new FetchWeatherJSON().execute("94043");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FetchWeatherJSON extends AsyncTask<String, Void, String[]> {

        private String INNER_TAG = FetchWeatherJSON.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr;

            try {

                int numOfDays = 7;
                String format = "json";
                String unit = "metric";

                final String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String COUNT_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri buildUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, unit)
                        .appendQueryParameter(COUNT_PARAM, Integer.toString(numOfDays))
                    //    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                Log.v(INNER_TAG, "Built uri " + buildUri.toString());

                URL url = new URL(buildUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {

                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
                return JSONWeatherDataParser.getWeatherDataFromJson(forecastJsonStr, numOfDays);
            } catch (IOException | IllegalFormatException | JSONException e) {
                Log.e(INNER_TAG, "Error ", e);

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] results) {
            if(null != results) {
                weatherAdapter.clear();
                weatherAdapter.addAll(results);
            }
        }
    }
}
