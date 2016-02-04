package com.tommilaurila.karttachat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

/**
 * Created by tommi.laurila on 29.1.2016.
 */
public class PaivitysvaliDialogFragment extends DialogFragment {

    public interface PaivitysvaliDialogListener {
        public void onPaivitysvaliPositiveClick(DialogFragment dialog);
        public void onPaivitysvaliNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    PaivitysvaliDialogListener mListener;


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PaivitysvaliDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PaivitysvaliDialogListener");
        }
    }//onAttach


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int saatuSek = 60;
        try {
            saatuSek = getArguments().getInt("sek");
            if(saatuSek > 3600) saatuSek = 3600;
            if(saatuSek < 0) saatuSek = 5;
        }
        catch (Exception e) {}

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.dialog_paivitysvali, null);
        NumberPicker np = (NumberPicker)v.findViewById(R.id.numberPicker);
        np.setMinValue(5);
        np.setMaxValue(3600);
        np.setValue(saatuSek);

        builder.setView(v)
            // Add action buttons
            .setPositiveButton(R.string.tallenna, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // Send the positive button event back to the host activity
                    mListener.onPaivitysvaliPositiveClick(PaivitysvaliDialogFragment.this);
                }
            })
            .setNegativeButton(R.string.peruuta, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Send the negative button event back to the host activity
                    mListener.onPaivitysvaliNegativeClick(PaivitysvaliDialogFragment.this);
                    PaivitysvaliDialogFragment.this.getDialog().cancel();
                }
            });

        return builder.create();
    }//onCreateDialog

}
