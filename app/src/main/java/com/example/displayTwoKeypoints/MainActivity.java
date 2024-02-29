package com.example.displayTwoKeypoints;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public int maxRes = 1360; // We scale our image to the resolution of maximum 1360 pixels
    public double[][] greyC = new double[maxRes][maxRes]; // Current grey image
    public double[][] maskS0 = new double[5][5]; // Mask with the Gaussian blur function's values
    public double[][] maskS1 = new double[5][5]; // Mask with the Gaussian blur function's values
    public double[][] maskS2 = new double[7][7]; // Mask with the Gaussian blur function's values
    public double[][] maskS3 = new double[9][9]; // Mask with the Gaussian blur function's values
    public double[][] maskS4 = new double[11][11]; // Mask with the Gaussian blur function's values
    public double[][] maskS = new double[137][137]; // Mask with the Gaussian blur function's values
    public double[][] octave1000First = new double[maxRes][maxRes]; // Here, the figure will have the white border
    public double[][] octave1000Second = new double[maxRes][maxRes]; // Here, the figure will have the white border
    public double[][] octave1000Third = new double[maxRes][maxRes]; // Here, the figure will have the white border
    public double[][] octave1000Fourth = new double[maxRes][maxRes]; // Here, the figure will have the white border
    public double[][] octave1000Fifth = new double[maxRes][maxRes]; // Here, the figure will have the white border
    public double[][] DoG1000First = new double[maxRes][maxRes];
    public double[][] DoG1000Second = new double[maxRes][maxRes];
    public double[][] DoG1000Third = new double[maxRes][maxRes];
    public double[][] DoG1000Fourth = new double[maxRes][maxRes];
    public double[][] Hessian = new double[2][2]; // 2x2 Hessian matrix
    public int[][] keypoints1000 = new int[3000][2]; // Info about keypoints
    public int radius0, radius1, radius2, radius3, radius4, MatrixBorder, flagMax, flagMin, nk, maxNoKeyPoints = 100; // maxNoKeyPoints - maximum number of keypoints
    public double minFirst, minSecond, sigma0, sigma1, sigma2, sigma3, sigma4, max, min, trace, det, threshold = 7.65; // 7.65 = 255 * 0.03;
    public double[] xk = new double[291]; // Coordinates of keypoints' net: 25 keypoints (1st level) + 58 keypoints (2nd level; 4 points, border, is included on the 1st level)
    public double[] yk = new double[291]; // Coordinates of keypoints' net
    public double[] IC = new double[291]; // Average intensities of in the circles around keypoints in the descriptor
    public int[][] ICdif = new int[291][291]; // Array with number of the point(s); differences are in the following array
    public double[][] ICdifDouble = new double[291][291]; // Array with number of the point(s); differences are in the following array

    public int x, y, i, j, width, height, pixel, k, i1, i2;
    public String fileSeparator = System.getProperty("file.separator");
    File file;
    Bitmap bmOut;
    OutputStream out;

    private static final String TAG = MainActivity.class.getSimpleName();
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    TextureView textureView;
    MediaPlayer mp = new MediaPlayer(); // MediaPlayer


    public int[][] keypoints1000Second = new int[1500][2]; // Info about keypoints: Second, i.e. thread, part
    public int nkSecond;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.view_finder);

        maxRes = 180;
        sigma0 = 0.707107;
        radius0 = 2; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1 = 1;
        radius1 = 3; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2 = 1.414214;
        radius2 = 5; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3 = 2;
        radius3 = 6; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4 = 2.828427;
        radius4 = 9; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        maxRes = 340;
        sigma0 = 1.414214;
        radius0 = 5; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1 = 2;
        radius1 = 6; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2 = 2.828427;
        radius2 = 9; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3 = 4;
        radius3 = 12; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4 = 5.656854;
        radius4 = 17; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        maxRes = 680;
        sigma0 = 2.828427;
        radius0 = 9; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1 = 4;
        radius1 = 12; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2 = 5.656854;
        radius2 = 17; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3 = 8;
        radius3 = 24; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4 = 11.313708;
        radius4 = 34; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        maxRes = 1360;
        sigma0 = 5.636854;
        radius0 = 17; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1 = 8;
        radius1 = 24; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2 = 11.313708;
        radius2 = 34; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3 = 16;
        radius3 = 48; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4 = 22.627417;
        radius4 = 68; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        // We calculate matrices maskS... to speed up the program and avoid repetitions
        sigma0 = 0.353553;
        radius0 = 2; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1 = 0.5;
        radius1 = 2; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2 = 0.707107;
        radius2 = 3; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3 = 1;
        radius3 = 4; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4 = 1.414214;
        radius4 = 5; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale


        for (x = -radius0; x <= radius0; x++) {
            for (y = -radius0; y <= radius0; y++) {
                maskS0[x + radius0][y + radius0] = Math.exp(-(x * x + y * y) / (2.0 * sigma0 * sigma0)) / (2.0 * Math.PI * sigma0 * sigma0);
            }
        }

        for (x = -radius1; x <= radius1; x++) {
            for (y = -radius1; y <= radius1; y++) {
                maskS1[x + radius1][y + radius1] = Math.exp(-(x * x + y * y) / (2.0 * sigma1 * sigma1)) / (2.0 * Math.PI * sigma1 * sigma1);
            }
        }

        for (x = -radius2; x <= radius2; x++) {
            for (y = -radius2; y <= radius2; y++) {
                maskS2[x + radius2][y + radius2] = Math.exp(-(x * x + y * y) / (2.0 * sigma2 * sigma2)) / (2.0 * Math.PI * sigma2 * sigma2);
            }
        }

        for (x = -radius3; x <= radius3; x++) {
            for (y = -radius3; y <= radius3; y++) {
                maskS3[x + radius3][y + radius3] = Math.exp(-(x * x + y * y) / (2.0 * sigma3 * sigma3)) / (2.0 * Math.PI * sigma3 * sigma3);
            }
        }

        for (x = -radius4; x <= radius4; x++) {
            for (y = -radius4; y <= radius4; y++) {
                maskS4[x + radius4][y + radius4] = Math.exp(-(x * x + y * y) / (2.0 * sigma4 * sigma4)) / (2.0 * Math.PI * sigma4 * sigma4);
            }
        }
        DisplayKeyPoints();


    }

    public void DisplayKeyPoints() {
        try {
            //opening the image to put marks for keypoints
            file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + fileSeparator + "MyFaces" + fileSeparator + "OInput.jpg");
            bmOut = BitmapFactory.decodeFile(file.getPath());
            Log.i(TAG, "We have bitmap");
            //saving the bitmap to the disk to see what we have from camera
            width = bmOut.getWidth();
            height = bmOut.getHeight();
            Log.i(TAG, "Width =  " + width + " Height =  " + height);

            if (height < width) {
                height = Math.round(maxRes * height / width);
                width = maxRes;
            } else {
                width = Math.round(maxRes * width / height);
                height = maxRes;
            }
            Log.i(TAG, "New width =  " + width + "  New height =  " + height);
            bmOut = Bitmap.createScaledBitmap(bmOut, width, height, true); // scaling bitmap to maxRes pixels; true -bilinear filtering for better image
            for (x = 0; x < width; x++)
                for (y = 0; y < height; y++) {
                    pixel = bmOut.getPixel(x, y); //get pixel colors
                    greyC[x][y] = 0.21 * Color.red(pixel) + 0.72 * Color.green(pixel) + 0.07 * Color.blue(pixel);
                    i = (int) Math.round(greyC[x][y]);
                    bmOut.setPixel(x, y, Color.argb(255, i, i, i));
                }
            file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + fileSeparator + "Temp" + fileSeparator + "CameraBlured.jpg");
            out = new FileOutputStream(file);
            bmOut.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Log.i(TAG, "Temporary file was saved");

            Log.i(TAG, "Final width:  " + width + "  Final height:  " + height);

            i = 1006;
            j = 961;
            xk[7] = i;
            yk[7] = j;
            for (x = -10; x <= 10; x++) {
                bmOut.setPixel(i + x, j - 1, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                bmOut.setPixel(i + x, j, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i + x, j + 1, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i - 1, j + x, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i, j + x, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i + 1, j + x, Color.argb(255, 64, 224, 208));
            }


            i = 352;
            j = 961;
            xk[10] = i;
            yk[10] = j;
            for (x = -10; x <= 10; x++) {
                bmOut.setPixel(i + x, j - 1, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                bmOut.setPixel(i + x, j, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i + x, j + 1, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i - 1, j + x, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i, j + x, Color.argb(255, 64, 224, 208));
                bmOut.setPixel(i + 1, j + x, Color.argb(255, 64, 224, 208));
            }

            sigma0 = (xk[7] - xk[10]) / 3;
            sigma1 = (yk[7] - yk[10]) / 3;

            xk[6] = xk[7] + sigma0;
            yk[6] = yk[7] + sigma1;

            xk[0] = xk[6] - sigma1;
            yk[0] = yk[6] + sigma0;

// Checking borders. They must be positive and less than width and height
// Top left
            xk[25] = xk[0] - sigma1 + sigma0;
            yk[25] = yk[0] + sigma0 + sigma1;
            xk[11] = xk[10] - sigma0;
            yk[11] = yk[10] - sigma1;
            xk[5] = xk[11] - sigma1;
            yk[5] = yk[11] + sigma0;
// Top right
            xk[26] = xk[5] - sigma1 - sigma0;
            yk[26] = yk[5] + sigma0 - sigma1;
// Bottom left
            xk[27] = xk[6] + 5.0 * sigma1 + sigma0;
            yk[27] = yk[6] - 5.0 * sigma0 + sigma1;
// Bottom right
            xk[28] = xk[11] + 5.0 * sigma1 - sigma0;
            yk[28] = yk[11] + 5.0 * sigma0 - sigma1;
            XkYk1st();

            for (pixel = 0; pixel < 18; pixel++) { //calculating average intensities and drawing circles
                radius0 = (int) Math.round(xk[pixel] - sigma4); //The top x coordinate within the circle, 0 keypoints
                if (radius0 < Math.round(xk[pixel] - sigma4))
                    radius0++; // adjusting x coordinate to be inside the circle
                radius1 = (int) Math.round(yk[pixel] - sigma4); // The most left coordinate y within the circle, 0 keypoints
                if (radius1 < Math.round(yk[pixel] - sigma4))
                    radius1++; // adjusting y coordinate to be inside the circle
                radius2 = (int) Math.round(xk[pixel] + sigma4); // The bottom x coordinate within the circle, 0 keypoints
                if (radius2 < Math.round(xk[pixel] + sigma4))
                    radius2++; // adjusting x coordinate to be outside the circle - nearest largest integer: to speed the following loopp
                radius3 = (int) Math.round(yk[pixel] + sigma4); // The most right coordinate y within the circle, 0 keypoints
                if (radius3 < Math.round(yk[pixel] + sigma4))
                    radius3++; // adjusting y coordinate to be outside the circle - nearest largest integer: to speed the following loopp

                IC[pixel] = 0; //average intencity of the circle around keypoint 0
                k = 0; //number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(244, 64, 224, 208));
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel + " : " + IC[pixel] + " ;  Number of pixels: " + k);
            }
            sigma4 = sigma4 * 1.5; //radius of the circle around the point, 1st and 2nd levels
            for (pixel = 18; pixel < 22; pixel++) {
                radius0 = (int) Math.round(xk[pixel] - sigma4);
                if (radius0 < Math.round(xk[pixel] - sigma4)) radius0++;
                radius1 = (int) Math.round(yk[pixel] - sigma4);
                if (radius1 < Math.round(yk[pixel] - sigma4)) radius1++;
                radius2 = (int) Math.round(xk[pixel] + sigma4);
                if (radius2 < Math.round(xk[pixel] + sigma4)) radius2++;
                radius3 = (int) Math.round(yk[pixel] + sigma4);
                if (radius3 < Math.round(xk[pixel] + sigma4)) radius3++;

                IC[pixel] = 0; //average intencity of the circle around keypoint 0
                k = 0; //number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(244, 64, 224, 208));
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel + " : " + IC[pixel] + " ;  Number of pixels: " + k);
            }

            sigma4 = sigma4 * 1.33333333333333333;
            for (pixel = 22; pixel < 25; pixel++) { // We calculate average intensities and draw circles here
                radius0 = (int) Math.round(xk[pixel] - sigma4); // The top coordinate X within the circle, 0 keypoint
                if (radius0 < Math.round(xk[pixel] - sigma4))
                    radius0++; // We adjust the coordinate X to be inside the circle
                radius1 = (int) Math.round(yk[pixel] - sigma4); // The most left coordinate Y within the circle, 0 keypoint
                if (radius1 < Math.round(yk[pixel] - sigma4))
                    radius1++; // We adjust the coordinate Y to be inside the circle
                radius2 = (int) Math.round(xk[pixel] + sigma4); // The bottom coordinate X within the circle, 0 keypoint
                if (radius2 < Math.round(xk[pixel] + sigma4))
                    radius2++; // We adjust the coordinate X to be outside the circle - nearest largest integer: to speed the following loop
                radius3 = (int) Math.round(yk[pixel] + sigma4); // The most right Y coordinate within the circle, 0 keypoint
                if (radius3 < Math.round(yk[pixel] + sigma4))
                    radius3++; // We adjust the coordinate Y to be outside the circle - nearest largest integer: to speed the following loop
                IC[pixel] = 0; // Average intensity of the circle around keypoint 0
                k = 0; // Number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel + " : " + IC[pixel] + " ;  Number of pixels: " + k);
            }

            // starting 2nd level
            XkYk2nd();
            for(pixel = 79; pixel < 83; pixel++){
                radius0 = (int)Math.round(xk[pixel] - sigma4);
                if(radius0 < Math.round(xk[pixel] - sigma4)) radius0++;
                radius1 = (int)Math.round(yk[pixel] - sigma4);
                if(radius1 < Math.round(yk[pixel] - sigma4)) radius1++;
                radius2 = (int)Math.round(xk[pixel] + sigma4);
                if(radius2 < Math.round(xk[pixel] + sigma4)) radius0++;
                radius3 = (int)Math.round(yk[pixel] + sigma4);
                if(radius3 < Math.round(yk[pixel] + sigma4)) radius3++;
                IC[pixel] = 0; // Average intensity of the circle around keypoint 0
                k = 0; // Number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel +" : " + IC[pixel] + " ;  Number of pixels: " + k);
            }
            sigma4=sigma4*0.75; // radius of the circle around the point, 1st and 2nd levels
            for (pixel=62;pixel<79;pixel++) { // We calculate average intensities and draw circles here
                radius0 = (int) Math.round(xk[pixel] - sigma4); // The top coordinate X within the circle, 0 keypoint
                if (radius0 < Math.round(xk[pixel] - sigma4)) radius0++; // We adjust the coordinate X to be inside the circle
                radius1 = (int) Math.round(yk[pixel] - sigma4); // The most left coordinate Y within the circle, 0 keypoint
                if (radius1 < Math.round(yk[pixel] - sigma4)) radius1++; // We adjust the coordinate Y to be inside the circle
                radius2 = (int) Math.round(xk[pixel] + sigma4); // The bottom coordinate X within the circle, 0 keypoint
                if (radius2 < Math.round(xk[pixel] + sigma4)) radius2++; // We adjust the coordinate X to be outside the circle - nearest largest integer: to speed the following loop
                radius3 = (int) Math.round(yk[pixel] + sigma4); // The most right Y coordinate within the circle, 0 keypoint
                if (radius3 < Math.round(yk[pixel] + sigma4)) radius3++; // We adjust the coordinate Y to be outside the circle - nearest largest integer: to speed the following loop
                IC[pixel] = 0; // Average intensity of the circle around keypoint 0
                k = 0; // Number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel +" : " + IC[pixel] + " ;  Number of pixels: " + k);
            }
            sigma4=sigma4*0.666666666666666666666; // radius of the circle around the point, 1st and 2nd levels
            for (pixel=25;pixel<62;pixel++) { // We calculate average intensities and draw circles here
                radius0 = (int) Math.round(xk[pixel] - sigma4); // The top coordinate X within the circle, 0 keypoint
                if (radius0 < Math.round(xk[pixel] - sigma4)) radius0++; // We adjust the coordinate X to be inside the circle
                radius1 = (int) Math.round(yk[pixel] - sigma4); // The most left coordinate Y within the circle, 0 keypoint
                if (radius1 < Math.round(yk[pixel] - sigma4)) radius1++; // We adjust the coordinate Y to be inside the circle
                radius2 = (int) Math.round(xk[pixel] + sigma4); // The bottom coordinate X within the circle, 0 keypoint
                if (radius2 < Math.round(xk[pixel] + sigma4)) radius2++; // We adjust the coordinate X to be outside the circle - nearest largest integer: to speed the following loop
                radius3 = (int) Math.round(yk[pixel] + sigma4); // The most right Y coordinate within the circle, 0 keypoint
                if (radius3 < Math.round(yk[pixel] + sigma4)) radius3++; // We adjust the coordinate Y to be outside the circle - nearest largest integer: to speed the following loop
                IC[pixel] = 0; // Average intensity of the circle around keypoint 0
                k = 0; // Number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel +" : " + IC[pixel] + " ;  Number of pixels: " + k);
            }

            // starting 2nd level
            XkYk3rd();
            for (pixel=83;pixel<291;pixel++) { // We calculate average intensities and draw circles here
                radius0 = (int) Math.round(xk[pixel] - sigma4); // The top coordinate X within the circle, 0 keypoint
                if (radius0 < Math.round(xk[pixel] - sigma4)) radius0++; // We adjust the coordinate X to be inside the circle
                radius1 = (int) Math.round(yk[pixel] - sigma4); // The most left coordinate Y within the circle, 0 keypoint
                if (radius1 < Math.round(yk[pixel] - sigma4)) radius1++; // We adjust the coordinate Y to be inside the circle
                radius2 = (int) Math.round(xk[pixel] + sigma4); // The bottom coordinate X within the circle, 0 keypoint
                if (radius2 < Math.round(xk[pixel] + sigma4)) radius2++; // We adjust the coordinate X to be outside the circle - nearest largest integer: to speed the following loop
                radius3 = (int) Math.round(yk[pixel] + sigma4); // The most right Y coordinate within the circle, 0 keypoint
                if (radius3 < Math.round(yk[pixel] + sigma4)) radius3++; // We adjust the coordinate Y to be outside the circle - nearest largest integer: to speed the following loop
                IC[pixel] = 0; // Average intensity of the circle around keypoint 0
                k = 0; // Number of the pixels inside the circle around keypoint
                for (i = radius0; i < radius2; i++)
                    for (j = radius1; j < radius3; j++)
                        if (Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4) {
                            k++;
                            IC[pixel] = IC[pixel] + greyC[i][j];
                            bmOut.setPixel(i, j, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                        }
                if (k != 0) IC[pixel] = IC[pixel] / k;
                Log.i(TAG, "Average intensity for keypoint " + pixel +" : " + IC[pixel] + " ;  Number of pixels: " + k);
            }
            // Sorting differences
            Log.i(TAG, "1st level:");
            for(i = 0; i <25; i++){ //sorting the differences in 1st level
                for(j = 0; j < 25; j++){ // calculating differences in 1st level
                    ICdif[i][j] =j;
                    ICdifDouble[i][j] = IC[i] - IC[j];
                }
                flagMin = 1;
                while (flagMin==1){
                    flagMin=0;
                    for (j=0; j<24; j++)
                        if (Math.abs(ICdifDouble[i][j])<Math.abs(ICdifDouble[i][j+1])) {
                            det = ICdifDouble[i][j]; ICdifDouble[i][j] = ICdifDouble[i][j+1]; ICdifDouble[i][j+1]=det;
                            k=ICdif[i][j]; ICdif[i][j]=ICdif[i][j+1]; ICdif[i][j+1]=k;
                            flagMin=1;
                        }
                }
                Log.i(TAG, i + " : " + ICdif[i][0] + "  " + ICdifDouble[i][0] + "  " + ICdif[i][1] + "  " + ICdifDouble[i][1] + "  " + ICdif[i][2] + "  " + ICdifDouble[i][2] + "  " + ICdif[i][3] + "  " + ICdifDouble[i][3] + "  " + ICdif[i][4] + "  " + ICdifDouble[i][4] + "  " + ICdif[i][5] + "  " + ICdifDouble[i][5] + "  " + ICdif[i][6] + "  " + ICdifDouble[i][6] + "  " + ICdif[i][7] + "  " + ICdifDouble[i][7] + "  " + ICdif[i][8] + "  " + ICdifDouble[i][8] + "  " + ICdif[i][9] + "  " + ICdifDouble[i][9] + ICdif[i][10] + "  " + ICdifDouble[i][10] + "  " + ICdif[i][11] + "  " + ICdifDouble[i][11] + "  " + ICdif[i][12] + "  " + ICdifDouble[i][12] + "  " + ICdif[i][13] + "  " + ICdifDouble[i][13] + "  " + ICdif[i][14] + "  " + ICdifDouble[i][14] + "  " + ICdif[i][15] + "  " + ICdifDouble[i][15] + "  " + ICdif[i][16] + "  " + ICdifDouble[i][16] + "  " + ICdif[i][17] + "  " + ICdifDouble[i][17] + "  " + ICdif[i][18] + "  " + ICdifDouble[i][18] + "  " + ICdif[i][19] + "  " + ICdifDouble[i][19]);
            }

            Log.i(TAG, "2nd level:");
            for (i=25;i<83;i++) { // sorting the differences, 2nd level
                for (j = 25; j < 83; j++) { // calculating differences, 2nd level
                    ICdif[i][j] = j;
                    ICdifDouble[i][j] = IC[i] - IC[j];
                }
                flagMin = 1;
                while (flagMin == 1) {
                    flagMin = 0;
                    for (j = 25; j < 82; j++)
                        if (Math.abs(ICdifDouble[i][j]) < Math.abs(ICdifDouble[i][j + 1])) {
                            det = ICdifDouble[i][j]; ICdifDouble[i][j] = ICdifDouble[i][j + 1]; ICdifDouble[i][j + 1] = det;
                            k = ICdif[i][j]; ICdif[i][j] = ICdif[i][j + 1]; ICdif[i][j + 1] = k;
                            flagMin = 1;
                        }
                }
                Log.i(TAG, i + " : " + ICdif[i][25] + "  " + ICdifDouble[i][25] + "  " + ICdif[i][26] + "  " + ICdifDouble[i][26] + "  " + ICdif[i][27] + "  " + ICdifDouble[i][27] + "  " + ICdif[i][28] + "  " + ICdifDouble[i][28] + "  " + ICdif[i][29] + "  " + ICdifDouble[i][29] + "  " + ICdif[i][30] + "  " + ICdifDouble[i][30] + "  " + ICdif[i][31] + "  " + ICdifDouble[i][31] + "  " + ICdif[i][32] + "  " + ICdifDouble[i][32] + "  " + ICdif[i][33] + "  " + ICdifDouble[i][33] + "  " + ICdif[i][34] + "  " + ICdifDouble[i][34] + "  " + ICdif[i][35] + "  " + ICdifDouble[i][35] + "  " + ICdif[i][36] + "  " + ICdifDouble[i][36] + "  " + ICdif[i][37] + "  " + ICdifDouble[i][37] + "  " + ICdif[i][38] + "  " + ICdifDouble[i][38] + "  " + ICdif[i][39] + "  " + ICdifDouble[i][39] + "  " + ICdif[i][40] + "  " + ICdifDouble[i][40] + "  " + ICdif[i][41] + "  " + ICdifDouble[i][41] + "  " + ICdif[i][42] + "  " + ICdifDouble[i][42] + "  " + ICdif[i][43] + "  " + ICdifDouble[i][43] + "  " + ICdif[i][44] + "  " + ICdifDouble[i][44]);
            }

            Log.i(TAG, "3rd level:");
            for (i=83;i<291;i++) { // In this loop, we gonna sort the differences, 3rd level
                for (j = 83; j < 291; j++) { // In this loop, we calculate differences, 3rd level
                    ICdif[i][j] = j;
                    ICdifDouble[i][j] = IC[i] - IC[j];
                }
                flagMin = 1;
                while (flagMin == 1) {
                    flagMin = 0;
                    for (j = 83; j < 290; j++)
                        if (Math.abs(ICdifDouble[i][j]) < Math.abs(ICdifDouble[i][j + 1])) {
                            det = ICdifDouble[i][j]; ICdifDouble[i][j] = ICdifDouble[i][j + 1]; ICdifDouble[i][j + 1] = det;
                            k = ICdif[i][j]; ICdif[i][j] = ICdif[i][j + 1]; ICdif[i][j + 1] = k;
                            flagMin = 1;
                        }
                }
                Log.i(TAG, i + " : " + ICdif[i][83] + "  " + ICdifDouble[i][83] + "  " + ICdif[i][84] + "  " + ICdifDouble[i][84] + "  " + ICdif[i][85] + "  " + ICdifDouble[i][85] + "  " + ICdif[i][86] + "  " + ICdifDouble[i][86] + "  " + ICdif[i][87] + "  " + ICdifDouble[i][87] + "  " + ICdif[i][88] + "  " + ICdifDouble[i][88] + "  " + ICdif[i][89] + "  " + ICdifDouble[i][89] + "  " + ICdif[i][90] + "  " + ICdifDouble[i][90] + "  " + ICdif[i][91] + "  " + ICdifDouble[i][91] + "  " + ICdif[i][92] + "  " + ICdifDouble[i][92] + "  " + ICdif[i][94] + "  " + ICdifDouble[i][94] + "  " + ICdif[i][95] + "  " + ICdifDouble[i][95] + "  " + ICdif[i][96] + "  " + ICdifDouble[i][96] + "  " + ICdif[i][97] + "  " + ICdifDouble[i][97] + "  " + ICdif[i][98] + "  " + ICdifDouble[i][98] + "  " + ICdif[i][99] + "  " + ICdifDouble[i][99] + "  " + ICdif[i][100] + "  " + ICdifDouble[i][100] + "  " + ICdif[i][101] + "  " + ICdifDouble[i][101] + "  " + ICdif[i][102] + "  " + ICdifDouble[i][102] + "  " + ICdif[i][103] + "  " + ICdifDouble[i][103]);
            }

            file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+fileSeparator+"Temp"+fileSeparator+"ImageWithKeyPoints"+ maxRes + ".jpg");
            out = new FileOutputStream(file);
            bmOut.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush(); out.close();

            //writing info about keypoints coordinates into the text file
            file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+fileSeparator+"Temp"+fileSeparator+"KeyPoints"+maxRes+".txt");
            file.createNewFile();
            //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
            FileOutputStream outputStream = new FileOutputStream(file, false);
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(Integer.toString(nk)); writer.println("");
            for (i=0;i<nk;i++) { // nk is the number of keypoints
                writer.println(keypoints1000[i][0]); // X coordinate
                writer.println(keypoints1000[i][1]); // Y coordinate
                writer.println("");
            }
            writer.flush(); writer.close();

            // writing info about keypoints to the Descriptor.txt
            file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath()+fileSeparator+"Temp"+fileSeparator+"Descriptor.txt");
            file.createNewFile();
            //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
            outputStream = new FileOutputStream(file, false);
            writer = new PrintWriter(outputStream);

            for (i=0;i<25;i++) { // storing first 25 points
                writer.println(i); // Number of keypoint
                for (j=0;j<12;j++){ // We store 12=2+10 comparisons
                    writer.println(ICdif[i][j]); // writing a number of keypoint j that is used in comparison with keypoint i
                    writer.println(ICdifDouble[i][j]); // writing a difference of the average intensities between keypoints i and j
                }
                writer.println(""); // An empty line is a separator
            }

            for (i=25;i<83;i++) { // storing 58 points
                writer.println(i); // Number of keypoint
                for (j=25;j<57;j++){ // We store 32=2+30 comparisons
                    writer.println(ICdif[i][j]); // writing a number of keypoint j that is used in comparison with keypoint i
                    writer.println(ICdifDouble[i][j]); // writing a difference of the average intensities between keypoints i and j
                }
                writer.println(""); // An empty line is a separator
            }

            for (i=83;i<291;i++) { // storing first 209 points
                writer.println(i); // Number of keypoint
                for (j=83;j<183;j++){ // We store 100 comparisons
                    writer.println(ICdif[i][j]); // writing a number of keypoint j that is used in comparison with keypoint i
                    writer.println(ICdifDouble[i][j]); // writing a difference of the average intensities between keypoints i and j
                }
                writer.println(""); // An empty line is a separator
            }

            writer.flush(); writer.close();

            Log.i(TAG, "Marks were added into the image. Resolution : " + maxRes);
            Log.i(TAG, "Time at the end = " + Calendar.getInstance().getTime());

        } catch (Exception e) {
            Log.i(TAG, "Exception " + e);
        }
    }

    public void XkYk1st() {
        xk[13] = xk[7] + sigma1; yk[13] = yk[7] - sigma0;
        xk[12] = xk[13] + sigma0; yk[12] = yk[13] + sigma1;
        xk[14] = xk[13] - sigma0; yk[14] = yk[13] - sigma1;
        xk[15] = xk[14] - sigma0; yk[15] = yk[14] - sigma1;
        xk[16] = xk[15] - sigma0; yk[16] = yk[15] - sigma1;
        xk[17] = xk[16] - sigma0; yk[17] = yk[16] - sigma1;
        xk[1] = xk[0] - sigma0; yk[1] = yk[0] - sigma1;
        xk[2] = xk[1] - sigma0; yk[2] = yk[1] - sigma1;
        xk[3] = xk[2] - sigma0; yk[3] = yk[2] - sigma1;
        xk[4] = xk[3] - sigma0; yk[4] = yk[3] - sigma1;
        xk[8] = xk[7] - sigma0; yk[8] = yk[7] - sigma1;
        xk[9] = xk[8] - sigma0; yk[9] = yk[8] - sigma1;
        xk[18] = xk[13] + sigma1; yk[18] = yk[13] - sigma0;
        xk[19] = xk[14] + sigma1; yk[19] = yk[14] - sigma0;
        xk[20] = xk[15] + sigma1; yk[20] = yk[15] - sigma0;
        xk[21] = xk[16] + sigma1; yk[21] = yk[16] - sigma0;
        xk[22] = xk[19] + sigma1; yk[22] = yk[19] - sigma0;
        xk[23] = xk[20] + sigma1; yk[23] = yk[20] - sigma0;
        xk[24] = xk[22] + sigma1 - sigma0 * 0.5; yk[24] = yk[22] - sigma0 - sigma1 * 0.5;
        sigma4 = Math.sqrt((xk[10] - xk[7]) * (xk[10] - xk[7]) + (yk[10] - yk[7]) * (yk[10] - yk[7])) / 24; // radius of the circle around the point, 1st and 2nd levels
    }

    public void XkYk2nd(){
        sigma0 = sigma0 * 0.5;
        sigma1 = sigma1 * 0.5;
        xk[25] = xk[0] - sigma0; yk[25] = yk[0] - sigma1;
        xk[26] = xk[1] - sigma0; yk[26] = yk[1] - sigma1;
        xk[27] = xk[2] - sigma0; yk[27] = yk[2] - sigma1;
        xk[28] = xk[3] - sigma0; yk[28] = yk[3] - sigma1;
        xk[29] = xk[4] - sigma0; yk[29] = yk[4] - sigma1;
        xk[30] = xk[6] - sigma1; yk[30] = yk[6] + sigma0;
        xk[31] = xk[30] - sigma0; yk[31] = yk[30] - sigma1;
        xk[32] = xk[31] - sigma0; yk[32] = yk[31] - sigma1;
        xk[33] = xk[32] - sigma0; yk[33] = yk[32] - sigma1;
        xk[34] = xk[33] - sigma0; yk[34] = yk[33] - sigma1;
        xk[35] = xk[34] - sigma0; yk[35] = yk[34] - sigma1;
        xk[36] = xk[35] - sigma0; yk[36] = yk[35] - sigma1;
        xk[37] = xk[36] - sigma0; yk[37] = yk[36] - sigma1;
        xk[38] = xk[37] - sigma0; yk[38] = yk[37] - sigma1;
        xk[39] = xk[38] - sigma0; yk[39] = yk[38] - sigma1;
        xk[40] = xk[39] - sigma0; yk[40] = yk[39] - sigma1;
        xk[41] = xk[6] - sigma0; yk[41] = yk[6] - sigma1;
        xk[42] = xk[7] - sigma0; yk[42] = yk[7] - sigma1;
        xk[43] = xk[8] - sigma0; yk[43] = yk[8] - sigma1;
        xk[44] = xk[9] - sigma0; yk[44] = yk[9] - sigma1;
        xk[45] = xk[10] - sigma0; yk[45] = yk[10] - sigma1;
        xk[46] = xk[12] - sigma1; yk[46] = yk[12] + sigma0;
        xk[47] = xk[46] - sigma0; yk[47] = yk[46] - sigma1;
        xk[48] = xk[47] - sigma0; yk[48] = yk[47] - sigma1;
        xk[49] = xk[48] - sigma0; yk[49] = yk[48] - sigma1;
        xk[50] = xk[49] - sigma0; yk[50] = yk[49] - sigma1;
        xk[51] = xk[50] - sigma0; yk[51] = yk[50] - sigma1;
        xk[52] = xk[51] - sigma0; yk[52] = yk[51] - sigma1;
        xk[53] = xk[52] - sigma0; yk[53] = yk[52] - sigma1;
        xk[54] = xk[53] - sigma0; yk[54] = yk[53] - sigma1;
        xk[55] = xk[54] - sigma0; yk[55] = yk[54] - sigma1;
        xk[56] = xk[55] - sigma0; yk[56] = yk[55] - sigma1;
        xk[57] = xk[12] - sigma0; yk[57] = yk[12] - sigma1;
        xk[58] = xk[13] - sigma0; yk[58] = yk[13] - sigma1;
        xk[59] = xk[14] - sigma0; yk[59] = yk[14] - sigma1;
        xk[60] = xk[15] - sigma0; yk[60] = yk[15] - sigma1;
        xk[61] = xk[16] - sigma0; yk[61] = yk[16] - sigma1;
        xk[62] = xk[57] + sigma1; yk[62] = yk[57] - sigma0;
        xk[63] = xk[62] - sigma0; yk[63] = yk[62] - sigma1;
        xk[64] = xk[63] - sigma0; yk[64] = yk[63] - sigma1;
        xk[65] = xk[64] - sigma0; yk[65] = yk[64] - sigma1;
        xk[66] = xk[65] - sigma0; yk[66] = yk[65] - sigma1;
        xk[67] = xk[66] - sigma0; yk[67] = yk[66] - sigma1;
        xk[68] = xk[67] - sigma0; yk[68] = yk[67] - sigma1;
        xk[69] = xk[68] - sigma0; yk[69] = yk[68] - sigma1;
        xk[70] = xk[69] - sigma0; yk[70] = yk[69] - sigma1;
        xk[71] = xk[18] - sigma0; yk[71] = yk[18] - sigma1;
        xk[72] = xk[19] - sigma0; yk[72] = yk[19] - sigma1;
        xk[73] = xk[20] - sigma0; yk[73] = yk[20] - sigma1;
        xk[74] = xk[71] + sigma1; yk[74] = yk[71] - sigma0;
        xk[75] = xk[74] - sigma0; yk[75] = yk[74] - sigma1;
        xk[76] = xk[75] - sigma0; yk[76] = yk[75] - sigma1;
        xk[77] = xk[76] - sigma0; yk[77] = yk[76] - sigma1;
        xk[78] = xk[77] - sigma0; yk[78] = yk[77] - sigma1;
        xk[79] = xk[22] - sigma0; yk[79] = yk[22] - sigma1;
        xk[80] = xk[22] + sigma1; yk[80] = yk[22] - sigma0;
        xk[81] = xk[80] - sigma0; yk[81] = yk[80] - sigma1;
        xk[82] = xk[81] - sigma0; yk[82] = yk[81] - sigma1;
    }

    public void XkYk3rd(){
        sigma0=sigma0*0.5; sigma1=sigma1*0.5;
        xk[83] = xk[0] - sigma0; yk[83] = yk[0] - sigma1;
        xk[84] = xk[25] - sigma0; yk[84] = yk[25] - sigma1;
        xk[85] = xk[1] - sigma0; yk[85] = yk[1] - sigma1;
        xk[86] = xk[26] - sigma0; yk[86] = yk[26] - sigma1;
        xk[87] = xk[2] - sigma0; yk[87] = yk[2] - sigma1;
        xk[88] = xk[27] - sigma0; yk[88] = yk[27] - sigma1;
        xk[89] = xk[3] - sigma0; yk[89] = yk[3] - sigma1;
        xk[90] = xk[28] - sigma0; yk[90] = yk[28] - sigma1;
        xk[91] = xk[4] - sigma0; yk[91] = yk[4] - sigma1;
        xk[92] = xk[29] - sigma0; yk[92] = yk[29] - sigma1;
        xk[93] = xk[30] - sigma1; yk[93] = yk[30] + sigma0;
        xk[94] = xk[93] - sigma0; yk[94] = yk[93] - sigma1;
        xk[95] = xk[94] - sigma0; yk[95] = yk[94] - sigma1;
        xk[96] = xk[95] - sigma0; yk[96] = yk[95] - sigma1;
        xk[97] = xk[96] - sigma0; yk[97] = yk[96] - sigma1;
        xk[98] = xk[97] - sigma0; yk[98] = yk[97] - sigma1;
        xk[99] = xk[98] - sigma0; yk[99] = yk[98] - sigma1;
        xk[100] = xk[99] - sigma0; yk[100] = yk[99] - sigma1;
        xk[101] = xk[100] - sigma0; yk[101] = yk[100] - sigma1;
        xk[102] = xk[101] - sigma0; yk[102] = yk[101] - sigma1;
        xk[103] = xk[102] - sigma0; yk[103] = yk[102] - sigma1;
        xk[104] = xk[103] - sigma0; yk[104] = yk[103] - sigma1;
        xk[105] = xk[104] - sigma0; yk[105] = yk[104] - sigma1;
        xk[106] = xk[105] - sigma0; yk[106] = yk[105] - sigma1;
        xk[107] = xk[106] - sigma0; yk[107] = yk[106] - sigma1;
        xk[108] = xk[107] - sigma0; yk[108] = yk[107] - sigma1;
        xk[109] = xk[108] - sigma0; yk[109] = yk[108] - sigma1;
        xk[110] = xk[109] - sigma0; yk[110] = yk[109] - sigma1;
        xk[111] = xk[110] - sigma0; yk[111] = yk[110] - sigma1;
        xk[112] = xk[111] - sigma0; yk[112] = yk[111] - sigma1;
        xk[113] = xk[112] - sigma0; yk[113] = yk[112] - sigma1;
        xk[114] = xk[30] - sigma0; yk[114] = yk[30] - sigma1;
        xk[115] = xk[31] - sigma0; yk[115] = yk[31] - sigma1;
        xk[116] = xk[32] - sigma0; yk[116] = yk[32] - sigma1;
        xk[117] = xk[33] - sigma0; yk[117] = yk[33] - sigma1;
        xk[118] = xk[34] - sigma0; yk[118] = yk[34] - sigma1;
        xk[119] = xk[35] - sigma0; yk[119] = yk[35] - sigma1;
        xk[120] = xk[36] - sigma0; yk[120] = yk[36] - sigma1;
        xk[121] = xk[37] - sigma0; yk[121] = yk[37] - sigma1;
        xk[122] = xk[38] - sigma0; yk[122] = yk[38] - sigma1;
        xk[123] = xk[39] - sigma0; yk[123] = yk[39] - sigma1;
        xk[124] = xk[6] - sigma1; yk[124] = yk[6] + sigma0;
        xk[125] = xk[124] - sigma0; yk[125] = yk[124] - sigma1;
        xk[126] = xk[125] - sigma0; yk[126] = yk[125] - sigma1;
        xk[127] = xk[126] - sigma0; yk[127] = yk[126] - sigma1;
        xk[128] = xk[127] - sigma0; yk[128] = yk[127] - sigma1;
        xk[129] = xk[128] - sigma0; yk[129] = yk[128] - sigma1;
        xk[130] = xk[129] - sigma0; yk[130] = yk[129] - sigma1;
        xk[131] = xk[130] - sigma0; yk[131] = yk[130] - sigma1;
        xk[132] = xk[131] - sigma0; yk[132] = yk[131] - sigma1;
        xk[133] = xk[132] - sigma0; yk[133] = yk[132] - sigma1;
        xk[134] = xk[133] - sigma0; yk[134] = yk[133] - sigma1;
        xk[135] = xk[134] - sigma0; yk[135] = yk[134] - sigma1;
        xk[136] = xk[135] - sigma0; yk[136] = yk[135] - sigma1;
        xk[137] = xk[136] - sigma0; yk[137] = yk[136] - sigma1;
        xk[138] = xk[137] - sigma0; yk[138] = yk[137] - sigma1;
        xk[139] = xk[138] - sigma0; yk[139] = yk[138] - sigma1;
        xk[140] = xk[139] - sigma0; yk[140] = yk[139] - sigma1;
        xk[141] = xk[140] - sigma0; yk[141] = yk[140] - sigma1;
        xk[142] = xk[141] - sigma0; yk[142] = yk[141] - sigma1;
        xk[143] = xk[142] - sigma0; yk[143] = yk[142] - sigma1;
        xk[144] = xk[143] - sigma0; yk[144] = yk[143] - sigma1;
        xk[145] = xk[6] - sigma0; yk[145] = yk[6] - sigma1;
        xk[146] = xk[41] - sigma0; yk[146] = yk[41] - sigma1;
        xk[147] = xk[7] - sigma0; yk[147] = yk[7] - sigma1;
        xk[148] = xk[42] - sigma0; yk[148] = yk[42] - sigma1;
        xk[149] = xk[8] - sigma0; yk[149] = yk[8] - sigma1;
        xk[150] = xk[43] - sigma0; yk[150] = yk[43] - sigma1;
        xk[151] = xk[9] - sigma0; yk[151] = yk[9] - sigma1;
        xk[152] = xk[44] - sigma0; yk[152] = yk[44] - sigma1;
        xk[153] = xk[10] - sigma0; yk[153] = yk[10] - sigma1;
        xk[154] = xk[45] - sigma0; yk[154] = yk[45] - sigma1;
        xk[155] = xk[46] - sigma1; yk[155] = yk[46] + sigma0;
        xk[156] = xk[155] - sigma0; yk[156] = yk[155] - sigma1;
        xk[157] = xk[156] - sigma0; yk[157] = yk[156] - sigma1;
        xk[158] = xk[157] - sigma0; yk[158] = yk[157] - sigma1;
        xk[159] = xk[158] - sigma0; yk[159] = yk[158] - sigma1;
        xk[160] = xk[159] - sigma0; yk[160] = yk[159] - sigma1;
        xk[161] = xk[160] - sigma0; yk[161] = yk[160] - sigma1;
        xk[162] = xk[161] - sigma0; yk[162] = yk[161] - sigma1;
        xk[163] = xk[162] - sigma0; yk[163] = yk[162] - sigma1;
        xk[164] = xk[163] - sigma0; yk[164] = yk[163] - sigma1;
        xk[165] = xk[164] - sigma0; yk[165] = yk[164] - sigma1;
        xk[166] = xk[165] - sigma0; yk[166] = yk[165] - sigma1;
        xk[167] = xk[166] - sigma0; yk[167] = yk[166] - sigma1;
        xk[168] = xk[167] - sigma0; yk[168] = yk[167] - sigma1;
        xk[169] = xk[168] - sigma0; yk[169] = yk[168] - sigma1;
        xk[170] = xk[169] - sigma0; yk[170] = yk[169] - sigma1;
        xk[171] = xk[170] - sigma0; yk[171] = yk[170] - sigma1;
        xk[172] = xk[171] - sigma0; yk[172] = yk[171] - sigma1;
        xk[173] = xk[172] - sigma0; yk[173] = yk[172] - sigma1;
        xk[174] = xk[173] - sigma0; yk[174] = yk[173] - sigma1;
        xk[175] = xk[174] - sigma0; yk[175] = yk[174] - sigma1;
        xk[176] = xk[46] - sigma0; yk[176] = yk[46] - sigma1;
        xk[177] = xk[47] - sigma0; yk[177] = yk[47] - sigma1;
        xk[178] = xk[48] - sigma0; yk[178] = yk[48] - sigma1;
        xk[179] = xk[49] - sigma0; yk[179] = yk[49] - sigma1;
        xk[180] = xk[50] - sigma0; yk[180] = yk[50] - sigma1;
        xk[181] = xk[51] - sigma0; yk[181] = yk[51] - sigma1;
        xk[182] = xk[52] - sigma0; yk[182] = yk[52] - sigma1;
        xk[183] = xk[53] - sigma0; yk[183] = yk[53] - sigma1;
        xk[184] = xk[54] - sigma0; yk[184] = yk[54] - sigma1;
        xk[185] = xk[55] - sigma0; yk[185] = yk[55] - sigma1;
        xk[186] = xk[12] - sigma1; yk[186] = yk[12] + sigma0;
        xk[187] = xk[186] - sigma0; yk[187] = yk[186] - sigma1;
        xk[188] = xk[187] - sigma0; yk[188] = yk[187] - sigma1;
        xk[189] = xk[188] - sigma0; yk[189] = yk[188] - sigma1;
        xk[190] = xk[189] - sigma0; yk[190] = yk[189] - sigma1;
        xk[191] = xk[190] - sigma0; yk[191] = yk[190] - sigma1;
        xk[192] = xk[191] - sigma0; yk[192] = yk[191] - sigma1;
        xk[193] = xk[192] - sigma0; yk[193] = yk[192] - sigma1;
        xk[194] = xk[193] - sigma0; yk[194] = yk[193] - sigma1;
        xk[195] = xk[194] - sigma0; yk[195] = yk[194] - sigma1;
        xk[196] = xk[195] - sigma0; yk[196] = yk[195] - sigma1;
        xk[197] = xk[196] - sigma0; yk[197] = yk[196] - sigma1;
        xk[198] = xk[197] - sigma0; yk[198] = yk[197] - sigma1;
        xk[199] = xk[198] - sigma0; yk[199] = yk[198] - sigma1;
        xk[200] = xk[199] - sigma0; yk[200] = yk[199] - sigma1;
        xk[201] = xk[200] - sigma0; yk[201] = yk[200] - sigma1;
        xk[202] = xk[201] - sigma0; yk[202] = yk[201] - sigma1;
        xk[203] = xk[202] - sigma0; yk[203] = yk[202] - sigma1;
        xk[204] = xk[203] - sigma0; yk[204] = yk[203] - sigma1;
        xk[205] = xk[204] - sigma0; yk[205] = yk[204] - sigma1;
        xk[206] = xk[205] - sigma0; yk[206] = yk[205] - sigma1;
        xk[207] = xk[12] - sigma0; yk[207] = yk[12] - sigma1;
        xk[208] = xk[57] - sigma0; yk[208] = yk[57] - sigma1;
        xk[209] = xk[13] - sigma0; yk[209] = yk[13] - sigma1;
        xk[210] = xk[58] - sigma0; yk[210] = yk[58] - sigma1;
        xk[211] = xk[14] - sigma0; yk[211] = yk[14] - sigma1;
        xk[212] = xk[59] - sigma0; yk[212] = yk[59] - sigma1;
        xk[213] = xk[15] - sigma0; yk[213] = yk[15] - sigma1;
        xk[214] = xk[60] - sigma0; yk[214] = yk[60] - sigma1;
        xk[215] = xk[16] - sigma0; yk[215] = yk[16] - sigma1;
        xk[216] = xk[61] - sigma0; yk[216] = yk[61] - sigma1;
        xk[217] = xk[12] + (xk[62]-xk[12])*0.5; yk[217] = yk[12] + (yk[62]-yk[12])*0.5;
        xk[218] = xk[62] - sigma1; yk[218] = yk[62] + sigma0;
        xk[219] = xk[218] - sigma0; yk[219] = yk[218] - sigma1;
        xk[220] = xk[219] - sigma0; yk[220] = yk[219] - sigma1;
        xk[221] = xk[220] - sigma0; yk[221] = yk[220] - sigma1;
        xk[222] = xk[221] - sigma0; yk[222] = yk[221] - sigma1;
        xk[223] = xk[222] - sigma0; yk[223] = yk[222] - sigma1;
        xk[224] = xk[223] - sigma0; yk[224] = yk[223] - sigma1;
        xk[225] = xk[224] - sigma0; yk[225] = yk[224] - sigma1;
        xk[226] = xk[225] - sigma0; yk[226] = yk[225] - sigma1;
        xk[227] = xk[226] - sigma0; yk[227] = yk[226] - sigma1;
        xk[228] = xk[227] - sigma0; yk[228] = yk[227] - sigma1;
        xk[229] = xk[228] - sigma0; yk[229] = yk[228] - sigma1;
        xk[230] = xk[229] - sigma0; yk[230] = yk[229] - sigma1;
        xk[231] = xk[230] - sigma0; yk[231] = yk[230] - sigma1;
        xk[232] = xk[231] - sigma0; yk[232] = yk[231] - sigma1;
        xk[233] = xk[232] - sigma0; yk[233] = yk[232] - sigma1;
        xk[234] = xk[233] - sigma0; yk[234] = yk[233] - sigma1;
        xk[235] = xk[70] + (xk[17]-xk[70])*0.5; yk[235] = yk[70] + (yk[17]-yk[70])*0.5;
        xk[236] = xk[62] - sigma0; yk[236] = yk[62] - sigma1;
        xk[237] = xk[63] - sigma0; yk[237] = yk[63] - sigma1;
        xk[238] = xk[64] - sigma0; yk[238] = yk[64] - sigma1;
        xk[239] = xk[65] - sigma0; yk[239] = yk[65] - sigma1;
        xk[240] = xk[66] - sigma0; yk[240] = yk[66] - sigma1;
        xk[241] = xk[67] - sigma0; yk[241] = yk[67] - sigma1;
        xk[242] = xk[68] - sigma0; yk[242] = yk[68] - sigma1;
        xk[243] = xk[69] - sigma0; yk[243] = yk[69] - sigma1;
        xk[244] = xk[62] + (xk[18]-xk[62])*0.5; yk[244] = yk[62] + (yk[18]-yk[62])*0.5;
        xk[245] = xk[18] - sigma1; yk[245] = yk[18] + sigma0;
        xk[246] = xk[245] - sigma0; yk[246] = yk[245] - sigma1;
        xk[247] = xk[246] - sigma0; yk[247] = yk[246] - sigma1;
        xk[248] = xk[247] - sigma0; yk[248] = yk[247] - sigma1;
        xk[249] = xk[248] - sigma0; yk[249] = yk[248] - sigma1;
        xk[250] = xk[249] - sigma0; yk[250] = yk[249] - sigma1;
        xk[251] = xk[250] - sigma0; yk[251] = yk[250] - sigma1;
        xk[252] = xk[251] - sigma0; yk[252] = yk[251] - sigma1;
        xk[253] = xk[252] - sigma0; yk[253] = yk[252] - sigma1;
        xk[254] = xk[253] - sigma0; yk[254] = yk[253] - sigma1;
        xk[255] = xk[254] - sigma0; yk[255] = yk[254] - sigma1;
        xk[256] = xk[255] - sigma0; yk[256] = yk[255] - sigma1;
        xk[257] = xk[256] - sigma0; yk[257] = yk[256] - sigma1;
        xk[258] = xk[21] + (xk[70]-xk[21])*0.5; yk[258] = yk[21] + (yk[70]-yk[21])*0.5;
        xk[259] = xk[18] - sigma0; yk[259] = yk[18] - sigma1;
        xk[260] = xk[71] - sigma0; yk[260] = yk[71] - sigma1;
        xk[261] = xk[19] - sigma0; yk[261] = yk[19] - sigma1;
        xk[262] = xk[72] - sigma0; yk[262] = yk[72] - sigma1;
        xk[263] = xk[20] - sigma0; yk[263] = yk[20] - sigma1;
        xk[264] = xk[73] - sigma0; yk[264] = yk[73] - sigma1;
        xk[265] = xk[18] + (xk[74]-xk[18])*0.5; yk[265] = yk[18] + (yk[74]-yk[18])*0.5;
        xk[266] = xk[74] - sigma1; yk[266] = yk[74] + sigma0;
        xk[267] = xk[266] - sigma0; yk[267] = yk[266] - sigma1;
        xk[268] = xk[267] - sigma0; yk[268] = yk[267] - sigma1;
        xk[269] = xk[268] - sigma0; yk[269] = yk[268] - sigma1;
        xk[270] = xk[269] - sigma0; yk[270] = yk[269] - sigma1;
        xk[271] = xk[270] - sigma0; yk[271] = yk[270] - sigma1;
        xk[272] = xk[271] - sigma0; yk[272] = yk[271] - sigma1;
        xk[273] = xk[272] - sigma0; yk[273] = yk[272] - sigma1;
        xk[274] = xk[273] - sigma0; yk[274] = yk[273] - sigma1;
        xk[275] = xk[78] + (xk[21]-xk[78])*0.5; yk[275] = yk[78] + (yk[21]-yk[78])*0.5;
        xk[276] = xk[74] - sigma0; yk[276] = yk[74] - sigma1;
        xk[277] = xk[75] - sigma0; yk[277] = yk[75] - sigma1;
        xk[278] = xk[76] - sigma0; yk[278] = yk[76] - sigma1;
        xk[279] = xk[77] - sigma0; yk[279] = yk[77] - sigma1;
        xk[280] = xk[74] + (xk[22]-xk[74])*0.5; yk[280] = yk[74] + (yk[22]-yk[74])*0.5;
        xk[281] = xk[22] - sigma1; yk[281] = yk[22] + sigma0;
        xk[282] = xk[281] - sigma0; yk[282] = yk[281] - sigma1;
        xk[283] = xk[282] - sigma0; yk[283] = yk[282] - sigma1;
        xk[284] = xk[283] - sigma0; yk[284] = yk[283] - sigma1;
        xk[285] = xk[284] - sigma0; yk[285] = yk[284] - sigma1;
        xk[286] = xk[23] + (xk[78]-xk[23])*0.5; yk[286] = yk[23] + (yk[78]-yk[23])*0.5;
        xk[287] = xk[282] + sigma1 + sigma1; yk[287] = yk[282] - sigma0 - sigma0;
        xk[288] = xk[284] + sigma1 + sigma1; yk[288] = yk[284] - sigma0 - sigma0;
        xk[289] = xk[80] + (xk[24]-xk[80])*0.5; yk[289] = yk[80] + (yk[24]-yk[80])*0.5;
        xk[290] = xk[82] + (xk[24]-xk[82])*0.5; yk[290] = yk[82] + (yk[24]-yk[82])*0.5;
    }
}