package com.mzhtech.sismonakdev.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.adapters.StatAdapter;
import com.mzhtech.sismonakdev.models.App;
import com.mzhtech.sismonakdev.utils.CustomValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static com.mzhtech.sismonakdev.activities.ParentSignedInActivity.CHILD_EMAIL_EXTRA;

public class StatFragment extends Fragment {
    public static final String TAG = "StatFragmentTAG";
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<App> apps;
    private StatAdapter statAdapter;
    private RecyclerView recyclerViewStat;
    private Context context;
    private String childEmail;
    private String appName;
    private String packageName;
    private Bundle bundle;

    private TextView total_usage;
    private TextView top1_app;
    private TextView top1_usage;
    private TextView top2_app;
    private TextView top2_usage;
    private TextView top3_app;
    private TextView top3_usage;
    private TextView top4_app;
    private TextView top4_usage;
    private TextView top5_app;
    private TextView top5_usage;
    private TextView other_app;
    private TextView other_usage;

    private PieChart pie_chart;

    private ArrayList<Long> usage = new ArrayList<>(Collections.nCopies(6, 0L));


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stat, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        Bundle bundle = getActivity().getIntent().getExtras();
        childEmail = bundle.getString(CHILD_EMAIL_EXTRA);

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = formatter.format(new Date());

//        recyclerViewStat = view.findViewById(R.id.recyclerViewStat);
//        recyclerViewStat.setHasFixedSize(true);
//        recyclerViewStat.setLayoutManager(new LinearLayoutManager(getContext()));


        total_usage = view.findViewById(R.id.total_usage);
        top1_app = view.findViewById(R.id.top1_app);
        top1_usage = view.findViewById(R.id.top1_usage);
        top2_app = view.findViewById(R.id.top2_app);
        top2_usage = view.findViewById(R.id.top2_usage);
        top3_app = view.findViewById(R.id.top3_app);
        top3_usage = view.findViewById(R.id.top3_usage);
        top4_app = view.findViewById(R.id.top4_app);
        top4_usage = view.findViewById(R.id.top4_usage);
        top5_app = view.findViewById(R.id.top5_app);
        top5_usage = view.findViewById(R.id.top5_usage);
        other_app = view.findViewById(R.id.other_app);
        other_usage = view.findViewById(R.id.other_usage);

        pie_chart = view.findViewById(R.id.pie_chart);

//        initializeChart();
        getData();

    }


    private String formatDuration(long duration) {
        String hour_string = getContext().getString(R.string.hour);
        String minute_string = getContext().getString(R.string.minute);
        long minutes = (duration / 1000) / 60;
        long hours = minutes / 60;
        minutes = minutes % 60;
        return String.format("%d %s %02d %s", hours, hour_string, minutes, minute_string);
    }

//    private void initializeChart() {
//        ArrayList<PieEntry> durations = new ArrayList<>();
//        durations.add(new PieEntry(200, "#1"));
//        durations.add(new PieEntry(200, "#2"));
//        durations.add(new PieEntry(200, "#3"));
//        durations.add(new PieEntry(200, "#4"));
//        durations.add(new PieEntry(200, "#5"));
//        durations.add(new PieEntry(200, "Other"));
//
//        PieDataSet pieDataSet = new PieDataSet(durations, "");
//        pieDataSet.setColors(new int[]{Color.rgb(69, 181, 255), Color.rgb(64, 168, 237), Color.rgb(55, 144, 204), Color.rgb(46, 121, 171), Color.rgb(40, 105, 148), Color.rgb(34, 88, 125)});
//        pieDataSet.setValueTextColor(Color.BLACK);
//        pieDataSet.setValueTextSize(16f);
//        pieDataSet.setDrawValues(false);
//
//        PieData pieData = new PieData(pieDataSet);
//        pie_chart.setData(pieData);
//        pie_chart.getDescription().setEnabled(false);
//        pie_chart.setCenterText("Usage");
//        pie_chart.setCenterTextSize(16f);
//        pie_chart.setUsePercentValues(true);
//        pie_chart.getLegend().setEnabled(false);
//        pie_chart.setNoDataText("No current data");
//        pie_chart.animateXY(500, 500);
//        pie_chart.invalidate();
//    }

    private void updateChart(ArrayList<Long> usage) {
        ArrayList<PieEntry> durations = new ArrayList<>();
        durations.add(new PieEntry(usage.get(0), ""));
        durations.add(new PieEntry(usage.get(1), ""));
        durations.add(new PieEntry(usage.get(2), ""));
        durations.add(new PieEntry(usage.get(3), ""));
        durations.add(new PieEntry(usage.get(4), ""));
        durations.add(new PieEntry(usage.get(5), ""));

        PieDataSet pieDataSet = new PieDataSet(durations, "");
        pieDataSet.setColors(new int[]{Color.rgb(69, 181, 255), Color.rgb(64, 168, 237), Color.rgb(55, 144, 204), Color.rgb(46, 121, 171), Color.rgb(40, 105, 148), Color.rgb(34, 88, 125)});
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16f);
        pieDataSet.setValueFormatter(new CustomValueFormatter(5.0f));
//        pieDataSet.setDrawValues(false);
//        pieDataSet.setValueLinePart1OffsetPercentage(80.f);
//        pieDataSet.setValueLinePart1Length(0.4f);
//        pieDataSet.setValueLinePart2Length(0.4f);
//        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(pieDataSet);
        pie_chart.setData(pieData);
        pie_chart.getDescription().setEnabled(false);
//        pie_chart.setCenterText(getContext().getString(R.string.graph));
        pie_chart.setCenterTextSize(16f);
        pie_chart.setUsePercentValues(true);
        pie_chart.getLegend().setEnabled(false);
        pie_chart.animateXY(500, 500);
        pie_chart.invalidate();
    }


    private void getData(){
        Query query = databaseReference.child("childs").orderByChild("email").equalTo(childEmail).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String childId = "";
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    childId = childSnapshot.getKey(); // This is your dynamic ID like "CxZiHIcM7tW52aOYtPJuTWwBpAf1"
                    Log.d("Firebase", "Child ID: " + childId);
                }
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                String currentDate = formatter.format(new Date());
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child("users")
                        .child("childs")
                        .child(childId)
                        .child("stat")
                        .child(currentDate);

                Log.i("Date", currentDate);

                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            top1_app.setText(dataSnapshot.child("top1_app").getValue(String.class));
                            top1_usage.setText(formatDuration(dataSnapshot.child("top1_usage").getValue(Long.class)));
                            usage.set(0,dataSnapshot.child("top1_usage").getValue(Long.class));
                            if (dataSnapshot.child("top2_app").exists()){
                                top2_app.setText(dataSnapshot.child("top2_app").getValue(String.class));
                                top2_usage.setText(formatDuration(dataSnapshot.child("top2_usage").getValue(Long.class)));
                                usage.set(1,dataSnapshot.child("top2_usage").getValue(Long.class));
                                if (dataSnapshot.child("top3_app").exists()) {
                                    top3_app.setText(dataSnapshot.child("top3_app").getValue(String.class));
                                    top3_usage.setText(formatDuration(dataSnapshot.child("top3_usage").getValue(Long.class)));
                                    usage.set(2,dataSnapshot.child("top3_usage").getValue(Long.class));
                                    if (dataSnapshot.child("top4_app").exists()) {
                                        top4_app.setText(dataSnapshot.child("top4_app").getValue(String.class));
                                        top4_usage.setText(formatDuration(dataSnapshot.child("top4_usage").getValue(Long.class)));
                                        usage.set(3,dataSnapshot.child("top4_usage").getValue(Long.class));
                                        if (dataSnapshot.child("top5_app").exists()){
                                            top5_app.setText(dataSnapshot.child("top5_app").getValue(String.class));
                                            top5_usage.setText(formatDuration(dataSnapshot.child("top5_usage").getValue(Long.class)));
                                            usage.set(4,dataSnapshot.child("top5_usage").getValue(Long.class));
                                            other_usage.setText(formatDuration(dataSnapshot.child("other_usage").getValue(Long.class)));
                                            usage.set(5,dataSnapshot.child("other_usage").getValue(Long.class));
                                        }
                                    }
                                }
                            }
                            total_usage.setText(formatDuration(dataSnapshot.child("totalAppDuration").getValue(Long.class)));
                            updateChart(usage);
                        }
                        else {
                            Log.i("Firebase", "No data for that date");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("Firebase", "Failed to read value.", databaseError.toException());
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



    }

}
