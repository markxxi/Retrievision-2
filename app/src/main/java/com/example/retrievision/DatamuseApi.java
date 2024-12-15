package com.example.retrievision;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DatamuseApi {
    @GET("words")
    Call<List<Synonym>> getSynonyms(@Query("ml") String word);
}
class Synonym {
    String word;

    @Override
    public String toString() {
        return word;
    }
}