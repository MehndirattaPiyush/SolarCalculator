package solar.piyush.com.solarcalculator;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.List;

public class LocationViewModel extends AndroidViewModel {

    private LocationRepository mRepository;

    private List<LocationEntity> mAllPinnedLocations;

    public LocationViewModel (Application application) {
        super(application);
        mRepository = new LocationRepository(application);
        mAllPinnedLocations = mRepository.getmAllPinnedLocations();
    }

    List<LocationEntity> getmAllPinnedLocations() { return mAllPinnedLocations; }

    public void insert(LocationEntity entity) { mRepository.insert(entity); }
}
