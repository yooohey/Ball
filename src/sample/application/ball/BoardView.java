package sample.application.ball;

import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Vibrator;
import android.view.Display;
import android.view.WindowManager;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region;

import android.graphics.Paint.Align;
import android.view.MotionEvent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class BoardView extends View {
	Timer timer;
	Bitmap ball;
	int w,h;
	static int ballR=16;
	static Context mContext;
	static Vibrator vib;
	
	float new_y = 0;
	float speed_y = 0;
	float new_x = 0;
	float speed_x = 0;
	float scale = 25f;
	float time = 0.04f;
	
	Path hole, holeCenter;
	Region screen, rHole,rHoleCenter;
	boolean inTheHole = false;
	
	static final int gameDuration = 20000;
	public int score, hiScore, timeLeft;
	
	public BoardView(Context context){
		super(context);
		mContext = context;
		
		vib = ((Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE));
		this.setFocusable(true);
		Display disp = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		this.w = disp.getWidth();
		this.h = disp.getHeight();
		
		if(this.w>480)ballR=16;
		else ballR=9;
		this.ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		this.ball = Bitmap.createScaledBitmap(ball,  ballR*2, ballR*2, false);
		
		//hole
		this.hole = new Path();
		this.hole.addCircle(w/2,h/2-(ballR/8),(int)(ballR*1.5),Direction.CW);
		this.screen = new Region(0,0,w,h);
		this.rHole = new Region();
		this.rHole.setPath(hole, screen);
		this.holeCenter = new Path();
		this.holeCenter.addCircle(this.w/2,this.h/2, (int)(ballR*1.2), Direction.CCW);
		this.rHoleCenter = new Region();
		this.rHoleCenter.setPath(this.holeCenter, this.screen);
		
		//score
		SharedPreferences prefs = mContext.getSharedPreferences("BallScorePrefs", Context.MODE_PRIVATE);
		hiScore = prefs.getInt("hiScore", 0);
		timeLeft = gameDuration;
		
		this.setOnTouchListener(new OnTouchListener(){
			
			public boolean onTouch(View arg0, MotionEvent arg1){
				if(timeLeft <= 0){
					BoardView.this.new_x = 0;
					BoardView.this.new_y = 0;
					BoardView.this.score = 0;
					BoardView.this.timeLeft = BoardView.this.gameDuration;
					BoardView.this.startTimer();
				}
				return false;
			}
		});
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO 自動生成されたメソッド・スタブ
		super.onDraw(canvas);
		canvas.drawColor(Color.rgb(0, 128, 0));
		Paint paint = new Paint();
		paint.setColor(Color.DKGRAY);
		canvas.drawPath(hole, paint);
		paint.setColor(Color.BLACK);
		canvas.drawPath(holeCenter,  paint);
		
		canvas.drawBitmap(ball, new_x-ballR, new_y-ballR,null);
		//this.getSystemUivisibility(View.STATUS_BAR_HIDDEN);
		
		if(inTheHole){
			paint.setColor(Color.argb(180, 0, 0, 0));
			canvas.drawPath(holeCenter,  paint);
		}
		
		//score
		paint.setColor(Color.BLACK);
		paint.setTextSize(h/16);
		paint.setTextAlign(Align.RIGHT);
		
		canvas.drawText(getResources().getString(R.string.label_timeleft)+String.valueOf((int)(timeLeft/1000)),(int)(w*0.95), h/12, paint);
		canvas.drawText(getResources().getString(R.string.label_score)+String.valueOf(score),(int)(w*0.95), h/6, paint);
		canvas.drawText(getResources().getString(R.string.label_hiscore)+String.valueOf(hiScore),(int)(w*0.95), h/4, paint);
		
		if(timeLeft <= 0){
			paint.setTextAlign(Align.CENTER);
			canvas.drawText(getResources().getString(R.string.message_replay), w/2, (int)(h*0.9), paint);
		}
		
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO 自動生成されたメソッド・スタブ
		super.onWindowVisibilityChanged(visibility);
		if(visibility==View.VISIBLE){
			startTimer();
		}else{
			timer.cancel();
		}
	}
	
	public void startTimer(){
		if(timer!= null) timer.cancel();
		timer = new Timer();
		final android.os.Handler handler = new android.os.Handler();
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				handler.post( new Runnable(){
					public void run(){
						newPos();
						BoardView.this.invalidate();
						
						timeLeft = timeLeft - 40;
						if(timeLeft <= 0){
							timer.cancel();
						}
					}
				});
			}
		}
		, 0, (int)(1000/scale));
	}
	
	public void newPos(){
		
		this.new_x = this.new_x+(((BallActivity.acceler_x*this.time*this.scale)/2)+this.speed_x*this.time*this.scale);
		this.new_y = this.new_y+(((BallActivity.acceler_y*this.time*this.scale)/2)+this.speed_y*this.time*this.scale);
		
		if(this.new_x >= this.w-(ballR)){
			this.new_x = this.w-(ballR);
			this.speed_x = -Math.abs(this.speed_x)*0.8f;
			if(Math.abs(this.speed_x)>1){
				vib.vibrate(50);
			}
		}else if(this.new_x<=ballR){
			this.new_x = ballR;
			this.speed_x = Math.abs(this.speed_x)*0.8f;
			if(Math.abs(this.speed_x)>1){
				vib.vibrate(50);
			}
		}else{
			this.speed_x = (this.speed_x + (BallActivity.acceler_x*this.time*this.scale))*0.95f;
		}
		
		if(this.new_y>=this.h-(ballR)){
			this.new_y = this.h - (ballR);
			this.speed_y = -Math.abs(this.speed_y)*0.8f;
			if(Math.abs(this.speed_y)>1){
				vib.vibrate(50);
			}
		}
		else if(this.new_y <= ballR){
			this.new_y = ballR;
			this.speed_y = Math.abs(this.speed_y)*0.8f;
			if(Math.abs(this.speed_y)>1){
				vib.vibrate(50);
			}
		}else{
			this.speed_y = (this.speed_y +(BallActivity.acceler_y * this.time * this.scale)) * 0.95f;
		}
		
		//hole
		if(rHole.contains((int)new_x, (int)new_y)){
			if(rHoleCenter.contains((int)new_x, (int)new_y)&Math.abs(BallActivity.acceler_x)<=1.0 & Math.abs(speed_x)<1.50 & Math.abs(speed_y)<1.50){
				getIn();
			}else{
				vib.vibrate(50);
				speed_x = (int)(speed_x + (w/2 - new_x)*0.2);
				speed_y = (int)(speed_y + (h/2 - new_y)*0.2);
			}
		}
	}
	
	public void getIn(){
		
		this.speed_x = 0;
		this.speed_y = 0;
		this.new_x = w/2;
		this.new_y = h/2;
		this.inTheHole = true;
		
		if(this.timer != null){
			timer.cancel();
		}
		addPoint(10);
		
		timer = new Timer();
		final android.os.Handler handler = new android.os.Handler();
		timer.schedule(new TimerTask(){
			@Override
			public void run(){
				handler.post( new Runnable(){
					public void run(){
						new_x = 0;
						new_y = 0;
						inTheHole = false;
						startTimer();
					}
				});
			}
		}, 500);
	}
	
	public void addPoint(int point){
		score = score + point;
		if(score>hiScore){
			hiScore = score;
			SharedPreferences prefs = mContext.getSharedPreferences("BallScorePrefs", Context.MODE_PRIVATE);
			Editor editor = prefs.edit();
			editor.putInt("hiScore", hiScore);
			editor.commit();
		}
	}
	

}
