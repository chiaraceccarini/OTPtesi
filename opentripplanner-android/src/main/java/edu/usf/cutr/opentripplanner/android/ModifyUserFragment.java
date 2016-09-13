package edu.usf.cutr.opentripplanner.android;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import util.Log;

/**
 * Created by chiara on 12/09/16.
 */
public class ModifyUserFragment extends Fragment {

    SharedPreferences myPref;

    EditText nome;
    EditText age;
    RadioButton uomo;
    RadioButton femm;
    EditText altezza;
    EditText peso;
    Button save;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modify_info_user, container, false);

        nome = (EditText) view.findViewById(R.id.nameText);
        age = (EditText) view.findViewById(R.id.ageText);
        uomo = (RadioButton) view.findViewById(R.id.sessoM);
        femm = (RadioButton) view.findViewById(R.id.sessoF);
        altezza = (EditText) view.findViewById(R.id.altezzaText);
        peso = (EditText) view.findViewById(R.id.pesotext);
        save = (Button) view.findViewById(R.id.saveButton);

        SharedPreferences sharedPref = Utility.getSharedPreferences(getActivity());

        if (!sharedPref.getString("name", "").equals("")) {
            nome.setText(sharedPref.getString("name", ""));
        }
        if (!sharedPref.getString("age", "").equals("")) {
            age.setText(sharedPref.getString("age", ""));
        }
        if (!sharedPref.getString("altezza", "").equals("")) {
            altezza.setText(sharedPref.getString("altezza", ""));
        }
        if (!sharedPref.getString("peso", "").equals("")) {
            peso.setText(sharedPref.getString("peso", ""));
        }

        nome.setOnEditorActionListener(listener);
        age.setOnEditorActionListener(listener);
        altezza.setOnEditorActionListener(listener);
        peso.setOnEditorActionListener(listener);

        nome.addTextChangedListener(textWatcher);
        age.addTextChangedListener(textWatcher);
        peso.addTextChangedListener(textWatcher);
        altezza.addTextChangedListener(textWatcher);

        RadioGroup rg = (RadioGroup) view.findViewById(R.id.radioGroupSesso);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                checkFields();
            }
        });

        checkFields();



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


    //TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFields();
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFields();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            checkFields();

        }
    };

    private TextView.OnEditorActionListener listener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // do something, e.g. set your TextView here via .setText()
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        }
    };

    private void checkFields() {

        if (nome.getText().toString().trim().length() == 0 || age.getText().toString().trim().length() == 0 || peso.getText().toString().trim().length() == 0 || altezza.getText().toString().trim().length() == 0
                || (!femm.isChecked() && !uomo.isChecked())) {
            save.setEnabled(false);
        } else {
            save.setEnabled(true);
        }
    }
}
