package com.optic.simpleblog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.optic.simpleblog.Adapter.CommentAdapter;
import com.optic.simpleblog.model.Comments;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlogSingleActivity extends AppCompatActivity {

    // POST KEY ENVIADO DESDE MainActivity
    private String post_key = null;

    // FIREBASE
    private ImageView imageViewSingleBlog;
    private DatabaseReference databaseReferenceBlog;
    private DatabaseReference mDatabaseComments;
    private FirebaseAuth firebaseAuth;
    private String mCurrentUserId;
    private FirebaseStorage mStorageImage;

    private Toolbar mToolbar;

    // VIEWS
    private TextView textViewTitleBlogSingle;
    private TextView textViewDescriptionBlogSingle;
    private TextView textViewUsernameBlogSingle;
    private FloatingActionButton fab;
    private ImageButton mImageButtonDeletePost;
    private String mImageUri;

    // RECYCLER, ADAPTER AND LIST
    private RecyclerView mRecyclerViewComments;
    private List<Comments> mCommentsList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private CommentAdapter mCommentAdapter;

    // ALERT DIALOG VIEWS
    private EditText mEditTextDialogComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_single);

        // AÑADIENDO UN TOOLBAR PERSONALIZADO
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.custom_toolbar_delete, null);
        actionBar.setCustomView(actionBarView);

        // ID DEL POST ENVIADO DESDE MainActivity
        post_key = getIntent().getExtras().getString("blog_id");

        // FIREBASE INSTANCE
        databaseReferenceBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseComments = FirebaseDatabase.getInstance().getReference().child("Comments").child(post_key);
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageImage = FirebaseStorage.getInstance();
        mCurrentUserId = firebaseAuth.getCurrentUser().getUid();

        // VIEWS INSTANCE
        imageViewSingleBlog = (ImageView) findViewById(R.id.imageViewBlogSingle);
        textViewTitleBlogSingle = (TextView) findViewById(R.id.textViewTitleBlogSingle);
        textViewDescriptionBlogSingle = (TextView) findViewById(R.id.textViewDescriptionBlogSingle);
        textViewUsernameBlogSingle = (TextView) findViewById(R.id.textViewUsernameBlogSingle);
        mImageButtonDeletePost = (ImageButton) findViewById(R.id.imageButtonDeletePost);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mRecyclerViewComments = (RecyclerView) findViewById(R.id.recyclerViewComments);

        // CONFIGURANDO RECYCLERVIEW

        mCommentAdapter = new CommentAdapter(mCommentsList);
        mLinearLayout = new LinearLayoutManager(this);
        mRecyclerViewComments.setHasFixedSize(true);
        mRecyclerViewComments.setLayoutManager(mLinearLayout);
        mRecyclerViewComments.setAdapter(mCommentAdapter);

        // CARGANDO COMENTARIOS
        loadComments();

        // CARGANDO DATOS DEL POST
        databaseReferenceBlog.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = (String) dataSnapshot.child("title").getValue();
                String description = (String) dataSnapshot.child("description").getValue();
                mImageUri = (String) dataSnapshot.child("image").getValue();

                String username = (String) dataSnapshot.child("username").getValue();
                String user_uid = (String) dataSnapshot.child("uid").getValue();

                textViewTitleBlogSingle.setText(title.toUpperCase());
                textViewDescriptionBlogSingle.setText(description);
                textViewUsernameBlogSingle.setText("By: " + username);
                Picasso.with(BlogSingleActivity.this).load(mImageUri).placeholder(R.drawable.icon_comment).into(imageViewSingleBlog);

                //Muestro el boton de elminar el post si el usuario fue el que lo creo
                if(firebaseAuth.getCurrentUser().getUid().equals(user_uid)) {
                    mImageButtonDeletePost.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // ELIMINANDO POST
        mImageButtonDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBlogPost();
            }
        });


        // FLOATING ACTION BUTTON

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogCreateComment();
            }
        });



    }

    // Metodo que permite mostrar todos los comentarios desde firebase

    private void loadComments() {

        mDatabaseComments.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comments comment = dataSnapshot.getValue(Comments.class);
                mCommentsList.add(comment);
                mCommentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Metodo que muestra una ventana emergente con el formulario para agregar Comentarios

    private void showAlertDialogCreateComment() {

        View view;

        LayoutInflater li = LayoutInflater.from(BlogSingleActivity.this);

        view = li.inflate(R.layout.custom_dialog_comment, null);
        mEditTextDialogComment = (EditText) view.findViewById(R.id.editTextCommentDialog);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BlogSingleActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(view);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Agregar Comentario", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String comment = mEditTextDialogComment.getText().toString();

                        HashMap<String, Object> commentHash = new HashMap<>();
                        commentHash.put("comment", comment);
                        commentHash.put("time", ServerValue.TIMESTAMP);
                        commentHash.put("posted_by", mCurrentUserId);

                        mDatabaseComments.push().setValue(commentHash).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(BlogSingleActivity.this, "Se añadio nuevo comentario", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }

    // Metodo que permite eliminar todos los datos del blog creado y su imagen almacenada en FirebaseStorage

    private void deleteBlogPost() {

        if(mImageUri != null) {

            StorageReference photoRef = mStorageImage.getReferenceFromUrl(mImageUri);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d("BlogSingleActivity", "onSuccess: deleted file");
                    databaseReferenceBlog.child(post_key).removeValue();
                    Intent mainIntent = new Intent(BlogSingleActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d("BlogSingleActivity", "onFailure: did not delete file");
                }
            });
        }
    }

    public void showToolbar(String tittle, boolean upButton){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tittle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }
}
