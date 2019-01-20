package solar.piyush.com.solarcalculator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class PinnedLocationsAdapter extends RecyclerView.Adapter<PinnedLocationsAdapter.LocationViewModel> {

    private final LayoutInflater mInflater;
    private List<LocationEntity> mPinnedLocations;
    Context context;
    CustomItemClickListener listener;

    PinnedLocationsAdapter(Context context , CustomItemClickListener listener){
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PinnedLocationsAdapter.LocationViewModel onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = mInflater.inflate(R.layout.pinned_list_item,viewGroup, false);

        final LocationViewModel mViewHolder = new LocationViewModel(itemView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(view , mViewHolder.getAdapterPosition());
            }
        });

        return mViewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull PinnedLocationsAdapter.LocationViewModel locationViewModel, int position) {
        if (mPinnedLocations != null) {
            LocationEntity current = mPinnedLocations.get(position);
            locationViewModel.mPinnedName.setText(current.getName());
            locationViewModel.mPinnedLatLng.setText(current.getMlat()+" , "+current.getMlng());

        } else {
            // Covers the case of data not being ready yet.
            locationViewModel.mPinnedName.setText("No Pinned Locations");
        }
    }

    @Override
    public int getItemCount() {
        if (mPinnedLocations != null)
            return mPinnedLocations.size();
        return 0;
    }
    void setData(List<LocationEntity> entities) {
        mPinnedLocations = entities;
        notifyDataSetChanged();
    }


    public class LocationViewModel extends RecyclerView.ViewHolder {
        private final TextView mPinnedName ;
        private final TextView mPinnedLatLng;
        public LocationViewModel(@NonNull View itemView) {
            super(itemView);

            mPinnedName = itemView.findViewById(R.id.tv_pinned_name);
            mPinnedLatLng = itemView.findViewById(R.id.tv_pinned_latLng);

        }
    }
}
