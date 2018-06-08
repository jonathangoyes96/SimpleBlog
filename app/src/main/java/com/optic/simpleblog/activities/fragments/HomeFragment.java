package com.optic.simpleblog.activities.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.optic.simpleblog.activities.BlogSingleActivity;
import com.optic.simpleblog.activities.MainActivity;
import com.optic.simpleblog.model.Blog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private View mView;

    // RECYCLERVIEW
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayout;

    // FIREBASE
    private DatabaseReference databaseReferenceBlog;
    private DatabaseReference databaseReferenceLikes;
    private DatabaseReference databaseReferenceUsers;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    // VARIABLE QUE SE UTILIZA PARA SABER SI SE PRESIONO EL BOTON DE "LIKE" O NO
    private boolean isClikedlIkeButton = false;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        // FIREBASE INSTANCE
        databaseReferenceBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        // -----------------    OBTENER POST SOLO DEL USUARIO LOGEADO ----------------
        //queryCurrentUser = databaseReferenceCurrentUser.orderByChild("uid").equalTo(currentUser);

        databaseReferenceBlog.keepSynced(true);
        databaseReferenceUsers.keepSynced(true);
        databaseReferenceLikes.keepSynced(true);

        // RECYCLER VIEW INSTANCES
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerViewMain);
        recyclerView.setHasFixedSize(true);
        mLinearLayout = new LinearLayoutManager(getContext());
        mLinearLayout.setReverseLayout(true);
        mLinearLayout.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLinearLayout);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ESTABLECIENDO LOS DATOS DE LAS CARDS
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

                        viewHolder.textViewLikesNumber.setText(dataSnapshot.getChildrenCount() +  " " + "Likes");


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getContext() ,model.getImage());
                viewHolder.setUsername(getActivity().getResources().getString(R.string.by_text) + " " + model.getUsername());
                viewHolder.setImageButtonLike(post_key);

                // Evento on click en recycler view con firebase
                // Enviando el Uid a otra actividad a traves de intent
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent blogSingleIntent = new Intent(getContext(), BlogSingleActivity.class);
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

                                    if(dataSnapshot.child(post_key).hasChild(mCurrentUserId)) {
                                        databaseReferenceLikes.child(post_key).child(mCurrentUserId).removeValue();
                                        isClikedlIkeButton = false;
                                    }
                                    else {
                                        databaseReferenceLikes.child(post_key).child(mCurrentUserId).setValue("RandomValue");
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

    // VIEW HOLDER
    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView textViewTitleCard;
        ImageButton imageButtonLike;
        DatabaseReference databaseReferenceLikes;
        TextView textViewLikesNumber;
        FirebaseAuth firebaseAuth;

        public BlogViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
             textViewLikesNumber = (TextView) view.findViewById(R.id.textViewLikesNumber);
            imageButtonLike = (ImageButton) view.findViewById(R.id.imageButtonLikeCard);
            firebaseAuth = FirebaseAuth.getInstance();
            databaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
            databaseReferenceLikes.keepSynced(true);

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


        public void setTitle(String title) {
            textViewTitleCard = (TextView) view.findViewById(R.id.textViewTitleCard);
            textViewTitleCard.setText(title);
        }

        public void setDescription(String description) {
            TextView textViewDescriptionCard = (TextView) view.findViewById(R.id.textViewDescriptionCard);
            textViewDescriptionCard.setText(description);
        }

        public void setImage(final Context context, final String image) {
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
            textViewUsernameCard.setText(username);
        }
    }

}
