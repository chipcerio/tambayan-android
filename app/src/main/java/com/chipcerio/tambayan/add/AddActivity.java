package com.chipcerio.tambayan.add;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chipcerio.tambayan.R;
import com.chipcerio.tambayan.model.pojo.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks;

public class AddActivity extends AppCompatActivity implements AuthStateListener, PermissionCallbacks {

    private static final String TAG = "AddActivity";

    private static final int RC_DISK_PERM = 100;

    private static final int RC_SETTINGS_SCREEN = 101;

    private static final int PICK_IMAGE = 200;

    private FirebaseAuth mAuth;

    private FirebaseUser mFirebaseUser;

    private DatabaseReference mRootRef;

    private FirebaseStorage mStorage;

    private StorageReference mStorageRef;
    private EditText mTitle;
    private EditText mDesc;
    private EditText mLoc;
    private EditText mPrice;
    private EditText mDateTime;

    private Button mSelectImage;
    private Button mSelectCategory;
    private Map<String, String> categories;

    private List<String> listCategories = new ArrayList<>();
    private AlertDialog dialog;
    private String selectedCategory;
    private StorageReference mImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Event");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mTitle = (EditText) findViewById(R.id.editTextTitle);
        mDesc = (EditText) findViewById(R.id.editTextDesc);
        mLoc = (EditText) findViewById(R.id.editTextLoc);
        mPrice = (EditText) findViewById(R.id.editTextPrice);
        mDateTime = (EditText) findViewById(R.id.editTextDateTime);
        mSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mSelectCategory = (Button) findViewById(R.id.buttonCategory);

        mAuth = FirebaseAuth.getInstance();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readDiskTask();
            }
        });

        getCategories();
        mSelectCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(listCategories);
            }
        });

        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReferenceFromUrl("gs://tambayan-ios-rey.appspot.com/");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RC_SETTINGS_SCREEN:
                Toast.makeText(this, R.string.returned_from_app_settings_to_activity, Toast.LENGTH_SHORT).show();
                break;

            case PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData();
                    String path = getAbsolutePathFromUri(imageUri);
                    uploadImage(path);
                    mSelectImage.setText(path);
                }
                break;

            default:
                break;
        }
    }

    private void uploadImage(String path) {
        // File or Blob
        File file = new File(path);
        Uri uri = Uri.fromFile(file);

        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        try {
            // Upload file and metadata to the path 'images/filename.jpg'
            InputStream stream = new FileInputStream(file);
            UploadTask uploadTask = mStorageRef.child("images/" + uri.getLastPathSegment()).putStream(stream, metadata);

            // Listen for state changes, errors, and completion of the upload.
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                    Log.d(TAG, "UploadTask.onSuccess:" + downloadUrl.getPath());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "UploadTask.onFailure:" + e.getMessage());
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getAbsolutePathFromUri(Uri imageUri) {
        String wholeId = DocumentsContract.getDocumentId(imageUri);
        // Split at colon, use second item in the array
        String id = wholeId.split(":")[1];

        String sel = MediaStore.Images.Media._ID + "=?";
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                sel,
                new String[]{ id },
                null
        );

        int columnIndex = cursor.getColumnIndex(projection[0]);
        String picturePath = "";
        if (cursor.moveToFirst()) {
            picturePath = cursor.getString(columnIndex); // returns null
        }
        cursor.close();

        Log.d(TAG, "imagePath:" + picturePath);

        return picturePath;
    }

    @AfterPermissionGranted(RC_DISK_PERM)
    private void readDiskTask() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Has Permission", Toast.LENGTH_LONG).show();

            Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
            getIntent.setType("image/*");
            Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");

            startActivityForResult(chooserIntent, PICK_IMAGE);

        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_read_storage),
                    RC_DISK_PERM, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
    }

    private void getCategories() {
        mRootRef.child("categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                categories = (Map<String, String>) dataSnapshot.getValue();
                for (Map.Entry<String, String> entry : categories.entrySet()) {
                    Log.d(TAG, "key:" + entry.getKey() + ",value:" + entry.getValue());
                    listCategories.add(entry.getValue());
                }
                Log.d(TAG, "datasnapshot: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showDialog(List<String> list) {
        if (list.isEmpty()) {
            Toast.makeText(this, "No Categories", Toast.LENGTH_LONG).show();
            return;
        }

        String[] categoriesArr = list.toArray(new String[list.size()]);

        dialog = new AlertDialog.Builder(this)
                .setTitle("Categories")
                .setSingleChoiceItems(
                        categoriesArr,
                        android.R.layout.simple_list_item_single_choice,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int which) {
                                String item = (String) dialog.getListView().getAdapter().getItem(which);
                                Log.d(TAG, "which:" + which + " item:" + item);
                                selectedCategory = item;
                            }
                        }
                )
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        mSelectCategory.setText(selectedCategory);
                        d.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        d.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    public void onAddClick(View view) {
        if (mFirebaseUser == null) return;

        String title = "Skateboard";//mTitle.getText().toString();
        String desc = "Biggest Skateboard Event";//mDesc.getText().toString();
        String location = "Cebu City";
        mLoc.getText().toString();
        String price = "250PHP";
        mPrice.getText().toString();
        String datetime = String.valueOf(System.currentTimeMillis());
        String imgUrl = "http://i.imgur.com/SRwSHaH.jpg"; // TODO selected image from sdcard
        mSelectCategory.getText().toString();

        Event event = new Event();
        event.setTitle(title);
        event.setPrice(price);
        event.setLocation(location);
        event.setDescription(desc);
        event.setDate(datetime);
        event.setImage(imgUrl);
        event.setCategory(selectedCategory);

        mRootRef.child("events").push().setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddActivity.this, "Event Successfully Added", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "event successfully added");
                } else {
                    Toast.makeText(AddActivity.this, "Failed Adding Event", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "failed adding event");
                }
            }
        });

        /*
        mRootRef.child("events").orderByChild("type").equalTo("Music").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    Event e = d.getValue(Event.class);
                    Log.d(TAG, "events: " + e.getTitle());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        */
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        mFirebaseUser = firebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());
            Toast.makeText(this, mFirebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "onAuthStateChanged:signed_out:");
            Toast.makeText(this, mFirebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }
}
