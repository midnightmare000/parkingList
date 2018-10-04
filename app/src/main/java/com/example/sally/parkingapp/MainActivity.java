package com.example.sally.parkingapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.baoyz.widget.PullRefreshLayout;
import com.example.sally.parkingapp.adapter.parkingAdapter;
import com.example.sally.parkingapp.item.Parking;
import com.example.sally.parkingapp.util.Network;
import com.example.sally.parkingapp.util.parkingDBController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private RecyclerView recyclerView;
    private parkingAdapter parkingAdapter;
    private EditText searchKeyEd, searchAreaEd;
    private ArrayAdapter<String> areaAdapter;
    private PullRefreshLayout pullRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setView();
        getDataFromDB();
    }

    private void setView(){

        //recyclerView setting
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        parkingAdapter = new parkingAdapter(new ArrayList<Parking>());
        recyclerView.setAdapter(parkingAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        //search bar setting
        searchKeyEd = (EditText)findViewById(R.id.searchEdit);
        searchAreaEd = (EditText)findViewById(R.id.searchArea);
        areaAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
        searchAreaEd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog_list = new AlertDialog.Builder(MainActivity.this);
                dialog_list.setTitle("選擇區域");
                dialog_list.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        dialog.dismiss();
                    }
                });
                dialog_list.setAdapter(areaAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        searchAreaEd.setText(areaAdapter.getItem(index));
                        if(index == 0)
                            getParlingsBySearch("",searchKeyEd.getText().toString());
                        else
                            getParlingsBySearch(searchAreaEd.getText().toString() ,searchKeyEd.getText().toString());
                    }
                });
                dialog_list.show();
            }
        });

        searchKeyEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(searchAreaEd.getText().toString().equals("全區域"))
                    getParlingsBySearch("",searchKeyEd.getText().toString());
                else
                    getParlingsBySearch(searchAreaEd.getText().toString() ,searchKeyEd.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //refresh layout setting
        pullRefreshLayout = (PullRefreshLayout)findViewById(R.id.pullRefreshLayout);
        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences sharedPreferences = getSharedPreferences("parking",MODE_PRIVATE);
                sharedPreferences
                        .edit()
                        .clear()
                        .commit();
                getDataFromDB();
            }
        });
    }

    private void getDataFromDB(){
        pullRefreshLayout.setRefreshing(true);
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,
                "連線中","請稍後", true);

        Network.getData("" + getResources().getString(R.string.databaseURL),null , new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(dialog.isShowing())
                            dialog.dismiss();
                        showErrorDialog("連線錯誤");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    Log.d("Response", responseStr);
                    try {
                        JSONObject jo = new JSONObject(responseStr.toString());
                        final boolean status = jo.getBoolean("success");
                        final JSONObject parkingData = jo.getJSONObject("result");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog.isShowing())
                                    dialog.dismiss();
                                if(status){
                                    saveToSQLite(parkingData);
                                }
                                else{
                                    showErrorDialog("伺服器錯誤");
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(dialog.isShowing())
                                    dialog.dismiss();
                                showErrorDialog("伺服器錯誤");
                            }
                        });
                    }
                }
                else {
                    showErrorDialog("伺服器錯誤");
                }
            }
        });
    }

    //save Parking Data to SQLite
    private void saveToSQLite(JSONObject parkingData){
        try {
            final SharedPreferences sharedPreferences = getSharedPreferences("parking",MODE_PRIVATE);
            String nowResourceId =  sharedPreferences.getString("resource_id",null);
            final String resouceId = parkingData.getString("resource_id");
            final JSONArray records = parkingData.getJSONArray("records");

            //check resouceID
            if(nowResourceId == null || !resouceId.equals(nowResourceId)) {
                // prepare all parking Data
                final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,
                        "資料儲存中", "請稍後", true);

                parkingDBController parkingDBController = new parkingDBController(MainActivity.this);
                parkingDBController.textToParkingDB(records, new parkingDBController.Callback() {
                    @Override
                    public void onFailure() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                showErrorDialog("資料儲存錯誤");
                            }
                        });
                    }

                    @Override
                    public void onSuccess(final int errorCount) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (dialog.isShowing())
                                    dialog.dismiss();
                                showDialog("更新成功");
                                Log.i("errorCount", "errorCount : " + errorCount + ",data:" + records.length());
                                sharedPreferences
                                        .edit()
                                        .putString("resource_id",resouceId)
                                        .commit();
                                setSearchAreaList();
                                getParkingsFromSQLite();
                            }
                        });
                    }
                });
            }
            else {
                showDialog("資料為最新版本，無需新增");
                setSearchAreaList();
                getParkingsFromSQLite();
            }

        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

    //get data from SQLite and show on recyclerView

    private void getParkingsFromSQLite(){
        parkingDBController parkingDBController = new parkingDBController(MainActivity.this);
        List<Parking> parkings = parkingDBController.getAllParking();
        parkingAdapter.setParking(parkings);
        parkingAdapter.notifyDataSetChanged();
        pullRefreshLayout.setRefreshing(false);
        searchAreaEd.setText("全區域");
        searchKeyEd.setText("");
    }

    private void getParlingsBySearch(String area, String name){
        parkingDBController parkingDBController = new parkingDBController(MainActivity.this);
        List<Parking> parkings = parkingDBController.getParkingBySearch(area,name);
        parkingAdapter.setParking(parkings);
        parkingAdapter.notifyDataSetChanged();
    }

    //get Area List
    private void setSearchAreaList(){
        parkingDBController parkingDBController = new parkingDBController(MainActivity.this);
        List<String> areaList = parkingDBController.getAllArea();
        areaAdapter.clear();
        areaAdapter.add("全區域");
        for(int i = 0; i< areaList.size(); i++)
            areaAdapter.add(areaList.get(i));
    }

    private void showErrorDialog(String errorMsg){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("錯誤")
                .setMessage(errorMsg)
                .setPositiveButton("重試", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDataFromDB();
                    }
                })
                .show();
    }

    private void showDialog(String Msg){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage(Msg)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }
}
