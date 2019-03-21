package uk.ac.shef.oak.com4510.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import uk.ac.shef.oak.com4510.R;

/**
 * The Help activity contains information related to the use of the app.
 */
public class Help extends AppCompatActivity {
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
    }

    @Override
    public void onBackPressed(){
        finish();
    }

}
