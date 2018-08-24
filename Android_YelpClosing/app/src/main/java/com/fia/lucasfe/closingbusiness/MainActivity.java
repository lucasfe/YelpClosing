package com.fia.lucasfe.closingbusiness;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.services.machinelearning.AmazonMachineLearningClient;
import com.amazonaws.services.machinelearning.model.EntityStatus;
import com.amazonaws.services.machinelearning.model.GetMLModelRequest;
import com.amazonaws.services.machinelearning.model.GetMLModelResult;
import com.amazonaws.services.machinelearning.model.PredictRequest;
import com.amazonaws.services.machinelearning.model.PredictResult;
import com.amazonaws.services.machinelearning.model.RealtimeEndpointStatus;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AWSCredentialsProvider credentialsProvider;
    private AWSConfiguration configuration;


    private EditText stars;
    private EditText reviewCount;
    private EditText mean;
    private EditText median;
    private EditText numberOfReviews;
    private EditText totalCheckIns;
    private EditText latitute;
    private EditText longitude;

    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);
        btn.setOnClickListener(this);

        stars = findViewById(R.id.tv_stars);
        reviewCount = findViewById(R.id.tv_review_count);
        mean = findViewById(R.id.tv_mean);
        median = findViewById(R.id.tv_median);
        numberOfReviews = findViewById(R.id.tv_number_of_reviews);
        totalCheckIns = findViewById(R.id.tv_total_checkins);
        latitute = findViewById(R.id.tv_latitude);
        longitude = findViewById(R.id.tv_longitude);


        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {

                credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();
                configuration = AWSMobileClient.getInstance().getConfiguration();

                // Use IdentityManager#getUserID to fetch the identity id.
                IdentityManager.getDefaultIdentityManager().getUserID(new IdentityHandler() {
                    @Override
                    public void onIdentityId(String identityId) {
                        Log.d("YourMainActivity", "Identity ID = " + identityId);

                        // Use IdentityManager#getCachedUserID to
                        //  fetch the locally cached identity id.
                        final String cachedIdentityId =
                                IdentityManager.getDefaultIdentityManager().getCachedUserID();
                    }

                    @Override
                    public void handleError(Exception exception) {
                        Log.d("YourMainActivity", "Error in retrieving the identity" + exception);
                    }
                });


            }
        }).execute();

    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onClick(View view) {


        final HashMap<String, String>  record = new HashMap<String, String>();
        record.put("stars", stars.getText().toString());
        record.put("review_count", reviewCount.getText().toString());
        record.put("Mean", mean.getText().toString());
        record.put("Median", median.getText().toString());
        record.put("NumberOfReviews", numberOfReviews.getText().toString());
        record.put("TotalCheckins", totalCheckIns.getText().toString());
        record.put("latitude", latitute.getText().toString());
        record.put("longitude", longitude.getText().toString());



        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                AmazonMachineLearningClient client = new AmazonMachineLearningClient(credentialsProvider);
                //AmazonMachineLearningClient client = new AmazonMachineLearningClient(credentialsProvider);

                // Use a created model that has a created real-time endpoint
                String mlModelId = "ml-GCqiz8j1wHa";

                // Call GetMLModel to get the realtime endpoint URL
                GetMLModelRequest getMLModelRequest = new GetMLModelRequest();
                getMLModelRequest.setMLModelId(mlModelId);
                GetMLModelResult mlModelResult = client.getMLModel(getMLModelRequest);

                // Validate that the ML model is completed
                if (!mlModelResult.getStatus().equals(EntityStatus.COMPLETED.toString())) {
                }

                // Validate that the realtime endpoint is ready
                if (!mlModelResult.getEndpointInfo().getEndpointStatus().equals(RealtimeEndpointStatus.READY.toString())){
                    System.out.println("Realtime endpoint is not ready: " + mlModelResult.getEndpointInfo().getEndpointStatus());
                }


                PredictRequest predictRequest = new PredictRequest();
                predictRequest.setMLModelId(mlModelId);


                predictRequest.setRecord(record);
                predictRequest.setPredictEndpoint(mlModelResult.getEndpointInfo().getEndpointUrl());

// Call Predict and print out your prediction
                PredictResult predictResult = client.predict(predictRequest);
                return predictResult;

            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                PredictResult predictResult = (PredictResult) o;
                String result =  predictResult.getPrediction().getPredictedLabel().toString();

                result = result.equals("1") ? "Não vai fechar" : "Vai fechar";

                Toast.makeText(getBaseContext(),result, Toast.LENGTH_SHORT).show();
                
            }
        }.execute();






    }
}
