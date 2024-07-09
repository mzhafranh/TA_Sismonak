package com.mzhtech.sismonakdev.services;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.activities.BlockedAppActivity;
import com.mzhtech.sismonakdev.activities.ChildSignedInActivity;
import com.mzhtech.sismonakdev.broadcasts.AppInstalledReceiver;
import com.mzhtech.sismonakdev.broadcasts.AppRemovedReceiver;
import com.mzhtech.sismonakdev.broadcasts.AppUsageReceiver;
import com.mzhtech.sismonakdev.broadcasts.PhoneStateReceiver;
import com.mzhtech.sismonakdev.broadcasts.ScreenTimeReceiver;
import com.mzhtech.sismonakdev.broadcasts.SmsReceiver;
import com.mzhtech.sismonakdev.models.App;
import com.mzhtech.sismonakdev.models.Child;
import com.mzhtech.sismonakdev.models.Contact;
import com.mzhtech.sismonakdev.models.ScreenLock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.mzhtech.sismonakdev.NotificationChannelCreator.CHANNEL_ID;

public class MainForegroundService extends Service {
	public static final int NOTIFICATION_ID = 27;
	public static final String TAG = "MainServiceTAG";
	public static final String BLOCKED_APP_NAME_EXTRA = "com.mzhtech.sismonakdev.services.BLOCKED_APP_NAME_EXTRA";
	public static final String ACTION_REQUEST_UPLOAD = "com.mzhtech.sismonakdev.services.ACTION_REQUEST_UPLOAD";
	public static final int LOCATION_UPDATE_INTERVAL = 1;    //every 5 seconds
	public static final int LOCATION_UPDATE_DISPLACEMENT = 5;  //every 10 meters
	private ExecutorService executorService;
	private ArrayList<App> apps;
	private PhoneStateReceiver phoneStateReceiver;
	private SmsReceiver smsReceiver;
	private AppInstalledReceiver appInstalledReceiver;
	private AppRemovedReceiver appRemovedReceiver;
	private ScreenTimeReceiver screenTimeReceiver;
	private AppUsageReceiver appUsageReceiver;
	private String uid;
	private String childEmail;
	private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
	private DatabaseReference databaseReference = firebaseDatabase.getReference("users");
	private LocationListener locationListener;
	private LocationManager locationManager;

	private boolean isTimerRunning = false;
	private long screenTime = 0;

	private DevicePolicyManager devicePolicyManager;


	@Override
	public void onCreate() {
		super.onCreate();
		executorService = Executors.newSingleThreadExecutor();
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		LockerThread thread = new LockerThread();

		executorService.submit(thread);
		new Thread(new Runnable() {
			@Override
			public void run() {
				getInstalledApplications();
			}
		}).start();
		Log.i(TAG, "onCreate: executed");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//String childEmail = intent.getStringExtra(CHILD_EMAIL);
		//String notificationContent = "Monitoring device";
		
		FirebaseAuth auth = FirebaseAuth.getInstance();
		FirebaseUser user = auth.getCurrentUser();
		childEmail = user.getEmail();
		uid = user.getUid();
		
		Intent notificationIntent = new Intent(this, ChildSignedInActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
		
		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
				//.setContentTitle(notificationContent)
				.setSmallIcon(R.drawable.sismonak_notification).setContentIntent(pendingIntent).build();
		
		startForeground(NOTIFICATION_ID, notification);
		
		getUserLocation();

		devicePolicyManager = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);


		ArrayList<Contact> contacts = getContacts();
		uploadContacts(contacts);

        /*FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");*/
		
		Query appsQuery = databaseReference.child("childs").child(uid).child("apps");
		appsQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					getApps();
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		Query locationQuery = databaseReference.child("childs").child(uid).child("location");
		locationQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					setFence(dataSnapshot);
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});

        /*Query webFilterQuery = databaseReference.child("childs").child(uid).child("webFilter");
        webFilterQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean checked = (boolean) dataSnapshot.getValue();
                    if (checked) {
                        Toast.makeText(MainForegroundService.this, "Web Filter Enabled", Toast.LENGTH_SHORT).show();
                        *//*String primaryDNS = "185.228.168.168";
                        String secondaryDNS = "185.228.169.168";
                        changeDNS(primaryDNS, secondaryDNS);
                        String newDNS1 = Settings.System.getString(getContentResolver(), Settings.System.WIFI_STATIC_DNS1);
                        String newDNS2 = Settings.System.getString(getContentResolver(), Settings.System.WIFI_STATIC_DNS2);
                        Log.i(TAG, "onDataChange: new DNS1: " + newDNS1);
                        Log.i(TAG, "onDataChange: new DNS2: " + newDNS2);*//*
                    } else {
                        Toast.makeText(MainForegroundService.this, "Web Filter Disabled", Toast.LENGTH_SHORT).show();
                        *//*String primaryDNS = "0.0.0.0";
                        String secondaryDNS = "0.0.0.0";
                        changeDNS(primaryDNS, secondaryDNS);
                        String newDNS1 = Settings.System.getString(getContentResolver(), Settings.System.WIFI_STATIC_DNS1);
                        String newDNS2 = Settings.System.getString(getContentResolver(), Settings.System.WIFI_STATIC_DNS2);
                        Log.i(TAG, "onDataChange: new DNS1: " + newDNS1);
                        Log.i(TAG, "onDataChange: new DNS2: " + newDNS2);*//*
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
		
		Query screenTimeQuery = databaseReference.child("childs").child(uid).child("screenLock");
		screenTimeQuery.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					ScreenLock screenLock = dataSnapshot.getValue(ScreenLock.class);
					Log.i(TAG, "onDataChangeX: hours: " + screenLock.getHours());
					Log.i(TAG, "onDataChangeX: minutes: " + screenLock.getMinutes());
					Log.i(TAG, "onDataChangeX: isLocked: " + screenLock.isLocked());
					
					if (screenLock.isLocked()) {
						screenTimeReceiver = new ScreenTimeReceiver(screenLock);
						IntentFilter screenTimeIntentFilter = new IntentFilter();
						screenTimeIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
						screenTimeIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
						if (screenLock.getTimeInSeconds() != 0 && isTimerRunning == false){
							isTimerRunning = true;
							new CountDownTimer((screenLock.getTimeInSeconds() * 1000), 1000){
								@Override
								public void onTick(long millisUntilFinished) {
									screenTime += 1;
									Log.i(TAG, "CountDownTimer " + screenTime);
								}

								@Override
								public void onFinish() {
									Log.i(TAG, "Timer finished");
									screenTime = 0;
									isTimerRunning = false;
									devicePolicyManager.lockNow();
									registerReceiver(screenTimeReceiver, screenTimeIntentFilter);
								}
							}.start();
						} else {
							devicePolicyManager.lockNow();
							registerReceiver(screenTimeReceiver, screenTimeIntentFilter);
						}
					} else {
						if (screenTimeReceiver != null) {
							unregisterReceiver(screenTimeReceiver);
						}
					}
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
		
		phoneStateReceiver = new PhoneStateReceiver(user);
		IntentFilter callIntentFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(phoneStateReceiver, callIntentFilter);
		
		smsReceiver = new SmsReceiver(user);
		IntentFilter smsIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(smsReceiver, smsIntentFilter);
		
		appInstalledReceiver = new AppInstalledReceiver(user);
		IntentFilter appInstalledIntentFilter = new IntentFilter();
		appInstalledIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		//appInstalledIntentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL);
		appInstalledIntentFilter.addDataScheme("package");
		registerReceiver(appInstalledReceiver, appInstalledIntentFilter);
		
		appRemovedReceiver = new AppRemovedReceiver(user);
		IntentFilter appRemovedIntentFilter = new IntentFilter();
		appRemovedIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		appRemovedIntentFilter.addDataScheme("package");
		registerReceiver(appRemovedReceiver, appRemovedIntentFilter);

		appUsageReceiver = new AppUsageReceiver(user);
		IntentFilter appUsageIntentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(appUsageReceiver, appUsageIntentFilter);

        /*screenTimeReceiver = new ScreenTimeReceiver(user);
        IntentFilter screenTimeIntentFilter = new IntentFilter();
        screenTimeIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenTimeIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenTimeReceiver, screenTimeIntentFilter);*/
		
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (executorService != null) {
			executorService.shutdown();
		}
		if (phoneStateReceiver != null) {
			unregisterReceiver(phoneStateReceiver);
		}
		if (smsReceiver != null) {
			unregisterReceiver(smsReceiver);
		}
		if (appInstalledReceiver != null) {
			unregisterReceiver(appInstalledReceiver);
		}
		if (appRemovedReceiver != null) {
			unregisterReceiver(appRemovedReceiver);
		}
		if (screenTimeReceiver != null) {
			unregisterReceiver(screenTimeReceiver);
		}
		if (appUsageReceiver != null){
			unregisterReceiver(appUsageReceiver);
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void getApps() {
		Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail);
		query.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					//Log.i(TAG, "onDataChange: dataSnapshot value: "+dataSnapshot.getValue());
					//Log.i(TAG, "onDataChange: dataSnapshot as a string: "+dataSnapshot.toString());
					//Log.i(TAG, "onDataChange: dataSnapshot children: " + dataSnapshot.getChildren());
					//Log.i(TAG, "onDataChange: dataSnapshot key: " + dataSnapshot.getKey());
					
					DataSnapshot nodeShot = dataSnapshot.getChildren().iterator().next();
					Child child = nodeShot.getValue(Child.class);
					apps = child.getApps();
					
					Log.i(TAG, "onDataChange: child name: " + child.getName());
					//updateAppStats(apps);
					
				}
			}
			
			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
			
			}
		});
	}
	
	private void getUserLocation() {
		Log.i(TAG, "getUserLocation: executed");
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		LocationListener locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
//					Log.i(TAG, "onLocationChanged: latitude: " + location.getLatitude());
//					Log.i(TAG, "onLocationChanged: longitude: " + location.getLongitude());
					addUserLocationToDatabase(location, uid);
				} else {
					Log.i(TAG, "onLocationChanged: location is null");
				}
			}
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			
			}
			
			@Override
			public void onProviderEnabled(String provider) {
			
			}
			
			@Override
			public void onProviderDisabled(String provider) {
			
			}
		};
		
		//these two statements will be only executed when the permission is granted.
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
			return;
		}
		
	}
	
	private void addUserLocationToDatabase(Location location, String uid) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		HashMap<String, Object> update = new HashMap<>();
		update.put("latitude", latitude);
		update.put("longitude", longitude);
		databaseReference.child("childs").child(uid).child("location").updateChildren(update);
	}
	
	private void uploadContacts(ArrayList<Contact> contacts) {
		databaseReference.child("childs").child(uid).child("contacts").setValue(contacts);
		
	}
	
	private void setFence(DataSnapshot dataSnapshot) {
		com.mzhtech.sismonakdev.models.Location childLocation = dataSnapshot.getValue(com.mzhtech.sismonakdev.models.Location.class);
//		Log.i(TAG, "setFence: getLatitude " + childLocation.getLatitude());
//		Log.i(TAG, "setFence: getLongitude " + childLocation.getLongitude());
//		Log.i(TAG, "setFence: isGeoFence " + childLocation.isGeoFence());
//		Log.i(TAG, "setFence: isOutOfFence " + childLocation.isOutOfFence());
//		Log.i(TAG, "setFence: getFenceCenterLatitude " + childLocation.getFenceCenterLatitude());
//		Log.i(TAG, "setFence: getFenceCenterLongitude " + childLocation.getFenceCenterLongitude());
//		Log.i(TAG, "setFence: getFenceDiameter " + childLocation.getFenceDiameter());

		Log.i(TAG, "setFence: before if isGeoFence");

		if (childLocation.isGeoFence()) {
			Log.i(TAG, "setFence: isGeoFence true");

			if (locationManager == null && locationListener == null){
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				locationListener = new LocationListener() {
					@Override
					public void onLocationChanged(Location location) {
						Log.i(TAG, "setFence: onLocationChanged");

						Query childLocationQuery = databaseReference.child("childs").child(uid).child("location");

						childLocationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
								childLocation.setLongitude(dataSnapshot.child("longitude").getValue(Double.class));
								childLocation.setLatitude(dataSnapshot.child("latitude").getValue(Double.class));
								childLocation.setFenceCenterLatitude(dataSnapshot.child("fenceCenterLatitude").getValue(Double.class));
								childLocation.setFenceCenterLongitude(dataSnapshot.child("fenceCenterLongitude").getValue(Double.class));
								childLocation.setFenceDiameter(dataSnapshot.child("fenceDiameter").getValue(Double.class));
								childLocation.setGeoFence(dataSnapshot.child("geoFence").getValue(Boolean.class));

								if (location != null && childLocation.isGeoFence()) {
									float[] distanceInMeters = new float[1];
									Location.distanceBetween(childLocation.getFenceCenterLatitude(), childLocation.getFenceCenterLongitude(), location.getLatitude(), location.getLongitude(), distanceInMeters);

									Log.i(TAG, "Distance in Meters " + String.valueOf(distanceInMeters[0]));
									Log.i(TAG, "Fence Diameter " + String.valueOf(childLocation.getFenceDiameter()));

									boolean outOfFence = distanceInMeters[0] > childLocation.getFenceDiameter();
									if (outOfFence) {
										Log.i(TAG, "setFence: OUT OF FENCE");
										databaseReference.child("childs").child(uid).child("location").child("outOfFence").setValue(true);
									} else {
										databaseReference.child("childs").child(uid).child("location").child("outOfFence").setValue(false);
									}
								} else {
									Log.i(TAG, "setFence: location is null");
								}
							}

							@Override
							public void onCancelled(@NonNull DatabaseError databaseError) {

							}
						});
					}

					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {

					}

					@Override
					public void onProviderEnabled(String provider) {

					}

					@Override
					public void onProviderDisabled(String provider) {

					}
				};
			}

			//these two statements will be only executed when the permission is granted.
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
				
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISPLACEMENT, locationListener);
				return;
			}
			
			
		}
		
	}

    /*private void changeDNS(String primaryDNS, String secondaryDNS) {
        Settings.System.putString(getContentResolver(), Settings.System.WIFI_STATIC_DNS1, primaryDNS);  //TODO:: DEPRECATED
        Settings.System.putString(getContentResolver(), Settings.System.WIFI_STATIC_DNS2, secondaryDNS);
    }*/
	
	public ArrayList<Contact> getContacts() {
		ArrayList<Contact> contacts = new ArrayList<>();
		ContentResolver contentResolver = getApplicationContext().getContentResolver();
		Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
				if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
					Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
					
					while (cursorInfo.moveToNext()) {
						String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						String contactNumber = cursorInfo.getString(cursorInfo.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						Contact contact = new Contact(contactName, contactNumber);
						contacts.add(contact);
					}
					
					cursorInfo.close();
				}
			}
			cursor.close();
		}
		return contacts;
	}
	
	private void getInstalledApplications(/*ArrayList<App> onlineAppsList*/) {
		PackageManager packageManager = getPackageManager();
		List<ApplicationInfo> applicationInfoList = packageManager.getInstalledApplications(0);
		Collections.sort(applicationInfoList, new ApplicationInfo.DisplayNameComparator(packageManager));
		Iterator<ApplicationInfo> iterator = applicationInfoList.iterator();
		while (iterator.hasNext()) {
			ApplicationInfo applicationInfo = iterator.next();
			if (applicationInfo.packageName.contains("com.google") || applicationInfo.packageName.matches("com.android.chrome"))
				continue;
			if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				iterator.remove();
			}
		}
		prepareData(applicationInfoList, packageManager/*, onlineAppsList*/);
	}
	
	private void prepareData(List<ApplicationInfo> applicationInfoList, PackageManager packageManager/*, ArrayList<App> onlineAppsList*/) {
		ArrayList<App> appsList = new ArrayList<>();
		for (ApplicationInfo applicationInfo : applicationInfoList) {
			if (applicationInfo.packageName != null) {
				appsList.add(new App((String) applicationInfo.loadLabel(packageManager), applicationInfo.packageName, false));
			}
		}
        /*if (onlineAppsList.isEmpty()) {
            Log.i(TAG, "prepareData: online appsList empty");
            for (ApplicationInfo applicationInfo : applicationInfoList) {
                if (applicationInfo.packageName != null) {
                    appsList.add(new App((String) applicationInfo.loadLabel(packageManager), (String) applicationInfo.packageName, false));
                }
            }
            //if not, check the app's blocked attribute and update it.
        } else {
            for (ApplicationInfo applicationInfo : applicationInfoList) {
                for (App app : onlineAppsList) {
                    if (app.getPackageName().equals((String) applicationInfo.packageName)) {
                        appsList.add(new App((String) applicationInfo.loadLabel(packageManager), (String) applicationInfo.packageName, app.isBlocked()));
                    }
                }

            }

        }*/
		
		uploadApps(appsList);
		
	}
	
	private void uploadApps(ArrayList<App> appsList) {
		databaseReference.child("childs").child(uid).child("apps").setValue(appsList);
		Log.i(TAG, "uploadApps: done");
	}
	
	public String getTopAppPackageName() {
		String appPackageName = "";
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				appPackageName = getLollipopForegroundAppPackageName();
			} else {
				appPackageName = getKitkatForegroundAppPackageName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return appPackageName;
	}

	private String getLollipopForegroundAppPackageName() {
		String topPackageName = "";
		long endTime = System.currentTimeMillis();
		long startTime = endTime - 10000; // Check the last 10 seconds

		UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
		if (usageStatsManager != null) {
			List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
			if (usageStatsList != null && !usageStatsList.isEmpty()) {
				SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
				for (UsageStats usageStats : usageStatsList) {
					sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
				}
				if (!sortedMap.isEmpty()) {
					topPackageName = sortedMap.get(sortedMap.lastKey()).getPackageName();
				}
			}

			// Fallback to UsageEvents
			if (topPackageName.isEmpty()) {
				UsageEvents usageEvents = usageStatsManager.queryEvents(startTime, endTime);
				UsageEvents.Event event = new UsageEvents.Event();
				while (usageEvents.hasNextEvent()) {
					usageEvents.getNextEvent(event);
					if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
						topPackageName = event.getPackageName();
					}
				}
			}
		}

		// Filter out system apps
		if (topPackageName.equals("com.google.android.gsf")) {
			topPackageName = "";
		}

		Log.i("TopApp", "Top app package name: " + topPackageName);
		return topPackageName;
	}
	
	private String getKitkatForegroundAppPackageName() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
		return tasks.get(0).processName;
	}

	class LockerThread implements Runnable {

		private Intent intent = null;

		public LockerThread() {
			intent = new Intent(MainForegroundService.this, BlockedAppActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		}

		@Override
		public void run() {
			while (true) {
//				 Log.i(TAG, "run: thread running");

				if (apps != null) {

					String foregroundAppPackageName = getTopAppPackageName();
//					 Log.i(TAG, "run: foreground app: " + foregroundAppPackageName);

					// TODO:: need to handle com.google.android.gsf & com.sec.android.provider.badge
					for (final App app : apps) {
//						 Log.i(TAG, "run: app name: " + app.getAppName() + " blocked: " + app.isBlocked() + "\n");
						if (foregroundAppPackageName.equals(app.getPackageName()) && app.isBlocked()) {
//							 Log.i(TAG, "run: " + app.getPackageName() + " is running");
							intent.putExtra(BLOCKED_APP_NAME_EXTRA, app.getAppName());
							startActivity(intent);
						}
					}
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}