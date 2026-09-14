package com.nyoho.krauzersrepeater;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.net.Uri;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.EditText;
import android.widget.Switch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import android.view.View;
import android.view.ViewGroup;


public class MainActivity extends AppCompatActivity {
    private EditText editRequest;
    private EditText editResponse;
    private final ActivityResultLauncher<String[]> filePicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) {
                            // Nyoho
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                                StringBuilder content = new StringBuilder();
                                String line;

                                while ((line = reader.readLine()) != null) {
                                    content.append(line).append("\n");
                                }

                                reader.close();

                                editRequest.setText(content.toString());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        editRequest = findViewById(R.id.editRequest);
        editResponse = findViewById(R.id.editResponse);
        Switch switchHttps = findViewById(R.id.switchHttps);


        Button btnLoadRequest = findViewById(R.id.btnLoadRequest);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {

            try {

                String rawRequest = editRequest.getText().toString();

                ParsedRequest parsed = RequestParser.parse(rawRequest);



                editResponse.setText("Enviando requesta papu...");

                HttpRequestSender.sendRequest(parsed, switchHttps.isChecked(), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            editResponse.setText(
                                    "ERRORSINHO DE REDSINHA:\n" + e.getMessage()
                            );
                        });

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                        try (Response res = response) {

                            String responseBody = "";

                            if (res.body() != null) {
                                responseBody = res.body().string();

                            }

                            String finalResponse =
                                    "HTTP " + res.code() + " " + res.message() + "\n" +
                                            res.headers().toString() + "\n" +
                                            responseBody;

                            runOnUiThread(() -> {
                                editResponse.setText(finalResponse);
                            });

                        }
                    }
                });


            } catch (Exception e) {
                editResponse.setText(
                        "ERROR:\n" + e.getMessage()
                );
            }
        });

        btnLoadRequest.setOnClickListener(v -> {
            filePicker.launch(new String[]{"text/plain"});
        });

        View mainScroll = findViewById(R.id.main);
        View imeSpacer = findViewById(R.id.imeSpacer);

        ViewCompat.setOnApplyWindowInsetsListener(mainScroll, (v, insets) -> {

            Insets systemBars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
            );

            Insets ime = insets.getInsets(
                    WindowInsetsCompat.Type.ime()
            );

            // Conservamos espacio para las barras normales de Android
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            // Si el teclado está abierto, añadimos al final del contenido
            // un espacio equivalente a su altura.
            int keyboardSpace = 0;

            if (insets.isVisible(WindowInsetsCompat.Type.ime())) {
                keyboardSpace = Math.max(
                        0,
                        ime.bottom - systemBars.bottom
                );
            }

            ViewGroup.LayoutParams params = imeSpacer.getLayoutParams();

            if (params.height != keyboardSpace) {
                params.height = keyboardSpace;
                imeSpacer.setLayoutParams(params);
            }

            return insets;
        });
    }
}