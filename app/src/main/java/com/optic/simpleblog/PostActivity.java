package com.optic.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
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
import com.optic.simpleblog.utils.CompressorBitmapImage;
import com.optic.simpleblog.utils.FileUtil;
import com.optic.simpleblog.utils.RandomName;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private ImageButton imageButton;
    private Button btnSubmit;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private DatabaseReference databaseReferenceBlog;
    private DatabaseReference databaseReferenceUsers;
    private ProgressDialog progressDialog;
    private static final int GALLERY_REQUEST = 1;
    // COMPRESION DE IMAGENES
    private Uri mImageUri;
    private File actualImage;
    private File compressedImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

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
                startPosting();
            }
        });
    }

    /*
     * Metodo que permite guardar los datos del post en firebase
     */
    private void startPosting() {

        progressDialog.setMessage("Posting to blog...");


        final String title = editTextTitle.getText().toString().trim();
        final String description = editTextDescription.getText().toString().trim();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && actualImage != null) {
            progressDialog.show();

            // COMPRIMIENDO LA IMAGEN Y TRANFORMANDOLA A BITMAP
            byte[] thumb_image = CompressorBitmapImage.getBitmapImageCompress(this, actualImage.getPath(), 300,300);

            StorageReference thumb_filepath = storageReference.child("Blog_Images").child(RandomName.randomName() + ".jpg");

            UploadTask uploadTask = thumb_filepath.putBytes(thumb_image);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getDownloadUrl() permite obtener la url de la imagen que se acaba de subir al storage de firebase
                    final Uri downloadUri = taskSnapshot.getDownloadUrl();
                    final DatabaseReference newPost = databaseReferenceBlog.push();

                    // Almacenando los datos del blog y el usuario que creo dicho Post

                    databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // DataSnapshot hace referencia al nodo principal del UID del usuario en firebase database
                            newPost.child("title").setValue(title);
                            newPost.child("description").setValue(description);
                            newPost.child("image").setValue(downloadUri.toString());
                            newPost.child("uid").setValue(firebaseUser.getUid());
                            newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        startActivity(new Intent(PostActivity.this, MainActivity.class));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    progressDialog.dismiss();

                }
            });

        }
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
