package com.example.stocksearch;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ListView auto_complete_view;
    TextView today_view;
    TextView worth_view;
    TextView balance_view;
    ProgressBar progressBar_view;
    float worth;
    float balance;
    List<Stocks> ports;
    List<Stocks> favs;
    ActivityResultLauncher<Intent> activityResultLauncher;
    PortAdapter portAdapter;
    FavAdapter favAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Stocksearch);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        auto_complete_view = findViewById(R.id.autocomplete);
        today_view = findViewById(R.id.today);
        progressBar_view = findViewById(R.id.progressBar1);

//        activityResultLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> loadData());

        begin_load();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
        LocalDateTime today = LocalDateTime.now();
        today_view.setText(dtf.format(today));



        String[] name = {};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>( this, android.R.layout.simple_list_item_1, name);
        auto_complete_view.setAdapter(arrayAdapter);
        auto_complete_view.setVisibility(View.GONE);
        auto_complete_view.setOnItemClickListener((adapterView, view, i, l) -> {
            String s = (String) auto_complete_view.getItemAtPosition(i);
            Log.d("aoligei", "Open Stock detail " + s);
            Intent intent = new Intent(MainActivity.this, StockActivity.class);
            intent.putExtra("symbol", s.split("\\|")[0].trim());
//            activityResultLauncher.launch(intent);
            startActivity(intent);
        });

        TextView source = findViewById(R.id.source);
        source.setClickable(true);
        source.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable s =(Spannable) Html.fromHtml("<a href='https://finnhub.io/'>Powered by Finnhub </a>",
                Html.FROM_HTML_MODE_LEGACY);
        for (URLSpan u: s.getSpans(0, s.length(), URLSpan.class)) {
            s.setSpan(new UnderlineSpan() {
                public void updateDrawState(TextPaint tp) {
                    tp.setUnderlineText(false);
                }
            }, s.getSpanStart(u), s.getSpanEnd(u), 0);
        }
        source.setText(s);

        finish_load();

    }

    private void begin_load() {
        progressBar_view.setVisibility(View.VISIBLE);
        LinearLayout main_content = findViewById(R.id.main_content);
        main_content.setVisibility(View.GONE);
    }

    private  void finish_load(){
        new CountDownTimer(500, 500) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
                progressBar_view.setVisibility(View.GONE);
                LinearLayout main_content = findViewById(R.id.main_content);
                main_content.setVisibility(View.VISIBLE);
            }

        }.start();

    }


    private void loadData() {

        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        balance = pref.getFloat("balance",25000);
        worth = pref.getFloat("worth", 25000);

        updatePortfolio(pref);
        updateFavorite(pref);

        worth_view = findViewById(R.id.worth);
        balance_view = findViewById(R.id.balance);
        String temp = "$" + String.format(Locale.ENGLISH,"%.2f", balance);
        balance_view.setText(temp);
        temp = "$" + String.format(Locale.ENGLISH,"%.2f", worth);
        worth_view.setText(temp);


    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//            ports.remove(viewHolder.getAdapterPosition());
//            portAdapter.notifyDataSetChanged();
        }
    };

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback2 = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            favs.remove(viewHolder.getAdapterPosition());
            Stocks s = favs.get(viewHolder.getAdapterPosition());
            String symbol = s.getSymbol();
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
            editor.putBoolean("fav_"+symbol, false);
            editor.apply();
            favAdapter.notifyDataSetChanged();
        }
    };

    private void updatePortfolio(SharedPreferences pref) {
        ports = new ArrayList<>();

        Map<String,?> keys = pref.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            String k = entry.getKey();
            if (k.contains("shares")){
                String[] a = k.split("_");
                String symbol = a[1];
                int shares = Integer.parseInt(entry.getValue().toString());
                if (shares > 0 ){
                    float total_cost = pref.getFloat("cost_"+symbol, 0);
                    Stocks p = new Stocks(symbol,shares,total_cost/shares,total_cost, "");
                    ports.add(p);
                }
            }
        }
        RecyclerView recyclerView = findViewById(R.id.recycler_portfolio); //todo update balance change

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        portAdapter = new PortAdapter(ports, MainActivity.this);
//        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(portAdapter);

        portAdapter.setRecyclerItemClickListener(position -> {
            Intent intent = new Intent(MainActivity.this, StockActivity.class);
            intent.putExtra("symbol", ports.get(position).getSymbol());
//            activityResultLauncher.launch(intent);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        loadData();
        super.onResume();
    }

    private void updateFavorite(SharedPreferences pref) {
        favs = new ArrayList<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_fav);
        LinearLayoutManager linearLayoutManager0 = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager0);
        favAdapter = new FavAdapter(favs, MainActivity.this);
        new ItemTouchHelper(itemTouchHelperCallback2).attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(favAdapter);


        Map<String,?> keys = pref.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            String k = entry.getKey();
            if (k.contains("fav")){
                String[] a = k.split("_");
                String symbol = a[1];
                if (!pref.getBoolean("fav_"+symbol, false)){
                    continue;
                }

                String name = pref.getString("name_"+symbol, "Apple Inc");
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url = "https://stock-search-backend-346009.wl.r.appspot.com/comp_latest/" + symbol.toUpperCase();
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            try {
                                JSONObject jsonObjectALL = getJson(response);
                                double current_price = jsonObjectALL.getDouble("c");
                                Stocks s = new Stocks(symbol, 0,current_price,0,name);
                                favs.add(s);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            favAdapter.setRecyclerItemClickListener(position -> {
                                Intent intent = new Intent(MainActivity.this, StockActivity.class);
                                intent.putExtra("symbol", favs.get(position).getSymbol());
                                startActivity(intent);
                            });
                            favAdapter.notifyDataSetChanged();
                        }, error -> Log.d("aoligei",error.toString()));

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(stringRequest);
            }
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putFloat("balance", balance);
        editor.putFloat("worth", worth);
        Log.d("aoligei",  "save balance: " + String.format(Locale.ENGLISH,"%.2f", balance));
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        MenuItem menuItem = menu.findItem( R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() <= 2){
                    auto_complete_view.setVisibility(View.GONE);
                    return false;
                }
                else{
                    auto_complete_view.setVisibility(View.VISIBLE);
                }
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url = "https://stock-search-backend-346009.wl.r.appspot.com/auto_complete/" + s.toUpperCase();

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        response -> {
                            Log.d("aoligei","Response is: " + response);
                            handleAutoComplete(response);
                        }, error -> Log.d("aoligei",error.toString()));

                stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(stringRequest);


                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
//        return true;
    }


    private void handleAutoComplete(String s){
        List<String> name = new ArrayList<>();
        try {

            JSONObject jsonObjectALL = new JSONObject(s);
            int count = jsonObjectALL.getInt("count");
            if (count != 0){
                JSONArray stocks = jsonObjectALL.getJSONArray("result");
                for (int i = 0; i < stocks.length(); i++) {
                    JSONObject stock = stocks.getJSONObject(i);
                    String symbol = stock.getString("symbol");
                    String description = stock.getString("description");
                    if(!symbol.contains(".")) {
                        name.add(symbol+" | " +description);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, name);
        auto_complete_view.setAdapter(arrayAdapter);
    }

    private JSONObject getJson(String s) throws JSONException {
        JSONObject jsonObjectALL = new JSONObject(s);
        return jsonObjectALL;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }
}

