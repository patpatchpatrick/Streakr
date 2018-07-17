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
    static final String ITEM_SKU_ADREMOVAL = "streakr.ad_removal";

    private Button mBuyButton;
    private TextView mAdRemovalPurchasedTextView;
    private String mAdRemovalPrice;
    private SharedPreferences mSharedPreferences;

    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });

        List skuList = new ArrayList<>();
        skuList.add(ITEM_SKU_ADREMOVAL);
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List skuDetailsList) {
                        // Process the result.

                        if (responseCode == BillingClient.BillingResponse.OK
                                && skuDetailsList != null) {
                            for (Object skuDetailsObject : skuDetailsList) {
                                SkuDetails skuDetails = (SkuDetails) skuDetailsObject;
                                String sku = skuDetails.getSku();
                                String price = skuDetails.getPrice();
                                if (ITEM_SKU_ADREMOVAL.equals(sku)) {
                                    mAdRemovalPrice = price;
                                }
                            }
                        }
                    }
                });


        mBuyButton = (Button) findViewById(R.id.buyButton);
        mAdRemovalPurchasedTextView = (TextView) findViewById(R.id.ad_removal_purchased);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                        .setSku(ITEM_SKU_ADREMOVAL)
                        .setType(BillingClient.SkuType.INAPP)
                        .build();
                int responseCode = mBillingClient.launchBillingFlow(InAppBillingActivity.this, flowParams);
            }
        });

        queryPurchases();
        queryPrefPurchases();

    }

    private void queryPrefPurchases() {
        Boolean adFree = mSharedPreferences.getBoolean(getResources().getString(R.string.pref_remove_ads_key), false);
        if (adFree) {
            Log.d(TAG, "Ad Free Yo");
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
                        Log.d(TAG, "AD REMOVAL PURCHASED!!");
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
            Log.d(TAG, "AD REMOVAL PURCHASED!!");
            mSharedPreferences.edit().putBoolean(getResources().getString(R.string.pref_remove_ads_key), true).commit();
            setAdFree(true);
            mBuyButton.setText(getResources().getString(R.string.pref_ad_removal_purchased));
            mBuyButton.setEnabled(false);
        }
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<com.android.billingclient.api.Purchase> purchases) {

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
