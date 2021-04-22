package inf.um.comov;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface mylnikov {

    @GET("geolocation/cell")
    Call<JsonObject> getGet(@Query("v") String v, @Query("data") String data, @Query("mcc") int mcc, @Query("mnc") int mnc, @Query("lac") int lac,
                            @Query("cellid") int cellid);

}
