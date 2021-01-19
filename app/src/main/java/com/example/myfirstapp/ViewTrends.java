package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewTrends extends AppCompatActivity {

    public static final String twitterBearerToken = "AAAAAAAAAAAAAAAAAAAAAEJ%2BLAEAAAAASl%2BZymBgDuRWDVjKU5ucFAbcflY%3Djjkv8RCzVGl2Iz4tZahIbgKreSyLWKCZYRYA4amQ8jFDhmcRZB";
    public static final String twitterUserId = "1343655836355342341";
    public static final String urlEndpoint = "https://api.twitter.com/2/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trends);

        try {
            getTrends();
        } catch (InterruptedException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getTrends() throws InterruptedException, UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        JSONArray responseArray = new JSONArray();
        LinearLayout trendsLinearLayout = (LinearLayout) this.findViewById(R.id.trendsLinearLayout);
        ArrayList<String> trendsList = new ArrayList<String>();

        String url = "https://api.twitter.com/1.1/trends/place.json?id=963291";

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + twitterBearerToken)
                .url(url)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String myResponse = response.body().string().substring(1);
                    myResponse = myResponse.substring(0, myResponse.length() - 1);
                   try {
                       JSONObject reader = new JSONObject(myResponse);
                       JSONArray trends = reader.getJSONArray("trends");

                       for (int i = 0; i < 10; i++) {
                           trendsList.add(String.valueOf(trends.getJSONObject(i).get("query")));
                       }
                   } catch (Exception e) {
                       Log.d("Error: ", "Error");
                       e.printStackTrace();
                   }

                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();

        for (int i = 0; i < trendsList.size(); i++) {
            final String trend = trendsList.get(i);

            TextView tv = new TextView(this);

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        searchTweets(trend);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            tv.setText(URLDecoder.decode(trend, "utf-8"));
            tv.setPadding(50, 20, 20, 20);
            trendsLinearLayout.addView(tv);
            trendsLinearLayout.addView(horizontalLine());
        }
    }

    public void searchTweets(String trend) throws InterruptedException, JSONException {
        OkHttpClient client = new OkHttpClient();
        JSONArray responseArray = new JSONArray();
        LinearLayout trendingTweetsContainer = (LinearLayout) findViewById(R.id.trendingTweetsContainer);

        String url = "https://api.twitter.com/2/tweets/search/recent?query=" + trend + "&tweet.fields=created_at&user.fields=created_at&expansions=author_id";

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + twitterBearerToken)
                .url(url)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    try {
                        JSONObject reader = new JSONObject(myResponse);
                        JSONArray data = reader.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject tweet = data.getJSONObject(i);
                            JSONObject obj = new JSONObject();
                            String createdAt = tweet.getString("created_at");
                            createdAt = createdAt.replace('T', ' ');
                            createdAt = createdAt.replace("Z", "");
                            createdAt = createdAt.replace(".000", "");
                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                            Date date = (Date)formatter.parse(createdAt);
                            obj.put("id", tweet.getString("id"));
                            obj.put("media", "twitter");
                            obj.put("text", tweet.getString("text"));
                            obj.put("created_at", createdAt);
                            obj.put("timestamp", date.getTime());
                            obj.put("author_id", tweet.getString("author_id"));
                            responseArray.put(obj);
                        }
                    } catch (JSONException | ParseException e) {
                        Log.d("Error: ", "Error");
                        e.printStackTrace();
                    }
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();

        getUser(responseArray.getJSONObject(0).getString("author_id"));

        for (int i = 0; i < responseArray.length(); i++) {
            TextView tweet = new TextView(this);
            JSONArray user = getUser(responseArray.getJSONObject(i).getString("author_id"));

            String tweetContent = "\nBy: " + user.getJSONObject(0).getString("name") + "  @" + user.getJSONObject(0).getString("username")
                    + "\n\n" + responseArray.getJSONObject(i).getString("text")
                    + "\n\nPosted on " + responseArray.getJSONObject(i).getString("media") + " @ " + responseArray.getJSONObject(i).getString("created_at")
                    + "\n";

            tweet.setText(tweetContent);

            trendingTweetsContainer.addView(tweet);
            trendingTweetsContainer.addView(horizontalLine());
        }
    }

    public JSONArray getUser(String userId) throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        JSONArray responseArray = new JSONArray();

        String url = "https://api.twitter.com/2/users/" + userId;

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + twitterBearerToken)
                .url(url)
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    try {
                        JSONObject reader = new JSONObject(myResponse);
                        JSONObject data = reader.getJSONObject("data");
                        JSONObject obj = new JSONObject();
                        obj.put("name", data.getString("name"));
                        obj.put("username", data.getString("username"));
                        responseArray.put(obj);
                    } catch (Exception e) {
                        Log.d("Error: ", "Error");
                        e.printStackTrace();
                    }
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();

        return responseArray;
    }

    public View horizontalLine() {
        View horizontalLine = new View(this);
        horizontalLine.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        ));
        horizontalLine.setBackgroundColor(Color.parseColor("#B3B3B3"));

        return horizontalLine;
    }
}