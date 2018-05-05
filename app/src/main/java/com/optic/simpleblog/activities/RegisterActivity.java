package com.optic.simpleblog.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.optic.simpleblog.R;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button btnRegister;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        // Creando otro nodo llamado Users en la base de datos de firebase donde se almacenaran tambien los datos del usuario tales como nombre y su imagen
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        editTextName = (EditText) findViewById(R.id.editTextNameRegister);
        editTextEmail = (EditText) findViewById(R.id.editTextEmailRegister);
        editTextPassword = (EditText) findViewById(R.id.editTextPasswordRegister);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUserInFirebase();
            }
        });
    }

    /*
     * Registrando el usuario en firebase y creando los datos del usuario en database de firebase
     */
    private void registerUserInFirebase() {

        final String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            progressDialog.setMessage("Signing up...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {

                        // Obteniendo el uId usuario que se registro
                        String user_id = firebaseAuth.getCurrentUser().getUid();
                        // Creando otro nodo que sera el Uid del usuario que se acabo de registrar
                        DatabaseReference current_user_db = databaseReference.child(user_id);
                        // Seteando los valores que tendran los usuarios su nombre e imagen
                        current_user_db.child("name").setValue(name);
                        current_user_db.child("image").setValue("default");
                        current_user_db.child("thumb_image").setValue("default");

                        progressDialog.dismiss();

                        // Navegando al MainActivity
                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                }
            });
        }

    }
}
