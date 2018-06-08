package com.optic.simpleblog.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditPostActivity extends AppCompatActivity {

    // VIEWS
    private EditText mEditTextTitlePost;
    private EditText mEditTextDescriptionPost;
    private ImageButton mImageButtonEditPost;
    private Button mButtonEditPost;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mBlogReference;
    private StorageReference mImagesBlogStorageReference;
    private String mCurrentUserId;

    // EXTRAS
    private String titleExtra;
    private String descriptionExtra;
    private String imageExtra;
    private String post_id;
    private ProgressDialog mProgress;

    // COMPRESION DE IMAGENES
    private Uri mImageUri;
    private static final int GALLERY_REQUEST = 1;
    private File actualImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        Toolbar.showToolbar(this, getResources().getString(R.string.edit_post_text), true);

        // VIEWS INSTANCES
        mEditTextTitlePost = (EditText) findViewById(R.id.editTextTitleEditPost);
        mEditTextDescriptionPost = (EditText) findViewById(R.id.editTextDescriptionEditPost);
        mImageButtonEditPost = (ImageButton) findViewById(R.id.imageButtonEditPost);
        mButtonEditPost = (Button) findViewById(R.id.btnEditPost);

        // EXTRAS
        titleExtra = getIntent().getStringExtra("title");
        descriptionExtra = getIntent().getStringExtra("description");
        imageExtra = getIntent().getStringExtra("image");
        post_id = getIntent().getStringExtra("post_id");


        // FIREBASE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mBlogReference = FirebaseDatabase.getInstance().getReference().child("Blog").child(post_id);
        mImagesBlogStorageReference = FirebaseStorage.getInstance().getReference().child("Blog_Images");


        // ESTABLECER VALORES
        mEditTextTitlePost.setText(titleExtra);
        mEditTextDescriptionPost.setText(descriptionExtra);
        Picasso.with(EditPostActivity.this).load(imageExtra).into(mImageButtonEditPost);

        // PROGRESS
        mProgress = new ProgressDialog(this);

        // CLICK - ABRIR GALERIA
        mImageButtonEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        // CLICK - EDITAR POST
        mButtonEditPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePost();
            }
        });
    }

    /*
     * METODO UQE PERMITE EDITAR LOS DATOS DEL POST
     */
    private void updatePost() {

        mProgress.setMessage(getResources().getString(R.string.editing_post_text));
        mProgress.show();

        final String title = mEditTextTitlePost.getText().toString();
        final String description = mEditTextDescriptionPost.getText().toString();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && actualImage != null) {

            // ACTUALIZANDO LA IMAGEN DEL POST
            byte[] thumb_image = CompressorBitmapImage.getBitmapImageCompress(this, actualImage.getPath(), 300,300);

            StorageReference thumb_filepath = mImagesBlogStorageReference.child(post_id + ".jpg");

            UploadTask uploadTask = thumb_filepath.putBytes(thumb_image);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    // OBTENIENDO LA IMAGEN QUE SE ALMACENO
                    final String imageUri = taskSnapshot.getDownloadUrl().toString();

                    // ACTUALIZANDO LOS DATOS DEL POST
                    editPostData(title, description, imageUri);

                }
            });

        }
        else if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description)) {
            // EDITAR SOLO EL TITULO Y LA DESCRIPCION DEL POST
            editPostData(title, description);
        }

    }


    /*
     * METODO QUE PERMITE EDITAR LOS DATOS DEL POST EN FIREBASE DATABASE
     */
    private void editPostData(String title, String description, String image){
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("description", description);
        postMap.put("timestamp", ServerValue.TIMESTAMP);
        postMap.put("image", image);

        mBlogReference.updateChildren(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgress.dismiss();
                Intent mainIntent = new Intent(EditPostActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
    }

    /*
     * METODO QUE PERMITE EDITAR LOS DATOS DEL POST (TITULO Y DESCRIPCION) EN FIREBASE DATABASE
     */
    private void editPostData(String title, String description){
        Map<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("description", description);
        postMap.put("timestamp", ServerValue.TIMESTAMP);

        mBlogReference.updateChildren(postMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mProgress.dismiss();
                Intent mainIntent = new Intent(EditPostActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            try {
                actualImage = FileUtil.from(this, data.getData());
                mImageButtonEditPost.setImageBitmap(BitmapFactory.decodeFile(actualImage.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
