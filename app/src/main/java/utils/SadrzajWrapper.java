package utils;
import com.google.gson.Gson;

import models.SadrzajResponse;

/**
 * Created by jelav on 28/12/2017.
 */

public class SadrzajWrapper {

    public  static SadrzajResponse fromJson(String jsonString){
        Gson gson = new Gson();
        return gson.fromJson(jsonString, SadrzajResponse.class);
    }

    public static String toString(SadrzajResponse response) {
        Gson gson = new Gson();
        return gson.toJson(response);
    }

}
