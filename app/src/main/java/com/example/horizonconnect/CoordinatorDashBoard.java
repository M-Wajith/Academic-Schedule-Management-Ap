package com.example.horizonconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class CoordinatorDashBoard extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private ArrayList<String> messagesList;
    private ImageView imageView;
    private Button addScheduleButton;

    private DatabaseReference messagesRef;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_dash_board);

        editTextMessage = findViewById(R.id.chatCor);
        buttonSend = findViewById(R.id.sendCor);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        imageView = findViewById(R.id.imageView);
        addScheduleButton = findViewById(R.id.addSchedule);

        messagesList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messagesList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("timetable");

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    messagesRef.push().setValue(message);
                    editTextMessage.setText("");
                }
            }
        });

        addScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        loadMessages();
        loadScheduleImage();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        StorageReference fileRef = storageRef.child("timetable.jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully
            loadScheduleImage();
        }).addOnFailureListener(e -> {
            // Handle any errors
        });
    }

    private void loadMessages() {
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String message = messageSnapshot.getValue(String.class);
                    messagesList.add(message);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void loadScheduleImage() {
        storageRef.child("timetable.jpg").getDownloadUrl().addOnSuccessListener(uri -> {
            Picasso.get()
                    .load(uri)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loaded successfully
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Picasso", "Failed to load image", e);
                        }
                    });
        }).addOnFailureListener(e -> {
            Log.e("Load Image", "Failed to load image", e);
        });
    }

    public void userAdd(View view) {
        Intent i = new Intent(getApplicationContext(), AddUser.class);
        startActivity(i);
    }
}
