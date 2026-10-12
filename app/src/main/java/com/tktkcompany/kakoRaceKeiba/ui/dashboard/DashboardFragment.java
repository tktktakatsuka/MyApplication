package com.tktkcompany.kakoRaceKeiba.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tktkcompany.kakoRaceKeiba.R;
import com.tktkcompany.kakoRaceKeiba.databinding.FragmentDashboardBinding;
import com.tktkcompany.kakoRaceKeiba.db.SupabaseManager;
import com.tktkcompany.kakoRaceKeiba.dto.SharedViewModel;
import com.tktkcompany.kakoRaceKeiba.util.WeekendDays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private ProgressBar progressBar;
    private LinearLayout buttonContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        buttonContainer = root.findViewById(R.id.button_container);

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getJoNames().observe(getViewLifecycleOwner(), joNames -> {
            if (joNames == null || joNames.isEmpty()) return;
            List<String> datelist = WeekendDays.getPastWeekendsInCurrentMonth();
            executeQueriesForAllLocations(datelist, joNames);
        });

        loadBannerAd();
        return root;
    }

    private Bundle setKeyjoNameString(String date, String jo) {
        Bundle bundle = new Bundle();
        bundle.putString("key", date);
        bundle.putString("jo", jo);
        return bundle;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void executeQueriesForAllLocations(List<String> datelist, List<String> joNames) {
        buttonContainer.removeAllViews();
        for (String joName : joNames) {
            querySupabaseData(datelist, joName);
        }
    }

    private void querySupabaseData(List<String> dateList, String joName) {
        SupabaseManager.fetchRaceDates(joName, new SupabaseManager.SupabaseCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> raceDates) {
                if (binding == null || !isAdded()) return;

                TextView textView = new TextView(getActivity());
                textView.setText(joName);
                textView.setTextSize(18);
                textView.setPadding(0, 20, 0, 10);
                buttonContainer.addView(textView);

                List<String> sortedDates = new ArrayList<>(raceDates);
                if (binding.checkboxDate.isChecked()) {
                    Collections.sort(sortedDates, Collections.reverseOrder());
                } else {
                    Collections.sort(sortedDates);
                }

                for (String date : sortedDates) {
                    if (dateList.contains(date)) createBundle(joName, date);
                }

                View spacer = new View(getActivity());
                spacer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 170));
                buttonContainer.addView(spacer);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull String message) {
                Log.e("DashboardFragment", "Supabase load failed: " + message);
                if (isAdded()) progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void createBundle(String joName, String date) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Button newButton = new Button(getActivity());
                newButton.setText(date + WeekendDays.getDayOfWeek(date));
                newButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    Bundle bundle = setKeyjoNameString(date, joName);
                    navController.navigate(R.id.action_fragmentB_to_fragmentC, bundle);
                });
                buttonContainer.addView(newButton);
            });
        }
    }

    public void loadBannerAd() {
        AdView adView = binding.adView;
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }
}
