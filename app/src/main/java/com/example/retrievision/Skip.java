package com.example.retrievision;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Skip extends Abstract_Generated {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.skip_report);

        InitIDs();
        setUpDropdowns();
        setUpClickListeners();
        setUpTags();
        autogenLostTags();
    }

    //initialized tags/object description from previous activity
    private void autogenLostTags() {
        Intent intent = getIntent();
        String highestObject = intent.getStringExtra("highestObject");
        String colorExtraction = intent.getStringExtra("dominant_colors");

        if (highestObject == null) {
            return;
        }
        Log.e("colors: ", colorExtraction);
        addChip(chipType, highestObject);
        if (colorExtraction != null && !colorExtraction.isEmpty()) {
            for (String color : colorExtraction.split("\n")) {
                addChip(chipColor, color);
            }
        }

        // Categories
        Set<String> electronics = new HashSet<>(Arrays.asList("Keyboard", "Earphones", "Flash Drive", "Charger", "Smartphone", "Headphones", "Wireless Earphones"));
        Set<String> personalBelongings = new HashSet<>(Arrays.asList("Keys", "Money", "ID", "Water Bottle", "Wallet"));
        Set<String> accessories = new HashSet<>(Arrays.asList("Cap", "Watch", "Glasses"));
        Set<String> clothing = new HashSet<>(Arrays.asList("Jacket", "Shirt"));
        Set<String> stationary = Collections.singleton("Pen");

        String category = getCategory(highestObject, electronics, personalBelongings, accessories, clothing, stationary);
        addChip(chipCategory, category);

        handleVisibilityBasedOnCategory(category);

    }

    private String getCategory(String item, Set<String> electronics, Set<String> personalBelongings, Set<String> accessories, Set<String> clothing, Set<String> stationary) {
        if (electronics.contains(item)) {
            return "Electronics";
        } else if (personalBelongings.contains(item)) {
            return "Personal Belongings";
        } else if (accessories.contains(item)) {
            return "Accessory";
        } else if (clothing.contains(item)) {
            return "Clothing";
        } else if (stationary.contains(item)) {
            return "Stationary";
        }
        return "";
    }

    private void handleVisibilityBasedOnCategory(String category) {
        if ("Electronics".equals(category)) {
            sizeInputLayout.setVisibility(View.GONE);
        } else if ("Personal Belongings".equals(category) || "Stationary".equals(category) || "Accessory".equals(category)) {
            sizeInputLayout.setVisibility(View.GONE);
            dimensionInputLayout.setVisibility(View.GONE);
        } else if ("Clothing".equals(category)) {
            shapeInputLayout.setVisibility(View.GONE);
            dimensionInputLayout.setVisibility(View.GONE);
        }
    }

    private void InitIDs(){
        autoCompleteCategory = findViewById(R.id.categoryDropdown);
        autoCompleteLocation = findViewById(R.id.locationDropdown);
        autoCompleteSizes = findViewById(R.id.sizeDropdown);
        autoCompleteShape = findViewById(R.id.shapeDropdown);
        autoCompleteColor = findViewById(R.id.colorDropdown);
        chipCategory = findViewById(R.id.categorychipgroup);
        addCategoryButton = findViewById(R.id.addcategorybtn);
        chipColor = findViewById(R.id.colorchipgroup);
        addColorButton = findViewById(R.id.addcolorbtn);
        chipLocation = findViewById(R.id.locationchipgroup);
        addLocationButton =findViewById(R.id.addlocationbtn);
        typeTags = findViewById(R.id.type);
        chipType = findViewById(R.id.typechipgroup);
        brandTags = findViewById(R.id.brandinput);
        chipBrand = findViewById(R.id.brandchipgroup);
        modelTags = findViewById(R.id.modelinput);
        chipModel = findViewById(R.id.modelchipgroup);
        textTags = findViewById(R.id.textinput);
        chipText = findViewById(R.id.textchipgroup);
        widthInput = findViewById(R.id.width);
        heightInput = findViewById(R.id.height);
        remarksInput = findViewById(R.id.UFinput);
        dateInput = findViewById(R.id.editTextDate);
        timeInput = findViewById(R.id.editTextTime);
        submit = findViewById(R.id.skipsubmit);
        timeInput.setFocusable(false);
        dateInput.setFocusable(false);
        changeColorCategory = findViewById(R.id.changeColorCategory);
        changeTypeColor = findViewById(R.id.changeColorType);
        changeColorColor = findViewById(R.id.changeColorColor);
        changeColorLocation = findViewById(R.id.changeColorLocation);
        getChangeColorTextTime = findViewById(R.id.changeColorTime);
        sizeInputLayout = findViewById(R.id.sizeInputLayout);
        shapeInputLayout = findViewById(R.id.shapeInputLayout);
        dimensionInputLayout = findViewById(R.id.dimensionInputLayout);
        autoCompleteRoom = findViewById(R.id.roomDropdown);
        roomInputLayout = findViewById(R.id.roomInputLayout);
        locationLayout = findViewById(R.id.locationLinear);
        chipLocationLayout = findViewById(R.id.chipLocationLayout);
    }

    private void setUpDropdowns(){
        setupDropdown(autoCompleteLocation, LOCATION_OPTIONS, chipLocation);
        setupDropdown(autoCompleteColor, COLOR_OPTIONS, chipColor);
        setupDropdown(autoCompleteCategory, CATEGORY_OPTIONS, chipCategory);
        setupDropdown(autoCompleteSizes, SIZES_OPTIONS,null);
        setupDropdown(autoCompleteShape, SHAPE_OPTIONS, null);
    }

    private void setUpClickListeners() {
        addCategoryButton.setOnClickListener(v -> autoCompleteCategory.showDropDown());
        addColorButton.setOnClickListener(v -> autoCompleteColor.showDropDown());
        addLocationButton.setOnClickListener(v -> autoCompleteLocation.showDropDown());
        dateInput.setOnClickListener(v -> showDatePickerDialog());
        timeInput.setOnClickListener(v -> showTimePickerDialog());
        submit.setOnClickListener(v-> submitGeneratedLost());
    }

    //text to tag conversion when other field is clicked
    private void setupTextInput(TextInputEditText textInput, ChipGroup chipGroup) {
        textInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addChip(chipGroup, textInput.getText().toString());
                return true;
            }
            return false;
        });
        textInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = textInput.getText().toString().trim();
                if (!text.isEmpty()) {
                    addChip(chipGroup, text);
                }
            }
        });
    }

    //inflate chip
    private void addChip(ChipGroup chipGroup, String text) {
        if (isChipAlreadySelected(chipGroup, text)) {
            Toast.makeText(this, "Item already selected", Toast.LENGTH_SHORT).show();
        } else if (chipGroup.getChildCount() < 4) {
            Chip newChip = createChip(chipGroup, text);
            chipGroup.addView(newChip);
            initializeChipRemoval(newChip, chipGroup, null);
        } else {
            Toast.makeText(this, "Chips are limited to 4", Toast.LENGTH_SHORT).show();
        }
    }

    //apply text to tag conversion to attributes
    private void setUpTags(){
        setupTextInput(typeTags, chipType);
        setupTextInput(brandTags, chipBrand);
        setupTextInput(modelTags, chipModel);
        setupTextInput(textTags, chipText);
    }

    private void submitGeneratedLost(){
        setArrayListChipText();
        setStringChipText();

        int redColor = ContextCompat.getColor(this, R.color.red);
        int defaultColor = ContextCompat.getColor(this, R.color.darkgray);
        if (categories.isEmpty()) {
            emptyFieldsException("", "Category", changeColorCategory);
            return;
        } else {
            emptyFieldsException("nonEmpty", "Category", changeColorCategory);
        }
        if (types.isEmpty()) {
            emptyFieldsException("", "Type", changeTypeColor);
            return;
        } else {
            emptyFieldsException("nonEmpty", "Type", changeTypeColor);
        }
        if (colors.isEmpty()) {
            emptyFieldsException("", "Color", changeColorColor);
            return;
        } else {
            emptyFieldsException("nonEmpty", "Color", changeColorColor);
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "Location is empty.", Toast.LENGTH_SHORT).show();
            changeColorLocation.setTextColor(redColor);
            return;
        } else {
            changeColorLocation.setTextColor(defaultColor);
        }
        if (time.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Time/Date is empty.", Toast.LENGTH_SHORT).show();
            getChangeColorTextTime.setTextColor(redColor);
            return;
        }
        if (!validateTags()) {
            return; // dont proceed if validation fails
        }
        infoToNextIntent(ConfirmationLost.class);
    }

}