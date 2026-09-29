package com.example.horizontourist.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.horizontourist.data.model.CartItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CartManager {

    public interface OnCartChangeListener {
        void onCartChanged(int count);
    }

    private static final String PREF_NAME = "horizon_tourist_cart";
    private static final String KEY_CART_ITEMS = "cart_items_json";

    private static CartManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;
    private final List<CartItem> inMemoryItems;
    private final List<OnCartChangeListener> listeners;

    private CartManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.listeners = new CopyOnWriteArrayList<>();
        this.inMemoryItems = new ArrayList<>();
        loadFromPrefs();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CartManager must be initialized in HorizonApp.onCreate()");
        }
        return instance;
    }

    private synchronized void loadFromPrefs() {
        inMemoryItems.clear();
        String json = prefs.getString(KEY_CART_ITEMS, null);
        if (json != null && !json.trim().isEmpty()) {
            Type listType = new TypeToken<ArrayList<CartItem>>() {}.getType();
            List<CartItem> loaded = gson.fromJson(json, listType);
            if (loaded != null) {
                inMemoryItems.addAll(loaded);
            }
        }
    }

    private synchronized void saveToPrefs() {
        String json = gson.toJson(inMemoryItems);
        prefs.edit().putString(KEY_CART_ITEMS, json).apply();
        notifyListeners();
    }

    public synchronized void add(CartItem item) {
        if (item == null || item.slotId == null) {
            return;
        }
        // Do not add duplicates (quantity fixed at 1 per slot)
        if (!contains(item.slotId)) {
            inMemoryItems.add(item);
            saveToPrefs();
        }
    }

    public synchronized void remove(Long slotId) {
        if (slotId == null) {
            return;
        }
        boolean removed = inMemoryItems.removeIf(item -> slotId.equals(item.slotId));
        if (removed) {
            saveToPrefs();
        }
    }

    public synchronized void updateItem(CartItem updated) {
        if (updated == null || updated.slotId == null) {
            return;
        }
        for (int i = 0; i < inMemoryItems.size(); i++) {
            if (updated.slotId.equals(inMemoryItems.get(i).slotId)) {
                inMemoryItems.set(i, updated);
                saveToPrefs();
                return;
            }
        }
    }

    public synchronized void saveItems() {
        saveToPrefs();
    }

    public synchronized void clear() {
        if (!inMemoryItems.isEmpty()) {
            inMemoryItems.clear();
            saveToPrefs();
        }
    }

    public synchronized List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(inMemoryItems));
    }

    public synchronized double getTotal() {
        double total = 0.0;
        for (CartItem item : inMemoryItems) {
            total += item.price;
        }
        return total;
    }

    public synchronized boolean contains(Long slotId) {
        if (slotId == null) {
            return false;
        }
        for (CartItem item : inMemoryItems) {
            if (slotId.equals(item.slotId)) {
                return true;
            }
        }
        return false;
    }

    public synchronized int count() {
        return inMemoryItems.size();
    }

    public void addListener(OnCartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(OnCartChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners() {
        int c = count();
        for (OnCartChangeListener listener : listeners) {
            listener.onCartChanged(c);
        }
    }
}
