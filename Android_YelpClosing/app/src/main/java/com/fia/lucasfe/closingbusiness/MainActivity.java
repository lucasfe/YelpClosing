package com.fia.lucasfe.closingbusiness;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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


    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.button);
        btn.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {

        AmazonMachineLearningClient client = new AmazonMachineLearningClient(credentialsProvider);
        //AmazonMachineLearningClient client = new AmazonMachineLearningClient(credentialsProvider);

        // Use a created model that has a created real-time endpoint
        String mlModelId = "example-model-id";

        // Call GetMLModel to get the realtime endpoint URL
        GetMLModelRequest getMLModelRequest = new GetMLModelRequest();
        getMLModelRequest.setMLModelId(mlModelId);
        GetMLModelResult mlModelResult = client.getMLModel(getMLModelRequest);

        // Validate that the ML model is completed
        if (!mlModelResult.getStatus().equals(EntityStatus.COMPLETED.toString())) {
            return;
        }

        // Validate that the realtime endpoint is ready
        if (!mlModelResult.getEndpointInfo().getEndpointStatus().equals(RealtimeEndpointStatus.READY.toString())){
            System.out.println("Realtime endpoint is not ready: " + mlModelResult.getEndpointInfo().getEndpointStatus());
            return;
        }


        PredictRequest predictRequest = new PredictRequest();
        predictRequest.setMLModelId(mlModelId);

        HashMap<String, String> record = new HashMap<String, String>();
        record.put("example attribute", "example value");

        predictRequest.setRecord(record);
        predictRequest.setPredictEndpoint(mlModelResult.getEndpointInfo().getEndpointUrl());

// Call Predict and print out your prediction
        PredictResult predictResult = client.predict(predictRequest);


    }
}
