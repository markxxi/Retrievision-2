package com.example.retrievision;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Looper;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;

import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import com.example.retrievision.ml.Rtrv2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;


import soup.neumorphism.NeumorphButton;

public abstract class Abstract_Submit extends AppCompatActivity {

    protected Button submitImage;
    protected ImageView openCamera, openGallery;
    protected int imageSize = 224, maxPos;
    protected ActivityResultLauncher<Intent> pictureLauncher;
    protected String currentPhotoPath;
    protected String[]  classes = {
            "bag", "ballpen", "belt", "blazer", "bracelet", "calculator", "camera", "cap", "card", "charger",
            "charging cable",  "earphones", "eraser", "eyeglasses", "flashdrive", "gimbal", "headphones", "heels",
            "id", "jacket", "keyboard", "keys", "laptop", "marker", "money", "mouse", "necktie",
            "notebook", "pants", "pencil", "powerbank", "purse", "remote", "ruler", "scissors", "shoes",
            "smartphone", "socks", "stapler", "tshirt", "umbrella", "wallet", "watch", "water bottle", "wireless earphones", "yellow pad"};
    protected NeumorphButton skip;
    ProgressBar progressBar;
    protected abstract void initActivityResultLauncher();

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private CountDownLatch latch = new CountDownLatch(2);

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

    protected void ImageClassificationModel(Bitmap image) {
        try {
            Rtrv2 model = Rtrv2.newInstance(getApplicationContext());
            TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;

            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255));
                }
            }
            inputBuffer.loadBuffer(byteBuffer);

            Rtrv2.Outputs outputs = model.process(inputBuffer);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            float[] confidences = outputFeature0.getFloatArray();
            maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void ImageClassification() {
        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        Bitmap resizedImage = ThumbnailUtils.extractThumbnail(imageBitmap, imageSize, imageSize);
        showProgressBar();
        executorService.execute(() -> {
            ImageClassificationModel(resizedImage);
            latch.countDown();
        });

        executorService.execute(() -> {
            scannedColor();
            latch.countDown();
        });
        executorService.execute(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    hideProgressBar();
                    dialogFound();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    protected void ImageClassificationLost() {
        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        Bitmap resizedImage = ThumbnailUtils.extractThumbnail(imageBitmap, imageSize, imageSize);
        showProgressBar();
        executorService.execute(() -> {
            ImageClassificationModel(resizedImage);
            latch.countDown();
        });

        executorService.execute(() -> {
            scannedColor();
            latch.countDown();
        });
        executorService.execute(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    hideProgressBar();
                    dialog();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
    protected void dialogFound(){
        String result = classes[maxPos];
        showDialog(result, false, Generated_Found.class);
    }
    private void dialog(){
        String result = classes[maxPos];
        showDialog(result, false, Skip.class);
    }
    protected void scannedColor() {
        Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
        Bitmap resizedBitmap = ThumbnailUtils.extractThumbnail(imageBitmap, imageSize, imageSize);
        extractColorsFromImage(resizedBitmap);
    }
    protected StringBuilder colorStringBuilder = new StringBuilder();
    protected void extractColorsFromImage(Bitmap bitmap) {
        Python py = Python.getInstance();
        PyObject module = py.getModule("colorIdentifier");

        if (currentPhotoPath == null) {
            Log.e("Error", "currentPhotoPath is null");
            return;
        }

        String imagePath = currentPhotoPath;
        PyObject result = module.callAttr("get_colors", imagePath);

        List<PyObject> resultList = result.asList();
        for (PyObject color : resultList) {
            colorStringBuilder.append(color.toString()).append("\n");
        }
        Log.e("Colors: ", resultList.toString());

    }

    private void showProgressBar() {
        runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideProgressBar() {
        runOnUiThread(() -> {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    protected abstract void showDialog(String message, boolean isError, Class className);

    protected void nullDialog() {
        showDialog("Image cannot be empty!", true, null);
    }

    protected void clearImageView(ImageView imageView) {
        imageView.setImageBitmap(null);
        currentPhotoPath = null;
    }

    protected void startPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }




}

