package edu.usf.cutr.opentripplanner.android;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;


public class UserInfoActivity extends Activity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (Utility.getSharedPreferences(this).getString("name","").equals("")) {
            ModifyUserFragment fragment = new ModifyUserFragment();
            fragmentTransaction.add(R.id.mainFragment, fragment);
            fragmentTransaction.commit();

        } else {
            ShowUserInfoFragment fragment = new ShowUserInfoFragment();
            fragmentTransaction.add(R.id.mainFragment, fragment);
            fragmentTransaction.commit();
        }


    }

}
