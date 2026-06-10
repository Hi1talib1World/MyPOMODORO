package com.denzo.mypomodoro;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.denzo.mypomodoro.ui.CustomSettingsFragment;
import com.denzo.mypomodoro.ui.FocusFragment;
import com.denzo.mypomodoro.ui.StatsFragment;
import com.denzo.mypomodoro.ui.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestNotificationPermission();

        viewPager = findViewById(R.id.main_viewpager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_focus);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_tasks);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_stats);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                        break;
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_focus) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (itemId == R.id.nav_tasks) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (itemId == R.id.nav_stats) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (itemId == R.id.nav_settings) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });
        
        viewPager.setUserInputEnabled(true);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.denzo.mypomodoro.PomodoroWidgetProvider.updateAllWidgets(this);
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new FocusFragment();
                case 1: return new TasksFragment();
                case 2: return new StatsFragment();
                case 3: return new CustomSettingsFragment();
                default: return new FocusFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
