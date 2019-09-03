package com.example.newsguardian.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.newsguardian.R;
import com.example.newsguardian.constants.Constants;
import com.example.newsguardian.data.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();

    private QueryUtils() {
    }

    public static List<News> fetchNewsData(String requestUrl, Context context) {
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem in QueryUtils - HTTP request.", e);
        }

        return extractFeatureFromJson(jsonResponse, context);
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // When the request was successful the response code equals 200
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error - response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the news JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static List<News> extractFeatureFromJson(String newsJSON, Context context) {
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Separate authors
        String conjunction = context.getString(R.string.comma) + " ";

        List<News> news = new ArrayList<>();

        try {

            JSONObject baseJsonResponse = new JSONObject(newsJSON);
            JSONObject responseJsonObject = baseJsonResponse.optJSONObject(Constants.RESPONSE);
            JSONArray newsArray = responseJsonObject.optJSONArray(Constants.RESULTS);

            for (int i = 0; i < newsArray.length(); i++) {

                JSONObject currentNews = newsArray.optJSONObject(i);

                // Get ID value
                String newsId = currentNews.optString(Constants.NEWS_ID);
                // Get section value
                String section = currentNews.optString(Constants.SECTION);
                // Get publication date value
                String date = currentNews.optString(Constants.DATE);
                // Get title value
                String title = currentNews.optString(Constants.TITLE);
                // Get url value
                String url = currentNews.optString(Constants.NEWS_URL);

                JSONObject fields = currentNews.optJSONObject(Constants.FIELDS);
                // Get thumbnail value
                String thumbnail = fields.optString(Constants.THUMBNAIL);

                String authors = "";
                JSONArray tagsArray = currentNews.optJSONArray(Constants.TAGS);

                // Get authors value
                for (int j = 0; j < tagsArray.length(); j++) {
                    JSONObject currentAuthor = tagsArray.optJSONObject(j);
                    String singleAuthor = currentAuthor.optString(Constants.AUTHOR_NAME);
                    if (j > 0) {
                        authors = authors + conjunction + singleAuthor;
                    } else {
                        authors = singleAuthor;
                    }
                }
                News newsObject = new News(title, section, date, authors, url, thumbnail, newsId);
                news.add(newsObject);
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing JSON results", e);
        }
        return news;
    }
}
