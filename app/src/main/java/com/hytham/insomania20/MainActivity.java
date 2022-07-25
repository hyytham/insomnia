package com.hytham.insomania20;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth auth;
    private DatabaseReference ref, postsRef;
    private CircleImageView navProfilePic;
    private FloatingActionButton fab;
    private TextView navUsername;
    private String currentUserID;
    private View navView;
    private int recentPostsNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);


        auth = FirebaseAuth.getInstance();


        currentUserID = auth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");




        recentPostsNumber = 15;

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        toolbar =  findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Insomnia");

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_closed);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        recyclerView = findViewById(R.id.all_posts);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);

        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);



        navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        navProfilePic = navView.findViewById(R.id.nav_profile_pic);
        navUsername = navView.findViewById(R.id.nav_username);
        fab = findViewById(R.id.add_new_post_button);







        ref.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    if (dataSnapshot.hasChild("username")) {

                        String navUsernameCurrent = dataSnapshot.child("username").getValue().toString();

                        if (!navUsernameCurrent.isEmpty()) {
                            navUsername.setText(navUsernameCurrent);
                        }
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String navProfilePicCurrent = dataSnapshot.child("profileimage").getValue().toString();

                        Picasso.get().load(navProfilePicCurrent).placeholder(R.drawable.profile).into(navProfilePic);
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                UserMenuSelector(menuItem);

                return false;
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sendUserToPostActivity();

            }
        });

        displayAllUserPosts();

    }

    private void displayAllUserPosts() {

        Query recentPosts = postsRef.limitToLast(recentPostsNumber);
        FirebaseRecyclerOptions <Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>().setQuery(recentPosts,Posts.class).build();

        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                if (model.getUsername().equals("Hytham"))
                {
                    holder.linearLayoutCompat.setBackground(getDrawable(R.drawable.card_view__));
                }
                holder.postTV.setText(model.getPost());

                if (model.getImage()==null){
                    holder.imageViewCompat.setVisibility(View.GONE);
                }

                else
                {Picasso.get().load(model.getImage()).into((holder.imageViewCompat));}

                Picasso.get().load(model.getProfileimage()).into(holder.profilePicView);
                holder.timeAndDateTV.setText(model.getDate()+" "+model.getTime());
            }
//TODO : the downloadURL is not displaying properly in the displayed image view.. should be easy, just follow
                    // the settings activity way
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                PostsViewHolder viewHolder = new PostsViewHolder(view);
                return viewHolder;
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }



    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        TextView postTV, timeAndDateTV;
        LinearLayoutCompat linearLayoutCompat;
        CircleImageView profilePicView;
        ImageView imageViewCompat;
        View view;
        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            postTV = itemView.findViewById(R.id.final_post);
            timeAndDateTV = itemView.findViewById(R.id.time_date);
            profilePicView = itemView.findViewById(R.id.all_posts_profile_pic);
            linearLayoutCompat = itemView.findViewById(R.id.card);
            imageViewCompat = itemView.findViewById(R.id.image_display);
        }
    }

    private void sendUserToPostActivity(){

        Intent postIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(postIntent);


    }



    @Override
    protected void onStart() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null){
            sendUserToLoginActivity();
        }
        else{
            checkUserExistence();
        }
        super.onStart();
    }

    private void checkUserExistence() {
        final String USER_ID = auth.getCurrentUser().getUid();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


               if (!dataSnapshot.hasChild(USER_ID)){
                    sendUserToSettingsActivityFirstTime();
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToSettingsActivityFirstTime() {


        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (toggle.onOptionsItemSelected(item))
        {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item){
       switch( item.getItemId()){
           case R.id.nav_home:
               break;
           case R.id.nav_settings:

               sendUserToSettingsActivity();
               break;
           case R.id.nav_logout:

               auth.signOut();
               sendUserToLoginActivity();

               break;
       }

    }

    private void sendUserToSettingsActivity() {
        Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(intent);
    }
}
// TODO: A container of views on the first tab saved on the device (persisted)
// TODO: A view maker
// TODO: Types of views (text view + picture)