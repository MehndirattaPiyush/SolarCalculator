package solar.piyush.com.solarcalculator;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "pinned_locations")
public class LocationEntity {



    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id; //try again nothing :( ek sec google kiya karta hun ye hata do aur id jab bhi store karo humesha 0 pass karana hai

    @ColumnInfo(name = "lat")
    private double mlat;

    @ColumnInfo(name = "lng")
    private double mlng;


    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    public LocationEntity(double mlat, double mlng,  String name , int id ) {
        this.id = id;
        this.mlat = mlat;
        this.mlng = mlng;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public double getMlat() {
        return mlat;
    }


    public double getMlng() {
        return mlng;
    }


    @NonNull
    public String getName() {
        return name;
    }
}
