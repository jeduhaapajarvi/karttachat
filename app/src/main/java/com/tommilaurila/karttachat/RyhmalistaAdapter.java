package com.tommilaurila.karttachat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by tommi.laurila on 19.5.2015.
 */
public class RyhmalistaAdapter extends ArrayAdapter<Ryhma> {


    public RyhmalistaAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public RyhmalistaAdapter(Context context, int resource, List<Ryhma> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.listarivi_ryhmat, null);
        }

        Ryhma p = getItem(position);

        if (p != null) {
            TextView tt0 = (TextView) v.findViewById(R.id.tvRyhmaId);
            TextView tt1 = (TextView) v.findViewById(R.id.tvRyhmaNimi);
            TextView tt2 = (TextView) v.findViewById(R.id.tvPerustamisaika);

            if (tt0 != null) {
                tt0.setText(p.getRyhma_id() + "");
            }

            if (tt1 != null) {
                tt1.setText(p.getNimi());
            }

            if (tt2 != null) {
                tt2.setText("Perustettu " + p.getPerustamisaika());
            }
        }

        return v;
    }

}
