package com.example.halalyon.mediaaccess;

/**
 * Created by halalyon on 28/09/15.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

//https://github.com/thecodepath/android_guides/wiki/Using-Hardware%2C-Sensors-and-Device-data
public class MainActivity extends Activity {

    public final String APP_TAG = "MobileComputingTutorial";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public final static int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 1035;
    public final static int PICK_PHOTO_CODE = 1046;
    public final static int PICK_VIDEO_CODE = 1047;

    public String photoFileName = "photo.jpg";
    public String videoFileName = "video.mp4";
    public String audioFileName = "audio.3gp";

    final MediaRecorder mediaRecorder = new MediaRecorder();
    final MediaPlayer mediaPlayer = new MediaPlayer();
    int length = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);


    }

    public void onLoadPhotoClick(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Bring up gallery to select a photo
        startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    public void onLoadVideoClick(View view) {
        // Create intent for picking a video from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        // Bring up gallery to select a video
        startActivityForResult(intent, PICK_VIDEO_CODE);
    }

    public void onTakePhotoClick(View v) {
        // create Intent to take a picture and return control to the calling
        // application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri(photoFileName)); // set file name

        // Start the image capture intent to take photo
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void onRecordVideoClick(View v) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            File mediaFile = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, getFileUri(videoFileName));
            startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    public void onRecordAudioClick(View view) {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile("/storage/emulated/0/Pictures/MobileComputingTutorial/audio.3gp");//file:///storage/emulated/0/Pictures/MobileComputingTutorial/audio.3gp
            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }else {
            Toast.makeText(this, "This device don't have a microphone!", Toast.LENGTH_LONG).show();
        }
    }

    public void onStopRecordAudioClick(View view){

        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
    }

    public void onResetRecordAudioClick(View view){

        mediaRecorder.reset();


    }

    public void onPlayAudioClick(View view) {

        if (mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }else{
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(getFileUri(audioFileName).toString());
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public void onPauseAudioClick(View view){

        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            length = mediaPlayer.getCurrentPosition();
        }else {
            mediaPlayer.seekTo(length);
            mediaPlayer.start();
        }

    }

    public void onRestartAudioClick(View view){
        mediaPlayer.reset();
    }

    // Returns the Uri for a photo/media stored on disk given the fileName
    public Uri getFileUri(String fileName) {
        // Get safe storage directory for photos
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator
                + fileName));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        final VideoView mVideoView = (VideoView) findViewById(R.id.videoview);
        ImageView ivPreview = (ImageView) findViewById(R.id.photopreview);

        mVideoView.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);


        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenPhotoUri = getFileUri(photoFileName);
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(takenPhotoUri
                        .getPath());
                // Load the taken image into a preview
                ivPreview.setImageBitmap(takenImage);
                ivPreview.setVisibility(View.VISIBLE);
                System.out.println(takenPhotoUri.toString());
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_PHOTO_CODE) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(
                            this.getContentResolver(), photoUri);
                    // Load the selected image into a preview

                    ivPreview.setImageBitmap(selectedImage);
                    ivPreview.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

        } else if (requestCode == PICK_VIDEO_CODE) {
            if (resultCode == RESULT_OK) {
                Uri videoUri = data.getData();

                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(videoUri);
//				MediaController mediaController = new MediaController(this);
//				mediaController.setAnchorView(mVideoView);
//				mVideoView.setMediaController(mediaController);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });

            }

        }else if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri takenVideoUri = getFileUri(videoFileName);
                mVideoView.setVisibility(View.VISIBLE);
                mVideoView.setVideoURI(takenVideoUri);
//				MediaController mediaController = new MediaController(this);
//				mediaController.setAnchorView(mVideoView);
//				mVideoView.setMediaController(mediaController);
                mVideoView.requestFocus();
                mVideoView.setOnPreparedListener(new OnPreparedListener() {
                    // Close the progress bar and play the video
                    public void onPrepared(MediaPlayer mp) {
                        mVideoView.start();
                    }
                });
            }
        }
    }
}