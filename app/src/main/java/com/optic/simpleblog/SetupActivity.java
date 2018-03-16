package com.optic.simpleblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.simpleblog.utils.CompressorBitmapImage;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class SetupActivity extends AppCompatActivity {

    private ImageButton imageButtonSetup;
    private EditText editTextName;
    private Button btnSetup;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReferenceUsers;
    private StorageReference storageReferenceImages;

    private ProgressDialog progressDialog;

    private Uri imageUri;
    private static final int GALLERY_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        firebaseAuth = FirebaseAuth.getInstance();
        // Creando un nodo en la base de datos llamado Users donde se almacenaran los datos del usuario
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        // Creando un nodo en storage llamado profiles_images donde se almacenaran las imagenes de perfil del usuario
        storageReferenceImages = FirebaseStorage.getInstance().getReference().child("profile_images");

        progressDialog = new ProgressDialog(this);

        imageButtonSetup = (ImageButton) findViewById(R.id.imageButtonSetup);
        editTextName = (EditText) findViewById(R.id.editTextNameSetup);
        btnSetup = (Button) findViewById(R.id.btnSetup);

        // -----------------ALMACENANDO IMAGEN DEL PERFIL DE USUARIO------------

        imageButtonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });

    }



    /*
     * Almacenando la imagen del usuario en firebase storage y si todo sale bien se creara
     * nombre de usuario e imagen en la base de datos de firebase en el nodo Users
     */

    private void startSetupAccount() {
        final String name = editTextName.getText().toString().trim();

        // Obteniendo el usuario que esta autenticado en la aplicacion
        final String user_id = firebaseAuth.getCurrentUser().getUid();

        if(!TextUtils.isEmpty(name) && imageUri != null) {
            progressDialog.setMessage("Finished setup...");
            progressDialog.show();

            // Obteniendo imagen bitmap comprimida
            final byte[] thumb_byte = CompressorBitmapImage.getBitmapImageCompress(this, imageUri.getPath(), 200, 200);


            // Almacenado la imagen bitmap comprimida en storage de firebase
            StorageReference filePath = storageReferenceImages.child(user_id + ".jpg");
            final StorageReference thumb_filePath = storageReferenceImages.child("thumbs").child(user_id + ".jpg");

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Obteniendo la Url de la imagen almacenada en storage
                    final String image_downloadUri = taskSnapshot.getDownloadUrl().toString();

                    // ALMACENANDO BITMAP

                    UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot thumb_taskSnapshot) {
                            // OBTENIENDO URL DE LA IMAGEN BITMAP
                            String image_thumb_downloadUri = thumb_taskSnapshot.getDownloadUrl().toString();


                            Map userHashMap = new HashMap<>();
                            userHashMap.put("name", name);
                            userHashMap.put("image", image_downloadUri);
                            userHashMap.put("thumb_image", image_thumb_downloadUri);

                            databaseReferenceUsers.child(user_id).updateChildren(userHashMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        // Navegando a MainActivity
                                        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });

                        }
                    });
                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {


                // Comprimiendo la imagen para almacenarla en firebase

                imageUri = result.getUri();

                imageButtonSetup.setImageURI(imageUri);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
