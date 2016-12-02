package com.chipcerio.tambayan.view.activity;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.chipcerio.tambayan.R;
import com.chipcerio.tambayan.util.Const;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @BindView(R.id.rvEvents)
    RecyclerView mRecyclerView;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRootRef = storage.getReferenceFromUrl(Const.Firebase.STORAGE_URL);

        // Create a child reference
        // eventImagesRef now points to "eventImage"
        StorageReference eventImageRef = storageRootRef.child("eventImage");

        String path = Environment.getExternalStorageDirectory().getPath();
        String downloadDir = path + "/Download";
        Log.d(TAG, "path: " + downloadDir);

        File dir = new File(downloadDir);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Log.d(TAG, "file: " + files[i].getName());
                }
            }

        }

//        try {
//            InputStream stream = new FileInputStream(new File());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        // Child references can also take paths
        // spaceRef now points to "users/me/profile.png
        // imagesRef still points to "images"
        StorageReference imageRef = eventImageRef.child("1762E243-F372-49EA-9109-94B50CF3A5BF");

        /*
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(imageRef)
                .centerCrop()
                .into(mImageView);
        */

        /*
        // create event
        Event event = new Event();

        // database upload
        event.setDate( Long.toString(System.currentTimeMillis()) );
        event.setDescription("Lorem ipsum dolor");
        event.setLocation("Cebu City");
        event.setImage("http://google.com/image.jpg");
        event.setPrice("500");
        event.setTitle("Brand New Event");


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("events").setValue(event);


        // storage upload
        event.setImage("");
        */
    }
}
