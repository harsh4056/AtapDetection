

package com.example.moon.atapdetection;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;

import android.widget.FrameLayout;

public class SensorActivity extends Activity{

	private RealtimeDataDisplayFragment mFragment;
	private static final int CONTENT_VIEW_ID = 10101010;

	boolean tapperPlayPause =true;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);




		FrameLayout frame = new FrameLayout(this);
		frame.setId(CONTENT_VIEW_ID);
		setContentView(frame, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

		if (savedInstanceState == null) {
			Fragment newFragment = new RealtimeDataDisplayFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.add(CONTENT_VIEW_ID, newFragment).commit();
		}

		TapDetection tp= new TapDetection(SensorActivity.this);
		tp.setOnTapDetectionListener(new TapDetection.OnTapDetectionListener() {
			@Override
			public void onTapDetected() {



				Intent i = new Intent("com.android.music.musicservicecommand.togglepause");
				i.putExtra("command", "togglepause");
				sendBroadcast(i);

			}

			@Override
			public void onDoubleTapDetected() {
				Intent i = new Intent("com.android.music.musicservicecommand");
				i.putExtra("command", "next");
				sendBroadcast(i);
			}






		});




	}
}

