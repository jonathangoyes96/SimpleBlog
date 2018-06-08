package com.optic.simpleblog.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.optic.simpleblog.activities.fragments.HomeFragment;
import com.optic.simpleblog.activities.fragments.MyPostsFragment;
import com.optic.simpleblog.activities.fragments.ProfileFragment;
import com.optic.simpleblog.activities.fragments.PublishFragment;
import com.optic.simpleblog.model.Blog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity {


    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;

    // FRAGMENTS
    private HomeFragment mHomeFragment;
    private ProfileFragment mProfileFragment;
    private MyPostsFragment mMyPostsFragment;
    private PublishFragment mPublishFragment;

    // BOTTOM NAVIGATION
    private BottomNavigationView mBottomNavigation;



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

        // FRAGMENT POR DEFECTO HomeFragment
        showSelectedFragment(new HomeFragment());

        // BOTTOM NAVIGATION INSTANCE
        mBottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        disableShiftMode(mBottomNavigation);

        mBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == R.id.action_home) {

                    mHomeFragment = new HomeFragment();
                    showSelectedFragment(mHomeFragment);

                }

                if(item.getItemId() == R.id.action_profile) {

                    mProfileFragment = new ProfileFragment();
                    showSelectedFragment(mProfileFragment);

                }

                if(item.getItemId() == R.id.action_my_posts) {

                    mMyPostsFragment = new MyPostsFragment();
                    showSelectedFragment(mMyPostsFragment);

                }

                if(item.getItemId() == R.id.action_upload) {

                    mPublishFragment = new PublishFragment();
                    showSelectedFragment(mPublishFragment);

                }

                return true;
            }
        });

    }

    @SuppressLint("RestrictedApi")
    public static void disableShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            //Timber.e(e, "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            //Timber.e(e, "Unable to change value of shift mode");
        }
    }


    /*
     * METODO QUE PERMITE ELEJIR EL FRAGMENT
     */
    private void showSelectedFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null).commit();
    }



    @Override
    protected void onStart() {
        super.onStart();

        if(firebaseAuth.getCurrentUser() == null) {
            Intent introIntent = new Intent(MainActivity.this, IntroActivity.class);
            introIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(introIntent);
            finish();
        }

        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

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
        firebaseAuth.signOut();
    }

    public void showToolbar(String tittle, boolean upButton){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tittle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }
}
