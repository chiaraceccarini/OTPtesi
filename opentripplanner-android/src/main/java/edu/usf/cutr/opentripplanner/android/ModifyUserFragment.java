package edu.usf.cutr.opentripplanner.android;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import util.Log;

/**
 * Created by chiara on 12/09/16.
 */
public class ModifyUserFragment extends Fragment {

    SharedPreferences myPref;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modify_info_user, container, false);

        SharedPreferences sharedPref = Utility.getSharedPreferences(getActivity());
        final EditText nome = (EditText) view.findViewById(R.id.nameText);
        if (!sharedPref.getString("name", "").equals("")) {
            nome.setText(sharedPref.getString("name", ""));
        }
        final EditText age = (EditText) view.findViewById(R.id.ageText);
        if (!sharedPref.getString("age", "").equals("")) {
            age.setText(sharedPref.getString("age", ""));
        }
        final RadioButton uomo = (RadioButton) view.findViewById(R.id.sessoM);
        final RadioButton femm = (RadioButton) view.findViewById(R.id.sessoF);


        final EditText altezza = (EditText) view.findViewById(R.id.altezzaText);
        if (!sharedPref.getString("altezza", "").equals("")) {
            altezza.setText(sharedPref.getString("altezza", ""));
        }
        final EditText peso = (EditText) view.findViewById(R.id.pesotext);
        if (!sharedPref.getString("peso", "").equals("")) {
            peso.setText(sharedPref.getString("peso", ""));
        }

        Button save = (Button) view.findViewById(R.id.saveButton);

    

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPref = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                myPref.edit().putString("name",nome.getText().toString()).apply();
                myPref.edit().putString("age",age.getText().toString()).apply();
                if (femm.isChecked()) {
                    myPref.edit().putString("sesso","Donna").apply();
                } else if (uomo.isChecked()) {
                    myPref.edit().putString("sesso","Uomo").apply();
                }
                myPref.edit().putString("altezza",altezza.getText().toString()).apply();
                myPref.edit().putString("peso",peso.getText().toString()).apply();

                getActivity().onBackPressed();

            }
        });


        return view;
    }
}
