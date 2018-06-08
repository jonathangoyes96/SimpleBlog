package com.optic.simpleblog.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.optic.simpleblog.R;
import com.optic.simpleblog.includes.Toolbar;
import com.optic.simpleblog.utils.CompressorBitmapImage;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    // VIEWS
    private TextInputEditText mEditTextNameProfile;
    private TextInputEditText mEditTextEmailProfile;
    private CircleImageView mCircleImageProfile;
    private Button mButtonEditProfile;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;
    private StorageReference mImagesStorageReference;
    private String mCurrentUserId;

    // EXTRAS
    private String name;
    private String email;
    private String image;

    // PROGRESS
    private ProgressDialog mProgress;

    // GALLERY INTENT
    private Uri imageUri;
    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar.showToolbar(this, getResources().getString(R.string.edit_profile_text), true);

        // FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);
        mImagesStorageReference = FirebaseStorage.getInstance().getReference().child("profile_images");

        // EXTRAS
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        image = getIntent().getStringExtra("image");

        // VIEWS INSTANCE
        mEditTextNameProfile = findViewById(R.id.editTextNameEditProfile);
        mEditTextEmailProfile = findViewById(R.id.editTextEmailEditProfile);
        mCircleImageProfile = findViewById(R.id.circleImageEditProfile);
        mButtonEditProfile = findViewById(R.id.btnEditProfile);

        // PROGRESS INSTANCE
        mProgress = new ProgressDialog(this);

        // SET VALUES
        mEditTextNameProfile.setText(name);
        mEditTextEmailProfile.setText(email);
        if(image != null) {
            if(!image.equals("default")) {
                Picasso.with(EditProfileActivity.this).load(image).into(mCircleImageProfile);
            }

        }

        // CLICK - SELECCIONAR IMAGEN DE GALERIA
        mCircleImageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        // CLICK - EDITAR INFORMACION DE USUARIO
        mButtonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfileinfo();
            }
        });


    }

    /*
     * METODO QUE PERMITE EDITAR LA INFORMACION DEL USUARIO ALMACENADA EN FIREBASE
     */
    private void editProfileinfo() {

        final String name = mEditTextNameProfile.getText().toString().trim();
        final String email = mEditTextEmailProfile.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && imageUri != null) {
            mProgress.setMessage("Actualizando datos...");
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.show();

            // Obteniendo imagen bitmap comprimida
            final byte[] thumb_byte = CompressorBitmapImage.getBitmapImageCompress(this, imageUri.getPath(), 200, 200);


            // Almacenado la imagen bitmap comprimida en storage de firebase
            StorageReference filePath = mImagesStorageReference.child(mCurrentUserId + ".jpg");
            final StorageReference thumb_filePath = mImagesStorageReference.child("thumbs").child(mCurrentUserId + ".jpg");


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

                            // ACTUALIZAND DATOS DEL USUARIO
                            updateUserInfo(name, email, image_downloadUri, image_thumb_downloadUri);

                        }
                    });
                }
            });

        }
        else if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email)){
            updateUserInfo(name, email);
        }
        else {
            Toast.makeText(EditProfileActivity.this, "Ingresa todos los campos", Toast.LENGTH_SHORT).show();
        }

    }


    /*
     * ACTUALIZA LOS DATOS DEL USUARIO SIN LA IMAGEN EN FIREBASE DATABASE
     */
    private void updateUserInfo(String name, String email) {

        Map userHashMap = new HashMap<>();
        userHashMap.put("name", name);
        userHashMap.put("email", email);


        mUserReference.updateChildren(userHashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()) {
                    mProgress.dismiss();

                    Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });

    }

    /*
     * ACTUALIZA LOS DATOS DEL USUARIO INCLUIDO LA IMAGEN EN FIREBASE DATABASE
     */
    private void updateUserInfo(String name, String email, String image, String thumb_image) {

        Map userHashMap = new HashMap<>();
        userHashMap.put("name", name);
        userHashMap.put("email", email);
        userHashMap.put("image", image);
        userHashMap.put("thumb_image", thumb_image);

        mUserReference.updateChildren(userHashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()) {
                    mProgress.dismiss();

                    Intent mainIntent = new Intent(EditProfileActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });

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

                // ESTABLECIENDO LA NUEVA IMAGEN SELECCIODA DESDE GALERIA
                imageUri = result.getUri();
                mCircleImageProfile.setImageURI(imageUri);

            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
