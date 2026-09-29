package com.example.horizontourist.util;

import android.util.Patterns;

public final class Validators {

    private Validators() {
    }

    public static boolean isEmailValid(CharSequence email) {
        return email != null && email.length() > 0 && Patterns.EMAIL_ADDRESS.matcher(email.toString().trim()).matches();
    }

    public static boolean isPasswordValid(CharSequence password) {
        return password != null && password.toString().trim().length() >= 6;
    }

    public static boolean isNonEmpty(CharSequence text) {
        return text != null && text.toString().trim().length() > 0;
    }
}
