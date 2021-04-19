package inf.um.comov;

import android.telecom.Call;

import java.util.List;

import retrofit2.http.GET;


public interface mylnikov {

    @GET
    retrofit2.Call<List<Get>> getGet();

}
