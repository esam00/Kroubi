package com.essam.chatapp.contacts.activity;

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
import com.essam.chatapp.chat.activity.ChatActivity;
import com.essam.chatapp.contacts.adapter.ContactsAdapter;
import com.essam.chatapp.contacts.model.Contact;
import com.essam.chatapp.contacts.utils.ContactsHelper;
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
    private List<Contact> contacts, users;

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
        appUserDb = mReference.child(Consts.USER);
    }

    private void initViews() {
        contacts = new ArrayList<>();
        users = new ArrayList<>();
        contactsRv = findViewById(R.id.rv_contacts);
        emptyTextView = findViewById(R.id.tv_empty);
        progressBar = findViewById(R.id.progress_bar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        contactsAdapter = new ContactsAdapter(this, users);
        contactsRv.setLayoutManager(linearLayoutManager);
        contactsRv.setAdapter(contactsAdapter);
    }

    private void getContactsList() {
        String isoPrefix = getCountryIso();
        Log.e(TAG, "isoPrefix: " + isoPrefix);

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

                Contact contact = new Contact("", name, phone);
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

    //sometimes contacts application repeats phone number that assigned in more than one application
    // So we want to display a contact only one time
    private boolean isRedundant(String phone) {
        for (Contact contact : contacts) {
            if (contact.getPhone().equals(phone)) {
                return true;
            }
        }
        return false;
    }

    private void checkIfThisContactIsUser(final Contact mContact) {
        Query query = appUserDb.orderByChild(Consts.PHONE).equalTo(mContact.getPhone());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = "", name = "";

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child(Consts.PHONE).getValue() != null) {
                            phone = snapshot.child(Consts.PHONE).getValue().toString();
                        }

                        if (snapshot.child(Consts.NAME).getValue() != null) {
                            name = snapshot.child(Consts.NAME).getValue().toString();
                        }

                        Contact mUser = new Contact(snapshot.getKey(), name, phone);

                        if (name.equals(phone)) {
                            mUser.setName(mContact.getName());
                        }

                        users.add(mUser);
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

    private String getCountryIso() {
        String iso = null;
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
        String otherUid = users.get(index).getUid();
        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
        intent.putExtra(Consts.USER_UID,otherUid);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed(); // make up button behave like back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
