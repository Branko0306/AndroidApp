package network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jelav on 26/12/2017.
 */

public final class QueryFilters {

    public QueryFilters(){
    }

    public double Latitude;
    public double Longitude;
    public int sadrzajID;
    public List<Integer> skriveniSadrzaji;

    public String getSkriveni(){
        String tmp = "";
        for (Integer i : skriveniSadrzaji) {
            tmp += i + ";";
        }

        if (tmp != null && tmp.length() > 0 && tmp.charAt(tmp.length() - 1) == ';') {
            tmp = tmp.substring(0, tmp.length() - 1);
        }

        return tmp;
    }
}
