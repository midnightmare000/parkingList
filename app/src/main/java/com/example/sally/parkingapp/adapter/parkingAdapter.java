package com.example.sally.parkingapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sally.parkingapp.ParkingDetailActivity;
import com.example.sally.parkingapp.R;
import com.example.sally.parkingapp.item.Parking;

import java.util.List;

public class parkingAdapter extends RecyclerView.Adapter<parkingAdapter.ViewHolder> {
    private List<Parking> parkingList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTv,areaTv,serviceTv,addressTv;
        public ViewHolder(View v) {
            super(v);
            nameTv = (TextView) v.findViewById(R.id.parkingName);
            areaTv = (TextView) v.findViewById(R.id.parkingArea);
            serviceTv = (TextView) v.findViewById(R.id.parkingServiceTime);
            addressTv = (TextView) v.findViewById(R.id.parkingAddress);
        }
    }

    public parkingAdapter(List<Parking> parkings) {
        parkingList = parkings;
    }

    public void setParking(List<Parking> parkings){
        parkingList.clear();
        parkingList.addAll(parkings);
    }

    @Override
    public parkingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_parking, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Parking parking = parkingList.get(position);
        holder.nameTv.setText(parking.getName());
        holder.areaTv.setText(parking.getArea());
        holder.serviceTv.setText(parking.getServiceTime());
        holder.addressTv.setText(parking.getAddress());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Bundle bundle = new Bundle();
                bundle.putString("name", parking.getName());
                bundle.putString("area", parking.getArea());
                bundle.putString("serviceTime", parking.getServiceTime());
                bundle.putString("address", parking.getAddress());
                bundle.putDouble("lat", parking.getLat());
                bundle.putDouble("lon", parking.getLon());
                Intent intent = new Intent(context, ParkingDetailActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parkingList.size();
    }
}

