package com.example.ashwin.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by ashwin on 15/6/16.
 */
public class AlertDialogFragment extends DialogFragment{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Oops! Sorry!");
        builder.setMessage("There was some problem");
        builder.setPositiveButton("OK",null);
        builder.setNegativeButton("Ignore",null);
        builder.setNeutralButton("Nuetral",null);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
