package com.optic.simpleblog.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.simpleblog.R;
import com.optic.simpleblog.includes.Toolbar;
import com.optic.simpleblog.utils.CompressorBitmapImage;
import com.optic.simpleblog.utils.FileUtil;
import com.optic.simpleblog.utils.RandomName;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private ImageButton imageButton;
    private Button btnSubmit;
    private EditText editTextTitle;
    private EditText editTextDescription;

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mCurrentUserId;
    private StorageReference storageReference;
    private DatabaseReference databaseReferenceBlog;
    private DatabaseReference databaseReferenceUsers;
    private ProgressDialog progressDialog;

    // COMPRESION DE IMAGENES
    private Uri mImageUri;
    private static final int GALLERY_REQUEST = 1;
    private File actualImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar.showToolbar(this, getResources().getString(R.string.new_post_text), true);


        // FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mCurrentUserId = firebaseAuth.getCurrentUser().getUid();

        storageReference = FirebaseStorage.getInstance().getReference();
        // Creando un nodo en la base de datos llamado Blog donde se almacenara los datos del Post
        databaseReferenceBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.getUid());


        editTextTitle = (EditText) findViewById(R.id.editTextTitlePost);
        editTextDescription =  (EditText) findViewById(R.id.editTextDescriptionPost);
        btnSubmit = (Button) findViewById(R.id.btnSubmitPost);
        progressDialog = new ProgressDialog(this);

        imageButton = (ImageButton) findViewById(R.id.imageButtonPost);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Guardar en firebase
                savePostInfo();
            }
        });
    }

    /*
     * METODO QUE ALMACENA LA INFORMACION DEL POST EN DATABASE
     */
    private void savePostInfo() {


        progressDialog.setMessage("Posting to blog...");
        progressDialog.show();


        final String title = editTextTitle.getText().toString();
        final String description = editTextDescription.getText().toString();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && actualImage != null) {

            // ALMACENO DATOS DEL POSTS Y EL USERNAME DEL USUARIO QUE LO CREO
            databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final DatabaseReference newPostReference = databaseReferenceBlog.push();

                    final String post_id = newPostReference.getKey();
                    String username = dataSnapshot.child("name").getValue().toString();

                    Map<String, Object> postMap = new HashMap<>();
                    postMap.put("title", title);
                    postMap.put("description", description);
                    postMap.put("uid", mCurrentUserId);
                    postMap.put("timestamp", ServerValue.TIMESTAMP);
                    postMap.put("username", username);

                    newPostReference.setValue(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // ALMACENANDO LA IMAGEN DEL POST
                            saveImagePost(post_id);
                        }
                    });


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        else {
            Toast.makeText(PostActivity.this, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();

        }
    }

    /*
     * ALMACENAR LA IMAGEN DEL POST EN FIREBASE STORAGE
     */
    private void saveImagePost(final String post_id) {

        // COMPRIMIENDO LA IMAGEN Y TRANFORMANDOLA A BITMAP
        byte[] thumb_image = CompressorBitmapImage.getBitmapImageCompress(this, actualImage.getPath(), 300,300);

        StorageReference thumb_filepath = storageReference.child("Blog_Images").child(post_id + ".jpg");

        UploadTask uploadTask = thumb_filepath.putBytes(thumb_image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // ALMACENANDO LA URL DE LA IMAGEN EN DATABASE
                final String imageUri = taskSnapshot.getDownloadUrl().toString();

                Map<String, Object> imageMap = new HashMap<>();
                imageMap.put("image", imageUri);

                final DatabaseReference newPostReference = databaseReferenceBlog.child(post_id);
                newPostReference.updateChildren(imageMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                    }
                });

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            try {
                actualImage = FileUtil.from(this, data.getData());
                imageButton.setImageBitmap(BitmapFactory.decodeFile(actualImage.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
