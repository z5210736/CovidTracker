package au.edu.unsw.infs3634.covidtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class DetailActivity extends AppCompatActivity {
    public static final String INTENT_MESSAGE = "TEXT";
    private static final String TAG = "DetailActivity";

    private TextView mCountry;
    private TextView mNewCases;
    private TextView mTotalCases;
    private TextView mNewDeaths;
    private TextView mTotalDeaths;
    private TextView mNewRecovered;
    private TextView mTotalRecovered;
    private ImageView mSearch;
    private ImageView mFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Get the intent
        Intent intent = getIntent();

        //Retrieve the message form the intent
        String countryCode = intent.getStringExtra(INTENT_MESSAGE);

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
                final Country country = Country.getCountry(countryCode);

                //Initialise TextView objects
                mCountry = findViewById(R.id.tvCountry);
                mNewCases = findViewById(R.id.tvNewCases);
                mTotalCases = findViewById(R.id.tvTotalCases);
                mNewDeaths = findViewById(R.id.tvNewDeaths);
                mTotalDeaths = findViewById(R.id.tvTotalDeaths);
                mNewRecovered = findViewById(R.id.tvNewRecovered);
                mTotalRecovered = findViewById(R.id.tvTotalRecovered);
                mSearch = findViewById(R.id.ivSearch);
                mFlag = findViewById(R.id.ivFlag);

                //Set value to the TextView objects
                DecimalFormat df = new DecimalFormat("#,###,###,###");
                mCountry.setText(country.getCountry());
                mNewCases.setText(String.valueOf(country.getNewConfirmed()));
                mTotalCases.setText(String.valueOf(country.getTotalConfirmed()));
                mNewDeaths.setText(String.valueOf(country.getNewDeaths()));
                mTotalDeaths.setText(String.valueOf(country.getTotalDeaths()));
                mNewRecovered.setText(String.valueOf(country.getNewRecovered()));
                mTotalRecovered.setText(String.valueOf(country.getTotalRecovered()));
                Glide.with(holder.itemView).load("https://www.countryflags.io/" +country.getCountryCode() + "/shiny/64.png").into(holder.flag);

                //Implement setOnClickListener for the search ImageView
                mSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Declare an intent object & open Youtube
                        Intent webintent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/" + country.getCountry()));
                        startActivity(webintent);
                    }

                });

            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Log.d(TAG, "onFailure: API call failed.");
            }

        });
    }
}