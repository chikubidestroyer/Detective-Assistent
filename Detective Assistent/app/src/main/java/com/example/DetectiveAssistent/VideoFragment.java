package com.example.DetectiveAssistent;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class VideoFragment extends Fragment implements View.OnTouchListener {

    private VideoView videoView;
    private String videoPath;
    private float dX, dY; // Delta values for touch event

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        // Find the VideoView
        videoView = view.findViewById(R.id.videoView);

        // Set touch listener for dragging
        videoView.setOnTouchListener(this);

        // Check if videoPath is not null or empty
        if (videoPath != null && !videoPath.isEmpty()) {
            // Set up video playback asynchronously
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setupVideo();
                }
            }).start();
        }

        return view;
    }

    // Method to set the video path from the activity
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        // If the videoView is already created, update the video path
        if (videoView != null) {
            // Set up video playback asynchronously
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setupVideo();
                }
            }).start();
        }
    }

    // Method to set up video playback
    private void setupVideo() {
        // Check if videoPath is not null or empty
        if (videoPath != null && !videoPath.isEmpty()) {
            // Set the video path
            videoView.setVideoPath(videoPath);

            // Start playback
            videoView.start();
        } else {
            // If videoPath is null or empty, stop any ongoing playback and display an error message
            videoView.stopPlayback();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Get the raw X and Y coordinates of the touch event
        float rawX = event.getRawX();
        float rawY = event.getRawY();

        // Handle touch events for dragging the video fragment
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Store the initial touch position
                dX = v.getX() - rawX;
                dY = v.getY() - rawY;
                break;
            case MotionEvent.ACTION_MOVE:
                // Calculate the new position of the video fragment based on touch movement
                float newX = rawX + dX;
                float newY = rawY + dY;

                // Update the position of the video fragment
                v.setX(newX);
                v.setY(newY);
                break;
            default:
                return false;
        }
        return true;
    }
}
