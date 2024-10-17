package com.example.DetectiveAssistent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.ai.client.generativeai.java.ChatFutures;

import java.util.concurrent.Executor;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.*;
import com.google.ai.client.generativeai.type.*;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;
import java.util.concurrent.Executors;

public class ChatFragment extends Fragment {

    private EditText userInputEditText;
    private Button sendMessageButton;
    private TextView chatTextView;

    private ChatFutures chat;

    private final Executor executor = Executors.newSingleThreadExecutor();; // Define your executor

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        userInputEditText = rootView.findViewById(R.id.userInputEditText);
        sendMessageButton = rootView.findViewById(R.id.sendMessageButton);
        chatTextView = rootView.findViewById(R.id.chatTextView);

        // Initialize chat
        initializeChat();

        // Set click listener for send message button
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userInputEditText.getText().toString();
                sendMessageToChat(message);
                userInputEditText.setText(""); // Clear input field after sending message
            }
        });

        return rootView;
    }

    private void initializeChat() {
        // Initialize generative model
        GenerativeModel gm = new GenerativeModel(/* modelName */ "gemini-pro", BuildConfig.apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Create previous chat history for context
        Content.Builder userContentB = new Content.Builder();
        userContentB.setRole("user");
        userContentB.addText("Hello, I have 2 dogs in my house.");
        Content userContent = userContentB.build();
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("model");
        userMessageBuilder.addText("Great to meet you. What would you like to know?");
        Content userMessage = userMessageBuilder.build();

        List<Content> history = Arrays.asList(userContent, userMessage);

        // Start chat
        chat = model.startChat(history);
    }

    private void sendMessageToChat(String message) {
        // Create user message
        Content.Builder userMessageBuilder = new Content.Builder();
        userMessageBuilder.setRole("user");
        userMessageBuilder.addText(message);
        Content userMessage = userMessageBuilder.build();

        // Send message to chat
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(userMessage);

        // Handle response
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String resultText = result.getText();
                displayChatResponse(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                // Run on the main/UI thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Show Toast message on the main thread
                        Toast.makeText(getContext(), "Error occurred: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
                t.printStackTrace();
            }

        }, executor);
    }

    private void displayChatResponse(final String response) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update UI with chat response
                chatTextView.append(response + "\n");
            }
        });
    }

}