package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    public static final String twitterKey = "WA54heQh2n24eY8Yq5SSO7XRy";
    public static final String twitterApiSecretKey="WhJQGT6Q2pQHpqiNaeLU0ad2Mvr9cKRTBLrEyuFWhIuVYAoOAy";
    public static final String twitterBearerToken = "AAAAAAAAAAAAAAAAAAAAAEJ%2BLAEAAAAASl%2BZymBgDuRWDVjKU5ucFAbcflY%3Djjkv8RCzVGl2Iz4tZahIbgKreSyLWKCZYRYA4amQ8jFDhmcRZB";
    public static final String twitterUserId = "1343655836355342341";
    public static final String twitterUrlEndpoint = "https://api.twitter.com/2/";
    public static final String fbAccessToken = "EAAGOZAwpqPIgBAEblcxZB9kvJLAzbyDCKTUNdmDX77gxoFEGSvSEtGSEI5BzZCjswPLxClKdMnbTBJSW0lgUPE6P4RxynFRxyOKos3znZB9ZAlLt46Qc5S9spdJYgF9Csbx6RUXmPXX490OU6dbqSO3Qlja0KCz3sfFmCfnM2sgZDZD";
    public static final String fbUserId = "109223564457074";
    public static final String fbUrl = "https://graph.facebook.com/";
    public static final String fbAppId = "438048184220808";
    public static final String fbAppSecret = "fdb990d6f4d986e8c6d0818014781def";

    private TextView resultsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            JSONArray tweets = getTweets("");
            JSONArray fbPosts = getFBPosts();
            JSONArray allPosts = concatArray(tweets, fbPosts);
            allPosts = sortJsonArray(allPosts);
            loadPosts(allPosts);
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getTweets(String searchTerm) throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        JSONArray responseArray = new JSONArray();

        String url = (searchTerm.isEmpty())
                ? twitterUrlEndpoint + "users/" + twitterUserId + "/tweets?"
                : twitterUrlEndpoint + "tweets/search/recent?query=" + searchTerm + "&";
        url += "tweet.fields=created_at&user.fields=created_at";

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
        return responseArray;
    }

    public JSONArray getFBPosts() throws InterruptedException {
        OkHttpClient client = new OkHttpClient();
        JSONArray responseArray = new JSONArray();

        String url = fbUrl + fbUserId + "/feed?access_token=" + fbAccessToken;

        Request request = new Request.Builder()
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
                            JSONObject fbPost = data.getJSONObject(i);
                            if (fbPost.has("message")) {
                                JSONObject obj = new JSONObject();
                                String createdAt = fbPost.getString("created_time");
                                createdAt = createdAt.replace('T', ' ');
                                createdAt = createdAt.replace("+000", "");
                                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd H:m:s");
                                Date date = (Date) formatter.parse(createdAt);
                                obj.put("id", fbPost.getString("id"));
                                obj.put("media", "facebook");
                                obj.put("text", fbPost.getString("message"));
                                obj.put("created_at", createdAt);
                                obj.put("timestamp", date.getTime());
                                responseArray.put(obj);
                            }
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
        return responseArray;
    }

    public void loadPosts(JSONArray posts) throws JSONException {
        setContentView(R.layout.activity_main);
//        ScrollView postsScrollView = (ScrollView) findViewById(R.id.postsScrollView);
        LinearLayout postsLinearLayout = (LinearLayout) this.findViewById(R.id.postsLinearLayout);
        for (int i = 0; i < posts.length(); i++) {
            JSONObject post = posts.getJSONObject(i);
            View horizontalLine = new View(this);
            horizontalLine.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    5
            ));
            horizontalLine.setBackgroundColor(Color.parseColor("#B3B3B3"));
            String postContent = "\n" + post.getString("text")
                    + "\n\nPosted on " + post.getString("media") + " @ " + post.getString("created_at")
                    + "\n";
            TextView postView = new TextView(this);
            postView.setText(postContent);

            postView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // TODO
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            postsLinearLayout.addView(postView);
            postsLinearLayout.addView(horizontalLine);
        }
    }

    private JSONArray concatArray(JSONArray arr1, JSONArray arr2) throws JSONException {
        JSONArray result = new JSONArray();
        for (int i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (int i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }

    public static JSONArray sortJsonArray(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        for (int i = 0; i < array.length(); i++) {
            jsons.add(array.getJSONObject(i));
        }
        Collections.sort(jsons, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                String lid = null;
                String rid = null;
                try {
                    lid = lhs.getString("timestamp");
                    rid = rhs.getString("timestamp");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Here you could parse string id to integer and then compare.
                return lid.compareTo(rid);
            }
        });
        return new JSONArray(jsons);
    }

    public void onClickButton(View view) throws InterruptedException, JSONException {
        TextView searchTermView = findViewById(R.id.searchTerm);
        String searchTerm = searchTermView.getText().toString().trim();

        JSONArray tweets = getTweets(searchTerm);
        loadPosts(tweets);
    }

    public void loadNewPostView(View view) {
        Intent createPostForm = new Intent(this, CreatePostForm.class);
        startActivity(createPostForm);
    }

    public void loadTrendsView(View view) {
        Intent viewTrends = new Intent(this, ViewTrends.class);
        startActivity(viewTrends);
    }
}