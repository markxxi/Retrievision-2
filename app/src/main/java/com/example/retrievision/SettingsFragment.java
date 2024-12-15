package com.example.retrievision;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsFragment extends Fragment {

    View view;
    WebView webView, webView2, webView3;
    CardView PolicyCard, TermsCard, FAQsCard, AboutCard, LogOut;

    public SettingsFragment() {
        // public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        privacyPolicyWeb();
        termsWeb();
        showWeb();
        faqWeb();
        LogOut();

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.getVisibility() == View.VISIBLE) {
                    setViewVisibilityGone();
                } else {
                    ProfileFragment profileFragment = new ProfileFragment();
                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, profileFragment).commit();

                }
            }
        });

        return view;
    }

    private void LogOut() {
        LogOut = view.findViewById(R.id.logout);
        LogOut.setOnClickListener(v -> {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Intent intent = new Intent(getActivity(), SignIn.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void privacyPolicyWeb() {
        webView = view.findViewById(R.id.privacypolicy);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/PrivacyPolicy.html");
        webView.setVisibility(View.GONE);
    }

    private void termsWeb() {
        webView2 = view.findViewById(R.id.termsandconditions);
        webView2.setWebViewClient(new WebViewClient());
        webView2.getSettings().setJavaScriptEnabled(true);
        webView2.loadUrl("file:///android_asset/TermsAndConditions.html");
        webView2.setVisibility(View.GONE);
    }

    private void faqWeb() {
        webView3 = view.findViewById(R.id.faqs);
        webView3.setWebViewClient(new WebViewClient());
        webView3.getSettings().setJavaScriptEnabled(true);
        webView3.loadUrl("file:///android_asset/FAQs.html");
        webView3.setVisibility(View.GONE);
    }

    private void showWeb() {
        PolicyCard = view.findViewById(R.id.privacypolicy_btn);
        TermsCard = view.findViewById(R.id.terms_btn);
        FAQsCard = view.findViewById(R.id.FAQs_btn);
        AboutCard = view.findViewById(R.id.about_btn);

        PolicyCard.setOnClickListener(v -> {
            webView.setVisibility(View.VISIBLE);
            webView2.setVisibility(View.GONE);
            webView3.setVisibility(View.GONE);
        });

        TermsCard.setOnClickListener(v -> {
            webView2.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            webView3.setVisibility(View.GONE);
        });

        FAQsCard.setOnClickListener(v -> {
            webView3.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            webView2.setVisibility(View.GONE);
        });

        AboutCard.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), AboutPage.class);
            startActivity(intent);
        });
    }

    private void setViewVisibilityGone() {
        if (webView != null) {
            webView.setVisibility(View.GONE);
        }
        if (webView2 != null) {
            webView2.setVisibility(View.GONE);
        }
        if (webView3 != null) {
            webView3.setVisibility(View.GONE);
        }
    }


}
