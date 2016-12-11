package com.chipcerio.tambayan.category;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chipcerio.tambayan.R;
import com.chipcerio.tambayan.model.pojo.Event;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CategoryActivity extends AppCompatActivity implements AuthStateListener {

    private static final String TAG = "CategoryActivity";

    public static final String EXTRA_CATEGORY = "extra:category";

    private RecyclerView mEvents;

    private FirebaseAuth mAuth;

    private FirebaseRecyclerAdapter<Event, EventsHolder> mAdapter;

    private DatabaseReference mEventsRef;

    private String mSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mSelected = getIntent().getStringExtra(EXTRA_CATEGORY);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mSelected);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        mEventsRef = ref.child("events");

        mEvents = (RecyclerView) findViewById(R.id.rvCategory);

        LinearLayoutManager mgr = new LinearLayoutManager(this);
        mEvents.setHasFixedSize(false);
        mEvents.setLayoutManager(mgr);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isSignedIn()) {
            attachRecyclerViewAdapter();
        } else {
            signInAnonymously();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth != null) {
            mAuth.removeAuthStateListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attachRecyclerViewAdapter() {
        Query query = mEventsRef.orderByChild("type").equalTo(mSelected);
        mAdapter = new FirebaseRecyclerAdapter<Event, EventsHolder>(
                Event.class,
                R.layout.item_event_preview,
                EventsHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(EventsHolder holder, Event model, int pos) {
                holder.setText(model.getTitle());
                holder.setTime(model.getDate());
                holder.setImage(model.getImage());
            }
        };
        mEvents.setAdapter(mAdapter);
    }

    private boolean isSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            attachRecyclerViewAdapter();
                        }
                    }
                });
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
    }

    public static class EventsHolder extends RecyclerView.ViewHolder {

        private final TextView mTitle;
        private final TextView mTime;
        private final ImageView mImage;
        private final Context mContext;

        public EventsHolder(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.txt_title);
            mTime = (TextView) itemView.findViewById(R.id.txt_time);
            mImage = (ImageView) itemView.findViewById(R.id.img_event);
            mContext = itemView.getContext();
        }

        public void setText(String name) {
            mTitle.setText(name);
        }

        public void setTime(String date) {
            mTime.setText(date);
        }

        public void setImage(String imageUrl) {

            StorageReference ref = FirebaseStorage.getInstance()
                    .getReferenceFromUrl("gs://tambayan-ios-rey.appspot.com/")
                    .child(imageUrl);

            Glide.with(mContext)
                    .using(new FirebaseImageLoader())
                    .load(ref)
                    .centerCrop()
                    .crossFade()
                    .into(mImage);
        }
    }

}
