package com.optic.simpleblog.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.optic.simpleblog.R;

public class IntroActivity extends AppCompatActivity {

    // VIEWS
    private Button btnCreateAccount;
    private Button btnGoLogin;

    // FIREBASE
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // VIEWS INSTANCES
        btnCreateAccount = (Button) findViewById(R.id.btnGoCreateAccount);
        btnGoLogin = (Button) findViewById(R.id.btnGoIHaveAccount);

        // FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(IntroActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(IntroActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });

        checkUserExist();
    }


    /*
     * Metodo que me permite verificar si el usuario que hizo login ya ha configurado sus datos de usuario en SetupActivity
     */
    private void checkUserExist() {

        // Si el usuario ya ha hecho login correctamente entra en esta condicion
        if (mAuth.getCurrentUser() != null) {

            // Obtengo el Uid del usuario que acaba de hacer login correctamente
            final String user_id = mAuth.getCurrentUser().getUid();

            mUsersReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // dataSnapshot hace referencia al Nodo de Users
                    // Si ya se ha configurado los datos del usuario lo envio al mainActivity sino lo envio a SetupActivity
                    // Con el fin de que configure sus datos de usuario tales como imagen y nombre
                    if(dataSnapshot.hasChild(user_id)) {
                        Intent mainIntent = new Intent(IntroActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                    else {
                        Intent setupIntent = new Intent(IntroActivity.this, SetupActivity.class);
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
}
