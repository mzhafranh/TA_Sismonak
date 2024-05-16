package com.mzhtech.sismonakdev.broadcasts;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static com.mzhtech.sismonakdev.services.MainForegroundService.ACTION_REQUEST_UPLOAD;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AppUsageReceiver extends BroadcastReceiver {
    public static final String TAG = "AppUsageReceiver";
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser user;
    private Context context;

    public AppUsageReceiver(FirebaseUser user) {
        this.user = user;
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.i(TAG, "sampai onReceive");
        Calendar minuteCheck = Calendar.getInstance();
        int minutes = minuteCheck.get(Calendar.MINUTE);
        Log.i(TAG, "minutes " + String.valueOf(minutes));

        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {

//            Calendar minuteCheck = Calendar.getInstance();
//            int minutes = minuteCheck.get(Calendar.MINUTE);

            if (minutes == 0) {

                String uid = user.getUid();

                Log.i(TAG, "onReceive: AppUsageReceiver");

                UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                Calendar calendar = Calendar.getInstance();
                long endTime = calendar.getTimeInMillis();
                calendar.add(Calendar.DATE, -1);
                long startTime = calendar.getTimeInMillis();


                List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);


                long totalAppDuration = 0;
                PackageManager pm = context.getPackageManager();

                Map<String, Long> appUsageDurationMap = new HashMap<>();
                if (usageStatsList != null && !usageStatsList.isEmpty()) {
                    for (UsageStats usageStats : usageStatsList) {
                        if (!checkSystemApp(usageStats.getPackageName(), pm)) {
                            long totalDuration = 0;
                            totalDuration = appUsageDurationMap.getOrDefault(usageStats.getPackageName(), 0L);
                            totalDuration += usageStats.getTotalTimeInForeground();
                            appUsageDurationMap.put(usageStats.getPackageName(), totalDuration);
                        }
                    }

                    LinkedHashMap<String, Long> sortedMap = appUsageDurationMap.entrySet()
                            .stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1, LinkedHashMap::new));

                    for (Long value : sortedMap.values()) {
                        totalAppDuration += value;
                    }

                    List<Map.Entry<String, Long>> entryList = new ArrayList<>(sortedMap.entrySet());
                    long totalTop5Duration = 0;

                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    String currentDate = formatter.format(new Date());

                    databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("totalAppDuration").setValue(totalAppDuration);

                    if (entryList.size() > 0) {
    //                    top1_app.setText(entryList.get(0).getKey());
    //                    top1_usage.setText(formatDuration(entryList.get(0).getValue()));
                        databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top1_app").setValue(getAppNameFromPackage(entryList.get(0).getKey(), pm));
                        databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top1_usage").setValue(entryList.get(0).getValue());
                        totalTop5Duration += entryList.get(0).getValue();
                        if (entryList.size() > 1) {
                            databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top2_app").setValue(getAppNameFromPackage(entryList.get(1).getKey(), pm));
                            databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top2_usage").setValue(entryList.get(1).getValue());
                            totalTop5Duration += entryList.get(1).getValue();
                            if (entryList.size() > 2) {
                                databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top3_app").setValue(getAppNameFromPackage(entryList.get(2).getKey(), pm));
                                databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top3_usage").setValue(entryList.get(2).getValue());
                                totalTop5Duration += entryList.get(2).getValue();
                                if (entryList.size() > 3) {
                                    databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top4_app").setValue(getAppNameFromPackage(entryList.get(3).getKey(), pm));
                                    databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top4_usage").setValue(entryList.get(3).getValue());
                                    totalTop5Duration += entryList.get(3).getValue();
                                    if (entryList.size() > 4) {
                                        databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top5_app").setValue(getAppNameFromPackage(entryList.get(4).getKey(), pm));
                                        databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("top5_usage").setValue(entryList.get(4).getValue());
                                        totalTop5Duration += entryList.get(4).getValue();
                                        databaseReference.child("childs").child(uid).child("stat").child(currentDate).child("other_usage").setValue(totalAppDuration - totalTop5Duration);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean checkSystemApp(String packageName, PackageManager packageManager) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1 || appInfo.packageName.contains("com.google"));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found ", e);
            return false; // Return the package name if an app name is not found
        }
    }

    private String getAppNameFromPackage(String packageName, PackageManager packageManager) {
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return (String) packageManager.getApplicationLabel(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Package name not found ", e);
            return packageName; // Return the package name if an app name is not found
        }
    }
}