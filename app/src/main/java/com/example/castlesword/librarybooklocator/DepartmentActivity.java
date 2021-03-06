package com.example.castlesword.librarybooklocator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

public class DepartmentActivity extends AppCompatActivity{

    private static String url = "http://bin2580.16mb.com/library_services/search_books_given_dept.php";
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawer;
    private NavigationView navView;
    private final static String TAG="SID";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ERROR = "error_msg";
    private static final String TAG_SINGLE_BRANCH = "SINGLE_BRANCH";
    public static final String ARRAY_NAME="DATA";
    private Context context;
    private ListView lv;
    String[] al;
    String q;

    private String errorMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(DepartmentActivity.this, mDrawer, R.string.open, R.string.close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigate();
        SessionManager sm= new SessionManager(getApplicationContext());
        String userid1= sm.getUserID();
        if(userid1=="")
        {
            Intent i= new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(i);
        }
        lv=(ListView)findViewById(R.id.listView1);
        context = this;
        Intent dept=getIntent();
        q = dept.getStringExtra("dept");


        new LoadData().execute(q);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                int itemPosition     = position;
                String  itemValue    = (String) lv.getItemAtPosition(position);
                Intent last = new Intent(context,LastActivity.class);
                last.putExtra("extra",itemValue);
                startActivity(last);
            }
        });
    }
    public void navigate(){
        navView = (NavigationView) findViewById(R.id.nvView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(menuItem.isChecked())
                    menuItem.setChecked(false);
                else
                    menuItem.setChecked(true);

                mDrawer.closeDrawers();

                switch (menuItem.getItemId()){

                    case R.id.nav_first_fragment:
                        Intent k = new Intent(DepartmentActivity.this, MainActivity.class);
                        startActivity(k);
                        return true;
                    case R.id.nav_second_fragment:
                        Intent bi = new Intent(DepartmentActivity.this, SearchActivity.class);
                        startActivity(bi);
                        return true;
                    case R.id.nav_third_fragment:
                        SessionManager smt = new SessionManager(getApplicationContext());
                        smt.removeUser();
                        Intent j = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(j);
                        return true;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mToggle.onOptionsItemSelected(item)){
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadData extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;
        JSONArray branches = null;
        LinkedHashMap<String, String> p;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(DepartmentActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String...args) {

            p = new LinkedHashMap<>();
            p.put("DEPT",args[0]);

            WebRequest webreq = new WebRequest();
            String jsonStr = webreq.makeWebServiceCall(url, WebRequest.POST,p);
            ParseJSON(jsonStr);
            Log.d(TAG,"json string"+jsonStr);

            return jsonStr;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(DepartmentActivity.this,
                            android.R.layout.simple_list_item_1,al);

                    lv.setAdapter(adapter);
                }
            });


        }


        private void ParseJSON(String json) {

            if (json != null) {
                try {
                    JSONObject jsonObj = new JSONObject(json);

                    if(jsonObj.getInt("success") == 1) {
                        branches = jsonObj.getJSONArray(ARRAY_NAME);

                        al = new String[branches.length()];
                        for (int i = 0; i < branches.length(); i++) {
                            JSONObject c = branches.getJSONObject(i);
                            String b = c.getString("name");
                            al[i] = b;

                        }
                    }
                    else
                    {
                        al=new String[1];
                        al[0]="Sorry !!! no related book found for selected department !!";
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
        }



    }

}
