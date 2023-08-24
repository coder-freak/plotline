package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private Button renderButton;
    private Spinner spinnerSelectElement;
    private EditText bgColorInput;
    private EditText textColorInput;
    private EditText Tooltiptext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        spinnerSelectElement = findViewById(R.id.editText0);
        bgColorInput = findViewById(R.id.editText2);
        Tooltiptext = findViewById(R.id.editText4);
        textColorInput = findViewById(R.id.editText1);

        // Create an ArrayAdapter for the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.select_element_options, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectElement.setAdapter(adapter);

        // Set up spinner item selection listener
        spinnerSelectElement.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected element from the spinner and set it as a tag for the renderButton
                String selectedElement = parentView.getItemAtPosition(position).toString();
                renderButton.setTag(selectedElement);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where no item is selected (if needed)
            }
        });

        // Initialize and set click listener for the renderButton
        renderButton = findViewById(R.id.renderBtn);
        renderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected element from the renderButton's tag and render the tooltip
                String selectedElement = (String) renderButton.getTag();
                renderTooltip(selectedElement);
            }
        });
    }

    // Method to render the tooltip
    private void renderTooltip(String selectedElement) {
        // Get input values from UI elements
        String bgColor = bgColorInput.getText().toString();
        String textColor = textColorInput.getText().toString();
        String tooltip = Tooltiptext.getText().toString();

        // Build tooltip text
        String tooltipText = buildTooltip(bgColor, textColor, tooltip, selectedElement);

        // Start TooltipActivity and pass the tooltip text as an extra
        Intent intent = new Intent(this, TooltipActivity.class);
        intent.putExtra("TOOLTIP", tooltipText);
        startActivity(intent);
    }

    // Method to build the tooltip text
    private String buildTooltip(String bgColor, String textColor, String tooltip1, String selectedElement) {
        String tooltip = "Background Color: " + bgColor +
                "\nText Color: " + textColor +
                "\nToolTip Text: " + tooltip1 +
                "\nSelected Element: " + selectedElement;

        return tooltip;
    }
}
