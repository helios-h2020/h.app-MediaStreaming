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
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.helios.helios_media_module.dto.MessageDTO;
import com.helios.helios_media_module.messaging.HeliosMessagingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.h2020.helios_social.core.messaging.HeliosMessagingException;
import eu.h2020.helios_social.modules.filetransfer.FileTransferActivity;
import eu.h2020.helios_social.modules.livevideostreaming.LiveVideoStreamingActivity;
import eu.h2020.helios_social.modules.videocall.VideoCallActivity;
import eu.h2020.helios_social.modules.videoplayer.VideoPlayerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AlertDialog dialogRoomVideoCall;
    private String TURN_URL;
    private String TURN_user;
    private String TURN_credential;
    private String STUN_URL;
    private String API_endpoint;

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

    private static final String TURN_URLS_KEY = "TURN_URLS_KEY";
    private static final String TURN_URLS_SEPARATOR = ", ";
    private List<String> turnUrls;
    private static final String USER_URLS_KEY = "USER_URLS_KEY";
    private static final String USER_URLS_SEPARATOR = ", ";
    private List<String> userUrls;
    private static final String STUN_URLS_KEY = "STUN_URLS_KEY";
    private static final String STUN_URLS_SEPARATOR = ", ";
    private List<String> stunUrls;
    private static final String API_URLS_KEY = "API_URLS_KEY";
    private static final String API_URLS_SEPARATOR = ", ";
    private List<String> apiUrls;

    private AutoCompleteTextView textViewTurn;
    private AutoCompleteTextView textViewTurnUser;
    private AutoCompleteTextView textViewTurnCredential;
    private AutoCompleteTextView textViewStun;
    private AutoCompleteTextView textViewApiEndpoint;

    private static final String MODULE_NAME_FILETRANSFER = "FileTransfer";
    private static final String MODULE_NAME_VIDEOCALL = "VideoCall";

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

        String turn_uri = sharedPref.getString(TURN_URLS_KEY, getString(R.string.turn));
        turnUrls = new ArrayList<>(Arrays.asList(turn_uri.split(TURN_URLS_SEPARATOR)));

        String user_uri = sharedPref.getString(USER_URLS_KEY, getString(R.string.user_turn));
        userUrls = new ArrayList<>(Arrays.asList(user_uri.split(USER_URLS_SEPARATOR)));

        String stun_uri = sharedPref.getString(STUN_URLS_KEY, getString(R.string.stun));
        stunUrls = new ArrayList<>(Arrays.asList(stun_uri.split(STUN_URLS_SEPARATOR)));

        String api_uri = sharedPref.getString(API_URLS_KEY, getString(R.string.signaling));
        apiUrls = new ArrayList<>(Arrays.asList(api_uri.split(API_URLS_SEPARATOR)));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        }

        try {
            heliosMessagingService = new HeliosMessagingService(this);
        } catch (HeliosMessagingException e) {
            e.printStackTrace();
        }

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
            MessageDTO msg = new MessageDTO();
            msg.setNameModule(MODULE_NAME_FILETRANSFER);
            msg.setUploadURL(data.getStringExtra("uploadURL"));
            msg.setTimeMillis(System.currentTimeMillis());

            Gson gson = new Gson();

            //transform a java object to json
            String messageKeys= gson.toJson(msg).toString();
            heliosMessagingService.publishToTopic(messageKeys);

            //heliosMessagingService.publishToTopic(HeliosMessagingService.FILETRANSFER_URL_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + data.getStringExtra("uploadURL"));
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

            if (TURN_URL == null) TURN_URL = "turn:$IP_of_server:3478";
            if (TURN_user == null) TURN_user = "user1";
            if (TURN_credential == null) TURN_credential = "user1";
            if (STUN_URL == null) STUN_URL = "stun:$IP_of_server:3478";
            if (API_endpoint == null) API_endpoint = "https://$IP_of_server:11794";

            MessageDTO msg = new MessageDTO();
            msg.setNameModule(MODULE_NAME_VIDEOCALL);
            msg.setRoomName(roomName);
            msg.setTurnURL(TURN_URL);
            msg.setTurnUser(TURN_user);
            msg.setTurnCredential(TURN_credential);
            msg.setStunURL(STUN_URL);
            msg.setApiEndpoint(API_endpoint);
            msg.setTimeMillis(System.currentTimeMillis());

            Gson gson = new Gson();

            //transform a java object to json
            String messageKeys= gson.toJson(msg).toString();

/*
            String messageKeys = HeliosMessagingService.VIDEOCALL_ROOM_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + roomName + HeliosMessagingService.MESSAGE_PAIR_KEY_VALUE_SEPARATOR +
                    HeliosMessagingService.VIDEOCALL_TURN_URL_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + TURN_URL + HeliosMessagingService.MESSAGE_PAIR_KEY_VALUE_SEPARATOR +
                    HeliosMessagingService.VIDEOCALL_TURN_USER_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + TURN_user + HeliosMessagingService.MESSAGE_PAIR_KEY_VALUE_SEPARATOR +
                    HeliosMessagingService.VIDEOCALL_TURN_CREDENTIAL_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + TURN_credential + HeliosMessagingService.MESSAGE_PAIR_KEY_VALUE_SEPARATOR +
                    HeliosMessagingService.VIDEOCALL_STUN_URL_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + STUN_URL + HeliosMessagingService.MESSAGE_PAIR_KEY_VALUE_SEPARATOR +
                    HeliosMessagingService.VIDEOCALL_API_ENDPOINT_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + API_endpoint;
*/
            //heliosMessagingService.publishDirect(HeliosMessagingService.VIDEOCALL_ROOM_KEY + HeliosMessagingService.MESSAGE_KEY_VALUE_SEPARATOR + roomName);
            heliosMessagingService.publishToTopic(messageKeys);
            startVideoCall(roomName, TURN_URL, TURN_user, TURN_credential, STUN_URL, API_endpoint);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Settings", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        showSettingsVideoCall();
                    }
                });
        dialogRoomVideoCall = builder.create();
        dialogRoomVideoCall.show();
        //builder.show();
    }

    private void showSettingsVideoCall(){
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Settings");

        // Set up the input
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, turnUrls);
        textViewTurn = new AutoCompleteTextView(this);
        textViewTurn.setAdapter(adapter1);
        textViewTurn.setInputType(InputType.TYPE_CLASS_TEXT);
        if(TURN_URL != null && TURN_URL != ""){
            textViewTurn.setText(TURN_URL);
        }else {
            textViewTurn.setText(R.string.turn);
        }
        textViewTurn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });
        TextView tTurn = new TextView(this);
        tTurn.setText("TURN_URL" + ":");
        layout.addView(tTurn);
        layout.addView(textViewTurn);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, userUrls);
        textViewTurnUser = new AutoCompleteTextView(this);
        textViewTurnUser.setAdapter(adapter2);
        textViewTurnUser.setInputType(InputType.TYPE_CLASS_TEXT);
        if(TURN_user != null && TURN_user != ""){
            textViewTurnUser.setText(TURN_user);
        }else {
            textViewTurnUser.setText(R.string.user_turn);
        }
        textViewTurnUser.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });
        TextView tuser = new TextView(this);
        tuser.setText("TURN user" + ":");
        layout.addView(tuser);
        layout.addView(textViewTurnUser);

        textViewTurnCredential = new AutoCompleteTextView(this);
        //textView.setAdapter(adapter);
        textViewTurnCredential.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if(TURN_credential != null && TURN_credential != ""){
            textViewTurnCredential.setText(TURN_credential);
        }else {
            textViewTurnCredential.setText(R.string.credential_turn);
        }
        textViewTurnCredential.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });
        TextView tpass = new TextView(this);
        tpass.setText("TURN credential" + ":");
        layout.addView(tpass);
        layout.addView(textViewTurnCredential);

        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, stunUrls);
        textViewStun = new AutoCompleteTextView(this);
        textViewStun.setAdapter(adapter3);
        textViewStun.setInputType(InputType.TYPE_CLASS_TEXT);
        if(STUN_URL != null && STUN_URL != ""){
            textViewStun.setText(STUN_URL);
        }else {
            textViewStun.setText(R.string.stun);
        }
        textViewStun.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });
        TextView tStun = new TextView(this);
        tStun.setText("STUN_URL" + ":");
        layout.addView(tStun);
        layout.addView(textViewStun);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, apiUrls);
        textViewApiEndpoint = new AutoCompleteTextView(this);
        textViewApiEndpoint.setAdapter(adapter4);
        textViewApiEndpoint.setInputType(InputType.TYPE_CLASS_TEXT);
        if(API_endpoint != null && API_endpoint != ""){
            textViewApiEndpoint.setText(API_endpoint);
        }else {
            textViewApiEndpoint.setText(R.string.signaling);
        }
        textViewApiEndpoint.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                ((AutoCompleteTextView) v).showDropDown();
            }
        });
        TextView tapi = new TextView(this);
        tapi.setText("Signaling URL" + ":");
        layout.addView(tapi);
        layout.addView(textViewApiEndpoint);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            TURN_URL = textViewTurn.getText().toString();
            TURN_user = textViewTurnUser.getText().toString();
            TURN_credential = textViewTurnCredential.getText().toString();
            STUN_URL = textViewStun.getText().toString();
            API_endpoint = textViewApiEndpoint.getText().toString();


            String turnUrl = textViewTurn.getText().toString();
            if (!turnUrls.contains(turnUrl)) {
                turnUrls.add(turnUrl);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(TURN_URLS_KEY, String.join(TURN_URLS_SEPARATOR, turnUrls));
                editor.apply();
            }

            String userUrl = textViewTurnUser.getText().toString();
            if (!userUrls.contains(userUrl)) {
                userUrls.add(userUrl);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(USER_URLS_KEY, String.join(USER_URLS_SEPARATOR, userUrls));
                editor.apply();
            }

            String stunUrl = textViewStun.getText().toString();
            if (!stunUrls.contains(stunUrl)) {
                stunUrls.add(stunUrl);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(STUN_URLS_KEY, String.join(STUN_URLS_SEPARATOR, stunUrls));
                editor.apply();
            }

            String apiUrl = textViewApiEndpoint.getText().toString();
            if (!apiUrls.contains(apiUrl)) {
                apiUrls.add(apiUrl);

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(API_URLS_KEY, String.join(API_URLS_SEPARATOR, apiUrls));
                editor.apply();
            }

            dialog.dismiss();
            dialogRoomVideoCall.show();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.cancel();
            dialogRoomVideoCall.show();
        });

        builder.show();
    }

    public void startVideoCall(String roomName, String turn_url, String turn_user, String turn_credential, String stun_url, String api_endpoint) {
        Intent videoCallIntent = new Intent(MainActivity.this, VideoCallActivity.class);
        videoCallIntent.putExtra("room_name", roomName);
        videoCallIntent.putExtra("TURN_URL", turn_url);
        videoCallIntent.putExtra("TURN_user", turn_user);
        videoCallIntent.putExtra("TURN_credential", turn_credential);
        videoCallIntent.putExtra("STUN_URL", stun_url);
        videoCallIntent.putExtra("API_endpoint", api_endpoint);

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
