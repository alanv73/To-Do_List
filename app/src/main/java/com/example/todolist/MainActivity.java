package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    Time today = new Time(Time.getCurrentTimezone());
    DBAdapter myDb;
    EditText etTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTasks = (EditText) findViewById(R.id.editTextTask);
        openDB();
        populateListView();
        listViewItemClick();
        listViewItemLongClick();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    private void openDB(){
        myDb = new DBAdapter(this);
        myDb.open();
    }

    public void onClick_AddTask(View v){
        today.setToNow();
        String timeStamp = today.format("%Y-%m-%d %H:%M:%S");
        if(!TextUtils.isEmpty(etTasks.getText().toString())){
            myDb.insertRow(etTasks.getText().toString(), timeStamp);
        }
        populateListView();
    }

    private void populateListView(){
        Cursor cursor = myDb.getAllRows();
        String[] fromFieldNames = new String[] {DBAdapter.KEY_ROWID, DBAdapter.KEY_TASK};
        int[] toViewIDs = new int[] {R.id.textViewItemNumber, R.id.textViewItemTask};
        SimpleCursorAdapter myCursorAdapter;
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.item_layout, cursor, fromFieldNames, toViewIDs, 0);
        ListView myList = (ListView) findViewById(R.id.listViewTasks);
        myList.setAdapter(myCursorAdapter);
    }

    private void updateTask(long id){
        Cursor cursor = myDb.getRow(id);
        if(cursor.moveToFirst()){
            String task = etTasks.getText().toString();
            today.setToNow();
            String date = today.format("%Y-%m-%d %H:%M:%S");
            myDb.updateRow(id, task, date);
        }

        cursor.close();
    }

    private void listViewItemClick(){
        ListView myList = (ListView) findViewById(R.id.listViewTasks);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                updateTask(id);
                populateListView();
                displayToast(id);
            }
        });
    }

    public void onClick_DeleteTasks(View v){
        myDb.deleteAll();
        populateListView();
    }

    public void listViewItemLongClick(){
        ListView myList = (ListView) findViewById(R.id.listViewTasks);
        myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
                myDb.deleteRow(id);
                populateListView();
                return false;
            }
        });
    }

    private void displayToast(long id){
        Cursor cursor = myDb.getRow(id);
        if(cursor.moveToFirst()){
            long idDB = cursor.getLong(DBAdapter.COL_ROWID);
            String task = cursor.getString(DBAdapter.COL_TASK);
            String date = cursor.getString(DBAdapter.COL_DATE);

            String message = String.format("ID: %d\nTask: %s\nDate: %s", idDB, task, date);

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        cursor.close();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        closeDB();
    }

    private void closeDB(){
        myDb.close();
    }

}
