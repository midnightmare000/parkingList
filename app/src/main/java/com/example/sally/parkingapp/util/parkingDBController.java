package com.example.sally.parkingapp.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.sally.parkingapp.item.Parking;
import com.example.sally.parkingapp.sqlite.MyDBHelper;
import com.example.sally.parkingapp.util.TW97Translator.TMParameter;
import com.example.sally.parkingapp.util.TW97Translator.TMToLatLon;
import com.example.sally.parkingapp.util.TW97Translator.TWD97;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;


public class parkingDBController {
    private static final String TABLE_NAME = "Parking";
    private static final String KEY_ID = "_id";
    private static final String AREA_COLUMN = "area";
    private static final String NAME_COLUMN = "name";
    private static final String ADDRESS_COLUMN = "address";
    private static final String SERVICE_COLUMN = "serviceTime";
    private static final String LAT_COLUMN = "lat";
    private static final String LON_COLUMN = "lon";
    private Context context;

    private SQLiteDatabase db;

    public parkingDBController(Context context){
        db = MyDBHelper.getDatabase(context);
        this.context = context;
    }

    public void close() {
        db.close();
    }

    public void insertParking(Parking parking){
        ContentValues cv = new ContentValues();
        cv.put(AREA_COLUMN,parking.getArea());
        cv.put(NAME_COLUMN,parking.getName());
        cv.put(ADDRESS_COLUMN,parking.getAddress());
        cv.put(SERVICE_COLUMN,parking.getServiceTime());
        cv.put(LAT_COLUMN,parking.getLat());
        cv.put(LON_COLUMN,parking.getLon());
        db.insert(TABLE_NAME, null, cv);
    }

    public void insertParkingTransaction(List<Parking> parkings){
        db.beginTransaction();
        for(int i = 0; i < parkings.size(); i++) {
            insertParking(parkings.get(i));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<Parking> getAllParking(){
        List<Parking> result = new ArrayList<Parking>();
        Cursor cursor = db.query(TABLE_NAME,null,null,null,null,null,null,null);
        while(cursor.moveToNext()){
            result.add(getParking(cursor));
        }
        cursor.close();
        return result;
    }

    public List<Parking> getParkingBySearch(String area,String name){
        List<Parking> result = new ArrayList<Parking>();
        String where = AREA_COLUMN + " like '" + "%" + area +"%' AND " + NAME_COLUMN  + " like '" + "%" + name +"%'";
        Cursor cursor = db.query(TABLE_NAME,null,where,null,null,null,null,null);
        while(cursor.moveToNext()){
            result.add(getParking(cursor));
        }
        cursor.close();
        return result;
    }

    public List<String> getAllArea(){
        List<String> result = new ArrayList<String>();
        String[] column = {AREA_COLUMN};
        Cursor cursor = db.query(TABLE_NAME, column,null,null, AREA_COLUMN,null, null,null);
        while(cursor.moveToNext()){
            result.add(getArea(cursor));
        }
        cursor.close();
        return result;
    }

    public Parking getParking(Cursor cursor){
        Parking parking = new Parking();
        parking.setId(cursor.getLong(0));
        parking.setArea(cursor.getString(1));
        parking.setName(cursor.getString(2));
        parking.setAddress(cursor.getString(3));
        parking.setServiceTime(cursor.getString(4));
        parking.setLat(cursor.getFloat(5));
        parking.setLon(cursor.getFloat(6));
        return parking;
    }

    public String getArea(Cursor cursor){
        return cursor.getString(0);
    }

    public boolean delectParking(long id){
        String where = KEY_ID + "=" + id;
        return db.delete(TABLE_NAME,where,null) > 0;
    }

    public boolean delectAllParking(){
        return db.delete(TABLE_NAME,null,null) > 0;
    }

    public void textToParkingDB(final JSONArray records, final Callback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<Parking> parkings = new ArrayList<Parking>();
                    TMParameter TWD97 = new TWD97();
                    int errorCount = 0;

                    for(int i = 0; i < records.length(); i++){
                        String area = records.getJSONObject(i).getString("AREA");
                        String name = records.getJSONObject(i).getString("NAME");
                        String address = records.getJSONObject(i).getString("ADDRESS");
                        String serviceTime = records.getJSONObject(i).getString("SERVICETIME");
                        String tw97x_text = records.getJSONObject(i).getString("TW97X");
                        String tw97y_text = records.getJSONObject(i).getString("TW97Y");



                        //tw97 translte lat,lon
                        try{
                            double tw97x = Double.valueOf(tw97x_text);
                            double tw97y = Double.valueOf(tw97y_text);
                            double latlon[] = TMToLatLon.convert(TWD97,tw97x,tw97y);
                            Log.i("parking",area + name + address + serviceTime + latlon[0] + latlon[1]);
                            parkings.add(new Parking(area,name,address,serviceTime,latlon[0],latlon[1]));
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            errorCount ++;
                        }
                    }

                    delectAllParking();
                    insertParkingTransaction(parkings);
                    callback.onSuccess(errorCount);
                }
                catch (JSONException e){
                    e.printStackTrace();
                    callback.onFailure();
                }
            }
        }).start();
    }

    public interface Callback{
        void onFailure();
        void onSuccess(int errorCount);
    }
}
