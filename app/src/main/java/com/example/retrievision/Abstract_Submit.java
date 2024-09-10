package com.example.retrievision;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.retrievision.ml.Rtrv;
import com.google.android.material.card.MaterialCardView;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class Abstract_Submit extends AppCompatActivity {
    protected Button submitImage;
    protected ImageView openCamera, openGallery;
    protected int imageSize = 224, maxPos;
    protected ActivityResultLauncher<Intent> pictureLauncher;
    protected String currentPhotoPath;
    protected String[] classes = {"Cap", "Charger", "Earphones", "Flash Drive", "Glasses", "Headphones", "ID", "Jacket", "Keyboard", "Keys", "Money", "Pen", "Smartphone", "Tshirt", "Umbrella", "Wallet", "Watch", "Water Bottle", "Wireless Earphones"};

    protected abstract void initActivityResultLauncher();
    protected void setImageView(ImageView setImage) {
        File imgFile = new File(currentPhotoPath);
        if (imgFile.exists()) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            if (imageBitmap != null) {
                Bitmap resizedBitmap = forHQView(imageBitmap);
                setImage.setImageBitmap(resizedBitmap);
            } else {
                Log.e("Error", "Failed to decode image from path: " + currentPhotoPath);
            }
        } else {
            Log.e("Error", "Image file not found: " + currentPhotoPath);
        }
    }

    protected Bitmap forHQView(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("Error", "Bitmap is null in forHQView");
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratioBitmap = (float) width / height;
        float ratioMax = (float) 4096 / 4096;

        int finalWidth = 4096;
        int finalHeight = 4096;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) (4096 * ratioBitmap);
        } else {
            finalHeight = (int) (4096 / ratioBitmap);
        }
        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true);
    }

    //image classification

    protected void ImageClassificationModel(Bitmap image){
            try {
                Rtrv model = Rtrv.newInstance(getApplicationContext());
                TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1,224,224,3}, DataType.FLOAT32);

                ByteBuffer byteBuffer =ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
                byteBuffer.order(ByteOrder.nativeOrder());

                int[] intValues = new int[imageSize * imageSize];
                image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                int pixel = 0;

                for(int i = 0; i < imageSize; i++){
                    for(int j = 0; j < imageSize; j++){
                        int val = intValues[pixel++];
                        byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                        byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                        byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
                    }
                } inputBuffer.loadBuffer(byteBuffer);

                Rtrv.Outputs outputs = model.process(inputBuffer);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                float[] confidences = outputFeature0.getFloatArray();
                maxPos=0;
                float maxConfidence = 0;
                for(int i = 0; i < confidences.length; i++){
                    if(confidences[i] > maxConfidence){
                        maxConfidence = confidences[i];
                        maxPos = i;
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
    }
    protected void ImageClassification(){
        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        Bitmap resizedImage = ThumbnailUtils.extractThumbnail(imageBitmap, imageSize, imageSize);
        ImageClassificationModel(resizedImage);
    }


    protected void showDialog(String message, boolean isError, Class className) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.inf_dialog, null);
        TextView resultTextView = dialogView.findViewById(R.id.objectresult);
        MaterialCardView materialCardView = dialogView.findViewById(R.id.RectangleView);
        TextView headerTextView = dialogView.findViewById(R.id.resultheader);
        Button cancelButton = dialogView.findViewById(R.id.noButton);
        Button yesButton = dialogView.findViewById(R.id.yesButton);

        resultTextView.setText(message);
        if (isError) {
            materialCardView.setStrokeColor(Color.parseColor("#D6293E"));
            headerTextView.setText("Photo required.");
            yesButton.setVisibility(View.GONE);
            cancelButton.setText("Cancel");
            resultTextView.setText("Image cannot be empty!");
        }

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        cancelButton.setOnClickListener(v -> alertDialog.dismiss());
        yesButton.setOnClickListener(v -> {
            Intent objectIntent = new Intent(this, className);
            objectIntent.putExtra("highestObject", classes[maxPos]);
            //objectIntent.putExtra("dominant_colors", colorStringBuilder.toString());
            objectIntent.putExtra("photoPath", currentPhotoPath);
            startActivity(objectIntent);
        });
    }

    protected void nullDialog() {
        showDialog("Image cannot be empty!", true, null);
    }
    protected void clearImageView(ImageView imageView) {
        imageView.setImageBitmap(null);
        currentPhotoPath = null;
    }

}



