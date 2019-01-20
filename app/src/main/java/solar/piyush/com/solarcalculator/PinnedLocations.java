package solar.piyush.com.solarcalculator;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class PinnedLocations extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_locations);

        RecyclerView recyclerView = findViewById(R.id.rl_pinned_locations);

        LocationViewModel locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);

        final List<LocationEntity> data = locationViewModel.getmAllPinnedLocations();

        final PinnedLocationsAdapter adapter = new PinnedLocationsAdapter(this, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext() , MapsActivity.class);
                intent.putExtra("name" , data.get(position).getName());
                intent.putExtra("lat" , data.get(position).getMlat());
                intent.putExtra("lng" , data.get(position).getMlng());
                startActivity(intent);
            }
        });


        adapter.setData(data);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }


}
