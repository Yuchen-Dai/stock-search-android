package com.example.stocksearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;


import org.json.JSONArray;
import org.json.JSONObject;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StockActivity extends AppCompatActivity {
    String symbol;
    boolean is_fav;
    float balance;
    float worth;
    float total_cost;
    int shares;
    double c_price;
    String c_name;
    ProgressBar progressBar_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        symbol = intent.getStringExtra("symbol");
        loadData();
        setTheme(R.style.Theme_Stocksearch);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        progressBar_view = findViewById(R.id.progressBar1);
        TextView hourly_var = findViewById(R.id.hourly_var);
        hourly_var.setText(symbol + " Hourly Price Variation");
        ImageView imageView1 = findViewById(R.id.image_var);
        ImageView imageView2 = findViewById(R.id.insight1);
        ImageView imageView3 = findViewById(R.id.insight2);
        ImageView imageView4 = findViewById(R.id.insight3);
        if (symbol.equals("TSLA")){
            imageView1.setImageDrawable(getResources().getDrawable(R.drawable.tsla_var, getTheme()));
            imageView2.setImageDrawable(getResources().getDrawable(R.drawable.insight1_tsla, getTheme()));
            imageView3.setImageDrawable(getResources().getDrawable(R.drawable.insight2_tsla, getTheme()));
            imageView4.setImageDrawable(getResources().getDrawable(R.drawable.insight3_tsla, getTheme()));
        }
        else if (symbol.equals("AAPL")){
            imageView1.setImageDrawable(getResources().getDrawable(R.drawable.aapl_var, getTheme()));
            imageView2.setImageDrawable(getResources().getDrawable(R.drawable.insight1_aapl, getTheme()));
            imageView3.setImageDrawable(getResources().getDrawable(R.drawable.insight2_aapl, getTheme()));
            imageView4.setImageDrawable(getResources().getDrawable(R.drawable.insight3_aapl, getTheme()));

        }
        else{
            imageView1.setImageDrawable(getResources().getDrawable(R.drawable.dell_var, getTheme()));
            imageView2.setImageDrawable(getResources().getDrawable(R.drawable.insight1_hp, getTheme()));
            imageView3.setImageDrawable(getResources().getDrawable(R.drawable.insight2_hp, getTheme()));
            imageView4.setImageDrawable(getResources().getDrawable(R.drawable.insight3_hp, getTheme()));
        }



        begin_load();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle(symbol);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



        load_from_finnhub();

    }

    private void load_from_finnhub() {
        RequestQueue queue = Volley.newRequestQueue(StockActivity.this);
        String url = "https://stock-search-backend-346009.wl.r.appspot.com/comp_description/" + symbol.toUpperCase();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("aoligei","Response is: " + response);
                    handleDescription(response);
                    finish_load();
                }, error -> Log.d("aoligei",error.toString()));

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

        url = "https://stock-search-backend-346009.wl.r.appspot.com/comp_latest/" + symbol.toUpperCase();
        stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("aoligei","Response is: " + response);
                    handleLatest(response);
                }, error -> Log.d("aoligei",error.toString()));

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

        url = "https://stock-search-backend-346009.wl.r.appspot.com/comp_peers/" + symbol.toUpperCase();
        stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("aoligei","Response is: " + response);
                    handlePeers(response);
                }, error -> Log.d("aoligei",error.toString()));

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate a_week_ago = today.minusDays(1);
        url = "https://stock-search-backend-346009.wl.r.appspot.com/comp_news/" + symbol.toUpperCase()
                + "/" + dtf.format(a_week_ago) +"/" + dtf.format(today);
        stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("aoligei","Response is: " + response);
                    handleNews(response);
                }, error -> Log.d("aoligei",error.toString()));

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(stringRequest);

//        WebView webView1 = findViewById(R.id.webview1);
//        webView1.loadUrl("https://stock-search-backend-346009.wl.r.appspot.com/search/home");
//        WebSettings webSettings = webView1.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setDomStorageEnabled(true);

    }

    private void handleNews(String s) {
        List<News> data = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(s);
            JSONObject jsonNews = jsonArray.getJSONObject(0);
            String source = jsonNews.getString("source");
            double datetime = jsonNews.getDouble("datetime");
            String headline = jsonNews.getString("headline");
            String des = jsonNews.getString("summary");
            String img = jsonNews.getString("image");
            String website = jsonNews.getString("url");
            TextView main_source = findViewById(R.id.news_main_source);
            TextView main_datetime = findViewById(R.id.news_main_datetime);
            TextView main_title = findViewById(R.id.news_main_title);
            ImageView main_img = findViewById(R.id.news_main_img);

            main_source.setText(source);
            main_title.setText(headline);
            java.util.Date time = new java.util.Date((long)datetime*1000);
            Date today = new Date();
            long diff_seconds = (today.getTime() - time.getTime())/1000;
            int diff_hours = (int) (diff_seconds / 3600);
            String temp =  diff_hours + " hours ago";
            main_datetime.setText(temp);
            Glide.with(this)
                    .load(img)
                    .placeholder(R.drawable.ic_app_playstore)
                    .fitCenter()
                    .into(main_img);

            for (int i = 1; i < jsonArray.length() && i < 20; ++ i){
                jsonNews = jsonArray.getJSONObject(i);
                source = jsonNews.getString("source");
                datetime = jsonNews.getDouble("datetime");
                headline = jsonNews.getString("headline");
                des = jsonNews.getString("summary");
                img = jsonNews.getString("image");
                website = jsonNews.getString("url");

                News news_object = new News(source, datetime,headline,des,img, website);
                data.add(news_object);
            }
            RecyclerView recyclerView = findViewById(R.id.news);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(StockActivity.this);
            recyclerView.setLayoutManager(linearLayoutManager);

            NewsAdapter newsAdapter = new NewsAdapter(data, StockActivity.this);
            recyclerView.setAdapter(newsAdapter);

            newsAdapter.setRecyclerItemClickListener(position -> {
                News open_new = data.get(position);
                final Dialog dialog = new Dialog(StockActivity.this);
                dialog.setContentView(R.layout.dialog_news);
                dialog.setTitle("News");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView source1 = dialog.findViewById(R.id.card_source);
                TextView date1 = dialog.findViewById(R.id.card_date);
                TextView title1 = dialog.findViewById(R.id.card_title);
                TextView des1 = dialog.findViewById(R.id.card_des);
                source1.setText(open_new.getSource());
                title1.setText(open_new.getTitle());
                des1.setText(open_new.getDes());
                double unix_date = open_new.getDate();
                DateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
                java.util.Date time1 = new java.util.Date((long)unix_date*1000);
                date1.setText(df.format(time1));
                ImageView chrome = dialog.findViewById(R.id.chrome);
                ImageView twitter = dialog.findViewById(R.id.twitter);
                ImageView facebook = dialog.findViewById(R.id.facebook);

                chrome.setOnClickListener(v ->{
                    Uri uri = Uri.parse(open_new.getWebsite());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
                
                twitter.setOnClickListener(v ->{
                    String twitter_url = "https://twitter.com/intent/tweet?text="+open_new.getTitle()+"&amp;url=" + open_new.getWebsite();
                    Uri uri = Uri.parse(twitter_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
                facebook.setOnClickListener(v ->{
                    String facebook_url = "https://www.facebook.com/sharer/sharer.php?u=" + open_new.getWebsite();
                    Uri uri = Uri.parse(facebook_url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
                dialog.show();
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handlePeers(String s) {
        try {

            JSONArray peers = new JSONArray(s);
            LinearLayout peers_view = findViewById(R.id.company_peers);
            for(int i = 0; i < peers.length(); ++i){
                String peer = peers.getString(i);
                if(peer.contains(".")){
                    continue;
                }
                TextView peer_view = new TextView(StockActivity.this);
                String temp = peer + ",  ";
                SpannableString content = new SpannableString(temp);
                content.setSpan(new UnderlineSpan(), 0, content.length()-2, 0);
                peer_view.setText(content);
                peer_view.setTextColor(getResources().getColor(R.color.blue, getTheme()));
                peer_view.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                peer_view.setOnClickListener(view -> {
                    Intent intent = new Intent(StockActivity.this, StockActivity.class);
                    intent.putExtra("symbol", peer);
                    startActivity(intent);
                });
                peers_view.addView(peer_view);

            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleLatest(String s) {

        try {

            JSONObject jsonObjectALL = new JSONObject(s);

            double current_price = jsonObjectALL.getDouble("c");
            double change_price = jsonObjectALL.getDouble("d");
            double change_percent = jsonObjectALL.getDouble("dp");
            double high_price = jsonObjectALL.getDouble("h");
            double low_price = jsonObjectALL.getDouble("l");
            double open_price = jsonObjectALL.getDouble("o");
            double prev_price = jsonObjectALL.getDouble("pc");

            c_price = current_price;
            TextView price_view = findViewById(R.id.c_price);
            TextView change_price_view = findViewById(R.id.c_percentage);
            ImageView change_icon = findViewById(R.id.c_icon);
            String xxx = "$" + String.format(Locale.ENGLISH,"%.2f", current_price);
            price_view.setText(xxx);
            String percentage = "$" + String.format(Locale.ENGLISH,"%.2f", change_price) + " ("
                    + String.format(Locale.ENGLISH,"%.2f", change_percent) + "%)";
            change_price_view.setText(percentage);

            if (change_percent < 0){
                change_price_view.setTextColor(getResources().getColor(R.color.red, getTheme()));
                change_icon.setImageResource(R.drawable.trending_down);
                change_icon.setColorFilter(getResources().getColor(R.color.red,getTheme()));
            }
            else{
                change_price_view.setTextColor(getResources().getColor(R.color.green, getTheme()));
                change_icon.setImageResource(R.drawable.trending_up);
                change_icon.setColorFilter(getResources().getColor(R.color.green,getTheme()));
            }


            // portfolio
            TextView shares_own_view = findViewById(R.id.shares_owned);
            TextView avg_cost_view = findViewById(R.id.avg_cost);
            TextView total_cost_view = findViewById(R.id.total_cost);
            TextView change_view = findViewById(R.id.change);
            TextView market_value_view = findViewById(R.id.market_value);

            String tempo;
            shares_own_view.setText(String.valueOf(shares));
            if(shares != 0) {
                float avg_cost = total_cost / shares;
                tempo = "$" + String.format(Locale.ENGLISH, "%.2f", avg_cost);
            }
            else{
                tempo = "$0.00";
            }
            avg_cost_view.setText(tempo);
            tempo = "$" + String.format(Locale.ENGLISH,"%.2f", total_cost);
            total_cost_view.setText(tempo);
            tempo = "$" + String.format(Locale.ENGLISH,"%.2f",current_price * shares - total_cost);
            change_view.setText(tempo);
            tempo = "$" + String.format(Locale.ENGLISH,"%.2f",current_price * shares);
            market_value_view.setText(tempo);


            //Stats
            TextView open_price_view = findViewById(R.id.open_price);
            TextView high_price_view = findViewById(R.id.high_price);
            TextView low_price_view = findViewById(R.id.low_price);
            TextView prev_price_view = findViewById(R.id.prev_close);

            tempo = "Open Price $" + String.format(Locale.ENGLISH,"%.2f", open_price);
            open_price_view.setText(tempo);
            tempo = "Low Price: $" + String.format(Locale.ENGLISH,"%.2f", prev_price);
            prev_price_view.setText(tempo);
            tempo = "High Price: $" + String.format(Locale.ENGLISH,"%.2f", high_price);
            high_price_view.setText(tempo);
            tempo = "Prev Close: $" + String.format(Locale.ENGLISH,"%.2f", low_price);
            low_price_view.setText(tempo);

            Button trade_button = findViewById(R.id.trade_button);
            trade_button.setOnClickListener(view -> {
                final Dialog dialog = new Dialog(StockActivity.this);
                dialog.setContentView(R.layout.dialog_trade);
                dialog.setTitle("Trade");
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                TextView text = (TextView) dialog.findViewById(R.id.trade_title);
                EditText input = dialog.findViewById(R.id.trade_input);
                TextView hint1 = dialog.findViewById(R.id.trade_hint1);
                TextView hint2 = dialog.findViewById(R.id.trade_hint2);

                String temp = "Trade " + symbol + " INC shares";
                text.setText(temp);

                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String s = editable.toString();
                        int num_share = 0;
                        try {
                            num_share = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }finally {
                            String temp;
                            temp = num_share + "*$" + String.format(Locale.ENGLISH,"%.2f", current_price)
                                    +"/share = " + String.format(Locale.ENGLISH,"%.2f", current_price * num_share);
                            hint1.setText(temp);
                        }

                    }
                });
                temp =  "0*$" + String.format(Locale.ENGLISH,"%.2f", current_price) + "/share = 0.00";
                hint1.setText(temp);

                temp = "$" + String.format(Locale.ENGLISH,"%.2f", balance) + " to buy " + symbol;
                hint2.setText(temp);

                Button buyButton = (Button) dialog.findViewById(R.id.trade_buy);
                buyButton.setOnClickListener(v -> {
                    try {
                        String input_text = input.getText().toString();
                        int num_share = Integer.parseInt(input_text);
                        double value = num_share * current_price;
                        if(num_share <= 0){
                            make_toast("‘Cannot buy non-positive shares");
                        }
                        else if(value <= balance){
                            buyShare(symbol, num_share, current_price);
                            dialog.dismiss();
                            openSuccessDialog("bought", input_text);
                        }else {
                            make_toast("Not enough money to buy");
                        }

                    }catch (NumberFormatException e){
                        make_toast("Please enter a valid amount");
                    }

                });

                Button sellButton = dialog.findViewById(R.id.trade_sell);
                sellButton.setOnClickListener(v -> {
                    try {
                        String input_text = input.getText().toString();
                        int num_share = Integer.parseInt(input_text);
                        if(num_share <= 0){
                            make_toast("‘Cannot sell non-positive shares");
                        }
                        else if(num_share <= shares){
                            sellShare(symbol, num_share, current_price);
                            dialog.dismiss();
                            openSuccessDialog("bought", input_text);
                        }else {
                            make_toast("Not enough shares to sell");
                        }

                    }catch (NumberFormatException e){
                        make_toast("Please enter a valid amount");
                    }
                });

                dialog.show();
            } );


        } catch (Exception e){

            e.printStackTrace();
        }
    }

    private void sellShare(String symbol, int num_share, double current_price) {
        shares -= num_share;
        balance += num_share * current_price;
        total_cost -= num_share * current_price;
        refresh_portfolio();
        saveData();
    }

    private void buyShare(String symbol, int num_share, double current_price) {
        shares += num_share;
        balance -= num_share * current_price;
        total_cost += num_share * current_price;
        refresh_portfolio();
        saveData();
    }

    private void refresh_portfolio() {
        TextView shares_own_view = findViewById(R.id.shares_owned);
        TextView avg_cost_view = findViewById(R.id.avg_cost);
        TextView total_cost_view = findViewById(R.id.total_cost);

        String tempo;
        shares_own_view.setText(String.valueOf(shares));
        if(shares != 0) {
            float avg_cost = total_cost / shares;
            tempo = "$" + String.format(Locale.ENGLISH, "%.2f", avg_cost);
        }
        else{
            tempo = "$0.00";
        }
        avg_cost_view.setText(tempo);
        tempo = "$" + String.format(Locale.ENGLISH,"%.2f", total_cost);
        total_cost_view.setText(tempo);
    }

    private void handleDescription(String s) {

        try {

            JSONObject jsonObjectALL = new JSONObject(s);
            String company_logo = jsonObjectALL.getString("logo");
            String company_name = jsonObjectALL.getString("name");
            String IPO = jsonObjectALL.getString("ipo");
            String industry = jsonObjectALL.getString("finnhubIndustry");
            String webpage = jsonObjectALL.getString("weburl");
            c_name = company_name;

            TextView symbol_view = findViewById(R.id.stock_symbol);
            TextView name_view = findViewById(R.id.stock_des);
            TextView IPO_view = findViewById(R.id.ipo_start_date);
            TextView industry_view = findViewById(R.id.industry);
            TextView webpage_view = findViewById(R.id.webpage);
            ImageView logo_view = findViewById(R.id.stock_icon);
            name_view.setText(company_name);
            symbol_view.setText(symbol);
            IPO_view.setText(IPO);
            industry_view.setText(industry);
            webpage_view.setText(webpage);

            Glide.with(StockActivity.this)
                    .load(company_logo)
                    .placeholder(R.drawable.ic_app_playstore)
                    .fitCenter()
                    .into(logo_view);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void openSuccessDialog(String operation, String num){
        final Dialog dialog = new Dialog(StockActivity.this);
        dialog.setContentView(R.layout.dialog_success);
        dialog.setTitle("Success");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textView1 = dialog.findViewById(R.id.message1);
        TextView textView2 = dialog.findViewById(R.id.message2);
        String temp = "You have successfully "+ operation + " " + num;
        textView1.setText(temp);
        temp = "shares of " + symbol;
        textView2.setText(temp);

        Button doneButton = dialog.findViewById(R.id.done);
        doneButton.setOnClickListener(v ->{
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        MenuItem menuItem = menu.findItem( R.id.action_fav);
        if (is_fav) {
            menuItem.setIcon(R.drawable.ic_baseline_star_rate_24);
        }

        menuItem.setOnMenuItemClickListener(menuItem1 -> {
            if (is_fav) {
                menuItem1.setIcon(R.drawable.ic_baseline_star_border_24);
                make_toast(symbol+" is removed from favorites");
            }
            else{
                menuItem1.setIcon(R.drawable.ic_baseline_star_rate_24);
                make_toast(symbol+" is added to favorites");
            }
            is_fav = !is_fav;
            saveData();
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void begin_load() {
        progressBar_view.setVisibility(View.VISIBLE);
        LinearLayout main_content = findViewById(R.id.main_content);
        main_content.setVisibility(View.GONE);
    }

    private void make_toast(String s) {
        Toast.makeText(StockActivity.this, s, Toast.LENGTH_LONG).show();
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
        is_fav = pref.getBoolean("fav_" +symbol,false);
        total_cost = pref.getFloat("cost_"+symbol, 0);
        shares = pref.getInt("shares_"+symbol, 0);

//        Log.d("aoligei", "load is_fav: " + is_fav);
//        Log.d("aoligei",  "load balance: " + String.format(Locale.ENGLISH,"%.2f", balance));

    }

    private void saveData() {
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putFloat("balance", balance);
        editor.putFloat("worth", worth);
        editor.putBoolean("fav_"+symbol, is_fav);
        editor.putFloat("cost_"+symbol, total_cost);
        editor.putInt("shares_"+ symbol, shares);
        editor.putFloat("last_"+ symbol, (float) c_price);
        editor.putString("name_"+ symbol, c_name);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveData();
    }
}