/*************************************************************************
 *
 * ATOS CONFIDENTIAL
 * __________________
 *
 *  Copyright (2020) Atos Spain SA
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Atos Spain SA and other companies of the Atos group.
 * The intellectual and technical concepts contained
 * herein are proprietary to Atos Spain SA
 * and other companies of the Atos group and may be covered by Spanish regulations
 * and are protected by copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Atos Spain SA.
 */
package com.helios.helios_media_module;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.helios.helios_media_module.messaging.HeliosMessagingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.h2020.helios_social.modules.livevideostreaming.LiveVideoStreamingActivity;
import eu.h2020.helios_social.modules.videocall.VideoCallActivity;
import eu.h2020.helios_social.modules.filetransfer.FileTransferActivity;
import eu.h2020.helios_social.modules.videoplayer.VideoPlayerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FILE_TRANSFER_ACTIVITY_REQUEST_CODE = 1;
    private static final int REQUEST_FILE_SELECT = 2;
    private static final int ALL_PERMISSIONS_CODE = 1;
    private static final String LIVEVIDEOSTREAMING_URLS_KEY = "LIVEVIDEOSTREAMING_URLS_KEY";
    private static final String LIVEVIDEOSTREAMING_URLS_SEPARATOR = ", ";
    private HeliosMessagingService heliosMessagingService;
    private List<String> liveVideoStreamingUrls;

    private static final String VIDEOPLAYER_URLS_KEY = "VIDEOPLAYER_URLS_KEY";
    private static final String VIDEOPLAYER_URLS_SEPARATOR = ", ";
    private List<String> videoPlayerUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mainButton1 = findViewById(R.id.main_button1);
        mainButton1.setOnClickListener(this);
        Button mainButton2 = findViewById(R.id.main_button2);
        mainButton2.setOnClickListener(this);
        Button mainButton3 = findViewById(R.id.main_button3);
        mainButton3.setOnClickListener(this);
        Button mainButton4 = findViewById(R.id.main_button4);
        mainButton4.setOnClickListener(this);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        String livevideostreamingUrls = sharedPref.getString(LIVEVIDEOSTREAMING_URLS_KEY, getString(R.string.livevideostreaming_url));
        liveVideoStreamingUrls = new ArrayList<>(Arrays.asList(livevideostreamingUrls.split(LIVEVIDEOSTREAMING_URLS_SEPARATOR)));

        String hls_uri = sharedPref.getString(VIDEOPLAYER_URLS_KEY, getString(R.string.hls_uri));
        videoPlayerUrls = new ArrayList<>(Arrays.asList(hls_uri.split(VIDEOPLAYER_URLS_SEPARATOR)));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        }

        heliosMessagingService = new HeliosMessagingService(this);

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.main_button1: {
                showLiveVideoStreamingDialog();

                break;
            }

            case  R.id.main_button2: {
                showVideocallRoomNameDialog();

                break;
            }

            case  R.id.main_button3: {
                showVideoPlayerDialog(getString(R.string.hls_uri));

                break;
            }

            case  R.id.main_button4: {
                Intent fileTransferIntent = new Intent(MainActivity.this, FileTransferActivity.class);
                MainActivity.this.startActivityForResult(fileTransferIntent, FILE_TRANSFER_ACTIVITY_REQUEST_CODE);
                break;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == FILE_TRANSFER_ACTIVITY_REQUEST_CODE) {
            heliosMessagingService.publishDirect(HeliosMessagingService.FILETRANSFER_URL_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + data.getStringExtra("uploadURL"));
        }

        if (requestCode == REQUEST_FILE_SELECT) {
            Uri uri = data.getData();
            startVideoPlayer(uri.toString());
        }
    }

    public void showDialog(String title, String message, Runnable positiveAction) {
        showDialog(title, message, false, positiveAction);
    }

    public void showDialogWithLink(String title, String link) {
        showDialog(title, link, true, null);
    }

    private void showDialog(String title, String message, boolean messageLinkable, Runnable positiveAction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(title);

        if (messageLinkable) {
            final TextView messageTextView = new TextView(this);
            final SpannableString messageSpannableString = new SpannableString(message);
            Linkify.addLinks(messageSpannableString, Linkify.WEB_URLS);
            messageTextView.setText(messageSpannableString);
            messageTextView.setMovementMethod(LinkMovementMethod.getInstance());
            messageTextView.setPadding(70, 20, 0, 0);

            builder.setView(messageTextView);

            builder.setPositiveButton("Download", (dialog, which) -> {
                Uri uri = Uri.parse(message);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            });
        } else {
            builder.setMessage(message);
        }

        if (positiveAction != null) {
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> positiveAction.run());
        }

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showLiveVideoStreamingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.livevideostreaming_title);
        builder.setMessage(R.string.livevideostreaming_message);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, liveVideoStreamingUrls);
        AutoCompleteTextView textView = new AutoCompleteTextView(this);
        textView.setAdapter(adapter);
        textView.setInputType(InputType.TYPE_CLASS_TEXT);
        textView.setText("rtmp://");
        textView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });

        builder.setView(textView);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String liveVideoStreamingUrl = textView.getText().toString();
            if (!liveVideoStreamingUrls.contains(liveVideoStreamingUrl)) {
                liveVideoStreamingUrls.add(liveVideoStreamingUrl);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(LIVEVIDEOSTREAMING_URLS_KEY, String.join(LIVEVIDEOSTREAMING_URLS_SEPARATOR, liveVideoStreamingUrls));
                editor.apply();
            }

            Intent liveVideoStreamingIntent = new Intent(MainActivity.this, LiveVideoStreamingActivity.class);
            liveVideoStreamingIntent.putExtra("stream_url", liveVideoStreamingUrl);
            MainActivity.this.startActivity(liveVideoStreamingIntent);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showVideocallRoomNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.room_dialog_title);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.room_dialog_ok, (dialog, which) -> {
            String roomName = input.getText().toString();
            heliosMessagingService.publishDirect(HeliosMessagingService.VIDEOCALL_ROOM_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + roomName);
            startVideoCall(roomName);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void startVideoCall(String roomName) {
        Intent videoCallIntent = new Intent(MainActivity.this, VideoCallActivity.class);
        videoCallIntent.putExtra("room_name", roomName);
        MainActivity.this.startActivity(videoCallIntent);
    }

    private void showVideoPlayerDialog(String videoUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.videoplayer_title);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, videoPlayerUrls);
        AutoCompleteTextView textView = new AutoCompleteTextView(this);
        textView.setAdapter(adapter);
        textView.setInputType(InputType.TYPE_CLASS_TEXT);
        textView.setText(R.string.hls_uri);
        textView.setText("http");
        textView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });

        builder.setView(textView);

        // Set up the buttons
        builder.setPositiveButton(R.string.videoplayer_ok, (dialog, which) -> {
            String hls_url = textView.getText().toString();
            if (!videoPlayerUrls.contains(hls_url)) {
                videoPlayerUrls.add(hls_url);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(VIDEOPLAYER_URLS_KEY, String.join(VIDEOPLAYER_URLS_SEPARATOR, videoPlayerUrls));
                editor.apply();
            }

            startVideoPlayer(hls_url);
        });

        builder.setNegativeButton(R.string.videoplayer_file, (dialog, which) -> {
            Intent intent = new Intent();
            intent.setType("video/mp4");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select file to play"), REQUEST_FILE_SELECT);
        });

        builder.show();
    }

    public void startVideoPlayer(String videoUri) {
        Intent videoPlayerIntent = new Intent(MainActivity.this, VideoPlayerActivity.class);
        videoPlayerIntent.putExtra("URI", videoUri);
        MainActivity.this.startActivity(videoPlayerIntent);
    }

    @Override
    protected void onDestroy() {
        heliosMessagingService.stop();
        super.onDestroy();
    }
}
