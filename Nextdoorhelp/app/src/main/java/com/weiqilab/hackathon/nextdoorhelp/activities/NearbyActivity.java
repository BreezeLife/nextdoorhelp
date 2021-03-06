package com.weiqilab.hackathon.nextdoorhelp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.BraintreePaymentActivity;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.Symbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.weiqilab.hackathon.nextdoorhelp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Weiqi Zhao on 9/11/16.
 * Copyright (c) 2016 Weiqi Zhao. All rights reserved.
 */
public class NearbyActivity extends AppCompatActivity {

    private static final String SERVER_BASE = "http://2ab0658a.ngrok.io"; // Replace with your own server
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;


    private static final String TAG = NearbyActivity.class.getSimpleName();

    // Define types of search.
    private enum SearchType {
        RESTROOM,
        DRESS,
        FURNITURE,
        CATERING,
        BATHROOM
    }
    SearchType mCurrentSearchType;
    final static double ZOOM_BY = 20;
    LinearUnit mMilesUnit = new LinearUnit(LinearUnit.Code.MILE_STATUTE);

    ProgressBar mProgress;
    MapView mMapView = null;
    SpatialReference mMapSr = null;
    GraphicsLayer mResultsLayer = null;
    //PictureMarkerSymbol mCoffeeMapIcon, mBarMapIcon, mPizzaMapIcon;
    PictureMarkerSymbol mDressIcon, mFurnitureIcon, mRestroomIcon, mCateringIcon, mBathroomIcon;

    // Views to show selected search result information.
    TextView mTitleTextView;
    TextView mAddressTextView;
    TextView mPhoneTextView;
    ImageView mPhoneImageView;
    TextView mDistanceTextView;
    RatingBar mRatingBar;
    Button mButtonAccept;

    Locator mLocator;
    ArrayList<String> mFindOutFields = new ArrayList<>();

    LocationDisplayManager mLDM;


    /**
     * When the map is tapped, select the graphic at that location.
     */
    final OnSingleTapListener mapTapCallback = new OnSingleTapListener() {
        @Override
        public void onSingleTap(float x, float y) {
            // Find out if we tapped on a Graphic
            int[] graphicIDs = mResultsLayer.getGraphicIDs(x, y, 25);
            if (graphicIDs != null && graphicIDs.length > 0) {
                // If there is more than one graphic, only select the first found.
                if (graphicIDs.length > 1){
                    int id = graphicIDs[0];
                    graphicIDs = new int[] { id };
                }

                // Only deselect the last graphic if user has tapped a new one. App
                // remains showing the last selected nearby service information,
                // as that is the main focus of the app.
                mResultsLayer.clearSelection();

                // Select the graphic
                mResultsLayer.setSelectedGraphics(graphicIDs, true);

                // Use the graphic attributes to update the information views.
                Graphic gr = mResultsLayer.getGraphic(graphicIDs[0]);
                updateContent(gr.getAttributes());
            }
        }
    };

    /**
     * When map is ready, set up the LocationDisplayManager.
     */
    final OnStatusChangedListener statusChangedListener = new OnStatusChangedListener() {

        private static final long serialVersionUID = 1L;

        @Override
        public void onStatusChanged(Object source, STATUS status) {
            if (source == mMapView && status == STATUS.INITIALIZED) {
                mMapSr = mMapView.getSpatialReference();
                if (mLDM == null) {
                    setupLocationListener();
                }
            }
        }
    };

    /**
     * When user touches phone number, send this to the dialler using an intent.
     */
    final View.OnTouchListener callTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            String num = mPhoneTextView.getText().toString();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
            startActivity(intent);
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {

        } else {
            //Toast.makeText(this, R.string.error_permission_map, Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        getToken();

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setOnStatusChangedListener(statusChangedListener);
        mMapView.setOnSingleTapListener(mapTapCallback);

        mTitleTextView = (TextView) findViewById(R.id.titleTextView);
        mTitleTextView.setText(R.string.startup_caption);
        mAddressTextView = (TextView) findViewById(R.id.addressTextView);
        mPhoneTextView = (TextView) findViewById(R.id.phoneTextView);
        mPhoneTextView.setOnTouchListener(callTouchListener);
        mPhoneImageView = (ImageView) findViewById(R.id.callImageView);
        mPhoneImageView.setOnTouchListener(callTouchListener);
        mDistanceTextView = (TextView) findViewById(R.id.distanceTextView);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mProgress = (ProgressBar) findViewById(R.id.findProgress);
        mButtonAccept = (Button) findViewById(R.id.btn_accept);
        mButtonAccept.setVisibility(View.GONE);

        mButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
//                builder.setMessage("Restroom for $20.00. Accept?");
//                builder.setPositiveButton("Accept",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(getBaseContext(), "Lets make it happen.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(NearbyActivity.this, PayActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
//                            }
//                        });
//
//                builder.setNeutralButton("Cancel",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.dismiss();
//                            }
//                        });
//                builder.show();


            }
        });

        mResultsLayer = new GraphicsLayer();
        mResultsLayer.setSelectionColorWidth(6);
        mMapView.addLayer(mResultsLayer);

//        mCoffeeMapIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_local_cafe_black));
//        mPizzaMapIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_local_pizza_black));
//        mBarMapIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_local_drink_black));

        //mDressIcon, mFurnitureIcon, mRestroomIcon, mCateringIcon, mBathroomIcon;
        mDressIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.dress_black_small));
        mFurnitureIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.furniture_black_small));
        mRestroomIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.toilet_black_small));
        mCateringIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.catering_black_small));
        mBathroomIcon = new PictureMarkerSymbol(getApplicationContext(), ContextCompat.getDrawable(getApplicationContext(), R.drawable.bathroom_black_small));

        Button ineedhelpBtn = (Button)findViewById(R.id.btn_ineedhelp);
        ineedhelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                intent = new Intent(NearbyActivity.this, NeedHelperActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
            }
        });
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#303F9F")));
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = null;
//                intent = new Intent(NearbyActivity.this, NeedHelperActivity.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.enter_righttoleft, R.anim.exit_righttoleft);
//                finish();
//               git
            //}
       // });

        setupLocator();
        setupLocationListener();
    }

    private void setupLocator() {
        // Parameterless constructor - uses the Esri world geocoding service.
        mLocator = Locator.createOnlineLocator();

        // Set up the outFields parameter for the search.
        mFindOutFields.add(getResources().getString(R.string.result_title));
        mFindOutFields.add(getResources().getString(R.string.result_type));
        mFindOutFields.add(getResources().getString(R.string.result_address));
        mFindOutFields.add(getResources().getString(R.string.result_phone));
        mFindOutFields.add(getResources().getString(R.string.result_distance));
    }

    private void setupLocationListener() {
        if ((mMapView != null) && (mMapView.isLoaded())) {
            mLDM = mMapView.getLocationDisplayManager();
            mLDM.setLocationListener(new LocationListener() {

                boolean locationChanged = false;

                // Zooms to the current location when first GPS fix arrives.
                @Override
                public void onLocationChanged(Location loc) {
                    if (!locationChanged) {
                        locationChanged = true;
                        zoomToLocation(loc);

                        // After zooming, turn on the Location pan mode to show the location
                        // symbol. This will disable as soon as you interact with the map.
                        mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    }
                }

                @Override
                public void onProviderDisabled(String arg0) {
                }

                @Override
                public void onProviderEnabled(String arg0) {
                }

                @Override
                public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                }
            });

            mLDM.start();
        }
    }

    /**
     * Zoom to location using a specific size of extent.
     *
     * @param loc  the location to center the MapView at
     */
    private void zoomToLocation(Location loc) {
        Point mapPoint = getAsPoint(loc);
        Unit mapUnit = mMapSr.getUnit();
        double zoomFactor = Unit.convertUnits(ZOOM_BY,
                Unit.create(LinearUnit.Code.MILE_US), mapUnit);
        Envelope zoomExtent = new Envelope(mapPoint, zoomFactor, zoomFactor);
        mMapView.setExtent(zoomExtent);
    }

    /**
     * Performs a find using the Locator, for a specific type of business.
     *
     * @param searchFor A string containing the type of business to search for.
     */
    private void doFindNearbyAsync(String searchFor) {


        final CallbackListener<List<LocatorGeocodeResult>> findCallback = new
                CallbackListener<List<LocatorGeocodeResult>>() {

                    @Override
                    public void onError(Throwable e) {
                        setProgressOnUIThread(false);

                        // Log the error
                        Log.e(TAG, "No Results Found");
                        Log.e(TAG, e.getMessage());
                        // Indicate to user we cannot show results in this area.
                        showToastOnUiThread("Error searching for results");
                    }

                    @Override
                    public void onCallback(List<LocatorGeocodeResult> results) {

                        // remove any previous graphics
                        mResultsLayer.removeAll();

                        // Use a Multipoint as a simple way to set total extent.
                        MultiPoint fullExtent = new MultiPoint();

                        if (results.size() > 0) {
                            // Set specific symbols and selection color for each type of search.
                            Symbol symbol = null;
                            if (mCurrentSearchType == SearchType.RESTROOM) {
                                mResultsLayer.setSelectionColor(getResources().getColor(
                                        R.color.beer_selection));
                                symbol = mRestroomIcon;
                            } else if (mCurrentSearchType == SearchType.DRESS) {
                                mResultsLayer.setSelectionColor(getResources().getColor(
                                        R.color.pizza_selection));
                                symbol = mDressIcon;
                            } else if (mCurrentSearchType == SearchType.FURNITURE) {
                                mResultsLayer.setSelectionColor(getResources().getColor(
                                        R.color.coffee_selection));
                                symbol = mFurnitureIcon;
                            } else if (mCurrentSearchType == SearchType.BATHROOM) {
                                mResultsLayer.setSelectionColor(getResources().getColor(
                                        R.color.beer_selection));
                                symbol = mBathroomIcon;
                            } else if (mCurrentSearchType == SearchType.CATERING) {
                                mResultsLayer.setSelectionColor(getResources().getColor(
                                        R.color.coffee_selection));
                                symbol = mCateringIcon;
                            }

                            // For each result, create a Graphic, using result attributes as
                            // graphic attributes.
                            for (LocatorGeocodeResult result : results) {
                                Point resultPoint = result.getLocation();
                                HashMap<String, Object> attrMap = new
                                        HashMap<String, Object>(result.getAttributes());
                                mResultsLayer.addGraphic(new Graphic(resultPoint, symbol, attrMap));
                                fullExtent.add(resultPoint);
                            }
                            // Zoom to the full extent
                            mMapView.setExtent(fullExtent, 100);
                        }
                        // Update the UI with the result information.
                        setResultCount(results.size(), mCurrentSearchType);
                    }
                };

        try {
            setProgressOnUIThread(true);

            // Get the current map extent.
            Envelope currExt = new Envelope();
            mMapView.getExtent().queryEnvelope(currExt);

            // Set up locator parameters based on the extent, search type, and the
            // outfields set previously.
            LocatorFindParameters fParams = new LocatorFindParameters(searchFor);
            fParams.setSearchExtent(currExt, mMapSr);
            fParams.setOutSR(mMapSr);
            fParams.setOutFields(mFindOutFields);

            // If LocationDisplayManger has a current location, set this to increase
            // priority and return a distance value in the results.
            if ((mLDM != null) && (mLDM.getLocation() != null)) {
                Point currentPoint = getAsPoint(mLDM.getLocation());
                fParams.setLocation(currentPoint, mMapSr);
            }

            // Call find, passing in the callback above.
            mLocator.find(fParams, findCallback);
        } catch (Exception e) {
            // Update UI and report any errors.
            setProgressOnUIThread(false);

            // Log the error
            Log.e(TAG, "No Results Found");
            Log.e(TAG, e.getMessage());

            // Indicate to user we cannot show results in this area.
            showToastOnUiThread("Error searching for results");
        }
    }

    private static String getRating() {
        // Randomized ratings could be replaced by a ratings service from a third
        // party.
        Random r = new Random();
        return String.valueOf(1 + (r.nextFloat() * 4));
    }

    /**
     * Update user interface with result set information. Ensure this can be
     * called from either background or UI thread by performing any actions on
     * Views within a runnable on the UI thread.
     *
     * @param resultCount  number of results in the result set
     * @param searchType  type of business searched for
     */
    private void setResultCount(int resultCount, SearchType searchType) {
        String searchTypeMessage = "";

        switch (searchType) {
            case RESTROOM:
                searchTypeMessage = getResources().getString(R.string.results_restroom);
                break;
            case DRESS:
                searchTypeMessage = getResources().getString(R.string.results_suit);
                break;
            case FURNITURE:
                searchTypeMessage = getResources().getString(R.string.results_furniture);
                break;
            case CATERING:
                searchTypeMessage = getResources().getString(R.string.results_catering);
                break;
            case BATHROOM:
                searchTypeMessage = getResources().getString(R.string.results_bathroom);
                break;
        }

        final String message = String.format("Found %d %s", resultCount,
                searchTypeMessage);

        runOnUiThread(new Runnable() {
            public void run() {
                mProgress.setIndeterminate(false);
                mTitleTextView.setText(message);
                mAddressTextView.setText("");
                mPhoneTextView.setText("");
                mPhoneImageView.setImageDrawable(null);
                mDistanceTextView.setText("");
                mRatingBar.setVisibility(View.GONE);
                mButtonAccept.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_nearby, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.dress:
                if (mMapView.isLoaded()) {
                    clearCurrentResults();
                    mCurrentSearchType = SearchType.DRESS;
                    try {
                        doFindNearbyAsync(getResources().getString(R.string.bar_query));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                return true;

            case R.id.furniture:
                if (mMapView.isLoaded()) {
                    mCurrentSearchType = SearchType.FURNITURE;
                    try {
                        doFindNearbyAsync(getResources().getString(R.string.pizza_query));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return true;

            case R.id.bathroom:
                if (mMapView.isLoaded()) {
                    mCurrentSearchType = SearchType.BATHROOM;
                    try {
                        doFindNearbyAsync(getResources().getString(R.string.coffee_query));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                return true;

            case R.id.restroom:
                if (mMapView.isLoaded()) {
                    mCurrentSearchType = SearchType.RESTROOM;
                    try {
                        doFindNearbyAsync(getResources().getString(R.string.coffee_query));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                return true;

            case R.id.catering:
                if (mMapView.isLoaded()) {
                    mCurrentSearchType = SearchType.CATERING;
                    try {
                        doFindNearbyAsync(getResources().getString(R.string.bar_query));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                return true;

            case R.id.locate:
                if (mMapView.isLoaded()) {
                    // If LocationDisplayManager has a fix, pan to that location. If no
                    // fix yet, this will happen when the first fix arrives, due to
                    // callback set up previously.
                    if ((mLDM != null) && (mLDM.getLocation() != null)) {
                        // Keep current scale and go to current location, if there is one.
                        mLDM.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void clearCurrentResults() {
        if (mResultsLayer != null) {
            mResultsLayer.removeAll();
        }
        mTitleTextView.setText("");
        mAddressTextView.setText("");
        mPhoneTextView.setText("");
        mPhoneImageView.setImageDrawable(null);
        mDistanceTextView.setText("");
        mRatingBar.setRating(0);
        mRatingBar.setVisibility(View.GONE);
        mButtonAccept.setVisibility(View.GONE);
    }


    private Point getAsPoint(Location loc) {
        Point wgsPoint = new Point(loc.getLongitude(), loc.getLatitude());
        return (Point) GeometryEngine.project(wgsPoint, SpatialReference.create(4326),
                mMapSr);
    }


    public void updateContent(Map<String, Object> attributes) {
        // This is called from UI thread (MapTap listener)

        String title = attributes.get(getResources().getString(
                R.string.result_title)).toString();
        //mTitleTextView.setText(title);

        String address = attributes.get(getResources().getString(
                R.string.result_address)).toString();
        mAddressTextView.setText(address);

        String distance = attributes.get(getResources().getString(
                R.string.result_distance)).toString();
        double meters = Double.parseDouble(distance);
        if (meters > 0) {
            if (mDistanceTextView.getVisibility() != View.VISIBLE) {
                mDistanceTextView.setVisibility(View.VISIBLE);
            }
            double miles = mMilesUnit.convertFromMeters(meters);
            mDistanceTextView.setText(String.format("%.2f %s", miles,
                    getResources().getString(R.string.miles)));
        } else {
            mDistanceTextView.setVisibility(View.GONE);
        }

        String phone = attributes.get(getResources().getString(
                R.string.result_phone)).toString();
        mPhoneTextView.setText(phone + " $20.00");
        mPhoneImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_action_call));

        Float rating = Float.parseFloat(getRating());
        mRatingBar.setRating(rating);
        mRatingBar.setVisibility(View.VISIBLE);
        mButtonAccept.setVisibility(View.VISIBLE);
    }

    public void showToastOnUiThread(final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(NearbyActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setProgressOnUIThread(final boolean isIndeterminate) {
        runOnUiThread(new Runnable() {
            public void run() {
                mProgress.setIndeterminate(isIndeterminate);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
        if (mLDM != null) {
            mLDM.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.unpause();
        if (mLDM != null) {
            mLDM.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLDM != null) {
            mLDM.stop();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            PaymentMethodNonce paymentMethodNonce = data.getParcelableExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

            RequestParams requestParams = new RequestParams();
            requestParams.put("payment_method_nonce", paymentMethodNonce.getNonce());
            requestParams.put("total", "3.00");
            requestParams.put("service", "0.03");
            requestParams.put("merchant_id", "yin_chen");

            client.post(SERVER_BASE + "/process", requestParams, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Toast.makeText(NearbyActivity.this, responseString, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    Toast.makeText(NearbyActivity.this, responseString, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void getToken() {
        client.get(SERVER_BASE + "/token", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //findViewById(R.id.btn_start).setEnabled(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                clientToken = responseString;
                //findViewById(R.id.btn_start).setEnabled(true);
            }
        });
    }

}