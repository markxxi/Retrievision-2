package com.example.retrievision;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Syn {
    private static final String BASE_URL = "https://api.datamuse.com/";
    private DatamuseApi api;

    public Syn() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(DatamuseApi.class);
    }

    public void getSynonyms(String word, Callback<List<Synonym>> callback) {
        Call<List<Synonym>> call = api.getSynonyms(word);
        call.enqueue(callback);
    }
}
