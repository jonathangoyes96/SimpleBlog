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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.optic.simpleblog.R;
import com.optic.simpleblog.activities.BlogSingleActivity;
import com.optic.simpleblog.activities.EditPostActivity;
import com.optic.simpleblog.model.Blog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyPostsFragment extends Fragment {


    private View mView;

    // RECYCLERVIEW
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearLayout;

    // FIREBASE
    private DatabaseReference databaseReferenceBlog;
    private DatabaseReference databaseReferenceLikes;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    // VARIABLE QUE SE UTILIZA PARA SABER SI SE PRESIONO EL BOTON DE "LIKE" O NO
    private boolean isClikedlIkeButton = false;

    public MyPostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_my_posts_frament, container, false);

        // FIREBASE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        databaseReferenceBlog = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReferenceLikes = FirebaseDatabase.getInstance().getReference().child("Likes");

        // -----------------    OBTENER POST SOLO DEL USUARIO LOGEADO ----------------
        //queryCurrentUser = databaseReferenceCurrentUser.orderByChild("uid").equalTo(currentUser);

        databaseReferenceBlog.keepSynced(true);
        databaseReferenceLikes.keepSynced(true);

        // RECYCLER VIEW INSTANCES
        recyclerView = (RecyclerView) mView.findViewById(R.id.recyclerViewMyPosts);
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

        Query query = databaseReferenceBlog.orderByChild("uid").equalTo(mCurrentUserId);

        // ESTABLECIENDO LOS DATOS DE LAS CARDS
        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>
        (
                Blog.class,
                R.layout.cardview_my_posts,
                BlogViewHolder.class,
                query
        )
        {
            @Override
            protected void populateViewHolder(final BlogViewHolder holder, final Blog model, int position) {

                // OBTENIENDO EL ID DE CADA POST
                final String post_key = getRef(position).getKey();

                // SETEANDO CANTIDAD DE LIKES DE CADA POST
                databaseReferenceLikes.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        holder.textViewLikesMyPosts.setText(dataSnapshot.getChildrenCount() + " Me gusta");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                holder.textViewTitleMyPosts.setText(model.getTitle());
                Picasso.with(getContext()).load(model.getImage()).into(holder.circleImageMyPosts);


                // CLICK - EDITAR POSTS
                holder.imageViewEditMyPosts.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editIntent = new Intent(getContext(), EditPostActivity.class);
                        editIntent.putExtra("post_id", post_key);
                        editIntent.putExtra("title", model.getTitle());
                        editIntent.putExtra("description", model.getDescription());
                        editIntent.putExtra("image", model.getImage());
                        startActivity(editIntent);
                    }
                });

                // CLICK - IR A DESCRIPCCION DEL POSTS
                // Evento on click en recycler view con firebase
                // Enviando el Uid a otra actividad a traves de intent
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent blogSingleIntent = new Intent(getContext(), BlogSingleActivity.class);
                        blogSingleIntent.putExtra("blog_id", post_key);
                        startActivity(blogSingleIntent);
                    }
                });
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    // VIEW HOLDER
    public static class BlogViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView textViewTitleMyPosts;
        TextView textViewLikesMyPosts;
        CircleImageView circleImageMyPosts;
        ImageView imageViewEditMyPosts;

        public BlogViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            textViewTitleMyPosts = (TextView) view.findViewById(R.id.textViewTitleMyPost);
            textViewLikesMyPosts = (TextView) view.findViewById(R.id.textViewLikesMyPost);
            circleImageMyPosts = (CircleImageView) view.findViewById(R.id.circleImageMyPost);
            imageViewEditMyPosts = (ImageView) view.findViewById(R.id.imageViewEditMyPosts);

        }

    }


}
