package com.example.DetectiveAssistent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

public class VideoDialogFragment extends DialogFragment {

    private static final String VIDEO_PATH_KEY = "videoPath";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_video, container, false);

        // Retrieve video path from arguments
        String videoPath = getArguments().getString(VIDEO_PATH_KEY);

        if (videoPath != null) {
            // Add VideoFragment to the dialog and pass the video path
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            VideoFragment videoFragment = new VideoFragment();
            videoFragment.setVideoPath(videoPath);
            transaction.replace(R.id.fragment_container, videoFragment);
            transaction.commit();
        } else {
            // Display an error message to the user
            Toast.makeText(getContext(), "Error: Video path is null", Toast.LENGTH_SHORT).show();
            // Close the dialog fragment
            dismiss();
        }

        return view;
    }
}
