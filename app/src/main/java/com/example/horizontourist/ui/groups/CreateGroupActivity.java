package com.example.horizontourist.ui.groups;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Group;
import com.example.horizontourist.data.model.GroupCreateRequest;
import com.example.horizontourist.databinding.ActivityCreateGroupBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupActivity extends BaseActivity {

    private ActivityCreateGroupBinding binding;
    private Group createdGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.btnCreateGroup.setOnClickListener(v -> submitCreateGroup());

        binding.btnCopyCode.setOnClickListener(v -> {
            if (createdGroup == null || createdGroup.joinCode == null) return;
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Join Code", createdGroup.joinCode);
                clipboard.setPrimaryClip(clip);
                toast(R.string.join_code_copied);
            }
        });

        binding.btnShareCode.setOnClickListener(v -> {
            if (createdGroup == null || createdGroup.joinCode == null) return;
            String shareText = getString(R.string.share_group_text,
                    createdGroup.groupName != null ? createdGroup.groupName : "Horizon Travel",
                    createdGroup.joinCode);
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_group_title)));
        });

        binding.btnViewGroup.setOnClickListener(v -> {
            if (createdGroup == null || createdGroup.id == null) return;
            Intent intent = GroupDetailActivity.createIntent(this, createdGroup.id);
            startActivity(intent);
            finish();
        });
    }

    private void submitCreateGroup() {
        String name = (binding.etGroupName.getText() != null)
                ? binding.etGroupName.getText().toString().trim()
                : "";

        if (name.isEmpty()) {
            toast(R.string.error_empty_group_name);
            return;
        }

        long myId = SessionManager.getInstance().getTouristId();
        if (myId <= 0) {
            toast(R.string.error_unexpected);
            return;
        }

        showLoading(true);
        GroupCreateRequest request = new GroupCreateRequest(name, myId);

        ApiClient.getApiService().createGroup(request).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    createdGroup = response.body();
                    displaySuccess(createdGroup);
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void displaySuccess(Group group) {
        binding.layoutForm.setVisibility(View.GONE);
        binding.layoutSuccess.setVisibility(View.VISIBLE);
        binding.tvCreatedJoinCode.setText(group.joinCode != null ? group.joinCode : "");
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnCreateGroup.setEnabled(!show);
        binding.etGroupName.setEnabled(!show);
    }
}
