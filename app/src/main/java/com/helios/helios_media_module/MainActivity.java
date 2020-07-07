package com.helios.helios_media_module;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import eu.h2020.helios_social.modules.videocall.VideoCallActivity;
import eu.h2020.helios_social.modules.filetransfer.FileTransferActivity;
import eu.h2020.helios_social.modules.videoplayer.VideoPlayerActivity;
import eu.h2020.helios_social.modules.livevideostreaming.LiveVideoStreamingActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final int ALL_PERMISSIONS_CODE = 1;
    private String dialog_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mainButton1 = (Button)findViewById(R.id.main_button1);
        mainButton1.setOnClickListener((View.OnClickListener) this);
        Button mainButton2 = (Button)findViewById(R.id.main_button2);
        mainButton2.setOnClickListener((View.OnClickListener) this);
        Button mainButton3 = (Button)findViewById(R.id.main_button3);
        mainButton3.setOnClickListener((View.OnClickListener) this);
        Button mainButton4 = (Button)findViewById(R.id.main_button4);
        mainButton4.setOnClickListener((View.OnClickListener) this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, ALL_PERMISSIONS_CODE);
        }

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.main_button1: {
                Intent liveVideoStreamingIntent = new Intent(MainActivity.this, LiveVideoStreamingActivity.class);
                MainActivity.this.startActivity(liveVideoStreamingIntent);
                break;
            }

            case  R.id.main_button2: {

                //TODO: generate a floating window to introduce the room_name.
                show_dialog();

                break;
            }

            case  R.id.main_button3: {
                Intent videoPlayerIntent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                //TODO: Change this hardcoded uri to the input of a text field or something like that
                videoPlayerIntent.putExtra("URI", getString(R.string.hls_uri));
                MainActivity.this.startActivity(videoPlayerIntent);
                break;
            }

            case  R.id.main_button4: {
                Intent fileTransferIntent = new Intent(MainActivity.this, FileTransferActivity.class);
                MainActivity.this.startActivity(fileTransferIntent);
                break;
            }

        }
    }

    private void show_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.room_dialog_title);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.room_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog_Text = input.getText().toString();
                Intent videoCallIntent = new Intent(MainActivity.this, VideoCallActivity.class);
                videoCallIntent.putExtra("room_name", dialog_Text);
                MainActivity.this.startActivity(videoCallIntent);
            }
        });
        builder.setNegativeButton(R.string.room_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
