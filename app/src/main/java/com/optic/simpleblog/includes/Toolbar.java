package com.optic.simpleblog.includes;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.optic.simpleblog.R;


/**
 * Created by Optic on 9/04/2018.
 */

public class Toolbar {

    public static void showToolbar(AppCompatActivity activity, String title, boolean upButton) {

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
    }

    public static void showToolbarBackEvent(final AppCompatActivity activity, String title, boolean upButton) {

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
    }

    public static void showToolbarForFragment(Fragment fragment, String tittle, boolean upButton, View view){
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) fragment.getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) fragment.getActivity()).getSupportActionBar().setTitle(tittle);
        ((AppCompatActivity) fragment.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }






}
