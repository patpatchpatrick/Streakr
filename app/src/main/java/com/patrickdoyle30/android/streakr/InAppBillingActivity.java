package com.patrickdoyle30.android.streakr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import static com.patrickdoyle30.android.streakr.helper.PreferenceHelper.setAdFree;

public class InAppBillingActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private static final String TAG = "InAppBilling";

    // In-app products. Currently only selling "ad removal"
    static final String ITEM_SKU_ADREMOVAL = "streakr.ad_removal";

    private Button mBuyButton;
    private String mAdRemovalPrice;
    private SharedPreferences mSharedPreferences;

    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Activity used to handle in-app billing purchases
        //Currently the only in-app product is ad removal
        //Once the ad removal product is purchased, it is tracked using shared preferences (as seen below)
        //The PreferenceHelper class is also used to help determine if ad removal has been purchased.


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Establish connection to billing client
        mBillingClient = BillingClient.newBuilder(InAppBillingActivity.this).setListener(this).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.

                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //TODO implement your own retry policy
                Toast.makeText(InAppBillingActivity.this,  getResources().getString(R.string.billing_connection_failure), Toast.LENGTH_SHORT);
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });


        mBuyButton = (Button) findViewById(R.id.buyButton);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If user clicks the buy button, launch the billing flow for an ad removal  purchase
                // Response is handled using onPurchasesUpdated listener
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSku(ITEM_SKU_ADREMOVAL)
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                int responseCode = mBillingClient.launchBillingFlow(InAppBillingActivity.this, flowParams);
            }
        });

        // Query purchases incase a user is connecting from a different device and they've already purchased the app
        queryPurchases();
        queryPrefPurchases();

    }

    private void queryPrefPurchases() {
        Boolean adFree = mSharedPreferences.getBoolean(getResources().getString(R.string.pref_remove_ads_key), false);
        if (adFree) {
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
    }

    private void queryPurchases() {

        //Method not being used for now, but can be used if purchases ever need to be queried in the future
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        if (purchasesResult != null) {
            List<Purchase> purchasesList = purchasesResult.getPurchasesList();
            if (purchasesList == null) {
                return;
            }
            if (!purchasesList.isEmpty()) {
                for (Purchase purchase : purchasesList) {
                    if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {
                        mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
                        setAdFree(true);
                        mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
                        mBuyButton.setEnabled(false);
                    }
                }
            }
        }

    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(ITEM_SKU_ADREMOVAL)) {
            mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
            setAdFree(true);
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<com.android.billingclient.api.Purchase> purchases) {

        //Handle the responseCode for the purchase
        //If response code is OK,  handle the purchase
        //If user already owns the item, then indicate in the shared prefs that item is owned
        //If cancelled/other code, log the error

        if (responseCode == BillingClient.BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            Log.d(TAG, "User Canceled" + responseCode);
        } else if (responseCode == BillingClient.BillingResponse.ITEM_ALREADY_OWNED) {
            mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
            setAdFree(true);
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        } else {
            Log.d(TAG, "Other code" + responseCode);
            // Handle any other error codes.
        }

    }
}
