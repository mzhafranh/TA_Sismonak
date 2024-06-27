package com.mzhtech.sismonakdev.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.models.Child;
import com.mzhtech.sismonakdev.models.Parent;
import com.mzhtech.sismonakdev.utils.LocaleUtils;
import com.mzhtech.sismonakdev.utils.SharedPrefsUtils;

public class AccountVerificationActivity extends AppCompatActivity {
	private static final String TAG = "AccountVerificationTAG";
	private FirebaseAuth auth;
	private Handler handler;
	private Runnable emailVerificationRunnable;
	private static final int CHECK_INTERVAL = 2000; // 2 seconds

	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private FirebaseStorage firebaseStorage;
	private StorageReference storageReference;

	private boolean googleAuth;
	private String signUpEmail;
	private String signUpName;
	private boolean parent;
	private String uid;
	private String parentEmail;
	private String imageUri;
	private String userEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_verification);
		sendVerificationMessage();

//		if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
//			startActivity(new Intent(this, LoginActivity.class));

		auth = FirebaseAuth.getInstance();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("users");
		firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference("profileImages");
		handler = new Handler();

//		googleAuth = getIntent().getBooleanExtra("googleAuth",false);
		googleAuth = SharedPrefsUtils.getBooleanPreference(this, "googleAuth", false);
//		signUpEmail = getIntent().getStringExtra("signUpEmail");
		signUpEmail = SharedPrefsUtils.getStringPreference(this, "signUpEmail","");
//		signUpName = getIntent().getStringExtra("signUpName");
		signUpName = SharedPrefsUtils.getStringPreference(this, "signUpName","");
//		parent = getIntent().getBooleanExtra("isParent",true);
		parent = SharedPrefsUtils.getBooleanPreference(this, "isParent", true);
//		uid = getIntent().getStringExtra("uid");
		uid = SharedPrefsUtils.getStringPreference(this, "uid", "testAccount");
//		parentEmail = getIntent().getStringExtra("parentEmail");
		parentEmail= SharedPrefsUtils.getStringPreference(this, "parentEmail", "");
		userEmail = SharedPrefsUtils.getStringPreference(this, "userEmail", "");
		if (getIntent().getStringExtra("imageUri") == null){
			imageUri = "android.resource://com.mzhtech.sismonakdev/drawable/ic_default_avatar";
		} else {
			imageUri = getIntent().getStringExtra("imageUri");
		}



		emailVerificationRunnable = new Runnable() {
			@Override
			public void run() {
				checkEmailVerification();
				handler.postDelayed(this, CHECK_INTERVAL);
			}
		};

		handler.post(emailVerificationRunnable);

		final Button btnVerify = findViewById(R.id.btnVerify);
		startCountDownTimer(btnVerify);
		
		btnVerify.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				sendVerificationMessage();
				startCountDownTimer(btnVerify);
			}
		});
		
	}

	private void checkEmailVerification() {
		FirebaseUser user = auth.getCurrentUser();
		if (user != null) {
			user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						if (user.isEmailVerified()) {
							user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
								@Override
								public void onComplete(@NonNull Task<GetTokenResult> task) {
									if (task.isSuccessful()) {
										// Token has been successfully refreshed
										handler.removeCallbacks(emailVerificationRunnable);
										addUserToDB();
										uploadProfileImage();
										if (parent){
											redirectToLogin();
										}
										else {
											Intent intent = new Intent(AccountVerificationActivity.this, PermissionsActivity.class);
											startActivity(intent);
											finish();
										}
									} else {
										// Handle the error
										Log.e("TokenRefresh", "Token refresh failed: " + task.getException().getMessage());
									}
								}
							});
						}
					} else {
						// Handle the error
						Log.e("UserReload", "User reload failed: " + task.getException().getMessage());
					}
				}
			});
		}
	}

	private void addUserToDB() {
		String email;
		String name;

		if (googleAuth) {
			email = auth.getCurrentUser().getEmail();
			name = auth.getCurrentUser().getDisplayName();
		} else {
			email = signUpEmail;
			name = signUpName;
		}
		Log.i(TAG, "signUpRoutine: UID: " + uid);

		if (parent) {
			Parent p = new Parent(name, email);
			databaseReference.child("parents").child(uid).setValue(p);
			databaseReference.child("parentsList").child(uid).child("email").setValue(email)
			.addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful()) {
						Log.i(TAG, "Email added successfully to parentsList.");
					} else {
						Log.e(TAG, "Failed to add email to parentsList.", task.getException());
					}
				}
			});

			Log.i(TAG, "Setelah parentsList");
		} else {
			Child c = new Child(name, email, parentEmail);
			databaseReference.child("childs").child(uid).setValue(c);
		}
	}

	private void uploadProfileImage() {
		if (googleAuth) {
			imageUri = auth.getCurrentUser().getPhotoUrl().toString();
			if (parent)
				databaseReference.child("parents").child(uid).child("profileImage").setValue(imageUri);
			else
				databaseReference.child("childs").child(uid).child("profileImage").setValue(imageUri);

		} else if (!googleAuth) {
			final StorageReference profileImageStorageReference = storageReference.child(uid + "_profileImage");
			profileImageStorageReference.putFile(Uri.parse(imageUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					profileImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
						@Override
						public void onSuccess(Uri uri) {
							if (uri != null) {
								if (parent)
									databaseReference.child("parents").child(uid).child("profileImage").setValue(uri.toString());
								else
									databaseReference.child("childs").child(uid).child("profileImage").setValue(uri.toString());
							}
//							Toast.makeText(SignUpActivity.this, getString(R.string.image_uploaded_successfully), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Toast.makeText(AccountVerificationActivity.this, getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private void startCountDownTimer(final Button btnVerify) {
		btnVerify.setEnabled(false);
		btnVerify.setClickable(false);
		new CountDownTimer(60000, 1000) {
			@Override
			public void onTick(long l) {
				btnVerify.setText(String.valueOf(l / 1000));
			}
			
			@Override
			public void onFinish() {
				btnVerify.setEnabled(true);
				btnVerify.setClickable(true);
				btnVerify.setText(R.string.resend_verification_email);
				
			}
		}.start();
	}
	
	private void sendVerificationMessage() {
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		FirebaseAuth.getInstance().setLanguageCode(LocaleUtils.getAppLanguage());
		user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				if (task.isSuccessful())
					Toast.makeText(AccountVerificationActivity.this, getString(R.string.verification_email_sent_it_may_be_within_your_drafts), Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onBackPressed() {
	}

	private void redirectToLogin() {
		Intent intent = new Intent(AccountVerificationActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(emailVerificationRunnable);
	}
}
