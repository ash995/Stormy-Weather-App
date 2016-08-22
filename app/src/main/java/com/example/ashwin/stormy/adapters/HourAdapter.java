package com.example.ashwin.stormy.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ashwin.stormy.R;
import com.example.ashwin.stormy.weather.Hour;

/**
 * Created by ashwin on 21/8/16.
 */
public class HourAdapter extends RecyclerView.Adapter<HourAdapter.HourViewHolder> {

    private Hour[] mHours;
    Context mContext;

    public HourAdapter(Hour[] hours,Context context){
        mHours = hours;mContext = context;
    }

    @Override
    public HourViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hourly_forecast_list_item,parent,false);
        HourViewHolder viewHolder = new HourViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HourViewHolder holder, int position) {

         holder.bindHour(mHours[position]);

    }

    @Override
    public int getItemCount() {
        return mHours.length;
    }

    public class HourViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTimeLabel;
        public TextView mSummaryLabel;
        public ImageView mIconLabel;
        public TextView mTemperatureLabel;

        public HourViewHolder(View itemView) {
            super(itemView);

            mTimeLabel = (TextView) itemView.findViewById(R.id.timeLabel);
            mSummaryLabel = (TextView) itemView.findViewById(R.id.summaryLabel);
            mTemperatureLabel = (TextView) itemView.findViewById(R.id.temperatureLabel);
            mIconLabel = (ImageView) itemView.findViewById(R.id.iconImageView);

            itemView.setOnClickListener(this);
        }

        public void bindHour(Hour hour){

            mIconLabel.setImageResource(hour.getIconId());
            mTemperatureLabel.setText(hour.getTemperature()+ " ");
            mSummaryLabel.setText(hour.getSummary());
            mTimeLabel.setText(hour.getHour());
        }


        @Override
        public void onClick(View v) {
            String time = mTimeLabel.getText().toString();
            String temp = mTemperatureLabel.getText().toString();
            String summary = mSummaryLabel.getText()
                    .toString();

            String message = String.format("At %s the high will be %s and it will be %s"
                    ,time
                    ,temp
                    ,summary);

            Toast.makeText(mContext,message,Toast.LENGTH_LONG).show();
        }
    }
}
