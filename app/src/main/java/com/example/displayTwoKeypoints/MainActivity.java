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
    public int [][] keypoints1000 = new int [3000][2]; // Info about keypoints
    public int radius0, radius1, radius2, radius3, radius4, MatrixBorder, flagMax, flagMin, nk, maxNoKeyPoints=100; // maxNoKeyPoints - maximum number of keypoints
    public double minFirst, minSecond, sigma0, sigma1, sigma2, sigma3, sigma4, max, min, trace, det, threshold = 7.65; // 7.65 = 255 * 0.03;
    public double [] xk = new double [291]; // Coordinates of keypoints' net: 25 keypoints (1st level) + 58 keypoints (2nd level; 4 points, border, is included on the 1st level)
    public double [] yk = new double [291]; // Coordinates of keypoints' net
    public double [] IC = new double [291]; // Average intensities of in the circles around keypoints in the descriptor
    public int [][] ICdif = new int [291][291]; // Array with number of the point(s); differences are in the following array
    public double [][] ICdifDouble = new double [291][291]; // Array with number of the point(s); differences are in the following array

    public int x, y, i, j, width, height, pixel, k, i1, i2;
    public String fileSeparator = System.getProperty("file.separator");
    File file;
    Bitmap bmOut;
    OutputStream out;

    private static final String TAG = MainActivity.class.getSimpleName();
    private int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    TextureView textureView;
    MediaPlayer mp=new MediaPlayer(); // MediaPlayer




    public int [][] keypoints1000Second = new int [1500][2]; // Info about keypoints: Second, i.e. thread, part
    public int nkSecond;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.view_finder);

        maxRes = 180;
        sigma0=0.707107; radius0=2; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1=1; radius1=3; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2=1.414214; radius2=5; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3=2; radius3=6; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4=2.828427; radius4=9; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        maxRes = 340;
        sigma0=1.414214; radius0=5; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1=2; radius1=6; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2=2.828427; radius2=9; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3=4; radius3=12; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4=5.656854; radius4=17; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        maxRes = 680;
        sigma0=2.828427; radius0=9; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1=4; radius1=12; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2=5.656854; radius2=17; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3=8; radius3=24; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4=11.313708; radius4=34; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        maxRes = 1360;
        sigma0=5.636854; radius0=17; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1=8; radius1=24; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2=11.313708; radius2=34; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3=16; radius3=48; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4=22.627417; radius4=68; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale

        // We calculate matrices maskS... to speed up the program and avoid repetitions
        sigma0=0.353553; radius0=2; // radius0 is the radius of the matrix for the Gaussian blur for the current scale
        sigma1=0.5; radius1=2; // radius1 is the radius of the matrix for the Gaussian blur for the current scale
        sigma2=0.707107; radius2=3; // radius2 is the radius of the matrix for the Gaussian blur for the current scale
        sigma3=1; radius3=4; // radius3 is the radius of the matrix for the Gaussian blur for the current scale
        sigma4=1.414214; radius4=5; // radius4 is the radius of the matrix for the Gaussian blur for the maximum scale


        for(x = - radius0; x <= radius0; x++){
            for(y = -radius0; y <= radius0; y++){
                maskS0[x + radius0][y + radius0] = Math.exp(-(x * x + y * y)/ (2.0 * sigma0 * sigma0)) / (2.0 * Math.PI * sigma0 * sigma0);
            }
        }

        for(x = - radius1; x <= radius1; x++){
            for(y = -radius1; y <= radius1; y++){
                maskS1[x + radius1][y + radius1] = Math.exp(-(x * x + y * y)/ (2.0 * sigma1 * sigma1)) / (2.0 * Math.PI * sigma1 * sigma1);
            }
        }

        for(x = -radius2; x <= radius2; x++){
            for(y = -radius2; y <= radius2; y++){
                maskS2[x + radius2][y + radius2] = Math.exp(-(x * x + y * y)/ (2.0 * sigma2 * sigma2)) / (2.0 * Math.PI * sigma2 * sigma2);
            }
        }

        for(x = -radius3; x <= radius3; x++){
            for(y = -radius3; y <= radius3; y++){
                maskS3[x + radius3][y + radius3] = Math.exp(-(x * x + y * y)/ (2.0 * sigma3 * sigma3)) / (2.0 * Math.PI * sigma3 * sigma3);
            }
        }

        for(x = -radius4; x <= radius4; x++){
            for(y = -radius4; y <= radius4; y++){
                maskS4[x + radius4][y + radius4] = Math.exp(-(x * x + y * y)/ (2.0 * sigma4 * sigma4)) / (2.0 * Math.PI * sigma4 * sigma4);
            }
        }
        DisplayKeyPoints();


    }

    public void DisplayKeyPoints()
    {
        try {
            //opening the image to put marks for keypoints
            file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + fileSeparator + "MyFaces" + fileSeparator + "OInput.jpg");
            bmOut = BitmapFactory.decodeFile(file.getPath());
            Log.i(TAG, "We have bitmap");
            //saving the bitmap to the disk to see what we have from camera
            width = bmOut.getWidth();
            height =bmOut.getHeight();
            Log.i(TAG, "Width =  " + width + " Height =  " + height);

            if(height < width) {
                height = Math.round(maxRes * height /width);
                width =maxRes;
            }
            else {
                width = Math.round(maxRes * width / height);
                height =maxRes;
            }
            Log.i(TAG, "New width =  " + width + "  New height =  " + height);
            bmOut = Bitmap.createScaledBitmap(bmOut, width, height, true); // scaling bitmap to maxRes pixels; true -bilinear filtering for better image
                for(x = 0; x < width; x++)
                    for(y = 0; y < height; y++) {
                        pixel = bmOut.getPixel(x,y); //get pixel colors
                        greyC[x][y] = 0.21 * Color.red(pixel) + 0.72 * Color.green(pixel) + 0.07 * Color.blue(pixel);
                        i = (int)Math.round(greyC[x][y]);
                        bmOut.setPixel(x, y, Color.argb(255, i, i, i));
                    }
                file = new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + fileSeparator + "Temp" + fileSeparator + "CameraBlured.jpg");
                out = new FileOutputStream(file);
                bmOut.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Log.i(TAG, "Temporary file was saved");

                Log.i(TAG, "Final width:  " + width + "  Final height:  " + height );

                i = 1006; j =961;
                xk[7] = i;
                yk[7] = j;
                for(x = -10; x <= 10; x++) {
                    bmOut.setPixel(i + x, j - 1, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                    bmOut.setPixel(i + x, j, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i + x, j + 1, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i - 1, j + x, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i, j + x, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i + 1, j + x, Color.argb(255, 64, 224, 208));
                }


                i = 352; j =961;
                xk[10] = i;
                yk[10] = j;
                for(x = -10; x <= 10; x ++){
                    bmOut.setPixel(i + x, j - 1, Color.argb(255, 64, 224, 208)); // turquoise color: rgb(64,224,208)
                    bmOut.setPixel(i + x, j, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i + x, j + 1, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i - 1, j + x, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i, j + x, Color.argb(255, 64, 224, 208));
                    bmOut.setPixel(i + 1, j + x, Color.argb(255, 64, 224, 208));
                }

                sigma0 =(xk[7] - xk[10])/3; sigma1 =(yk[7] - yk[10])/3;

                xk[6] = xk[7] + sigma0; yk[6] = yk[7] + sigma1;

                xk[0] = xk[6]- sigma1; yk[0] = yk[6] + sigma0;

// Checking borders. They must be positive and less than width and height
// Top left
                xk[25] = xk[0] - sigma1 + sigma0; yk[25] = yk[0] + sigma0 + sigma1;
                xk[11] = xk[10] - sigma0; yk[11] = yk[10] - sigma1;
                xk[5] = xk[11] - sigma1; yk[5] = yk[11] + sigma0;
// Top right
                xk[26] = xk[5] - sigma1 - sigma0; yk[26] = yk[5] + sigma0 - sigma1;
// Bottom left
                xk[27] = xk[6] + 5.0 * sigma1 + sigma0; yk[27] = yk[6] - 5.0 * sigma0 + sigma1;
// Bottom right
                xk[28] = xk[11] + 5.0 * sigma1 - sigma0; yk[28] = yk[11] + 5.0 * sigma0 - sigma1;

                //write XkYk1st method
                for(pixel = 0; pixel < 18; pixel ++) { //calculating average intensities and drawing circles
                    radius0 = (int) Math.round(xk[pixel] - sigma4); //The top x coordinate within the circle, 0 keypoints
                    if(radius0 < Math.round(xk[pixel] - sigma4)) radius0 ++ ; // adjusting x coordinate to be inside the circle
                    radius1 = (int) Math.round(yk[pixel] - sigma4); // The most left coordinate y within the circle, 0 keypoints
                    if(radius1 < Math.round(yk[pixel] - sigma4)) radius1 ++; // adjusting y coordinate to be inside the circle
                    radius2 = (int) Math.round(xk[pixel] + sigma4); // The bottom x coordinate within the circle, 0 keypoints
                    if(radius2 < Math.round(xk[pixel] + sigma4)) radius2 ++; // adjusting x coordinate to be outside the circle - nearest largest integer: to speed the following loopp
                    radius3 = (int) Math.round(yk[pixel] + sigma4); // The most right coordinate y within the circle, 0 keypoints
                    if(radius3 < Math.round(yk[pixel] + sigma4)) radius3 ++; // adjusting y coordinate to be outside the circle - nearest largest integer: to speed the following loopp

                    IC[pixel] = 0; //average intencity of the circle around keypoint 0
                    k = 0; //number of the pixels inside the circle around keypoint
                    for(i = radius0; i < radius2; i++)
                        for(j = radius1; j < radius3; j++)
                            if(Math.sqrt((i - xk[pixel]) * (i - xk[pixel]) + (j - yk[pixel]) * (j - yk[pixel])) <= sigma4 ){
                                k++;
                                IC[pixel] = IC[pixel] +greyC[i][j];
                                bmOut.setPixel(i, j, Color.argb(244, 64, 224, 208));
                            }
                    if (k != 0) IC[pixel] = IC[pixel]/k;
                    Log.i(TAG, "Average intensity for keypoint " + pixel + ": ") + IC[pixel] + " ;   Number of pixels:  " + k);
                }
        }
        catch (Exception e){
            Log.i(TAG, "Exception " + e);
        }
    }

}