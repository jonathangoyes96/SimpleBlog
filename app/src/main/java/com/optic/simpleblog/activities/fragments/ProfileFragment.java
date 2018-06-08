package com.optic.simpleblog.activities.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.simpleblog.R;
import com.optic.simpleblog.activities.EditProfileActivity;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    // VIEWS
    private View mView;
    private TextView mTextViewNameProfile;
    private TextView mTextViewEmailProfile;
    private CircleImageView mCircleImageProfile;
    private Button mButtonEditProfile;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mUserReference;
    private String mCurrentUserId;

    // USER INFO
    private String name;
    private String email;
    private String image;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_profile, container, false);

        // FIREBASE INSTANCE
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserId);


        // VIEWS INSTANCE
        mTextViewNameProfile = mView.findViewById(R.id.textViewNameProfile);
        mTextViewEmailProfile = mView.findViewById(R.id.textViewEmailProfile);
        mCircleImageProfile = mView.findViewById(R.id.circleImageProfile);
        mButtonEditProfile = mView.findViewById(R.id.btnGoToEditProfile);

        // CARGAR LOS DATOS DEL USUARIO
        getUserInfo();

        // CLICK - EDITAR USUARIO
        mButtonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editIntent = new Intent(getContext(), EditProfileActivity.class);
                editIntent.putExtra("name", name);
                editIntent.putExtra("image", image);
                editIntent.putExtra("email", email);
                startActivity(editIntent);
            }
        });

        return mView;
    }

    /*
     * METODO QUE PERMITE OBTENER LA INFORMACION DEL USUARIO DESDE FIREBASE
     */
    private void getUserInfo() {
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    name = dataSnapshot.child("name").getValue().toString();
                    email = dataSnapshot.child("email").getValue().toString();

                    if(dataSnapshot.hasChild("image")) {
                        image = dataSnapshot.child("image").getValue().toString();

                        if(!image.equals("default"))
                            Picasso.with(getContext()).load(image).into(mCircleImageProfile);
                    }


                    mTextViewNameProfile.setText(name);
                    mTextViewEmailProfile.setText(email);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
