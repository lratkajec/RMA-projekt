package hr.helloworld.david.esports;

import android.content.Intent;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class SignupActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText pwdEditText;
    private EditText rpwdEditText;
    private Button continueButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Uri selectedImage;
    private Uri uploadedImageUri;


    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        profileImageView = findViewById(R.id.SignUpActivityImageSelector);
        usernameEditText = findViewById(R.id.SignUpActivityUsernameEditView);
        emailEditText = findViewById(R.id.SignUpActivityEmailEditView);
        pwdEditText = findViewById(R.id.SignUpActivityPasswordEditView);
        rpwdEditText = findViewById(R.id.SignUpActivityRePasswordEditView);
        continueButton = findViewById(R.id.SignUpActivityCreateAccountButton);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    createNewUser();
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            profileImageView.setImageURI(selectedImage);

        }


    }


    private void createNewUser() {
        if (!validateForm()) {
            return;
        }

        String email = emailEditText.getText().toString();
        String password = pwdEditText.getText().toString();


        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        signInUser();
                        firebaseUser = authResult.getUser();

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference().child("profile_picture/" + firebaseUser.getUid());

                        storageReference.putFile(selectedImage)
                                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    }
                                });

                        setUserInfo();
                    }
                });

    }

    private void setUserInfo() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();


        storageReference.child("profile_picture/" + firebaseUser.getUid()).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        User user = new User(firebaseUser.getUid(), usernameEditText.getText().toString(),
                                emailEditText.getText().toString(), uri);

                        user.saveNewUser();


                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().
                                setDisplayName(usernameEditText.getText().toString()).
                                setPhotoUri(uri).build();

                        firebaseUser.updateProfile(profileUpdates).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });


    }

    private void signInUser() {
        String email = emailEditText.getText().toString();
        String password = pwdEditText.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //TODO nastvi ovdje ili gore
                        } else {
                            Toast.makeText(SignupActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;


        String username = usernameEditText.getText().toString();
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Required.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        String mail = emailEditText.getText().toString();
        if (TextUtils.isEmpty(mail)) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }


        String pwd = pwdEditText.getText().toString();
        if (pwd.matches("")) {
            pwdEditText.setError("Required.");
            valid = false;
        } else {
            pwdEditText.setError(null);
        }

        String rpwd = rpwdEditText.getText().toString();
        if (rpwd.matches("")) {
            rpwdEditText.setError("Required.");
            valid = false;
        } else {
            rpwdEditText.setError(null);
        }

        if (!TextUtils.equals(pwd, rpwd)) {
            rpwdEditText.setError("Must be equal.");
            pwdEditText.setError("Must be equal.");
            valid = false;
        } else if (pwd.length() < 6 || rpwd.length() < 6) {
            rpwdEditText.setError("Min 6 char.");
            pwdEditText.setError("Min 6 char.");
            valid = false;
        } else {
            rpwdEditText.setError(null);
            pwdEditText.setError(null);
        }

        return valid;
    }
}
