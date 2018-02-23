package database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class SadrzajLogEntity {

    @PrimaryKey
    @ColumnInfo(name = "sadrzaj_pk")
    private Integer sadrzajPK;

    @ColumnInfo(name = "skriven")
    private boolean skriven;

    @ColumnInfo(name = "prikazanaObavijest")
    private boolean prikazanaObavijest;

    //region getters setters

    public Integer getSadrzajPK() {
        return sadrzajPK;
    }

    public void setSadrzajPK(Integer sadrzajPK) {
        this.sadrzajPK = sadrzajPK;
    }

    public boolean isSkriven() {
        return skriven;
    }

    public void setSkriven(boolean skriven) {
        this.skriven = skriven;
    }

    public boolean isPrikazanaObavijest() {
        return prikazanaObavijest;
    }

    public void setPrikazanaObavijest(boolean prikazanaObavijest) {
        this.prikazanaObavijest = prikazanaObavijest;
    }

    //endregion
}
