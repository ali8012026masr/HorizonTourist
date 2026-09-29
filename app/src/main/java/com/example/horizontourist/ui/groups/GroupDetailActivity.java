package com.example.horizontourist.ui.groups;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.horizontourist.R;
import com.example.horizontourist.data.api.ApiClient;
import com.example.horizontourist.data.api.ApiError;
import com.example.horizontourist.data.model.Booking;
import com.example.horizontourist.data.model.Constants;
import com.example.horizontourist.data.model.Group;
import com.example.horizontourist.data.model.GroupBookingRequest;
import com.example.horizontourist.data.model.GroupMember;
import com.example.horizontourist.databinding.ActivityGroupDetailBinding;
import com.example.horizontourist.ui.BaseActivity;
import com.example.horizontourist.util.Formatters;
import com.example.horizontourist.util.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupDetailActivity extends BaseActivity {

    public static final String EXTRA_GROUP_ID = "extra_group_id";

    public static Intent createIntent(Context context, long groupId) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        return intent;
    }

    private ActivityGroupDetailBinding binding;
    private long groupId;
    private Group currentGroup;
    private GroupMemberAdapter memberAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        groupId = getIntent().getLongExtra(EXTRA_GROUP_ID, -1L);
        if (groupId <= 0) {
            toast(R.string.error_unexpected);
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        memberAdapter = new GroupMemberAdapter();
        binding.rvMembers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMembers.setAdapter(memberAdapter);
    }

    private void setupListeners() {
        binding.btnRetry.setOnClickListener(v -> loadGroupData());

        binding.btnCopyJoinCode.setOnClickListener(v -> copyJoinCode());

        binding.btnShareCode.setOnClickListener(v -> shareJoinCode());

        binding.btnLinkBooking.setOnClickListener(v -> showLinkBookingDialog());

        binding.btnMarkPaid.setOnClickListener(v -> markMySharePaid());

        binding.btnLeaveGroup.setOnClickListener(v -> showLeaveGroupDialog());

        binding.btnGroupChat.setOnClickListener(v -> {
            if (currentGroup == null) return;
            String name = (currentGroup.groupName != null) ? currentGroup.groupName : "";
            Intent intent = GroupChatActivity.createIntent(this, groupId, name);
            startActivity(intent);
        });
    }

    private void copyJoinCode() {
        if (currentGroup == null || currentGroup.joinCode == null) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("Join Code", currentGroup.joinCode);
            clipboard.setPrimaryClip(clip);
            toast(R.string.join_code_copied);
        }
    }

    private void shareJoinCode() {
        if (currentGroup == null || currentGroup.joinCode == null) return;
        String shareText = getString(R.string.share_group_text,
                currentGroup.groupName != null ? currentGroup.groupName : "Horizon Travel",
                currentGroup.joinCode);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_group_title)));
    }

    private void loadGroupData() {
        showLoading(true);
        binding.layoutError.setVisibility(View.GONE);

        ApiClient.getApiService().getGroupById(groupId).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentGroup = response.body();
                    displayGroupInfo(currentGroup);
                    loadMembers();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    showError(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Group> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                showError(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void displayGroupInfo(Group group) {
        binding.tvGroupName.setText(group.groupName != null ? group.groupName : "");
        binding.tvJoinCode.setText(group.joinCode != null ? group.joinCode : "");

        if (group.createdAt != null && !group.createdAt.trim().isEmpty()) {
            binding.tvCreatedAt.setText(Formatters.formatDate(group.createdAt));
            binding.tvCreatedAt.setVisibility(View.VISIBLE);
        } else {
            binding.tvCreatedAt.setVisibility(View.GONE);
        }

        memberAdapter.setCreatorId(group.createdByTouristId);

        long myId = SessionManager.getInstance().getTouristId();
        boolean isCreator = (group.createdByTouristId != null && group.createdByTouristId == myId);

        // Linked Booking display
        if (group.bookingId != null && group.bookingId > 0) {
            binding.tvLinkedBookingStatus.setText(String.format(getString(R.string.booking_linked_badge), group.bookingId));
            binding.btnLinkBooking.setVisibility(View.GONE);
        } else {
            binding.tvLinkedBookingStatus.setText(R.string.no_booking_linked);
            binding.btnLinkBooking.setVisibility(isCreator ? View.VISIBLE : View.GONE);
        }
    }

    private void loadMembers() {
        ApiClient.getApiService().getGroupMembers(groupId).enqueue(new Callback<List<GroupMember>>() {
            @Override
            public void onResponse(@NonNull Call<List<GroupMember>> call, @NonNull Response<List<GroupMember>> response) {
                if (isFinishing() || isDestroyed()) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<GroupMember> members = response.body();
                    memberAdapter.setItems(members);
                    checkMyPaymentStatus(members);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GroupMember>> call, @NonNull Throwable t) {
                // Ignore, optional fallback
            }
        });
    }

    private void checkMyPaymentStatus(List<GroupMember> members) {
        long myId = SessionManager.getInstance().getTouristId();
        boolean canMarkPaid = false;

        for (GroupMember member : members) {
            if (member != null && member.touristId != null && member.touristId == myId) {
                if (!member.isPaid() && member.amountOwed > 0) {
                    canMarkPaid = true;
                }
                break;
            }
        }
        binding.btnMarkPaid.setVisibility(canMarkPaid ? View.VISIBLE : View.GONE);
    }

    private void markMySharePaid() {
        long myId = SessionManager.getInstance().getTouristId();
        if (myId <= 0) return;

        showLoading(true);
        ApiClient.getApiService().markGroupMemberPaid(groupId, myId).enqueue(new Callback<GroupMember>() {
            @Override
            public void onResponse(@NonNull Call<GroupMember> call, @NonNull Response<GroupMember> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    toast(R.string.mark_paid_success);
                    loadMembers();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GroupMember> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void showLeaveGroupDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_leave_group_title)
                .setMessage(R.string.confirm_leave_group_msg)
                .setPositiveButton(R.string.btn_leave_group, (dialog, which) -> executeLeaveGroup())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void executeLeaveGroup() {
        long myId = SessionManager.getInstance().getTouristId();
        if (myId <= 0) return;

        showLoading(true);
        ApiClient.getApiService().leaveOrRemoveGroupMember(groupId, myId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful()) {
                    toast(R.string.leave_group_success);
                    finish();
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void showLinkBookingDialog() {
        long myId = SessionManager.getInstance().getTouristId();
        if (myId <= 0) return;

        showLoading(true);
        ApiClient.getApiService().getTouristBookings(myId).enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> eligible = new ArrayList<>();
                    for (Booking b : response.body()) {
                        if (Constants.BOOKING_STATUS_PENDING.equalsIgnoreCase(b.status)
                                || Constants.BOOKING_STATUS_CONFIRMED.equalsIgnoreCase(b.status)) {
                            eligible.add(b);
                        }
                    }

                    if (eligible.isEmpty()) {
                        toast(R.string.no_eligible_bookings);
                    } else {
                        displayBookingChooser(eligible);
                    }
                } else {
                    String msg = ApiError.extractMessage(response.errorBody());
                    toast(msg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                toast(ApiError.networkErrorMessage(t));
            }
        });
    }

    private void displayBookingChooser(List<Booking> bookings) {
        String[] titles = new String[bookings.size()];
        for (int i = 0; i < bookings.size(); i++) {
            Booking b = bookings.get(i);
            titles[i] = String.format(Locale.US, "Booking #%d (%s) - %s",
                    b.id != null ? b.id : 0L,
                    b.status != null ? b.status : "",
                    Formatters.formatMoney(b.totalAmount));
        }

        final int[] selected = new int[]{0};

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.link_booking_title)
                .setSingleChoiceItems(titles, 0, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.btn_link_booking, (dialog, which) -> {
                    Booking chosen = bookings.get(selected[0]);
                    linkBooking(chosen.id);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void linkBooking(Long bookingId) {
        if (bookingId == null) return;
        showLoading(true);

        ApiClient.getApiService().linkGroupBooking(groupId, new GroupBookingRequest(bookingId)).enqueue(new Callback<Group>() {
            @Override
            public void onResponse(@NonNull Call<Group> call, @NonNull Response<Group> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    toast(R.string.link_booking_success);
                    currentGroup = response.body();
                    displayGroupInfo(currentGroup);
                    loadMembers();
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

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message != null ? message : getString(R.string.error_unexpected));
        binding.scrollView.setVisibility(View.GONE);
        binding.layoutBottomBar.setVisibility(View.GONE);
    }
}
