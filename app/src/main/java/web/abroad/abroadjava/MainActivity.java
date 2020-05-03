package web.abroad.abroadjava;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

import web.abroad.abroadjava.model.User;

public class MainActivity extends AppCompatActivity {
    GridView gridView;
    Button btnFrom, btnTo, btnAccom, btnCost;
    DatePickerDialog datePickerDialog;
    EditText txtCity;
    int year;
    int month;
    int dayOfMonth;
    Calendar calendar;

	// In order to have a maintable code, these things should be put into arrays/resources etc.
    int[] mainImages = {
            R.drawable.barcelona,
            R.drawable.berlin,
            R.drawable.budapest,
            R.drawable.paris,
            R.drawable.new_york,
            R.drawable.rio_de_janeiro,
            R.drawable.roma,
            R.drawable.shanghai
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        gridView = findViewById(R.id.gridview);
        txtCity  = findViewById(R.id.txt_city);


        CustomAdapter customAdapter = new CustomAdapter();
        gridView.setAdapter(customAdapter);

        btnFrom = findViewById(R.id.btn_from);
        if(btnFrom != null){
            btnFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popCalendarFrom(v);
                }
            });
        }

        btnTo = findViewById(R.id.btn_to);
        if(btnTo != null){
            btnTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popCalendarTo(v);
                }
            });
        }

        btnAccom = findViewById(R.id.btn_search_accomm);
        if(btnAccom != null){
            btnAccom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean error = false;
                    String city = txtCity.getText().toString();
                    if (city.matches("")) {
                        txtCity.setHint(R.string.DASHBOARD_empty_city);
                        txtCity.setHintTextColor(getResources().getColor(R.color.colorLightRed));
                        error = true;
                    }

                    if(btnFrom.getText().toString().matches("") || btnFrom.getText().toString().matches(getString(R.string.DASHBOARD_empty_date))){
                        btnFrom.setText(R.string.DASHBOARD_empty_date);
                        btnFrom.setTextColor(getResources().getColor(R.color.colorLightRed));
                        error = true;
                    }

                    if(btnTo.getText().toString().matches("") || btnTo.getText().toString().matches(getString(R.string.DASHBOARD_empty_date))){
                        btnTo.setText(R.string.DASHBOARD_empty_date);
                        btnTo.setTextColor(getResources().getColor(R.color.colorLightRed));
                        error = true;
                    }

                    if(error){
                        return;
                    }

                    Intent intent = new Intent(getBaseContext(), SeeAccommodationsActivity.class);
                    intent.putExtra("city", city);
                    intent.putExtra("from", btnFrom.getText().toString());
                    intent.putExtra("to", btnTo.getText().toString());
                    startActivity(intent);

                    //Toast.makeText(getApplicationContext(),city,Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void popCalendarFrom(View v){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        btnFrom.setText(day + "/" + (month + 1) + "/" + year);
                        //Toast.makeText(getApplicationContext(),day + "/" + (month + 1) + "/" + year,Toast.LENGTH_SHORT).show();
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void popCalendarTo(View v){
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        btnTo.setText(day + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, dayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Boolean logged = User.Companion.checkIfUserIsLogged();
        switch (item.getItemId()){
            case R.id.chat:
                if(logged){
                    Intent intent = new Intent(getApplicationContext(), ChatsActivity.class);
                    startActivity(intent);
                    return true;
                }else{
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    return true;
                }
            case R.id.profile:
                if(logged){
                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }else{
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private class CustomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mainImages.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view1 = getLayoutInflater().inflate(R.layout.row_data,null);
            //getting view in row_data
            ImageView image = view1.findViewById(R.id.images);

            image.setImageResource(mainImages[i]);
            return view1;
        }
    }
}