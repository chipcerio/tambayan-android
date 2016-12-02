package com.chipcerio.tambayan.categorylist;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.chipcerio.tambayan.R;
import com.chipcerio.tambayan.category.CategoryActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class CategoryListActivity extends AppCompatActivity implements AuthStateListener {

    private static final String TAG = "CategoryListActivity";

    private FirebaseAuth mAuth;

    private RecyclerView mCategories;

    private DatabaseReference mRef;

    private DatabaseReference mCategoriesRef;

    private FirebaseRecyclerAdapter<String, CategoriesHolder> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);


        mRef = FirebaseDatabase.getInstance().getReference();
        mCategoriesRef = mRef.child("categories");

        mCategories = (RecyclerView) findViewById(R.id.rvCategories);

        LinearLayoutManager mgr = new LinearLayoutManager(this);
        mCategories.setHasFixedSize(false);
        mCategories.setLayoutManager(mgr);
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

    private void attachRecyclerViewAdapter() {
        Query query = mCategoriesRef.orderByValue();
        mAdapter = new FirebaseRecyclerAdapter<String, CategoriesHolder>(
                String.class,
                android.R.layout.simple_list_item_1,
                CategoriesHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(CategoriesHolder holder, String name, int pos) {
                holder.setText(name);
            }
        };
        mCategories.setAdapter(mAdapter);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

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

    public static class CategoriesHolder extends RecyclerView.ViewHolder {

        private final TextView mText;

        public CategoriesHolder(View itemView) {
            super(itemView);
            mText = (TextView) itemView.findViewById(android.R.id.text1);
        }

        public void setText(final String text) {
            mText.setText(text);
            mText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "CategoriesHolder.onClick: " + text);
                    Intent intent = new Intent(view.getContext(), CategoryActivity.class);
                    intent.putExtra(CategoryActivity.EXTRA_CATEGORY, text);
                    view.getContext().startActivity(intent);
                }
            });
        }

    }

}