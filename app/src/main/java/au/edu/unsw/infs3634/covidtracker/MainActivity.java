package au.edu.unsw.infs3634.covidtracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private CountryAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private CountryDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = findViewById(R.id.rvList);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        CountryAdapter.Listener listener = new CountryAdapter.Listener() {
            @Override
            public void onClick(View view, String countryCode) {
                launchDeathlyActivity(countryCode);
            }
        };

        mAdapter = new CountryAdapter(new ArrayList<Country>(), listener);
        mRecyclerView.setAdapter(mAdapter);

        mDb = Room.databaseBuilder(getApplicationContext(), CountryDatabase.class, "country-database").build();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // set the adapter with the list of countries from the database
                mAdapter.setData(mDb.countryDao().getCountries());
                // sort the recycleview list


            }
        });

        Retrofit retrofit = new Retrofit.Builder() // Implement a Retrofit instance
                .baseUrl("https://api.covid19api.com") // Specify the baseUrl
                .addConverterFactory(GsonConverterFactory.create()) //Implement Retrofit GSON converter
                .build();

        // Implement the service interface
        CovidService service = retrofit.create(CovidService.class);

        //Implement a call & response object
        Call<Response> responseCall = service.getResponse();

        responseCall.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Log.d(TAG, "onResponse: API call succeeded!");
                List<Country> countries = response.body().getCountries();
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.countryDao().deleteAll(mDb.countryDao().getCountries().toArray(new Country[0]));

                        mDb.countryDao().insertAll(mDb.countryDao().getCountries().toArray(new Country[0]));

                    }
                });
                mAdapter.setData(countries);
                mRecyclerView.setAdapter(mAdapter);
            }
            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d(TAG, "onFailure: API call failed.");
            }
            });
    }

    private void launchDeathlyActivity(String message) {
        //Declare a new Intent to launch DetailActivity
        Intent intent = new Intent(this, DetailActivity.class);

        //put the message into intent
        intent.putExtra(DetailActivity.INTENT_MESSAGE,message);
        //start the intent
        startActivity(intent);
    }

    //Instantiate the menu
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
    //React to the user interaction with the menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.sort_new:
                //call a sort method form CountryAdapter class
                mAdapter.sort(1);
                //To-do
                return true;
            case R.id.sort_total:
                mAdapter.sort(2);
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }
}