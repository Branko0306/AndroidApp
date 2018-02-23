package models;

import android.arch.persistence.room.ColumnInfo;

/**
 * Created by jelav on 28/12/2017.
 */

public class Sadrzaj {

    public Integer pk;

    public String naziv;

    public String skraceniopis;

    public String opis;

    public String kategorija;

    public Double lokacijaLatitude;

    public Double lokacijaLongitude;

    public Integer udaljenost;

    public Integer lokacijaPK;
}
