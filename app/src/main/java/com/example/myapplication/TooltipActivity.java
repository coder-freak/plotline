package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myapplication.tip.tooltip.Tooltip;


public class TooltipActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;
    private Button btn5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tooltip);

        constraintLayout = findViewById(R.id.consLayout);
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn3 = findViewById(R.id.button3);
        btn4 = findViewById(R.id.button4);
        btn5 = findViewById(R.id.button5);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("TOOLTIP")) {
            String tooltip = intent.getStringExtra("TOOLTIP");

            // Configure and show tooltips
            configureTooltips(tooltip);
        }
    }

    private void configureTooltips(String tooltipDetails) {
        String[] details = tooltipDetails.split("\n");
        String bgColor = details[0].replace("Background Color: ", "");
        String textColor = details[1].replace("Text Color: ", "");
        String padding = details[2].replace("ToolTip Text: ", "");
        String selectedElement = details[3].replace("Selected Element: ", "");

        Button selectedButton = null;

        switch (selectedElement) {
            case "Button 1":
                selectedButton = btn1;
                break;
            case "Button 2":
                selectedButton = btn2;
                break;
            case "Button 3":
                selectedButton = btn3;
                break;
            case "Button 4":
                selectedButton = btn4;
                break;
            case "Button 5":
                selectedButton = btn5;
                break;
            // Add cases for other buttons
        }

        if (selectedButton != null) {
            selectedButton.setBackgroundColor(Color.parseColor(bgColor));
            selectedButton.setTextColor(Color.parseColor(textColor));
            Button finalSelectedButton = selectedButton;
            selectedButton.setOnClickListener(v -> {
                Tooltip tooltip = new Tooltip.Builder(finalSelectedButton)
                        .setText(padding )
                        .setTextColor(getResources().getColor(R.color.white))
                        .setBackgroundColor(getResources().getColor(R.color.black))
                        .show();
            });
            // Configure other buttons similarly
        }
    }
}
