package com.example.currencyconverter;
//import retrofit2.*;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//import java.util.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner cur_from = findViewById(R.id.from);
        final Spinner cur_to = findViewById(R.id.to);
        Button convert = findViewById(R.id.convert);
        Button reverse = findViewById(R.id.reverse);
        final EditText from_num = findViewById(R.id.from_num);
        final EditText to_num = findViewById(R.id.to_num);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.currency));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        cur_from.setAdapter(adapter);
        cur_to.setAdapter(adapter);

        final Double usd = 1.0;
        final Double eur = .909087;
        final Double btc = .000104405;
        final Double jpy = 109.866;

        convert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String from = cur_from.getSelectedItem().toString();
                String to = cur_to.getSelectedItem().toString();
                if (from.equals("USD")) {
                    if (to.equals("USD"))
                        to_num.setText(from_num.getText());
                    else if (to.equals("EUR"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) * eur));
                    else if (to.equals("BTC"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) * btc));
                    else if (to.equals("JPY"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) * jpy));

                }
                else if (from.equals("EUR")) {
                    if (to.equals("USD"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / eur));
                    else if (to.equals("EUR"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString())));
                    else if (to.equals("BTC"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / eur * btc));
                    else if (to.equals("JPY"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / eur * jpy));
                }

                else if (from.equals("BTC")) {
                    if (to.equals("USD"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / btc));
                    else if (to.equals("EUR"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / btc * eur));
                    else if (to.equals("BTC"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString())));
                    else if (to.equals("JPY"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / btc * jpy));
                }

                else {
                    if (to.equals("USD"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / jpy));
                    else if (to.equals("EUR"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / jpy * eur));
                    else if (to.equals("BTC"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString()) / jpy * btc));
                    else if (to.equals("JPY"))
                        to_num.setText(String.valueOf(Double.parseDouble(from_num.getText().toString())));
                }

            }
        });

        reverse.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int old_from = cur_from.getSelectedItemPosition();
                int old_to = cur_to.getSelectedItemPosition();
                cur_from.setSelection(old_to);
                cur_to.setSelection(old_from);
            }
        });
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.nomics.com/v1/markets?key=71f9717129072cdbd45edf20f6317c10")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        JsonPlaceHolderApi json = retrofit.create(JsonPlaceHolderApi.class);
//
//        Call<List<Post>> call = json.getPosts();
//
//        call.enqueue((new Callback<List<Post>>() {
//            @Override
//            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
//                if (!response.isSuccessful())
//
//            }
//
//            @Override
//            public void onFailure(Call<List<Post>> call, Throwable t) {
//
//            }
//        }));
    }

}
