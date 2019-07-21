package com.overcomersprayer.app.overcomersprayers.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.overcomersprayer.app.overcomersprayers.Listerners;
import com.overcomersprayer.app.overcomersprayers.R;
import com.overcomersprayer.app.overcomersprayers.activities.LoginActivity;
import com.overcomersprayer.app.overcomersprayers.activities.MainActivity;
import com.overcomersprayer.app.overcomersprayers.adapters.MainPageAdapter;
import com.overcomersprayer.app.overcomersprayers.models.Prayer;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PrayerListFragment extends Fragment implements Listerners.SearchListener {

    private static final String LOG_TAG = PrayerListFragment.class.getSimpleName();
    private static final String MOTION_X_ARG = null;
    private static final String MOTION_Y_ARG = null;
    @BindView(R.id.prayerHeadingList)
    RecyclerView prayerHeadingList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout refreshLayout;
    Bundle bundle;
    MainPageAdapter mainPageAdapter;
    Listerners.PrayerListener prayerListener;
    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    public static Listerners.SearchListener sSearchListener;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public static PrayerListFragment NewInstance() {
        return new PrayerListFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prayerHeadingList.setLayoutManager(new LinearLayoutManager(getContext()));
        if (mainPageAdapter == null) {
            mainPageAdapter = new MainPageAdapter(prayerListener, true);
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Seems like your session expired, Please login again", Toast.LENGTH_SHORT).show();
            getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
            return;
        }
        prayerHeadingList.setAdapter(mainPageAdapter);
        refreshLayout.setOnRefreshListener(this::getPrayers);
        refreshLayout.setRefreshing(true);
        getPrayers();
//      prayerHeadingList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


    }

    private void getPrayers() {
        String table = "userprayer";

        rootRef.child(table).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Prayer> prayers = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot prayerSnapshot : dataSnapshot.getChildren()) {
                        Prayer prayer = prayerSnapshot.getValue(Prayer.class);
                        prayer.setId(prayerSnapshot.getKey());
                        prayers.add(prayer);
                    }
                }
                refreshLayout.setRefreshing(false);
                mainPageAdapter.swapData(prayers);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                refreshLayout.setRefreshing(false);
            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_content, container, false);
        ButterKnife.bind(this, view);
        bundle = getArguments();
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        prayerListener = (Listerners.PrayerListener) context;
        sSearchListener = this;
    }

    @Override
    public void onPrayerSearched(String query) {
        //Log.e("TAAAAG1", query);
        mainPageAdapter.getFilter().filter(query);
    }

    @Override
    public void onStart() {
        super.onStart();
        //getActivityCast().showFavButton();
    }

    public MainActivity getActivityCast() {
        return (MainActivity) getActivity();
    }
}