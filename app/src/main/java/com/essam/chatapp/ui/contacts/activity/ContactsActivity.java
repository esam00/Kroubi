package com.essam.chatapp.ui.contacts.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.essam.chatapp.R;
import com.essam.chatapp.ui.chat.activity.ChatActivity;
import com.essam.chatapp.ui.contacts.adapter.ContactsAdapter;
import com.essam.chatapp.models.User;
import com.essam.chatapp.ui.contacts.utils.ContactsHelper;
import com.essam.chatapp.utils.Consts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.ListItemClickListener {
    private RecyclerView contactsRv;
    private TextView emptyTextView;
    private ProgressBar progressBar;
    private ContactsAdapter contactsAdapter;
    private List<User> users, contacts;

    // firebase
    private  DatabaseReference appUserDb;

    private static final String TAG = ContactsContract.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        initFirebase();
        initViews();

        // start fetching contacts from user device
        getContactsList();
    }

    private void initFirebase(){
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mReference = mDatabase.getReference();
        // Database reference to user/ node in firebaseDatabase
        appUserDb = mReference.child(Consts.USER);
    }

    private void initViews() {
        users = new ArrayList<>();
        contacts = new ArrayList<>();
        contactsRv = findViewById(R.id.rv_contacts);
        emptyTextView = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progress_bar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        contactsAdapter = new ContactsAdapter(this, users);
        contactsRv.setLayoutManager(linearLayoutManager);
        contactsRv.setAdapter(contactsAdapter);
    }

    /**
     * Basically we need to read all user contacts that are saved on device,
     * and for each item on this contacts list we make a firebase query by phone number to check
     * if this user is exist on our database [We need to display only users who are using our app]
     */
    private void getContactsList() {
        // isoPrefix is country code before each number like [+20, +966 ..]
        // this must be added to the phone number because all numbers in firebase database starts with
        // this iso code
        String isoPrefix = getCountryIso();
        Log.i(TAG, "isoPrefix: " + isoPrefix);

        // TODO: 4/15/2020 Use CursorLoader for better performance
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if (!String.valueOf(phone.charAt(0)).equals("+")) {
                    phone = isoPrefix + phone;
                }

                User contact = new User("", name, phone);
                if (!isRedundant(phone)) {
                    contacts.add(contact);
                    checkIfThisContactIsUser(contact);
                }
            }
            cursor.close();
        }else {
            Log.i(TAG, "contacts list size=0");
            noUsersState();
        }
    }

    /**
     * In some cases contacts application repeats phone number which is assigned
     * to more than one application [whatsAap , telegram ..]
     * So we want to display a contact only one time
     * @param phone of contact that we want to check if we already parsed it
     * @return true if contacts list contains this one
     */
    private boolean isRedundant(String phone) {
        for (User contact : contacts) {
            if (contact.getPhone().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    /**
     * For every contact of this user we will check if this contact is a user of our application
     * So we make query by phone number to check if this phone number is in our database
     * and if so add this to users list and display it
     * @param mContact item of contacts list
     */
    private void checkIfThisContactIsUser(final User mContact) {
        Query query = appUserDb.orderByChild(Consts.PHONE).equalTo(mContact.getPhone());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User mUser = snapshot.getValue(User.class);
                        if(mUser !=null){
                            if (mUser.getName().equals(mUser.getPhone())) {
                                mUser.setName(mContact.getName());
                            }
                            users.add(mUser);
                        }
                        contactsAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * isoPrefix is country code before each number like [+20, +966 ..]
     * this must be added to the phone number because all numbers in
     * firebase database starts with this iso code
     * @return iso according to country
     */
    private String getCountryIso() {
        String iso = "";
        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext()
                .getSystemService(TELEPHONY_SERVICE);
        if (telephonyManager.getNetworkCountryIso() != null) {
            if (!telephonyManager.getNetworkCountryIso().equals("")) {
                iso = telephonyManager.getNetworkCountryIso();
            }
        }

        return ContactsHelper.getPhone(iso);
    }

    private void noUsersState(){
        emptyTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        contactsRv.setVisibility(View.GONE);
    }

    @Override
    public void onClick(final int index) {
        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
        intent.putExtra(Consts.USER,users.get(index));
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed(); // make up button behave like back button
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
