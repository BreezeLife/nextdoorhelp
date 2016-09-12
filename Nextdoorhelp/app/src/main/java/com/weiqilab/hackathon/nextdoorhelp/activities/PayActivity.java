package com.weiqilab.hackathon.nextdoorhelp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.PaymentRequest;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.weiqilab.hackathon.nextdoorhelp.R;

import java.util.Random;

import cz.msebera.android.httpclient.Header;

/**
 * Created by yinchen on 9/11/16.
 */
public class PayActivity extends Activity{
    private static final String SERVER_BASE = "http://2ab0658a.ngrok.io"; // Replace with your own server
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;
    private RatingBar mRatingBar2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getToken();
        setContentView(R.layout.activity_pay);

        mRatingBar2 = (RatingBar) findViewById(R.id.ratingBar2);
        mRatingBar2.setRating(3);

    }

    private static String getRating() {
        // Randomized ratings could be replaced by a ratings service from a third
        // party.
        Random r = new Random();
        return String.valueOf(1 + (r.nextFloat() * 4));
    }

    public void onStartClick(View view) {
        PaymentRequest paymentRequest = new PaymentRequest()
                .clientToken(clientToken)
                .amount("$20.00")
                .primaryDescription("Payment to Yin")
                .secondaryDescription("Borrowed 5 chairs for party")
                .submitButtonText("Pay");
        Log.d("pay","pay start" );
        startActivityForResult(paymentRequest.getIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

            RequestParams requestParams = new RequestParams();
            requestParams.put("payment_method_nonce", paymentMethodNonce.getNonce());
            requestParams.put("total", "20.00");
            requestParams.put("service", "0.03");
            requestParams.put("merchant_id", "yin_chen");

            client.post(SERVER_BASE + "/process", requestParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(PayActivity.this, responseString, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Toast.makeText(PayActivity.this, responseString, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void getToken() {
        client.get(SERVER_BASE + "/token", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                findViewById(R.id.btn_start).setEnabled(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                clientToken = responseString;
                findViewById(R.id.btn_start).setEnabled(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
