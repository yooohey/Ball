package sample.application.ball;

import android.os.Bundle;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.Menu;
import java.util.List;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.content.Context;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class BallActivity extends Activity implements SensorEventListener {
	
	public static float acceler_y, acceler_x;
	private SensorManager sensorManager;
	PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new BoardView(this));
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        
    }

    @Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(sensors.size() > 0){
			Sensor sensor = sensors.get(0);
			sensorManager.registerListener(this, sensor, 1);
		}
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK+PowerManager.ON_AFTER_RELEASE, "My Tag");
		wl.acquire();
	}
    

	@Override
	protected void onStop() {
		// TODO 自動生成されたメソッド・スタブ
		super.onStop();
		sensorManager.unregisterListener(this);
		if(wl.isHeld())wl.release();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_ball, menu);
        return true;
    }

	@Override
	public void onSensorChanged(SensorEvent event) {
		acceler_y = event.values[0];
		acceler_x = event.values[1];
		//((TextView)findViewById(R.id.textView1)).setText(acceler_x+" , "+acceler_y);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setMessage(R.string.message_exit);
			ab.setPositiveButton(R.string.label_yes,new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					finish();
				}
			});
			ab.setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					
				}
			});
			ab.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	
    
}
