package com.example.retrievision;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Abstract_Generated extends AppCompatActivity {
    protected TextInputLayout changeColorCategory, changeTypeColor, changeColorColor,sizeInputLayout, shapeInputLayout;
    protected LinearLayout dimensionInputLayout;
    protected TextView changeColorLocation, changeColorTime;
    protected Typeface typeface;
    protected TextView typeTextView;
    protected TextView changeColorTextLocation, getChangeColorTextTime;
    protected LinearLayout chipSize, chipShape, chipDimension;
    protected ChipGroup chipCategory, chipColor, chipLocation, chipType, chipBrand, chipModel, chipText;
    protected Button addCategoryButton, addColorButton, addLocationButton, submit;
    protected TextInputEditText typeTags,brandTags, modelTags, heightInput, textTags,widthInput, remarksInput, timeInput,dateInput;
    protected AutoCompleteTextView autoCompleteLocation, autoCompleteCategory, autoCompleteSizes, autoCompleteShape, autoCompleteColor;
    protected static final String[] LOCATION_OPTIONS = {"Ground Floor", "1st Floor (Faculty and Conference Room)", "1st Floor (Mezzanine)", "2nd Floor", "3rd Floor", "4th Floor (Multipurpose Hall)", "4th Floor (Library)", "Roof Deck", "Court", "Parking"};
    protected static final String[] CATEGORY_OPTIONS = {"Personal Belongings", "Electronics", "Clothing", "Stationary", "Accessory"};
    protected static final String [] COLOR_OPTIONS = {"Black", "White", "Red", "Yellow", "Blue","Purple", "Pink", "Green","Gray", "Orange", "Brown","Silver", "Gold", "Transparent", "Rainbow"};
    protected static final String[] SIZES_OPTIONS = {"Extra Small", "Small", "Medium", "Large", "Extra Large"};
    protected static final String[] SHAPE_OPTIONS = {"Circle", "Square", "Rectangle", "Oblong", "Triangle"};

    protected static final String [] ROOM_OPTIONS_1ST = {"101","102","103","Women's Restroom (G)", "Men's Restroom (G)"};
    protected static final String [] ROOM_OPTIONS_2ND = {"Men's Restroom (2nd)","201","202","203","204","205","206","207","Women's Restroom","208","209","210","211","212","213"};
    protected static final String [] ROOM_OPTIONS_3RD = {"Men's Restroom (3rd)","301","302","303","304","305","306","Women's Restroom (3rd)","307","308","309","310","MIS","311"};

    protected Chip newChip;
    protected Button addTypeButton, addModelButton, addBrandButton, addTextButton;
    protected ArrayList<String> categories,types, colors,models, brands, texts, location;
    protected String time, date, size, shape, width, height, remarks, photoPath;

    protected AutoCompleteTextView autoCompleteRoom;
    protected TextInputLayout roomInputLayout;
    LinearLayout locationLayout;
    //inflating the tag
    protected Chip createChip(final ChipGroup chipGroup, String placeholder) {
        LayoutInflater inflater = LayoutInflater.from(this);
        Chip chip = (Chip) inflater.inflate(R.layout.inf_chip, chipGroup, false);
        chip.setText(placeholder);
        chip.setCloseIconVisible(true);
        chip.setCloseIconResource(R.drawable.icon_close);
        chip.setOnCloseIconClickListener(v -> {
                    if (chip.getText().toString().equals("2nd Floor")) {
                        removeRoomChips(chipGroup);
                    }
                    chipGroup.removeView(chip);
                });
        chipDesign(chip);
        return chip;
    }

    private void removeRoomChips(ChipGroup chipGroup) {
        for (String roomNumber : ROOM_OPTIONS_2ND) {
            for (int j = 0; j < chipGroup.getChildCount(); j++) {
                View chipView = chipGroup.getChildAt(j);
                if (chipView instanceof Chip && ((Chip) chipView).getText().toString().equals(roomNumber)) {
                    chipGroup.removeView(chipView);
                    j--;
                }
            }
        }
    }

    //design of tag
    private void chipDesign(Chip chip) {
        chip.setTypeface(typeface);
        chip.setTextColor(ColorStateList.valueOf(Color.parseColor("#DBDBDB")));
        chip.setChipMinHeight(90);
    }
    protected void initializeChipRemoval(final Chip chip, final ChipGroup chipGroup, Button addButton) {
        chip.setOnClickListener(v -> editChip(chip));
    }
    //edit the tag
    protected void editChip(Chip chip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.inf_edit_chip, null);
        final EditText editTextChipText = dialogView.findViewById(R.id.editTextChipText);
        Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdate);
        editTextChipText.setText(chip.getText());
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        buttonUpdate.setOnClickListener(v -> {
            String newText = editTextChipText.getText().toString();
            if (newText.isEmpty() || newText.trim().isEmpty()){
                ViewGroup viewGroup = (ViewGroup) chip.getParent();
                if (viewGroup != null){
                    viewGroup.removeView(chip);
                }
            } else {chip.setText(newText);}
            dialog.dismiss();
        });
    }

    //set up the dropdowns
    protected void setupDropdown(AutoCompleteTextView dropdown, String[] items, ChipGroup chipGroup) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.inf_dropdown_list, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedItem = adapter.getItem(i);
            if (chipGroup != null) {
                handleDropdownItemSelected(chipGroup, selectedItem);
            }
            if (selectedItem.equals("2nd Floor")) {
                setRoomVisibility(ROOM_OPTIONS_2ND);
            } else if (selectedItem.equals("Ground Floor")) {
                setRoomVisibility(ROOM_OPTIONS_1ST);
            } else if (selectedItem.equals("3rd Floor")) {
                setRoomVisibility(ROOM_OPTIONS_3RD);
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

    protected void setRoomVisibility(String [] room_option){
        roomInputLayout.setVisibility(View.VISIBLE);
        roomDropdown(room_option);

    }

    private void roomDropdown(String [] room_option){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.inf_dropdown_list, room_option);
        autoCompleteRoom.setAdapter(adapter);
        autoCompleteRoom.setOnItemClickListener((adapterView, view, i, l) -> {
            String selectedItem = adapter.getItem(i);
            if (chipLocation != null) {
                handleDropdownItemSelected(chipLocation, selectedItem);
            }
        });
    }

LinearLayout chipLocationLayout;
    protected void handleDropdownItemSelected(ChipGroup chipGroup, String selectedItem) {
        if (isChipAlreadySelected(chipGroup, selectedItem)) {
            Toast.makeText(this, "Item already selected", Toast.LENGTH_SHORT).show();
        } else {
            if (chipGroup.getChildCount() < 5) {
                Chip newChip = createChip(chipGroup, selectedItem);
                chipGroup.addView(newChip);

            } else {
                Toast.makeText(this, "Tags are limited to 5", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected boolean isChipAlreadySelected(ChipGroup chipGroup, String selectedItem) {
        for (int j = 0; j < chipGroup.getChildCount(); j++) {
            View chipView = chipGroup.getChildAt(j);
            if (chipView instanceof Chip && ((Chip) chipView).getText().toString().equals(selectedItem)) {
                return true;
            }
        }
        return false;
    }

    protected ArrayList<String> getChipsText(ChipGroup chipGroup) {
        ArrayList<String> texts = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View chipView = chipGroup.getChildAt(i);
            if (chipView instanceof Chip) {
                texts.add(((Chip) chipView).getText().toString());
            }
        }
        return texts;
    }

    protected void emptyFieldsException(String input, String inputType, TextInputLayout textColorLayout) {
        if (input.isEmpty()) {
            Toast.makeText(this, inputType + " is Empty", Toast.LENGTH_SHORT).show();
            int redColor = ContextCompat.getColor(this, R.color.red);
            textColorLayout.requestFocus();
            textColorLayout.setBoxStrokeColor(redColor);
            textColorLayout.setDefaultHintTextColor(ColorStateList.valueOf(redColor));
            ScrollView scrollView = findViewById(R.id.scrollView);
            scrollView.post(() -> {
                scrollView.smoothScrollTo(0, 0);
            });
        } else {
            int defaultColor = ContextCompat.getColor(this, R.color.darkgray);
            textColorLayout.setBoxStrokeColor(defaultColor);
            textColorLayout.setDefaultHintTextColor(ColorStateList.valueOf(defaultColor));
        }
    }
    // Show date picker dialog
    protected void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();

        Calendar minDateCalendar = (Calendar) calendar.clone();
        minDateCalendar.add(Calendar.MONTH, -1);

        long minDate = minDateCalendar.getTimeInMillis();
        long maxDate = calendar.getTimeInMillis();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            dateInput.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(minDate);
        datePickerDialog.getDatePicker().setMaxDate(maxDate);

        datePickerDialog.show();
    }


    // Show time picker dialog
    protected void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        @SuppressLint("DefaultLocale") TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String timeFormat = (hourOfDay >= 12) ? "PM" : "AM";
            timeInput.setText(String.format("%02d:%02d %s", (hourOfDay > 12) ? hourOfDay - 12 : hourOfDay, minute, timeFormat));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
        timePickerDialog.show();
    }

    //get the text from chips
    protected void setArrayListChipText(){
        categories = getChipsText(chipCategory);
        types = getChipsText(chipType);
        colors = getChipsText(chipColor);
        models = getChipsText(chipModel);
        brands = getChipsText(chipBrand);
        texts = getChipsText(chipText);
        location = getChipsText(chipLocation);
    }

    protected void setStringChipText(){
        time = timeInput.getText().toString();
        date = dateInput.getText(). toString();
        size = autoCompleteSizes.getText().toString();
        shape = autoCompleteShape.getText().toString();
        width = widthInput.getText().toString();
        height = heightInput.getText().toString();
        remarks = remarksInput.getText().toString();
        photoPath = getIntent().getStringExtra("photoPath");
    }


    protected void infoToNextIntent(Class<?> confirmation){
        Intent passOnConfirmationIntent = new Intent(this, confirmation);

        passOnConfirmationIntent.putExtra("photoPath", photoPath);

        passOnConfirmationIntent.putStringArrayListExtra("category", categories);
        passOnConfirmationIntent.putStringArrayListExtra("type", types);
        passOnConfirmationIntent.putStringArrayListExtra("color", colors);
        passOnConfirmationIntent.putStringArrayListExtra("model", models);
        passOnConfirmationIntent.putStringArrayListExtra("brand", brands);
        passOnConfirmationIntent.putStringArrayListExtra("texts", texts);

        passOnConfirmationIntent.putStringArrayListExtra("selectedLocation", location);
        passOnConfirmationIntent.putExtra("selectedSize", size);
        passOnConfirmationIntent.putExtra("selectedShape", shape);
        passOnConfirmationIntent.putExtra("width", width);
        passOnConfirmationIntent.putExtra("height", height);
        passOnConfirmationIntent.putExtra("remarks", remarks);
        passOnConfirmationIntent.putExtra("date", date);
        passOnConfirmationIntent.putExtra("time", time);

        displayName(passOnConfirmationIntent);

        startActivity(passOnConfirmationIntent);
    }
    protected void displayName(Intent intent){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String userID = firebaseAuth.getUid();

        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            intent.putExtra("displayName", displayName);
            intent.putExtra("userID", userID);
        }
    }

    protected boolean validateTags() {
        Set<String> typeTagsSet = new HashSet<>();
        Set<String> brandTagsSet = new HashSet<>();
        Set<String> modelTagsSet = new HashSet<>();

        for (int i = 0; i < chipType.getChildCount(); i++) {
            View chipView = chipType.getChildAt(i);

            if (chipView instanceof Chip) {
                Chip chip = (Chip) chipView;
                String chipText = chip.getText().toString().trim();

                if (chipText.contains("Add a Tag")) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Please add a valid Type tag.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (chipText.length() == 1) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Type tag cannot be just one character.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (chipText.matches("\\d+")) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Type tag cannot be just numbers.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isTagUnique(typeTagsSet, chipText)) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Duplicate Type tag detected.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                typeTagsSet.add(normalizeTag(chipText));
                setChipErrorState(chip, false);
            }
        }

        for (int i = 0; i < chipBrand.getChildCount(); i++) {
            View chipView = chipBrand.getChildAt(i);

            if (chipView instanceof Chip) {
                Chip chip = (Chip) chipView;
                String chipText = chip.getText().toString().trim();

                if (chipText.contains("Add a Tag")) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Please add a valid Brand tag.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (chipText.length() == 1) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Brand tag cannot be just one character.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isTagUnique(brandTagsSet, chipText)) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Duplicate Brand tag detected.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                brandTagsSet.add(normalizeTag(chipText));
                setChipErrorState(chip, false);
            }
        }

        for (int i = 0; i < chipModel.getChildCount(); i++) {
            View chipView = chipModel.getChildAt(i);

            if (chipView instanceof Chip) {
                Chip chip = (Chip) chipView;
                String chipText = chip.getText().toString().trim();

                if (chipText.contains("Add a Tag")) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Please add a valid Model tag.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (chipText.length() == 1) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Model tag cannot be just one character.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isTagUnique(modelTagsSet, chipText)) {
                    setChipErrorState(chip, true);
                    scrollToChip(chip);
                    Toast.makeText(this, "Duplicate Model tag detected.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                modelTagsSet.add(normalizeTag(chipText));
                setChipErrorState(chip, false);
            }
        }

        return true;
    }

    private boolean isTagUnique(Set<String> tagsSet, String tag) {
        String normalizedTag = normalizeTag(tag);
        return !tagsSet.contains(normalizedTag);
    }

    private String normalizeTag(String tag) {
        return tag.trim().toLowerCase();
    }

    private void setChipErrorState(Chip chip, boolean isError) {
        int redColor = ContextCompat.getColor(this, R.color.red);
        int defaultColor = ContextCompat.getColor(this, R.color.white); // Default border color

        if (isError) {
            chip.setChipStrokeColor(ColorStateList.valueOf(redColor));
            chip.setChipStrokeWidth(3);
            chip.setTextColor(ColorStateList.valueOf(redColor));
        } else {
            chip.setChipStrokeColor(ColorStateList.valueOf(defaultColor));
            chip.setChipStrokeWidth(1);
            chip.setTextColor(ColorStateList.valueOf(defaultColor));
        }
    }

    private void scrollToChip(View chip) {
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> {
            int yOffset = chip.getTop();
            scrollView.smoothScrollTo(0, yOffset);
        });
    }

}

