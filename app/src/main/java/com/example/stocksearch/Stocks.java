package com.example.stocksearch;

public class Stocks {
    private String symbol;
    private int shares;
    private double current_price;
    private double total_cost;
    private String name;

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getShares() {
        return shares;
    }

    public double getTotal_cost() {
        return total_cost;
    }

    public double getCurrent_price() {
        return current_price;
    }

    public Stocks(String symbol, int shares, double current_price, double total_cost, String name) {
        this.symbol = symbol;
        this.shares = shares;
        this.current_price = current_price;
        this.total_cost = total_cost;
        this.name = name;
    }
}
