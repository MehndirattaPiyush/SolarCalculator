package solar.piyush.com.solarcalculator;

import android.app.Application;
import android.location.Location;
import android.os.AsyncTask;

import java.util.List;

public class LocationRepository {

    private LocationDao mLocationDao;
    private List<LocationEntity> mAllPinnedLocations;

    LocationRepository(Application application) {
        LocationRoomDatabase db = LocationRoomDatabase.getDatabase(application);
        mLocationDao = db.locationDao();
        mAllPinnedLocations = mLocationDao.getAllPinnedLocations();
    }

    List<LocationEntity> getmAllPinnedLocations() {
        return mAllPinnedLocations;
    }


    public void insert (LocationEntity locationEntity) {
        new insertAsyncTask(mLocationDao).execute(locationEntity);
    }

    private static class insertAsyncTask extends AsyncTask<LocationEntity, Void, Void> {

        private LocationDao mAsyncTaskDao;

        insertAsyncTask(LocationDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final LocationEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }
}
