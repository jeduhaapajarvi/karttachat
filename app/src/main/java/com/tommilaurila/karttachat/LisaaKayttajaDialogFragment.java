package com.tommilaurila.karttachat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

/**
 * Created by tommi.laurila on 19.3.2015.
 */
public class LisaaKayttajaDialogFragment extends DialogFragment {

    public interface LisaaKayttajaDialogListener {
        public void onKayttajaPositiveClick(DialogFragment dialog);
        public void onKayttajaNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    LisaaKayttajaDialogListener mListener;


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (LisaaKayttajaDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement LisaaKayttajaDialogListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_lisaa_kayttaja, null))
                // Add action buttons
                .setPositiveButton(R.string.lisaa, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onKayttajaPositiveClick(LisaaKayttajaDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.peruuta, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onKayttajaNegativeClick(LisaaKayttajaDialogFragment.this);
                        LisaaKayttajaDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}

