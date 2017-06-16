package com.example.shan.myfirstbot;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.analytics.api.Analytics;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;



import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ChatAdapter mAdapter;
    private ArrayList messageArrayList;
    private EditText inputMessage;
    private ImageButton btnSend;
    private Map<String,Object> context = new HashMap<>();
    private boolean initialRequest;

    private boolean listening = false;
    private Context mContext;
    private String workspace_id;
    private String conversation_username;
    private String conversation_password;


    private static final String TAG = "myMessage";

     protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mContext = getApplicationContext();
            conversation_username = "8b5538ba-6523-4603-aacf-20e11b248d4d";
            conversation_password = "ebFTpS5BWKAC";
            workspace_id = "4af5db83-3753-464c-b169-c5af787fa711";



            inputMessage = (EditText) findViewById(R.id.message);
            btnSend = (ImageButton) findViewById(R.id.btn_send);
//            btnRecord= (ImageButton) findViewById(R.id.btn_record);
           String customFont = "Montserrat-Regular.ttf";
           Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);
           inputMessage.setTypeface(typeface);
           recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

            messageArrayList = new ArrayList<>();
            mAdapter = new ChatAdapter(messageArrayList);

            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
            this.inputMessage.setText("");
            this.initialRequest = true;
            sendMessage();


            btnSend.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(checkInternetConnection()) {
                        sendMessage();
                    }
                }
            });


     };

        // Sending a message to Watson Conversation Service
        private  void sendMessage() {

            final String inputmessage = this.inputMessage.getText().toString().trim();
            if(!this.initialRequest) {
                Message inputMessage = new Message();
                inputMessage.setMessage(inputmessage);
                inputMessage.setId("1");
                messageArrayList.add(inputMessage);
//                myLogger.info("Sending a message to Watson Conversation Service");

            }
            else
            {
                Log.i(TAG,"Inside send message 2.....");
                Message inputMessage = new Message();
                inputMessage.setMessage(inputmessage);
                inputMessage.setId("100");
                this.initialRequest = false;
                Log.i(TAG,"Inside send message 2.....");

            }

            this.inputMessage.setText("");
            mAdapter.notifyDataSetChanged();

            Thread thread = new Thread(new Runnable(){
                public void run() {
                    try {
                        Log.i(TAG,"Watson Thread 1.....");
                        ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2017_02_03);
                        Log.i(TAG,"Watson Thread 11.....");
                        service.setUsernameAndPassword(conversation_username, conversation_password);
                        Log.i(TAG,"Watson Thread 12.....");
                        MessageRequest newMessage = new MessageRequest.Builder().inputText(inputmessage).context(context).build();
                        Log.i(TAG,"Watson Thread 13.....");
                        MessageResponse response = service.message(workspace_id, newMessage).execute();
                        Log.i(TAG,"Watson Thread 2.....");
                        //Passing Context of last conversation
                        if(response.getContext() !=null)
                        {
                            context.clear();
                            context = response.getContext();

                        }
                        Message outMessage=new Message();
                        if(response!=null)
                        {
                            if(response.getOutput()!=null && response.getOutput().containsKey("text"))
                            {

                                ArrayList responseList = (ArrayList) response.getOutput().get("text");
                                if(null !=responseList && responseList.size()>0){
                                    outMessage.setMessage((String)responseList.get(0));
                                    outMessage.setId("2");
                                }
                                Log.i(TAG,"Watson Thread 3.....");
                                messageArrayList.add(outMessage);
                            }
                            Log.i(TAG,"Watson Thread 4.....");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                    if (mAdapter.getItemCount() > 1) {
                                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount()-1);

                                    }

                                }
                            });


                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

             thread.start();

        }


        /**
         * Check Internet Connection
         * @return
         */
        private boolean checkInternetConnection(){
            // get Connectivity Manager object to check connection
            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();

            // Check for network connections
            if (isConnected){
                return true;
            }
            else {
                Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show();
                return false;
            }

        }

        private void showError(final Exception e) {
            runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }


    };


