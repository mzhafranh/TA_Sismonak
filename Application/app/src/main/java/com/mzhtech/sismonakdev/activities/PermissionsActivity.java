package com.mzhtech.sismonakdev.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.fragments.LocationPermissionsFragment;
import com.mzhtech.sismonakdev.fragments.PermissionsMainFragment;
import com.mzhtech.sismonakdev.fragments.PhoneCallsPermissionsFragment;
import com.mzhtech.sismonakdev.fragments.SMSPermissionsFragment;
import com.mzhtech.sismonakdev.fragments.SettingsPermissionsFragment;
import com.mzhtech.sismonakdev.interfaces.OnFragmentChangeListener;
import com.mzhtech.sismonakdev.utils.Constant;
import com.mzhtech.sismonakdev.utils.SharedPrefsUtils;

public class PermissionsActivity extends AppCompatActivity implements OnFragmentChangeListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permissions);
		
		getSupportFragmentManager().beginTransaction().replace(R.id.permissionFragmentContainer, new PermissionsMainFragment()).commit();
	}
	
	@Override
	public void onFragmentChange(int id) {
		Fragment selectedFragment = null;
		switch (id) {
			case Constant.PERMISSIONS_MAIN_FRAGMENT:
				selectedFragment = new PermissionsMainFragment();
				break;
			case Constant.PERMISSIONS_SMS_FRAGMENT:
				selectedFragment = new SMSPermissionsFragment();
				break;
			case Constant.PERMISSIONS_PHONE_CALLS_FRAGMENT:
				selectedFragment = new PhoneCallsPermissionsFragment();
				break;
			case Constant.PERMISSIONS_LOCATION_FRAGMENT:
				selectedFragment = new LocationPermissionsFragment();
				break;
			case Constant.PERMISSIONS_SETTINGS_FRAGMENT:
				selectedFragment = new SettingsPermissionsFragment();
				break;
			case Constant.PERMISSIONS_FRAGMENTS_FINISH:
				SharedPrefsUtils.setBooleanPreference(this, Constant.CHILD_FIRST_LAUNCH, false);
				Intent intent = new Intent(this, ChildSignedInActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				break;
		}
		
		if (selectedFragment != null)
			getSupportFragmentManager().beginTransaction().replace(R.id.permissionFragmentContainer, selectedFragment).commit();
		
	}
	
	@Override
	public void onBackPressed() {
		//No going back to childSignedIn activity
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
