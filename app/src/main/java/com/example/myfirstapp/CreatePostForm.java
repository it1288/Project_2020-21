package com.example.myfirstapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CreatePostForm extends AppCompatActivity {

    Uri imageUri;
    private static final int PICK_PHOTO = 1;
    public static final String twitterBearerToken = "AAAAAAAAAAAAAAAAAAAAAEJ%2BLAEAAAAASl%2BZymBgDuRWDVjKU5ucFAbcflY%3Djjkv8RCzVGl2Iz4tZahIbgKreSyLWKCZYRYA4amQ8jFDhmcRZB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post_form);
    }

    public void pickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView imageView = (ImageView) findViewById(R.id.image_display);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO) {
                try {
                    imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSubmit(View view) throws UnsupportedEncodingException {
        TextView postText = findViewById(R.id.postText);
        CheckBox checkBoxTwitter = findViewById(R.id.checkBoxTwitter);
        CheckBox checkBoxFacebook = findViewById(R.id.checkBoxFacebook);
        CheckBox checkBoxInstagram = findViewById(R.id.checkBoxInstagram);

        if (imageUri != null) {
            // TODO: Unable to implement it
        }

        if (checkBoxTwitter.isChecked()) {
            postOnTwitter(postText.getText().toString());
        }
        if (checkBoxFacebook.isChecked()) {
            postOnFb(postText.getText().toString());
        }
        if (checkBoxInstagram.isChecked()) {
            // Unable to implement it
        }
    }

    public void postOnTwitter(String message) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String messageUrlEncode = URLEncoder.encode(message, "utf-8");
        String url = "https://api.twitter.com/1.1/statuses/update.json";

        RequestBody formBody = new FormBody.Builder()
                .add("status", messageUrlEncode)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + twitterBearerToken)
                .post(formBody)
                .build();

        client.newCall(request);
    }

    public void postOnFb(String message) throws UnsupportedEncodingException {
        OkHttpClient client = new OkHttpClient();
        String pageId = "102687225151570";
        String messageUrlEncode = URLEncoder.encode(message, "utf-8");
        String accessToken = "EAAGOZAwpqPIgBAEblcxZB9kvJLAzbyDCKTUNdmDX77gxoFEGSvSEtGSEI5BzZCjswPLxClKdMnbTBJSW0lgUPE6P4RxynFRxyOKos3znZB9ZAlLt46Qc5S9spdJYgF9Csbx6RUXmPXX490OU6dbqSO3Qlja0KCz3sfFmCfnM2sgZDZD";
        String url = "https://graph.facebook.com/" + pageId + "/feed";

        RequestBody formBody = new FormBody.Builder()
                .add("message", messageUrlEncode)
                .add("access_token", accessToken)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request);
    }
}