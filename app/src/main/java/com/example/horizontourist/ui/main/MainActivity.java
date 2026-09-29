package com.example.horizontourist.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.horizontourist.R;
import com.example.horizontourist.databinding.ActivityMainBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.ui.bookings.BookingsFragment;
import com.example.horizontourist.ui.guides.GuidesFragment;
import com.example.horizontourist.ui.profile.ProfileFragment;
import com.example.horizontourist.ui.slots.SlotsFragment;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_SELECT_TAB = "extra_select_tab";
    private static final String KEY_SELECTED_TAB_ID = "selected_tab_id";
    private static final String TAG_HOME = "tab_home";
    private static final String TAG_EXPLORE = "tab_explore";
    private static final String TAG_GUIDES = "tab_guides";
    private static final String TAG_BOOKINGS = "tab_bookings";
    private static final String TAG_PROFILE = "tab_profile";

    public static android.content.Intent createTabIntent(android.content.Context context, int tabItemId) {
        android.content.Intent intent = new android.content.Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_SELECT_TAB, tabItemId);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    private ActivityMainBinding binding;
    private int selectedTabId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedTabId && getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null) {
                return true;
            }
            showTab(itemId);
            return true;
        });

        int initialTab = R.id.navigation_home;
        if (getIntent() != null && getIntent().hasExtra(EXTRA_SELECT_TAB)) {
            initialTab = getIntent().getIntExtra(EXTRA_SELECT_TAB, R.id.navigation_home);
        } else if (savedInstanceState != null) {
            initialTab = savedInstanceState.getInt(KEY_SELECTED_TAB_ID, R.id.navigation_home);
        }
        binding.bottomNav.setSelectedItemId(initialTab);
        showTab(initialTab);
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra(EXTRA_SELECT_TAB)) {
            int tab = intent.getIntExtra(EXTRA_SELECT_TAB, R.id.navigation_home);
            selectTab(tab);
            if (tab == R.id.navigation_explore) {
                Fragment f = getSupportFragmentManager().findFragmentByTag(TAG_EXPLORE);
                if (f instanceof SlotsFragment) {
                    ((SlotsFragment) f).handleIntentFilters(intent);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB_ID, selectedTabId);
    }

    public void selectTab(int itemId) {
        if (binding != null && binding.bottomNav != null) {
            binding.bottomNav.setSelectedItemId(itemId);
        }
    }

    private void showTab(int itemId) {
        selectedTabId = itemId;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        String targetTag = getTagForId(itemId);
        Fragment currentFragment = fm.findFragmentByTag(targetTag);

        // Hide all fragments
        hideAllFragments(fm, ft);

        if (currentFragment == null) {
            currentFragment = createFragmentForId(itemId);
            ft.add(R.id.fragmentContainer, currentFragment, targetTag);
        } else {
            ft.show(currentFragment);
        }

        ft.commit();
    }

    private void hideAllFragments(FragmentManager fm, FragmentTransaction ft) {
        String[] allTags = {TAG_HOME, TAG_EXPLORE, TAG_GUIDES, TAG_BOOKINGS, TAG_PROFILE};
        for (String tag : allTags) {
            Fragment f = fm.findFragmentByTag(tag);
            if (f != null && !f.isHidden()) {
                ft.hide(f);
            }
        }
    }

    private String getTagForId(int itemId) {
        if (itemId == R.id.navigation_explore) {
            return TAG_EXPLORE;
        } else if (itemId == R.id.navigation_guides) {
            return TAG_GUIDES;
        } else if (itemId == R.id.navigation_bookings) {
            return TAG_BOOKINGS;
        } else if (itemId == R.id.navigation_profile) {
            return TAG_PROFILE;
        } else {
            return TAG_HOME;
        }
    }

    private Fragment createFragmentForId(int itemId) {
        if (itemId == R.id.navigation_explore) {
            return new SlotsFragment();
        } else if (itemId == R.id.navigation_guides) {
            return new GuidesFragment();
        } else if (itemId == R.id.navigation_bookings) {
            return new BookingsFragment();
        } else if (itemId == R.id.navigation_profile) {
            return new ProfileFragment();
        } else {
            return new HomeFragment();
        }
    }
}
