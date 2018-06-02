package hr.helloworld.david.esports;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class FriendListActivity extends AppCompatActivity {

    private User user;
    private ListView friendsList;
    private ProgressBar dialog;
    private CustomListView customListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null){
            user = new User(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail(),
                    firebaseUser.getPhotoUrl());

            user.getUserFriendsUUID();
        }



        setContentView(R.layout.activity_friend_list);

        Toolbar toolbar = findViewById(R.id.toolbarSearchActivity);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.updateFriendsStatus();
                finish();
            }
        });

        friendsList = findViewById(R.id.userFriendsList);

    }

    @Override
    protected void onStart() {
        super.onStart();

        dialog = findViewById(R.id.progressBar);
        dialog.setIndeterminate(true);
        dialog.setVisibility(View.VISIBLE);

        new GetData().execute();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        user.updateFriendsStatus();
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("ConstantConditions")
    private class GetData extends AsyncTask<Void, Void, Void> {

        private ArrayList<String> UUID = new ArrayList<>();
        private ArrayList<String> urls = new ArrayList<>();
        private ArrayList<String> userNames = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... strings) {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
            Query query = databaseReference.orderByKey();

            while (user.friendsUUID.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.d("TAG", e.getMessage());
                }

                if (user.numFriends != -1) break;
            }

            for (String uuid : user.friendsUUID) {
                query.equalTo(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            userNames.add(snapshot.child("username").getValue().toString());
                            urls.add(snapshot.child("photoUrl").getValue().toString());
                            UUID.add(snapshot.child("uuid").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            while ((userNames.isEmpty() && urls.isEmpty() && UUID.isEmpty()) && user.numFriends != 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.d("TAG", e.getMessage());
                }
            }

            customListView = new CustomListView(FriendListActivity.this,
                    urls, userNames, UUID, user);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            while (customListView.getUrls().isEmpty() && customListView.getUserNames().isEmpty()
                    && customListView.getUuid().isEmpty()) {

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.d("TAG", e.getMessage());
                }
                
            }

            dialog.setVisibility(View.GONE);
            friendsList.setAdapter(customListView);
        }
    }
}
