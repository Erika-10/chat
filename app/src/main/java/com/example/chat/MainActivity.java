package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
      private CircleImageView fotoPerfil;
      private TextView nombre;
      private RecyclerView rvMensajes;
      private EditText txtMensaje;
      private Button btnEnviar;
      private AdapterMensajes adapter;
      private ImageButton btnEnviarFoto;

      private FirebaseDatabase database;
      private DatabaseReference databaseReference;
      private FirebaseStorage storage;
      private StorageReference storageReference;
      private static final int PHOTO_SEND = 1;
      private static final int PHOTO_PERFIL = 2;
      private String fotoPerfilCadena;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fotoPerfil = (CircleImageView) findViewById(R.id.fotoPerfil);
        nombre = (TextView) findViewById(R.id.nombre);
        rvMensajes = (RecyclerView)findViewById(R.id.rvMensajes);
        btnEnviar = (Button)findViewById(R.id.btnEnviar);
        btnEnviarFoto = (ImageButton) findViewById(R.id.btnEnviarFoto);
        fotoPerfilCadena = "";

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("chat");
        storage = FirebaseStorage.getInstance();

        adapter = new AdapterMensajes(this);
        LinearLayoutManager l = new LinearLayoutManager(this);
        rvMensajes.setLayoutManager(l);
        rvMensajes.setAdapter(adapter);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.push().setValue(new MensajeEnviar(txtMensaje.getText().toString(),nombre.getText().toString(),"fotoPerfilCadena","1", ServerValue.TIMESTAMP));
                txtMensaje.setText("");

            }
        });
           btnEnviarFoto.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                   i.setType("image/jpeg");
                   i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                   startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PHOTO_SEND);

               }
           });
           fotoPerfil.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                   i.setType("image/jpeg");
                   i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                   startActivityForResult(Intent.createChooser(i, "Selecciona una foto"), PHOTO_PERFIL);
               }
           });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
            public void onItemRangeInserted(int positionStart,int itemCount){
                super.onItemRangeInserted(positionStart, itemCount);
                setSrollbar();
            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                  MensajeRecibir m= dataSnapshot.getValue(MensajeRecibir.class);
                  adapter.addMensaje(m);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void setSrollbar(){
        rvMensajes.scrollToPosition(adapter.getItemCount()-1);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);
        if(requestCode == PHOTO_SEND && resultCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("imagenes_chat");
            final StorageReference fotoReferencia = storageReference.child(u.getLastPathSegment());
            fotoReferencia.putFile(u).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> u = taskSnapshot.getStorage().getDownloadUrl();
                    MensajeEnviar m = new MensajeEnviar("Te han enviado una foto", u.toString(), nombre.getText().toString(), "fotoPerfilCadena", "2", ServerValue.TIMESTAMP);
                    databaseReference.push().setValue(m);

                }
            });

        }else if(requestCode == PHOTO_PERFIL && resultCode == RESULT_OK){
            Uri u = data.getData();
            storageReference = storage.getReference("foto_perfil");
            final StorageReference fotoReferencia = storageReference.child(u.getLastPathSegment());
            fotoReferencia.putFile(u).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> u = taskSnapshot.getStorage().getDownloadUrl();
                    fotoPerfilCadena = u.toString();
                    MensajeEnviar m = new MensajeEnviar("Erika actualizo su foto de perfil", u.toString(), nombre.getText().toString(), "fotoPerfilCadena", "2", ServerValue.TIMESTAMP);
                    databaseReference.push().setValue(m);
                    Glide.with(MainActivity.this).load(u.toString()).into(fotoPerfil);
                }
            });
        }
    }
}

