package com.mashv.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.Resource;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private static final int PICTURE_RESULT = 42;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    TravelDeal deal;
    ImageView mImageView;
    private TravelDeal mDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        txtTitle = (EditText) findViewById(R.id.txtTitle);
        txtDescription = (EditText) findViewById(R.id.txtDescription);
        txtPrice = (EditText) findViewById(R.id.txtPrice);
        mImageView = (ImageView) findViewById(R.id.image);

        Intent intent = getIntent();
        mDeal = (TravelDeal) intent.getSerializableExtra("Deal");
        if(mDeal == null){
            mDeal = new TravelDeal();
        }
        this.deal = mDeal;
        txtTitle.setText(mDeal.getTitle());
        txtDescription.setText(mDeal.getDescription());
        txtPrice.setText(mDeal.getPrice());
        showImage(mDeal.getImageUrl());
        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent,
                        "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                backToList();
                return true;
//            case R.id.insert_menu:
//                //deleteDeal();
//               // saveDeal();
//                insertReady();
//                Toast.makeText(this, "Please Enter New Deal ", Toast.LENGTH_LONG).show();

                //backToList();
//                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
//            menu.findItem(R.id.insert_menu).setVisible(true);
            enabledEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
//            menu.findItem(R.id.insert_menu).setVisible(false);
            enabledEditTexts(false);
            findViewById(R.id.btnImage).setVisibility(View.GONE);

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICTURE_RESULT && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            deal.setImageUrl(url);
                            showImage(url);
                            Log.d("Url: ", url);
                        }
                    });
                    String url = taskSnapshot.getStorage().getDownloadUrl().toString();
                    String pictureName = taskSnapshot.getStorage().getPath();
                    deal.setImageUrl(url);
                    deal.setImageName(pictureName);
                    Log.d("Name: ", pictureName);
                    showImage(url);

                }
            });
        }
    }

    private void saveDeal() {



        this.deal.setTitle(this.txtTitle.getText().toString());
        this.deal.setDescription(this.txtDescription.getText().toString());

        this.deal.setPrice(this.txtPrice.getText().toString());

        if (this.deal.getId() == null) {
            this.mDatabaseReference.push().setValue(this.deal);

        } else {
            this.mDatabaseReference.child(this.deal.getId()).setValue(this.deal);
        }
    }

    private void deleteDeal() {


        TravelDeal travelDeal = this.deal;
        if (travelDeal == null) {
            Toast.makeText(this, "Please save the deal before deleting",Toast.LENGTH_LONG).show();
            return;
        }
        this.mDatabaseReference.child(travelDeal.getId()).removeValue();
        //Log.d("image name", this.deal.getImageName());
        if (this.deal.getImageName() != null && !this.deal.getImageName().isEmpty()) {
            Log.d("image name", this.deal.getImageName());
            FirebaseUtil.mStorage.getReference().child(this.deal.getImageName()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                public void onSuccess(Void voidR) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                public void onFailure(@NonNull Exception exc) {
                    Log.d("Delete Image", exc.getMessage());
                }
            });
        }
    }

    private void backToList() {

        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    private void clean() {
        txtTitle.setText("");
        txtPrice.setText("");
        txtDescription.setText("");
        txtTitle.requestFocus();

    }



    private void enabledEditTexts(boolean isEnabled) {
        txtTitle.setFocusable(isEnabled);
        txtTitle.setClickable(isEnabled);
        txtTitle.setTextIsSelectable(isEnabled);
        txtTitle.setCursorVisible(isEnabled);
        txtTitle.setFocusableInTouchMode(isEnabled);
        txtTitle.setActivated(isEnabled);


        txtDescription.setFocusable(isEnabled);
        txtDescription.setClickable(isEnabled);
        txtDescription.setTextIsSelectable(isEnabled);
        txtDescription.setCursorVisible(isEnabled);
        txtDescription.setFocusableInTouchMode(isEnabled);
        txtDescription.setActivated(isEnabled);
        txtDescription.setPressed(isEnabled);


        txtPrice.setFocusable(isEnabled);
        txtPrice.setClickable(isEnabled);
        txtPrice.setTextIsSelectable(isEnabled);
        txtPrice.setCursorVisible(isEnabled);
        txtPrice.setFocusableInTouchMode(isEnabled);
        txtPrice.setActivated(isEnabled);
        txtPrice.setPressed(isEnabled);



    }


    public void showImage(String url){
        if(url != null && !url.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;

            Picasso.with(this)
                    .load(url)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(this.mImageView);
        }
    }

}
