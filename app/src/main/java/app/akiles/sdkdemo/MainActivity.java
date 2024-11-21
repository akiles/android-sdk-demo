package app.akiles.sdkdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.Manifest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.akiles.sdk.ActionListener;
import app.akiles.sdk.Akiles;
import app.akiles.sdk.AkilesException;
import app.akiles.sdk.Gadget;
import app.akiles.sdk.GadgetAction;
import app.akiles.sdk.Permissioner;

public class MainActivity extends AppCompatActivity {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    Akiles akiles;
    Spinner sessionSpinner;
    Spinner gadgetSpinner;
    Spinner actionSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        akiles = new Akiles(this, "AndroidDemo", event -> runOnUiThread(() -> Toast.makeText(MainActivity.this, event.toString(), Toast.LENGTH_SHORT).show()));

        updateSessions();

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateGadgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        gadgetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateActions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        ((Button) findViewById(R.id.btnRequestBle)).setOnClickListener(v -> {
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
            },BLE_REQUEST_CODE);
        });

        ((Button) findViewById(R.id.btnAddSession)).setOnClickListener(v -> {
            String token = ((EditText) findViewById(R.id.inpToken)).getText().toString();
            executorService.execute(() -> {
                try {
                    akiles.addSession(token);
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "Session added OK!", Toast.LENGTH_SHORT).show();
                        updateSessions();
                    });
                } catch (AkilesException e) {
                    showException(e);
                }
            });
        });
        ((Button) findViewById(R.id.btnRemoveSession)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            executorService.execute(() -> {
                try {
                    akiles.removeSession(sessionID);
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "Session removed OK!", Toast.LENGTH_SHORT).show();
                        updateSessions();
                    });
                } catch (AkilesException e) {
                    showException(e);
                }
            });
        });
        ((Button) findViewById(R.id.btnRemoveAllSessions)).setOnClickListener(v -> {
            executorService.execute(() -> {
                try {
                    akiles.removeAllSessions();
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "All sessions removed OK!", Toast.LENGTH_SHORT).show();
                        updateSessions();
                    });
                } catch (AkilesException e) {
                    showException(e);
                }
            });
        });
        ((Button) findViewById(R.id.btnRefreshSession)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            executorService.execute(() -> {
                try {
                    akiles.refreshSession(sessionID);
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "Session refreshed OK!", Toast.LENGTH_SHORT).show();
                        updateGadgets();
                    });
                } catch (AkilesException e) {
                    showException(e);
                }
            });
        });
        ((Button) findViewById(R.id.btnRefreshAllSessions)).setOnClickListener(v -> {
            executorService.execute(() -> {
                try {
                    akiles.refreshAllSessions();
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "All sessions refreshed OK!", Toast.LENGTH_SHORT).show();
                        updateGadgets();
                    });
                } catch (AkilesException e) {
                    showException(e);
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

            akiles.doGadgetAction(sessionID, gadgetID, actionID, null, new Permissioner() {
                @Override
                public boolean hasPermissions(String[] permissions) {
                    boolean hasPermission = true;
                    for (String permission : permissions) {
                        if (checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                            hasPermission = false;
                            break;
                        }
                    }
                    return hasPermission;
                }

                @Override
                public void requestPermissions(String[] permissions) {
                    MainActivity.this.requestPermissions(permissions, BLE_REQUEST_CODE);
                }
            }, new ActionListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }

                @Override
                public void onFailure(AkilesException ex) {
                    showException(ex);
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }
            });
        });
    }

    private void showException(AkilesException e) {
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
            updateActions();
        } catch (AkilesException ex)  {
            showException(ex);
        }
    }

    private void updateActions() {

        Gadget gadget = (Gadget)gadgetSpinner.getSelectedItem();
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
    }

    public static final int BLE_REQUEST_CODE = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BLE_REQUEST_CODE:
                Toast.makeText(this, "Permissions granted OK!", Toast.LENGTH_SHORT).show();
        }
    }
}