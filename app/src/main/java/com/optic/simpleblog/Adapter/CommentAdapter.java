package com.optic.simpleblog.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.simpleblog.R;
import com.optic.simpleblog.model.Comments;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by User on 24/01/2018.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentsViewHolder>{

    private List<Comments> mCommentsList;
    private DatabaseReference mDatabaseUsers;

    public CommentAdapter(List<Comments> commentsList) {

        this.mCommentsList = commentsList;

    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_comments ,parent, false);

        return new CommentsViewHolder(v);

    }

    // ---------------- INSTANCIANDO DATOS --------------------
    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewUsername;
        public TextView textViewComment;
        private CircleImageView circleImageProfile;

        public CommentsViewHolder(View view) {
            super(view);

            textViewComment = (TextView) view.findViewById(R.id.textViewCommentCard);
            textViewUsername = (TextView) view.findViewById(R.id.textViewUsernameCard);
            circleImageProfile = (CircleImageView) view.findViewById(R.id.circleImageProfileCard);
        }
    }

    // ------------------SETEANDO DATOS ------------------

    @Override
    public void onBindViewHolder(final CommentsViewHolder viewHolder, int i) {


        final Comments c = mCommentsList.get(i);

        String comment = c.getComment();
        String posted_by = c.getPosted_by();

        viewHolder.textViewComment.setText(comment);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(posted_by);
        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.textViewUsername.setText(name);
                Picasso.with(viewHolder.circleImageProfile.getContext()).load(thumb_image).placeholder(R.drawable.profile_default_male).into(viewHolder.circleImageProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    // ----------------OBTENIENDO LONGITUD DE LA LISTA------------

    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

}
