package com.hytham.insomania20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {


    private FirebaseAuth auth;
    private AppCompatButton doingB,thinkingB, goingB, feelingB, sendB;
    private TextView usernameTV;
    private EditText messageET;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private RelativeLayout layout;
    private DatabaseReference ref, postReference;
    private String currentUserID, post, saveCurrentDate, saveCurrentTime, postRandomName, userProfileImage, currentUsername;
    private int number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);



        layout = findViewById(R.id.activity_post);

        progressBar = new ProgressBar(this,null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        layout.addView(progressBar,params);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setVisibility(View.GONE);





        auth = FirebaseAuth.getInstance();

        currentUserID = auth.getCurrentUser().getUid();

        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        number =0;
        doingB = findViewById(R.id.doing);
        thinkingB = findViewById(R.id.thinking);
        goingB = findViewById(R.id.going);
        feelingB = findViewById(R.id.feeling);

        fab = findViewById(R.id.picturepostsend);
        sendB = findViewById(R.id.send_post);

        usernameTV = findViewById(R.id.post_username);
        messageET = findViewById(R.id.message);

        messageET.setVisibility(View.GONE);
        sendB.setVisibility(View.GONE);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageactivity = new Intent(PostActivity.this, PhotoActivity.class);
                startActivity(imageactivity);
            }
        });

        doingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageET.setVisibility(View.VISIBLE);
                sendB.setVisibility(View.VISIBLE);

                number = 1;
                sendB.setBackground(doingB.getBackground());

                messageET.setHint("What are you doing?");
                messageET.requestFocus();
            }
        });

        goingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageET.setVisibility(View.VISIBLE);
                sendB.setVisibility(View.VISIBLE);
                number=2;

                sendB.setBackground(goingB.getBackground());

                messageET.setHint("Where are you going?");
                messageET.requestFocus();
            }
        });

        thinkingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageET.setVisibility(View.VISIBLE);
                sendB.setVisibility(View.VISIBLE);

                number = 3;
                sendB.setBackground(thinkingB.getBackground());

                messageET.setHint("What are you thinking about?");
                messageET.requestFocus();
            }
        });

        feelingB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageET.setVisibility(View.VISIBLE);
                sendB.setVisibility(View.VISIBLE);
                number = 4;
                sendB.setBackground(feelingB.getBackground());

                messageET.setHint("What are you feeling?");
                messageET.requestFocus();
            }
        });



        ref.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("username")) {

                        currentUsername = dataSnapshot.child("username").getValue().toString();

                        if (!currentUsername.isEmpty()) {
                            usernameTV.setText(currentUsername+" is ");
                        }
                    }
                    if (dataSnapshot.hasChild("profileimage")){

                        userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    }

                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String action;
                switch (number){

                    case 2:
                        action = "going to ";
                        break;
                    case 3:
                        action = "thinking about ";
                        break;
                    case 4:
                        action = "feeling ";
                        break;

                        default:
                            action="";
                }
                if (messageET.getText().toString().isEmpty()){
                    Toast.makeText(PostActivity.this, "Write something first, eh", Toast.LENGTH_SHORT).show();
                }
                else {


                    post = usernameTV.getText().toString() + action + messageET.getText().toString();

                    storeIntoFirebaseDatabase();
                }


            }
        });


    }

    private void storeIntoFirebaseDatabase() {

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        Calendar date = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new SimpleDateFormat( "yy-MM-dd");
        saveCurrentDate = currentDate.format(date.getTime());

        Calendar time = Calendar.getInstance();
        java.text.SimpleDateFormat currentTime = new SimpleDateFormat( "HH:mm:ss");
        saveCurrentTime = currentTime.format(time.getTime());
        postRandomName = saveCurrentDate+saveCurrentTime;

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){


                    HashMap postMap = new HashMap();

                    postMap.put("uid",currentUserID);
                    postMap.put("username",currentUsername);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("profileimage",userProfileImage);
                    postMap.put("post",post);

                    postReference.child(postRandomName+currentUserID).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                Toast.makeText(PostActivity.this, "Successfully posted!", Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                            }
                            else
                            {
                                progressBar.setVisibility(View.GONE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                Toast.makeText(PostActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
