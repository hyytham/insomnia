package com.hytham.insomania20;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static com.hytham.insomania20.SettingsActivity.GALLERY_PICKING_INT;

public class PhotoActivity extends AppCompatActivity {

    private String description, saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserID;
    private Uri imageUri;
    private AppCompatButton button;
    private AppCompatImageButton image;
    private AppCompatEditText descriptionET;
    private StorageReference postImageRef;
    private FirebaseAuth auth;
    private DatabaseReference userRef, postRef;
    private RelativeLayout layout;
    private  ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        button = findViewById(R.id.picturepostbutton);
        image = findViewById(R.id.selectedImage);
        descriptionET = findViewById(R.id.description);


        layout = findViewById(R.id.activity_photo);

        progressBar = new ProgressBar(this,null, android.R.attr.progressBarStyleLarge);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);

        params.addRule(RelativeLayout.CENTER_IN_PARENT);

        layout.addView(progressBar,params);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
        progressBar.setVisibility(View.GONE);


        postImageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        auth = FirebaseAuth.getInstance();


        currentUserID = auth.getCurrentUser().getUid();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validatePostInfo();

            }
        });


    }

    private void validatePostInfo() {

        description = descriptionET.getText().toString();

        if (imageUri == null){

            Toast.makeText(this, "Choose an image first", Toast.LENGTH_SHORT).show();
        }
        if (description.isEmpty()){

            Toast.makeText(this, "Write something about it", Toast.LENGTH_SHORT).show();
        }
        else{

            storingImageToStorage();
        }



    }

    private void storingImageToStorage() {

        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);



        Calendar date = Calendar.getInstance();
        java.text.SimpleDateFormat currentDate = new SimpleDateFormat( "dd-MM-yy");
        saveCurrentDate = currentDate.format(date.getTime());

        Calendar time = Calendar.getInstance();
        java.text.SimpleDateFormat currentTime = new SimpleDateFormat( "HH:mm");
        saveCurrentTime = currentTime.format(time.getTime());
        postRandomName = saveCurrentDate+saveCurrentTime;





        final StorageReference filePath  = postImageRef.child("Post Images").child(imageUri.getLastPathSegment()+postRandomName+".jpg");


        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUrl = uri.toString();
                        Toast.makeText(PhotoActivity.this, "Image uploaded.", Toast.LENGTH_SHORT).show();
                        savePostInformationToDatabase();
                    }

                });
            }
        });


    }

    private void savePostInformationToDatabase() {



        userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String username = dataSnapshot.child("username").getValue().toString();
                    String userprofileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();

                    postMap.put("uid",currentUserID);
                    postMap.put("username",username);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("profileimage",userprofileImage);
                    postMap.put("post",description);
                    postMap.put("image", downloadUrl);

                    postRef.child(postRandomName+currentUserID).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                Toast.makeText(PhotoActivity.this, "Successfully posted!", Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                            }
                            else
                            {
                                progressBar.setVisibility(View.GONE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                Toast.makeText(PhotoActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
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


    private void openGallery() {

        Intent gallery = new Intent();

        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery,GALLERY_PICKING_INT);

    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(PhotoActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == GALLERY_PICKING_INT && data!=null){

            imageUri = data.getData();
            image.setImageURI(imageUri);


        }



        super.onActivityResult(requestCode, resultCode, data);
    }
}
