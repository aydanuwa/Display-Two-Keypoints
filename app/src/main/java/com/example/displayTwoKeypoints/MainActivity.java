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
            }

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
}