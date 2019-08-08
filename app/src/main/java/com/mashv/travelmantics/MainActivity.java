package com.mashv.travelmantics;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
      ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;

      static String adminCheck;
   // private RecyclerView mRvDeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FirebaseUtil.isAdmin) {
//                    Snackbar.make(view, adminCheck, Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
                  insertNewDeal();
                }
                else {
                    Snackbar.make(view, "Sorry No Specials Available", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                Log.d("Admin status ", Boolean.toString(FirebaseUtil.isAdmin));
            }
        });
    }

    private void insertNewDeal() {
        Intent intent = new Intent(this, DealActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);
        //MenuItem insertMenu = menu.findItem(R.id.insert_menu);
//        if (FirebaseUtil.isAdmin) {
//            menu.findItem(R.id.insert_menu).setVisible(true);
//        } else {
//            menu.findItem(R.id.insert_menu).setVisible(false);
//        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.insert_menu:
//                Intent intent = new Intent(this, DealActivity.class);
//                startActivity(intent);
//                return true;
            case R.id.logout_menu:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                Log.d("Logout", "User Logged Out");
                                FirebaseUtil.attachListener();
                            }
                        });
                FirebaseUtil.detachListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.detachListener();
    }

    @Override
    protected void onResume() {

        super.onResume();
        FirebaseUtil.openFbReference("traveldeals", this);
        RecyclerView recycleDeals = (RecyclerView) findViewById(R.id.list_items);
        final DealAdapter adapter = new DealAdapter();
        recycleDeals.setAdapter(adapter);
        LinearLayoutManager dealsLayoutManager =
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recycleDeals.setLayoutManager(dealsLayoutManager);
        FirebaseUtil.attachListener();
    }

    public void showMenu(String admin) {

         adminCheck = admin;
        //invalidateOptionsMenu();
    }

}
