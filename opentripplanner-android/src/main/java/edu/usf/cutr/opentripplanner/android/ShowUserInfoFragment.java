package edu.usf.cutr.opentripplanner.android;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Created by chiara on 12/09/16.
 */
public class ShowUserInfoFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_info_user, container, false);
        TextView nome = (TextView) view.findViewById(R.id.nameTextShow);
        TextView age = (TextView) view.findViewById(R.id.ageTextShow);
        TextView sesso = (TextView) view.findViewById(R.id.sessoTextShow);
        TextView altezza = (TextView) view.findViewById(R.id.altezzaTextShow);
        TextView peso = (TextView) view.findViewById(R.id.pesotextShow);

        nome.setText(Utility.getSharedPreferences(getActivity()).getString("name", ""));
        age.setText(Utility.getSharedPreferences(getActivity()).getString("age", ""));
        sesso.setText(Utility.getSharedPreferences(getActivity()).getString("sesso", ""));
        altezza.setText(Utility.getSharedPreferences(getActivity()).getString("altezza", ""));
        peso.setText(Utility.getSharedPreferences(getActivity()).getString("peso", ""));

        Button modify = (Button) view.findViewById(R.id.modifyButton);

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                ModifyUserFragment fragment = new ModifyUserFragment();
                fragmentTransaction.replace(R.id.mainFragment, fragment);
                fragmentTransaction.commit();
            }
        });

        return view;
    }
}
