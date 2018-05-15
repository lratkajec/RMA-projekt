package hr.helloworld.david.esports;


import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity {

    private EditText searchBar;
    private ListView usersList;
    private String searchString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbarSearchActivity);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchBar = findViewById(R.id.searchEditText);
        usersList = findViewById(R.id.userList);
    }

    @Override
    protected void onStart() {
        super.onStart();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    searchForUser(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void searchForUser(final String searchString) {
        this.searchString = searchString;
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference database = firebaseDatabase.getReference("users");
        Query query = database.orderByChild("searchUsername")
                .startAt(this.searchString)
                .limitToFirst(10);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> urls = new ArrayList<>();
                ArrayList<String> usernames = new ArrayList<>();


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("searchUsername").exists() &&
                            snapshot.child("searchUsername").getValue().toString().startsWith(searchString)) {

                        usernames.add(snapshot.child("username").getValue().toString());
                        if (snapshot.child("photoUrl").exists())
                            urls.add(snapshot.child("photoUrl").getValue().toString());
                    }
                }

                CustomListView customListView = new CustomListView(SearchActivity.this, urls, usernames);
                usersList.setAdapter(customListView);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
