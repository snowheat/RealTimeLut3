package mecha.id.realtimelut3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.R.attr.max;
import static android.R.attr.mode;
import static android.R.attr.oneshot;
import static android.R.attr.text;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "MainActivity";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity(){
        Log.i(TAG,"Instantiated new " + this.getClass());
    }

    private CameraBridgeViewBase mOpenCvCameraView,mOpenCvCameraView2;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceViewHolder;
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    private Canvas mCanvas;
    private TextView peakTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tes);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.main_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setMaxFrameSize(480,360);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceViewHolder = mSurfaceView.getHolder();
        mSurfaceViewHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                tryDrawing(holder);
                Log.i(TAG,"DIBUAT SURFACE");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                tryDrawing(holder);
                Log.i(TAG,"DIRUBAH SURFACE");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

            private void tryDrawing(SurfaceHolder holder) {
                Log.i(TAG, "Trying to draw...");

                Canvas canvas = holder.lockCanvas();
                if (canvas == null) {
                    Log.e(TAG, "Cannot draw onto the canvas as it's null");
                } else {
                    drawMyStuff(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            private void drawMyStuff(final Canvas canvas) {
                Random random = new Random();
                Log.i(TAG, "Drawing...");
                canvas.drawRGB(255, 128, 128);
            }

        });

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_lut:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            case R.id.action_grayscale:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "called onResume");
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        }else{
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG, "called onPause");
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "called onDestroy");
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        System.out.println("oleleho");
        Mat originalMRgba = inputFrame.rgba();

        Mat mRgba = originalMRgba.clone();
        //Log.i(TAG,"oleleho channel : "+mRgba.channels()+" : total : "+mRgba.total()+" type : "+mRgba.type()+" col : "+mRgba.cols()+" rows : "+mRgba.rows()+" Mat get 1,1 : "+mRgba.get(0,0)[0]+","+mRgba.get(0,0)[1]+","+mRgba.get(0,0)[2]);


        int i,mod;


        // https://stackoverflow.com/questions/12394364/opencv-for-android-access-elements-of-mat
        //https://stackoverflow.com/questions/42403741/how-to-get-area-of-pixels-of-a-mat-image

        int buffSize = (int) mRgba.total() * mRgba.channels();
        byte[] buff = new byte[buffSize];
        int[] redFrequencies = new int[256];
        int[] greenFrequencies = new int[256];
        int[] blueFrequencies = new int[256];

        int[] redAccuFrequencies = new int[256];
        int[] greenAccuFrequencies = new int[256];
        int[] blueAccuFrequencies = new int[256];

        int[] redColorTransformator = new int[256];
        int[] greenColorTransformator = new int[256];
        int[] blueColorTransformator = new int[256];

        try{
            mRgba.get(0,0,buff);
            //Log.i(TAG,"bUFFER BERHASIL DIBUAT : "+(buff[0] & (0xff))+","+(buff[1] & (0xff))+","+(buff[2] & (0xff))+","+(buff[3] & (0xff)));
            //Log.i(TAG,"bUFFER BERHASIL DIBUAT 2 : "+byteToInt(buff[4])+","+byteToInt(buff[5])+","+byteToInt(buff[6])+","+byteToInt(buff[7]));

            // GET COLOR FREQUENCIES
            int colorIntensity;
            for(i=0;i<buffSize;i++){
                mod = (i+4)%4;

                colorIntensity = (buff[i] & (0xff));

                switch(mod){
                    case 0: //RED
                        redFrequencies[colorIntensity] += 1; /*red color intensity frequencies*/
                    break;
                    case 1: //GREEN
                        greenFrequencies[colorIntensity] += 1; /*green color intensity frequencies*/
                    break;
                    case 2: //BLUE
                        blueFrequencies[colorIntensity] += 1; /*blue color intensity frequencies*/
                    break;
                }
            }

            // GET PEAK & VALLEY
            int redMaxFrequency = 0, redPeakIntensity = 0;
            int greenMaxFrequency = 0, greenPeakIntensity = 0;
            int blueMaxFrequency = 0, bluePeakIntensity = 0;

            for(int intensity=0;intensity < redFrequencies.length;intensity ++){
                if( redFrequencies[intensity] > redMaxFrequency ){
                    redMaxFrequency = redFrequencies[intensity];
                }

                if( greenFrequencies[intensity] > greenMaxFrequency ){
                    greenMaxFrequency = greenFrequencies[intensity];
                }

                if( blueFrequencies[intensity] > blueMaxFrequency ){
                    blueMaxFrequency = blueFrequencies[intensity];
                }
            }

            for(int intensity=0;intensity < redFrequencies.length;intensity ++){
                if( redFrequencies[intensity] == redMaxFrequency ){
                    redPeakIntensity = intensity;
                }

                if( greenFrequencies[intensity] == greenMaxFrequency ){
                    greenPeakIntensity = intensity;
                }

                if( blueFrequencies[intensity] == blueMaxFrequency ){
                    bluePeakIntensity = intensity;
                }
            }


            Log.i("PEAK","PEAK : "+String.valueOf(redPeakIntensity));

            class OneShotTask implements Runnable {
                String rPI,gPI,bPI;
                OneShotTask(int rParam,int gParam, int bParam) {
                    rPI = String.valueOf(rParam);
                    gPI = String.valueOf(gParam);
                    bPI = String.valueOf(bParam);
                }
                public void run() {
                    TextView ee = (TextView) findViewById(R.id.peakTextView);
                    ee.setText("PEAK Red : "+rPI+", Green : "+gPI+", Blue : "+bPI);
                }
            }

            runOnUiThread(new OneShotTask(redPeakIntensity,greenPeakIntensity,bluePeakIntensity));

            //GET ACCUMULATED FREQUENCIES
            int redSumOfAccuFrequencies = 0;
            int greenSumOfAccuFrequencies = 0;
            int blueSumOfAccuFrequencies = 0;

            for(i=0;i<256;i++){
                if(i>0){
                    redAccuFrequencies[i] = redFrequencies[i]+redAccuFrequencies[(i-1)];
                    greenAccuFrequencies[i] = greenFrequencies[i]+greenAccuFrequencies[(i-1)];
                    blueAccuFrequencies[i] = blueFrequencies[i]+blueAccuFrequencies[(i-1)];

                }else{
                    redAccuFrequencies[i] = redFrequencies[i];
                    greenAccuFrequencies[i] = greenFrequencies[i];
                    blueAccuFrequencies[i] = blueFrequencies[i];
                }

                redSumOfAccuFrequencies += redAccuFrequencies[i];
                greenSumOfAccuFrequencies += greenAccuFrequencies[i];
                blueSumOfAccuFrequencies += blueAccuFrequencies[i];
            }

            //GET TRANSFORMED COLOR INTENSITY
            for(i=0;i<256;i++) {
                redColorTransformator[i] = (int)((double)redAccuFrequencies[i]/(double)redAccuFrequencies[255]*255.0);
                greenColorTransformator[i] = (int)((double)greenAccuFrequencies[i]/(double)greenAccuFrequencies[255]*255.0);
                blueColorTransformator[i] = (int)((double)blueAccuFrequencies[i]/(double)blueAccuFrequencies[255]*255.0);
            }

            Log.i(TAG,"Color Accumulated Frequencies 0,20,50,100,255 : "+redAccuFrequencies[0]+","+redAccuFrequencies[20]+","+redAccuFrequencies[50]+","+redAccuFrequencies[100]+","+redAccuFrequencies[255]);
            Log.i(TAG,"redSumOfAccuFruequencies[255]"+redAccuFrequencies[255]);
            Log.i(TAG,"Color Transformator RED : Red 20 Manual = "+redAccuFrequencies[20]+" / "+redAccuFrequencies[255]+" * 255 = "+(redAccuFrequencies[20]/redAccuFrequencies[255]*255));
            Log.i(TAG,"Color Transformator RED : Red 20 "+redColorTransformator[20]+" Red 50 "+redColorTransformator[50]+" Red 100 "+redColorTransformator[100]+" Red 255 "+redColorTransformator[255]);


            for(i=0;i<buffSize;i++){
                mod = (i+4)%4;

                colorIntensity = (buff[i] & (0xff));

                switch(mod){
                    case 0: //RED
                        buff[i] = (byte)redColorTransformator[colorIntensity];
                        break;
                    case 1: //GREEN
                        buff[i] = (byte)greenColorTransformator[colorIntensity];
                        break;
                    case 2: //BLUE
                        buff[i] = (byte)blueColorTransformator[colorIntensity];
                        break;
                }
            }

            mRgba.put(0,0,buff);
        }catch(Exception e){
            Log.e(TAG,e.getMessage());
        }


        //Log.i(TAG,"Size nya "+size);

        //for(i = 0;i<iMax;i++){
        //    for(j = 0;j<jMax;j++){
        //        //colorKey = (int)mRgba.get(i,j)[0];
        //    }
        //}

        //redFrequenciesMap = getColorFrequenciesMap(mRgba,0);
        //redColorConverterMap = getColorConverterMap(redFrequenciesMap);

        //greenFrequenciesMap = getColorFrequenciesMap(mRgba,1);
        //greenColorConverterMap = getColorConverterMap(greenFrequenciesMap);

        //blueFrequenciesMap = getColorFrequenciesMap(mRgba,2);
        //blueColorConverterMap = getColorConverterMap(blueFrequenciesMap);


        //convertedRgba = getConvertedRgba(mRgba,redColorConverterMap,greenColorConverterMap,blueColorConverterMap);



        try{
            Canvas canvas = mSurfaceViewHolder.lockCanvas();
            if (canvas == null) {
                Log.e(TAG, "COYYY Cannot draw onto the canvas as it's null");
            } else {
                Bitmap newBitmap = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRgba,newBitmap);

                int surfaceViewHeight = new Integer(mSurfaceView.getWidth()*mRgba.height()/mRgba.width());
                int topMargin = new Integer((mSurfaceView.getHeight() - surfaceViewHeight)/2);

                canvas.drawBitmap(newBitmap, null, new RectF(0, topMargin, mSurfaceView.getWidth(), surfaceViewHeight+topMargin), null);
                mSurfaceViewHolder.unlockCanvasAndPost(canvas);
                //Log.i(TAG, "Gambar lagi : "+mSurfaceView.getWidth());
            }

        }catch(Exception e){
            Log.e(TAG, e.getMessage());
        }


        return originalMRgba;
    }

    private Mat getConvertedRgba(Mat mRgba, Map<Integer, Integer> redColorConverterMap, Map<Integer, Integer> greenColorConverterMap, Map<Integer, Integer> blueColorConverterMap) {
        Mat convertedRgba = null;

        return convertedRgba;
    }

    private int byteToInt(int colorIntensity){
        int newColorIntensity = 0;

        if(colorIntensity < 0){
            newColorIntensity = colorIntensity+=256;
        }else{
            newColorIntensity = colorIntensity;
        }

        return newColorIntensity;
    }

    private int intToByte(int colorIntensity){
        int newColorIntensity = 0;

        if(colorIntensity < 0){
            newColorIntensity = colorIntensity+=256;
        }else{
            newColorIntensity = colorIntensity;
        }

        return newColorIntensity;
    }


    private Map<Integer,Integer> getColorFrequenciesMap(Mat mRgba,int rgb012) {
        Map<Integer,Integer> colorFrequenciesMap = new HashMap<Integer, Integer>();
        int i,j,colorKey,updatedColorKeyFrequency;

        int iMax = mRgba.rows();
        int jMax = mRgba.cols();

        Mat newRgba = mRgba.clone();

        for(i = 0;i<iMax;i++){
            for(j = 0;j<jMax;j++){
                colorKey = (int)newRgba.get(i,j)[rgb012];

            }
        }

        return colorFrequenciesMap;
    }

    private Map<Integer,Integer> getColorConverterMap(Map<Integer,Integer> redFrequenciesMap) {
        Map<Integer,Integer> colorConverterMap = new HashMap<Integer, Integer>();

        return colorConverterMap;
    }


    private void buildTransformedImage() {

    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();
}
