package app.akiles.sdkdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;

import java.time.Duration;
import java.util.ArrayList;

import app.akiles.sdk.ActionBluetoothStatus;
import app.akiles.sdk.ActionCallback;
import app.akiles.sdk.ActionInternetStatus;
import app.akiles.sdk.Akiles;
import app.akiles.sdk.AkilesException;
import app.akiles.sdk.Callback;
import app.akiles.sdk.Gadget;
import app.akiles.sdk.GadgetAction;
import app.akiles.sdk.Hardware;
import app.akiles.sdk.Card;
import app.akiles.sdk.ScanCallback;
import app.akiles.sdk.SyncCallback;
import app.akiles.sdk.SyncStatus;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ak";

    Akiles akiles;
    Spinner sessionSpinner;
    Spinner gadgetSpinner;
    Spinner actionSpinner;
    Spinner hardwareSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        akiles = new Akiles(this);

        updateSessions();

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("Update gadgets");
                updateGadgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        gadgetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("Update actions");
                updateActions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        hardwareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        ((Button) findViewById(R.id.btnAddSession)).setOnClickListener(v -> {
            String token = ((EditText) findViewById(R.id.inpToken)).getText().toString();
            akiles.addSession(token, new Callback<String>() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Session added OK!", Toast.LENGTH_SHORT).show();
                        updateSessions();
                    });
                }

                @Override
                public void onError(AkilesException ex) {
                    showException(ex);
                }
            });
        });
        ((Button) findViewById(R.id.btnRemoveSession)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            try {
                akiles.removeSession(sessionID);
            } catch (AkilesException e) {
                showException(e);
            }
            Toast.makeText(this, "Session removed OK!", Toast.LENGTH_SHORT).show();
            updateSessions();
        });
        ((Button) findViewById(R.id.btnRemoveAllSessions)).setOnClickListener(v -> {
            try {
                akiles.removeAllSessions();
            } catch (AkilesException e) {
                showException(e);
            }
            Toast.makeText(this, "All sessions removed OK!", Toast.LENGTH_SHORT).show();
            updateSessions();
        });
        ((Button) findViewById(R.id.btnRefreshSession)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            akiles.refreshSession(sessionID, new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Session refreshed OK!", Toast.LENGTH_SHORT).show();
                        updateGadgets();
                    });
                }

                @Override
                public void onError(AkilesException ex) {
                    showException(ex);
                }
            });
        });
        ((Button) findViewById(R.id.btnRefreshAllSessions)).setOnClickListener(v -> {
            akiles.refreshAllSessions(new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "All sessions refreshed OK!", Toast.LENGTH_SHORT).show();
                        updateGadgets();
                    });
                }

                @Override
                public void onError(AkilesException ex) {
                    showException(ex);
                }
            });
        });

        ((Button) findViewById(R.id.btnOpen)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            String gadgetID = ((Gadget)gadgetSpinner.getSelectedItem()).id;
            String actionID = ((GadgetAction)actionSpinner.getSelectedItem()).id;

            View spinner = ((View) findViewById(R.id.spinner));
            if(spinner.getVisibility() == View.VISIBLE) {
                return;
            }
            spinner.setVisibility(View.VISIBLE);

            akiles.action(sessionID, gadgetID, actionID, new ActionCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "action: onSuccess");
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }

                @Override
                public void onError(AkilesException ex) {
                    Log.i(TAG, "action: onError " + ex);
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }

                @Override
                public void onInternetSuccess() {
                    Log.i(TAG, "action: onInternetSuccess");
                }

                @Override
                public void onInternetError(AkilesException ex) {
                    Log.i(TAG, "action: onInternetError " + ex);
                }

                @Override
                public void onInternetStatus(ActionInternetStatus status) {
                    Log.i(TAG, "action: onInternetStatus " + status);
                }

                @Override
                public void onBluetoothSuccess() {
                    Log.i(TAG, "action: onBluetoothSuccess");
                }

                @Override
                public void onBluetoothError(AkilesException ex) {
                    Log.i(TAG, "action: onBluetoothError " + ex);
                }

                @Override
                public void onBluetoothStatus(ActionBluetoothStatus status) {
                    Log.i(TAG, "action: onBluetoothStatus " + status);
                }

                @Override
                public void onBluetoothStatusProgress(float progress) {
                    Log.i(TAG, "action: onBluetoothStatusProgress " + progress);
                }
            });
        });

        ((Button) findViewById(R.id.btnSync)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            String hardwareID = ((Hardware)hardwareSpinner.getSelectedItem()).id;

            View spinner = ((View) findViewById(R.id.spinner2));
            if(spinner.getVisibility() == View.VISIBLE) {
                return;
            }
            spinner.setVisibility(View.VISIBLE);

            akiles.sync(sessionID, hardwareID, Duration.ofSeconds(60), new SyncCallback() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "sync: onSuccess");
                    runOnUiThread(() -> {
                        spinner.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Hardware Synced", Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(AkilesException ex) {
                    Log.i(TAG, "sync: onError " + ex);
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }

                @Override
                public void onStatus(SyncStatus status) {
                    Log.i(TAG, "sync: onStatus " + status);
                }

                @Override
                public void onStatusProgress(float progress) {
                    Log.i(TAG, "sync: onStatusProgress " + progress);
                }
            });
        });

        ((Button) findViewById(R.id.btnScan)).setOnClickListener(v -> {
            View spinner = ((View) findViewById(R.id.spinner2));
            if(spinner.getVisibility() == View.VISIBLE) {
                return;
            }
            spinner.setVisibility(View.VISIBLE);

            clearHardwares();
            akiles.scan(Duration.ofSeconds(10), new ScanCallback() {
                @Override
                public void onDiscover(Hardware hw) {
                    runOnUiThread(() -> updateHardwares(hw));
                }

                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        spinner.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Scan finished", Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(AkilesException ex) {
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                    showException(ex);
                }
            });
        });

        ((Button) findViewById(R.id.btnScanCard)).setOnClickListener(v -> {
            akiles.scanCard(new Callback<Card>() {
                @Override
                public void onSuccess(Card card) {
                    Log.i(TAG, "is akiles card: " + card.isAkilesCard());
                    Log.i(TAG, "UID: " + card.getUid());
                    if (card.isAkilesCard()) {
                        card.update(new Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                Log.i("ak", "Card updated OK");
                                card.close();
                            }

                            @Override
                            public void onError(AkilesException ex) {
                                Log.i("ak", "Card update error");
                                ex.printStackTrace();
                                card.close();
                            }
                        });
                    } else {
                        card.close();
                    }
                }

                @Override
                public void onError(AkilesException ex) {
                    showException(ex);
                }
            });
        });

        ((Button) findViewById(R.id.btnCancelScanCard)).setOnClickListener(v -> {
            akiles.cancelScanCard();
        });
    }

    private void showException(AkilesException e) {
        e.printStackTrace();
        this.runOnUiThread(() -> {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            updateGadgets();
        });
    }

    private void updateSessions() {
        try {
            String[] sessionIDs = akiles.getSessionIDs();
            sessionSpinner = (Spinner) findViewById(R.id.inpSessionSpinner);
            gadgetSpinner = (Spinner) findViewById(R.id.inpGadgetSpinner);
            actionSpinner = (Spinner) findViewById(R.id.inpActionSpinner);
            hardwareSpinner = (Spinner) findViewById(R.id.inpHardwareSpinner);
            ArrayAdapter<String> adapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    sessionIDs
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sessionSpinner.setAdapter(adapter);

            updateGadgets();
        } catch (AkilesException e) {
            showException(e);
        }
    }

    private void updateGadgets() {
        try {
            String sessionID = (String) sessionSpinner.getSelectedItem();
            Gadget previousSelected = (Gadget) gadgetSpinner.getSelectedItem();
            Gadget[] gadgets = {};
            if (sessionID != null) {
                gadgets = akiles.getGadgets(sessionID);
            }
            ArrayAdapter<Gadget> adapter = new ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    gadgets
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            gadgetSpinner.setAdapter(adapter);

            if(previousSelected != null) {
                for(int i = 0; i < gadgets.length; i++) {
                    if(gadgets[i].id.equals(previousSelected.id)) {
                        gadgetSpinner.setSelection(i);
                    }
                }
            }
            updateActions();
        } catch (AkilesException ex)  {
            showException(ex);
        }
    }

    private void updateActions() {
        Gadget gadget = (Gadget)gadgetSpinner.getSelectedItem();
        GadgetAction previousSelected = (GadgetAction)actionSpinner.getSelectedItem();
        GadgetAction[] actions = {};
        if (gadget != null) {
            actions = gadget.actions;
        }
        ArrayAdapter<GadgetAction> adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                actions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(adapter);
        if(previousSelected != null) {
            for(int i = 0; i < actions.length; i++) {
                if(actions[i].id.equals(previousSelected.id)) {
                    actionSpinner.setSelection(i);
                }
            }
        }
    }

    private void updateHardwares(Hardware hw) {
        System.out.println("Discovered " + hw.id.toString());
        ArrayAdapter<Hardware> adapter = (ArrayAdapter<Hardware>) hardwareSpinner.getAdapter();
        adapter.add(hw);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hardwareSpinner.setAdapter(adapter);
    }

    private void clearHardwares() {
        ArrayList<Hardware> hardwares = new ArrayList<>();

        ArrayAdapter<Hardware> adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                hardwares
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hardwareSpinner.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        akiles.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}