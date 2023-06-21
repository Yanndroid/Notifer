package de.dlyt.yanndroid.notifer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setNavigationButtonAsBack();

    }
}