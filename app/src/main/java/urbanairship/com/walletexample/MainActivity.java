package urbanairship.com.walletexample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.wallet.WalletConstants;
import com.urbanairship.wallet.Logger;
import com.urbanairship.wallet.pass.Pass;
import com.urbanairship.wallet.pass.PassActivity;
import com.urbanairship.wallet.pass.PassRequest;


public class MainActivity extends AppCompatActivity {

    private static final int SAVE_TO_WALLET = 100;

    private Pass pass;
    private PassRequest passRequest;

    private View fetchProgressView;
    private Button passButton;

    // Configure UA Wallet
    static {
        Logger.setLogLevel(Log.VERBOSE);
        PassActivity.setWalletEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION);
        PassActivity.setWalletTheme(WalletConstants.THEME_LIGHT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        fetchProgressView = findViewById(R.id.progress);
        passButton = (Button) findViewById(R.id.save_pass);
        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pass != null) {
                    pass.requestToSavePass(MainActivity.this, SAVE_TO_WALLET);
                }
            }
        });

        if (savedInstanceState != null) {
            pass = savedInstanceState.getParcelable("pass");
        }

        if (pass == null) {
            passButton.setEnabled(false);
        } else {
            fetchProgressView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (pass != null) {
            outState.putParcelable("pass", pass);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (pass == null) {
            passRequest = new PassRequest.Builder()
                    .setApiKey(BuildConfig.URBANAIRSHIP_API_KEY)
                    .setTemplateId(BuildConfig.URBANAIRSHIP_TEMPLATE_ID)
                    .setIssuerName("Urban Airship Example")
                    .setName("Pass Title")
                    .build();

            passRequest.execute(new PassRequest.Callback() {
                @Override
                public void onResult(Pass pass) {
                    MainActivity.this.pass = pass;

                    passButton.setEnabled(true);
                    fetchProgressView.setVisibility(View.GONE);
                }

                @Override
                public void onError(@PassRequest.Error int errorCode) {
                    Toast.makeText(getApplicationContext(), "Unable to fetch pass", Toast.LENGTH_SHORT).show();
                    fetchProgressView.setVisibility(View.GONE);

                    if (errorCode == PassRequest.ERROR_NETWORK) {
                        // Retry
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (passRequest != null) {
            passRequest.cancel();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SAVE_TO_WALLET) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    Toast.makeText(this, "Pass added successfully!", Toast.LENGTH_SHORT).show();
                    break;

                case Activity.RESULT_CANCELED:
                    Toast.makeText(this, "Pass canceled!", Toast.LENGTH_SHORT).show();
                    break;

                case PassActivity.RESULT_GOOGLE_PLAY_SERVICES_ERROR:
                    Toast.makeText(this, "Google Play Service Error :(", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    int errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
                    Toast.makeText(this, "Pass failed with error code: " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
