package cz.mega_akce.myapplication;

//import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;
import org.jtransforms.fft.DoubleFFT_1D;

//import android.view.Menu;
//import android.view.MenuItem;
//import android.os.Bundle;
//;


public class AndroidAccelerometerExample extends Activity implements SensorEventListener {

    private float lastX, lastY, lastZ;
    private long  startT,lastT,TSample;
    private float[] X,Y,Z;
    private long cpoint,cpoint1;
    private int cir_buff_start, cir_buff_end;
    private DoubleFFT_1D fft;



    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;

    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;

    private float vibrateThreshold = 0;

    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ;

    public Vibrator v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }

        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        X=new float[1024];
        Y=new float[1024];
        Z=new float[1024];
        TSample=1;
        startT= System.currentTimeMillis();
        cpoint1=0;
        cpoint=0;
        fft = new DoubleFFT_1D(1024);
        cir_buff_start=0;
        cir_buff_end=0;
    }

    public void initializeViews() {
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);

        maxX = (TextView) findViewById(R.id.maxX);
        maxY = (TextView) findViewById(R.id.maxY);
        maxZ = (TextView) findViewById(R.id.maxZ);
    }

    //onResume() register the accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //onPause() unregister the accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // clean current values
        displayCleanValues();
        // display the current x,y,z accelerometer values
        displayCurrentValues();
        // display the max x,y,z accelerometer values
        displayMaxValues();

        // get the change of the x,y,z values of the accelerometer
        deltaX = Math.abs(lastX - event.values[0]);
        deltaY = Math.abs(lastY - event.values[1]);
        deltaZ = Math.abs(lastZ - event.values[2]);

        lastX=event.values[0];
        lastY=event.values[1];
        lastZ=event.values[2];
        lastT= System.currentTimeMillis();

        cpoint1=cpoint;
        cpoint= (lastT-startT)/TSample;

        for (int i= 0; i< (cpoint-cpoint1); i++) {
            cir_buff_end= (int) ((cpoint1 + i) % 1024);
            X[cir_buff_end]= lastX;
            Y[cir_buff_end]= lastY;
            Z[cir_buff_end]= lastZ;
        }

        if (cir_buff_start>cir_buff_end){
            //buffer overflow do fft
           cir_buff_start=cir_buff_end;

           double[] a;
           a=new double[1024];

           for(int i=0; i<1024;i++) {
               a[i]=Math.sqrt(X[i]*X[i]+Y[i]*Y[i]+Z[i]*Z[i]);
           }
            //DoubleFFT_1D
            fft.realForward(a);
        }


        // if the change is below 2, it is just plain noise
        /*if (deltaX < 2)
            deltaX = 0;
        if (deltaY < 2)
            deltaY = 0;
        if (deltaZ vibrateThreshold) || (deltaY > vibrateThreshold) || (deltaZ > vibrateThreshold)) {
            v.vibrate(50);
        }*/
    }

    public void displayCleanValues() {
        currentX.setText("0.0");
        currentY.setText("0.0");
        currentZ.setText("0.0");
    }

    // display the current x,y,z accelerometer values
    public void displayCurrentValues() {
        currentX.setText(Float.toString(deltaX));
        currentY.setText(Float.toString(deltaY));
        currentZ.setText(Float.toString(deltaZ));
    }

    // display the max x,y,z accelerometer values
    public void displayMaxValues() {
        if (deltaX > deltaXMax) {
            deltaXMax = deltaX;
            maxX.setText(Float.toString(deltaXMax));
        }
        if (deltaY > deltaYMax) {
            deltaYMax = deltaY;
            maxY.setText(Float.toString(deltaYMax));
        }
        if (deltaZ > deltaZMax) {
            deltaZMax = deltaZ;
            maxZ.setText(Float.toString(deltaZMax));
        }
    }
}
