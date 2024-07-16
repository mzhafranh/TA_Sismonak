package com.mzhtech.sismonakdev.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.adapters.ActivityLogFragmentPagerAdapter;

public class ActivityLogFragment extends Fragment {
	public static final String TAG = "ActivityLogTAG";
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_activity_log, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Log.i(TAG, "sampai onViewCreated ActivityLogFragment");
		ViewPager viewPager = view.findViewById(R.id.activityLogViewPager);
		viewPager.setAdapter(setupActivityLogFragmentPagerAdapter());
		
		TabLayout tabLayout = view.findViewById(R.id.activityLogTabLayout);
		tabLayout.setupWithViewPager(viewPager);

	}

	@Override
	public void onResume() {
		super.onResume();
		setupViewPager();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void setupViewPager() {
		ViewPager viewPager = getView().findViewById(R.id.activityLogViewPager);
		viewPager.setAdapter(setupActivityLogFragmentPagerAdapter());

		TabLayout tabLayout = getView().findViewById(R.id.activityLogTabLayout);
		tabLayout.setupWithViewPager(viewPager);
	}
	
	private PagerAdapter setupActivityLogFragmentPagerAdapter() {
		ActivityLogFragmentPagerAdapter pagerAdapter = new ActivityLogFragmentPagerAdapter(getChildFragmentManager());
		pagerAdapter.addFragment(new CallsFragment(), getResources().getString(R.string.calls));
		pagerAdapter.addFragment(new MessagesFragment(), getResources().getString(R.string.messages));
		pagerAdapter.addFragment(new ContactsFragment(), getResources().getString(R.string.contacts));
		
		return pagerAdapter;
	}
}
