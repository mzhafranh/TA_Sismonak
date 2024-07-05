package com.mzhtech.sismonakdev.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mzhtech.sismonakdev.R;

import static com.mzhtech.sismonakdev.services.MainForegroundService.BLOCKED_APP_NAME_EXTRA;

public class BlockedAppActivity extends AppCompatActivity {
	private TextView txtBlockedAppName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blocked_app);
		
		txtBlockedAppName = findViewById(R.id.txtBlockedAppName);
		Intent intent = getIntent();
		String blockedAppName = intent.getStringExtra(BLOCKED_APP_NAME_EXTRA);
		txtBlockedAppName.setText(blockedAppName);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
//		finish();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
