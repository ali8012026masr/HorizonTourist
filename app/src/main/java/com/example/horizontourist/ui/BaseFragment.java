package com.example.horizontourist.ui;

import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    public boolean isSafe() {
        return isAdded() && getContext() != null && !isDetached()
                && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed();
    }

    public void toast(String message) {
        if (isSafe() && message != null && !message.trim().isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void toast(@StringRes int resId) {
        if (isSafe()) {
            Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show();
        }
    }

    public void showLoading(boolean show) {
        // Subclasses override when implementing progress views
    }

    public void showError(String message) {
        if (isSafe() && message != null && !message.trim().isEmpty()) {
            toast(message);
        }
    }

    public void showEmpty(boolean show) {
        // Subclasses override when implementing empty views
    }
}
