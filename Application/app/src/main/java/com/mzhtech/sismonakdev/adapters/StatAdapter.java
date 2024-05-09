package com.mzhtech.sismonakdev.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.interfaces.OnAppClickListener;
import com.mzhtech.sismonakdev.models.App;
import com.mzhtech.sismonakdev.models.Stat;
import com.mzhtech.sismonakdev.utils.BackgroundGenerator;

import java.util.ArrayList;

public class StatAdapter extends RecyclerView.Adapter<StatAdapter.StatAdapterViewHolder> {
    private static final String TAG = "StatAdapterTAG";
    private Context context;
    private ArrayList<Stat> stats;
//    private OnAppClickListener onAppClickListener;

    public StatAdapter(Context context, ArrayList<Stat> stats){

        this.context = context;
        this.stats = stats;
    }


//    public void setOnAppClickListener(OnAppClickListener onAppClickListener) {
//        this.onAppClickListener = onAppClickListener;
//    }

    @NonNull
    @Override
    public StatAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_app, viewGroup, false);
        return new StatAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatAdapterViewHolder statAdapterViewHolder, int i) {
//        App app = apps.get(i);
//        if (app != null) {
//            statAdapterViewHolder.txtAppName.setText(app.getAppName());
//            statAdapterViewHolder.switchAppState.setChecked(app.isBlocked());
//            statAdapterViewHolder.txtAppBackground.setText(BackgroundGenerator.getFirstCharacters(app.getAppName()));
//            statAdapterViewHolder.txtAppBackground.setBackground(BackgroundGenerator.getBackground(context));
//        }
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    public class StatAdapterViewHolder extends RecyclerView.ViewHolder {
        private TextView txtAppBackground;
        private TextView txtAppName;
        private Switch switchAppState;

        public StatAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
//            txtAppBackground = itemView.findViewById(R.id.txtAppBackground);
//            txtAppName = itemView.findViewById(R.id.txtAppName);
//            switchAppState = itemView.findViewById(R.id.switchAppState);
//            switchAppState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (buttonView.isPressed()) {
//                        onAppClickListener.onItemClick(apps.get(getAdapterPosition()).getPackageName(), apps.get(getAdapterPosition()).getAppName(), isChecked); //changed from txtAppName.getText()
//                        Log.i(TAG, "onCheckedChanged: packageName: " + apps.get(getAdapterPosition()).getPackageName());
//                        Log.i(TAG, "onCheckedChanged: appName: " + apps.get(getAdapterPosition()).getAppName());
//                    }
//
//                }
//            });
        }
    }

}
