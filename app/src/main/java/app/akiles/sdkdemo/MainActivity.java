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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.akiles.sdk.ActionListener;
import app.akiles.sdk.Akiles;
import app.akiles.sdk.AkilesException;
import app.akiles.sdk.Gadget;
import app.akiles.sdk.GadgetAction;
import app.akiles.sdk.Hardware;
import app.akiles.sdk.PermissionHelper;
import app.akiles.sdk.Card;

public class MainActivity extends AppCompatActivity {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    Akiles akiles;
    Spinner sessionSpinner;
    Spinner gadgetSpinner;
    Spinner actionSpinner;
    Spinner hardwareSpinner;
    PermissionHelper permissionHelper = new PermissionHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        akiles = new Akiles(this, "AndroidDemo", event -> runOnUiThread(() -> Toast.makeText(MainActivity.this, event.toString(), Toast.LENGTH_SHORT).show()));

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

            executorService.execute(() -> akiles.doGadgetAction(sessionID, gadgetID, actionID, null, permissionHelper, new ActionListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }

                @Override
                public void onFailure(AkilesException ex) {
                    showException(ex);
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                }
            }));
        });

        ((Button) findViewById(R.id.btnSync)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            String hardwareID = ((Hardware)hardwareSpinner.getSelectedItem()).id;

            View spinner = ((View) findViewById(R.id.spinner2));
            if(spinner.getVisibility() == View.VISIBLE) {
                return;
            }
            spinner.setVisibility(View.VISIBLE);

            executorService.execute(() -> {
                try {
                    akiles.doHardwareSync(sessionID, hardwareID, Duration.ofSeconds(60), permissionHelper);
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                    this.runOnUiThread(() -> {
                        Toast.makeText(this, "Hardware Synced", Toast.LENGTH_LONG).show();
                    });
                } catch (AkilesException e) {
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                    showException(e);
                }
            });
        });

        ((Button) findViewById(R.id.btnScan)).setOnClickListener(v -> {
            String sessionID = sessionSpinner.getSelectedItem().toString();
            View spinner = ((View) findViewById(R.id.spinner2));
            if(spinner.getVisibility() == View.VISIBLE) {
                return;
            }
            spinner.setVisibility(View.VISIBLE);

            clearHardwares();
            executorService.execute(() -> {
                try {
                    akiles.getNearbyHardwares(permissionHelper, Duration.ofSeconds(60),
                            hw -> runOnUiThread(() -> updateHardwares(hw)));
                    runOnUiThread(() -> {
                        spinner.setVisibility(View.GONE);
                        Toast.makeText(this, "Scan finished", Toast.LENGTH_LONG).show();
                    });
                } catch (AkilesException e) {
                    runOnUiThread(() -> spinner.setVisibility(View.GONE));
                    showException(e);
                }
            });
        });

        ((Button) findViewById(R.id.btnScanCard)).setOnClickListener(v -> {
            executorService.execute(() -> {
                try {
                    Card card = akiles.scanCard();
                    Log.i("ak", "is akiles card: " + card.isAkilesCard());
                    Log.i("ak", "UID: " + card.getUid());
                    if (card.isAkilesCard()) {
                        card.update();
                    }
                } catch (AkilesException e) {
                    showException(e);
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
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}