package de.hdmstuttgart.carfind;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class AddCarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AddCarFragment fragment = new AddCarFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.addCarContent, fragment)
                .commit();
    }

}