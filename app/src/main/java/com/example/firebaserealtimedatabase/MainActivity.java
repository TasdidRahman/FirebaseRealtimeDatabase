package com.example.firebaserealtimedatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";

    EditText editTextName;
    Button buttonAddArtist;
    Spinner spinnerGeneres;
    DatabaseReference databaseArtists;
    ListView listViewArtists;
    List<Artist> artistList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");

        editTextName = findViewById(R.id.editTextName);
        buttonAddArtist= findViewById(R.id.buttonAddArtist);
        spinnerGeneres= findViewById(R.id.spinnerGeneres);
        listViewArtists= findViewById(R.id.listViewArtist);
        artistList= new ArrayList<>();

        buttonAddArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addArtist();
            }
        });


        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Artist artist = artistList.get(position);

                Intent intent = new Intent(getApplicationContext(), AddTrackActivity.class);

                intent.putExtra(ARTIST_ID, artist.getArtistID());
                intent.putExtra(ARTIST_NAME, artist.getArtistName());

                startActivity(intent);


            }
        });

      listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          @Override
          public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

              Artist artist = artistList.get(position);

              showUpdateDialog(artist.getArtistID(), artist.getArtistName());
              return false;
          }
      });
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                artistList.clear();

                for(DataSnapshot artistSnapshot: dataSnapshot.getChildren()){

                    Artist artist = artistSnapshot.getValue(Artist.class);

                    artistList.add(artist);

                }

                ArtistList adapter = new ArtistList(MainActivity.this, artistList);
                listViewArtists.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUpdateDialog(final String artistId, String artistName){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        final View  dialogView = inflater.inflate(R.layout.update_dialog,null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName= dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate= dialogView.findViewById(R.id.buttonUpdate);
        final Spinner spinnerGenres= dialogView.findViewById(R.id.spinnerGenres);
        final Button buttonDelete= dialogView.findViewById(R.id.buttonDelete);
        dialogBuilder.setTitle("Updating Artist "+ artistName);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String genre = spinnerGenres.getSelectedItem().toString();

                if(TextUtils.isEmpty((name))){
                    editTextName.setError("Name required");
                    return;
                }else{

                    updateArtist(artistId, name, genre);
                    alertDialog.dismiss();

                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteArtist(artistId);
            }
        });


    }

    private void deleteArtist(String artistId) {

        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artists").child(artistId);
        DatabaseReference drTracks =  FirebaseDatabase.getInstance().getReference("tracks").child(artistId);

        drArtist.removeValue();
        drTracks.removeValue();

        Toast.makeText(this, "Artist deleted", Toast.LENGTH_LONG).show();

    }

    private boolean updateArtist(String id, String name, String genre){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artists").child(id);
        Artist artist = new Artist(id, name, genre);

        databaseReference.setValue(artist);

        Toast.makeText(this, "Artist updated", Toast.LENGTH_LONG).show();
        return true;
    }

    private void addArtist(){
        String name=editTextName.getText().toString().trim();
        String genre= spinnerGeneres.getSelectedItem().toString();

        if(!TextUtils.isEmpty(name)){

            String id= databaseArtists.push().getKey();

            Artist artist = new Artist(id, name, genre);

            databaseArtists.child(id).setValue(artist);

            Toast.makeText(this, "Artist Added", Toast.LENGTH_SHORT).show();



        }else{

            Toast.makeText(this,"Please enter a name", Toast.LENGTH_LONG).show();
        }
    }


}
