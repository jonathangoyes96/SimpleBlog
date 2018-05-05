package com.optic.simpleblog.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.simpleblog.R;
import com.optic.simpleblog.model.Blog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    // RECYCLERVIEW
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayout;

    // FIREBASE
    private DatabaseReference databaseReferenceBlog;
    private DatabaseReference databaseReferenceLikes;
    private DatabaseReference databaseReferenceUsers;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;


    private boolean isClikedlIkeButton = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showToolbar("Blog App", false);

        firebaseAuth = FirebaseAuth.getInstance();

        if(firebaseAuth.getCurrentUser() == null) return;

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {

                if(auth.getCurrentUser() == null) {
                    Log.d("Ejecucion: " , "4");
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }

            }
        };

        // Guardando datos en una carpeta llamada Blog dentro de storage
        databaseReferenceBlog = FirebaseDatabase.getInstance().getReference().child("Blog");

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        databaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("Likes");


        String currentUser = firebaseAuth.getCurrentUser().getUid();

        // -----------------    OBTENER POST SOLO DEL USUARIO LOGEADO ----------------
        //queryCurrentUser = databaseReferenceCurrentUser.orderByChild("uid").equalTo(currentUser);

        databaseReferenceBlog.keepSynced(true);
        databaseReferenceUsers.keepSynced(true);
        databaseReferenceLikes.keepSynced(true);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewMain);
        recyclerView.setHasFixedSize(true);
        mLinearLayout = new LinearLayoutManager(this);
        mLinearLayout.setReverseLayout(true);
        mLinearLayout.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLinearLayout);


        checkUserExist();

    }



    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView textViewTitleCard;
        ImageButton imageButtonLike;
        DatabaseReference databaseReferenceLikes;
        FirebaseAuth firebaseAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            imageButtonLike = (ImageButton) view.findViewById(R.id.imageButtonLikeCard);
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
            databaseReferenceLikes.keepSynced(true);



            //Ejemplo Forma de setear evento para un elemento especifico de las cardView incluidas en recycler view
            /*
            textViewTitleCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(MainActivity.this, "Event Onclick solo en titulo del cardView")
                }
            });
            */
        }

        public void setImageButtonLike(final String post_key) {
            databaseReferenceLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())){
                        imageButtonLike.setImageResource(R.drawable.like_blue);
                    }
                    else {
                        imageButtonLike.setImageResource(R.drawable.like_gray);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setLikesNumber(long likesNumber) {
            TextView textViewLikesNumber = (TextView) view.findViewById(R.id.textViewLikesNumber);
            textViewLikesNumber.setText(likesNumber + " Me Gusta");
        }

        public void setTitle(String title) {
            textViewTitleCard = (TextView) view.findViewById(R.id.textViewTitleCard);
            textViewTitleCard.setText(title);
        }

        public void setDescription(String description) {
            TextView textViewDescriptionCard = (TextView) view.findViewById(R.id.textViewDescriptionCard);
            textViewDescriptionCard.setText(description);
        }

        public void setImage(final Context context,final String image) {
            final ImageView imageViewCard = (ImageView) view.findViewById(R.id.imageViewCard);
            Picasso.with(context).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(imageViewCard, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(image).into(imageViewCard);
                }
            });
        }

        public void setUsername(String username) {
            TextView textViewUsernameCard = (TextView) view.findViewById(R.id.textViewUsernameCard);
            textViewUsernameCard.setText("By: " + username);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(firebaseAuth.getCurrentUser() == null) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            return;
        }

        firebaseAuth.addAuthStateListener(authStateListener);



        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>
                (
                Blog.class,
                R.layout.cardview_blog,
                BlogViewHolder.class,
                databaseReferenceBlog
                )
        {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, int position) {

                // OBTENIENDO EL ID DE CADA POST
                final String post_key = getRef(position).getKey();

                // SETEANDO CANTIDAD DE LIKES DE CADA POST
                databaseReferenceLikes.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.setLikesNumber(dataSnapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext() ,model.getImage());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setImageButtonLike(post_key);

                // Evento on click en recycler view con firebase
                // Enviando el Uid a otra actividad a traves de intent
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent blogSingleIntent = new Intent(MainActivity.this, BlogSingleActivity.class);
                        blogSingleIntent.putExtra("blog_id", post_key);
                        startActivity(blogSingleIntent);
                    }
                });


                viewHolder.imageButtonLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isClikedlIkeButton = true;


                            databaseReferenceLikes.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // Si el post tiene un like por parte del usuario se elimina ese registro en la base de datos sino se crea
                                    if(isClikedlIkeButton) {

                                        if(dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid())) {
                                            databaseReferenceLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                            isClikedlIkeButton = false;
                                        }
                                        else {
                                            databaseReferenceLikes.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                            isClikedlIkeButton = false;
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    }
                });
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }


    /*
     * Metodod que verifica si el usuario tiene configurado sus datos tales como Nombre, Imagen
     * Si no es el caso se lo envia a la actividad de Configurar sus datos
     */

    private void checkUserExist() {

        if (firebaseAuth.getCurrentUser() != null) {
            final String user_id = firebaseAuth.getCurrentUser().getUid();

            databaseReferenceUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.hasChild(user_id)) {
                        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_add) {
            startActivity(new Intent(MainActivity.this, PostActivity.class));
        }

        if(item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SetupActivity.class));
        }

        // Cerrar sesion del usuario logeado con firebase

        if(item.getItemId() == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }


    /*
     * Cerrar sesion den firebase
     */
    private void logout() {
        firebaseAuth.signOut();;
    }

    public void showToolbar(String tittle, boolean upButton){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tittle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }
}
