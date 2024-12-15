package com.example.retrievision;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Generated_Found extends Abstract_Generated {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable( this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.generated);

        InitIDs();
        setupClickListeners();
        setUpDropdowns();
        autogenLostTags();

    }
    private void autogenLostTags(){
        Intent intent = getIntent();

        String highestObject = intent.getStringExtra("highestObject");
        if (highestObject != null) {
            addChipToGroup(chipType, highestObject);
        } else {
            createChip(chipType, "Add a Tag");
        }

        String dominantColors = intent.getStringExtra("dominant_colors");

        if (dominantColors != null) {
            for (String color : dominantColors.split("\n")) {
                addChipToGroup(chipColor, color);
            }
        } else {
            createChip(chipType, "Add a Tag");
        }



        Set<String> electronics = new HashSet<>(Arrays.asList("Keyboard", "Earphones", "Flash Drive", "Charger", "Smartphone", "Headphones", "Wireless Earphones"));
        Set<String> personalBelongings = new HashSet<>(Arrays.asList("Keys", "Money", "ID", "Water Bottle", "Wallet"));
        Set<String> accessories = new HashSet<>(Arrays.asList("Cap", "Watch", "Glasses"));
        Set<String> clothing = new HashSet<>(Arrays.asList("Jacket", "Tshirt"));
        Set<String> stationary = Collections.singleton("Pen");

        if (electronics.contains(highestObject)) {
            addChipToGroup(chipCategory, "Electronics");
        } else if (personalBelongings.contains(highestObject)) {
            addChipToGroup(chipCategory, "Personal Belongings");
        } else if (accessories.contains(highestObject)) {
            addChipToGroup(chipCategory, "Accessory");
        } else if (clothing.contains(highestObject)) {
            addChipToGroup(chipCategory, "Clothing");
        } else if (stationary.contains(highestObject)) {
            addChipToGroup(chipCategory, "Stationary");
        }

        View categoryIndexOne = chipCategory.getChildAt(0);
        if (categoryIndexOne instanceof Chip) {
            Chip chip = (Chip) categoryIndexOne;
            String chipText = chip.getText().toString();
            if ("Electronics".equals(chipText)) {
                chipSize.setVisibility(View.GONE);
            } else if ("Personal Belongings".equals(chipText) || "Stationary".equals(chipText) || "Accessory".equals(chipText)) {
                chipSize.setVisibility(View.GONE);
                chipDimension.setVisibility(View.GONE);
            } else if ("Clothing".equals(chipText)) {
                chipShape.setVisibility(View.GONE);
                chipDimension.setVisibility(View.GONE);
            }
        }
    }

    private void InitIDs(){

        autoCompleteCategory = findViewById(R.id.categoryDropdown);
        autoCompleteLocation = findViewById(R.id.locationDropdown);
        autoCompleteSizes = findViewById(R.id.sizeDropdown);
        autoCompleteShape = findViewById(R.id.shapeDropdown);
        autoCompleteColor = findViewById(R.id.colorDropdown);

        chipCategory = findViewById(R.id.chipCategory);
        addCategoryButton = findViewById(R.id.categoryButton);

        chipType = findViewById(R.id.chipType);
        addTypeButton = findViewById(R.id.addTypeButton);

        chipColor = findViewById(R.id.chipColor);
        addColorButton = findViewById(R.id.addColorButton);

        chipModel = findViewById(R.id.chipModel);
        addModelButton = findViewById(R.id.addModelButton);

        chipBrand = findViewById(R.id.chipBrand);
        addBrandButton = findViewById(R.id.addBrandButton);

        chipText = findViewById(R.id.chipText);
        addTextButton = findViewById(R.id.addTextButton);

        widthInput = findViewById(R.id.widthInput);
        heightInput = findViewById(R.id.heightInput);
        remarksInput = findViewById(R.id.remarksInput);

        dateInput = findViewById(R.id.editTextDate);
        timeInput = findViewById(R.id.editTextTime);

        chipLocation = findViewById(R.id.chipLocation);
        submit = findViewById(R.id.submitfoundobject);
        dateInput.setFocusable(false);
        timeInput.setFocusable(false);

        chipSize = findViewById(R.id.chipSize);
        chipShape = findViewById(R.id.chipShape);
        chipDimension = findViewById(R.id.chipDimension);

        changeColorTextLocation = findViewById(R.id.TextLocation);
        getChangeColorTextTime = findViewById(R.id.TextTime);

        changeColorColor = findViewById(R.id.changeColorColor);
        changeColorCategory = findViewById(R.id.changeColorCategory);
        autoCompleteRoom = findViewById(R.id.roomDropdown);
        roomInputLayout = findViewById(R.id.roomInputLayout);

        typeTextView = findViewById(R.id.type);
    }
    //apply adding a tag function to all buttons
    private void setupClickListeners() {
        addTypeButton.setOnClickListener(v -> addNewNextChip(chipType, addTypeButton));
        addModelButton.setOnClickListener(v -> addNewNextChip(chipModel, addModelButton));
        addBrandButton.setOnClickListener(v -> addNewNextChip(chipBrand, addBrandButton));
        addTextButton.setOnClickListener(v -> addNewNextChip(chipText, addTextButton));
        addCategoryButton.setOnClickListener(v -> autoCompleteCategory.showDropDown());
        addColorButton.setOnClickListener(v -> autoCompleteColor.showDropDown());
        dateInput.setOnClickListener(v -> showDatePickerDialog());
        timeInput.setOnClickListener(v -> showTimePickerDialog());
        submit.setOnClickListener(v->{
            SubmitGeneratedFound();
        });
    }

    //adding a new tag
    private void addNewNextChip(final ChipGroup chipGroup, Button addbutton) {
        if (chipGroup.getChildCount() -2 < 5) {
            newChip = createChip(chipGroup,"Add a Tag");
            updateChipText(newChip,chipGroup);
            chipGroup.addView(newChip, chipGroup.getChildCount() - 1);
            initializeChipRemoval(newChip, chipGroup, addbutton);
        } else {
            Toast.makeText(this, "Tags are only limited to 5", Toast.LENGTH_SHORT).show();
        }
    }
    //tag will appear before the add button
    @SuppressLint("SetTextI18n")
    private void updateChipText(Chip chip, ChipGroup chipGroup) {
        int childCount = chipGroup.getChildCount() - 1;
        chip.setText("Add a Tag (" + childCount + ")");
    }
    //autocomplete function
    private void setUpDropdowns(){
        setUpLocationDropdown(autoCompleteLocation, chipLocation);
        setupDropdown(autoCompleteCategory, CATEGORY_OPTIONS, chipCategory);
        setupDropdown(autoCompleteColor, COLOR_OPTIONS, chipColor);
        setupDropdown(autoCompleteSizes, SIZES_OPTIONS,null);
        setupDropdown(autoCompleteShape, SHAPE_OPTIONS, null);
    }

    //dropdown for a single location
        private void setUpLocationDropdown(AutoCompleteTextView dropdown, ChipGroup chipGroup) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.inf_dropdown_list, LOCATION_OPTIONS);
            dropdown.setAdapter(adapter);
            dropdown.setOnItemClickListener((adapterView, view, i, l) -> {
                String selectedItem = adapter.getItem(i);
                if (chipGroup != null) {
                    clearChips(chipGroup);
                    handleSelectionDropdown(chipGroup, selectedItem);
                }
                if (selectedItem.equals("2nd Floor")) {
                    setRoomFound(ROOM_OPTIONS_2ND);

                } else if (selectedItem.equals("Ground Floor")) {
                    setRoomFound(ROOM_OPTIONS_1ST);
                } else if (selectedItem.equals("3rd Floor")) {
                    setRoomFound(ROOM_OPTIONS_3RD);
                } else if (selectedItem.equals("Roof Deck")) {
                    roomInputLayout.setVisibility(View.GONE);
                } else if (selectedItem.equals("1st Floor (Faculty and Conference Room)")) {
                    roomInputLayout.setVisibility(View.GONE);
                } else if (selectedItem.equals("1st Floor (Mezzanine)")) {
                    roomInputLayout.setVisibility(View.GONE);
                } else if (selectedItem.equals("4th Floor (Multipurpose Hall)")) {
                    roomInputLayout.setVisibility(View.GONE);
                } else if (selectedItem.equals("4th Floor (Library)")) {
                    roomInputLayout.setVisibility(View.GONE);
                }
            });
        }

        //replace location
        private void handleSelectionDropdown(ChipGroup chipGroup, String selectedItem) {
            if (isChipAlreadySelected(chipGroup, selectedItem)) {
                Toast.makeText(this, "Item already selected", Toast.LENGTH_SHORT).show();
            } else {
                if (chipGroup.getChildCount() > 0) {
                    chipGroup.removeViewAt(0);
                }
                Chip newChip = createChip(chipGroup, selectedItem);
                chipGroup.addView(newChip);

            }
        }

    private void setRoomFound(String [] room_option) {
        roomInputLayout.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.inf_dropdown_list, room_option);
        autoCompleteRoom.setAdapter(adapter);
        autoCompleteRoom.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedItem = adapter.getItem(i);
            if (chipLocation != null) {
                if (isChipAlreadySelected(chipLocation, selectedItem)) {
                    Toast.makeText(this, "Item already selected", Toast.LENGTH_SHORT).show();
                } else {
                    // If not already selected, create a new chip
                    if (chipLocation.getChildCount() <=1) {
                        Chip newChip = createChip(chipLocation, selectedItem);
                        chipLocation.addView(newChip);

                    } else {
                        Toast.makeText(this, "Tags are limited to 2", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void clearChips(ChipGroup chipGroup) {
        if (chipGroup != null) {
            chipGroup.removeAllViews();
        }
    }

    private void SubmitGeneratedFound() {
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
            Toast.makeText(this, "Location is empty.", Toast.LENGTH_SHORT).show();
            typeTextView.setTextColor(redColor);
            ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.post(() -> {
                scrollView.smoothScrollTo(0, 0);
            });
            return;
        } else {
            typeTextView.setTextColor(defaultColor);
        }
        if (colors.isEmpty()) {
            emptyFieldsException("", "Color", changeColorColor);
            return;
        } else {
            emptyFieldsException("nonEmpty", "Color", changeColorColor);
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "Location is empty.", Toast.LENGTH_SHORT).show();
            changeColorTextLocation.setTextColor(redColor);
            return;
        } else {
            changeColorTextLocation.setTextColor(defaultColor);
        }
        if (time.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Time/Date is empty.", Toast.LENGTH_SHORT).show();
            getChangeColorTextTime.setTextColor(redColor);
            return;
        }

        if (!validateTags()) {
            return;
        }


        infoToNextIntent(ConfirmationFound.class);
    }
    private void addChipToGroup(ChipGroup chipGroup, String text) {
        if (chipGroup.getChildCount() < 4) {
            Chip newChip = createChip(chipGroup, text);
            chipGroup.addView(newChip, chipGroup.getChildCount() - 1);
        }
    }
}
