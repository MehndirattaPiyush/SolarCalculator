package solar.piyush.com.solarcalculator;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;
@Dao
public interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(LocationEntity location);

    @Query("SELECT * from pinned_locations")
    List<LocationEntity> getAllPinnedLocations(); // run karna backspace ni chal raha patani q :Plol
}
