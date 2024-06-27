package com.mzhtech.sismonakdev.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.dialogfragments.ConfirmationDialogFragment;
import com.mzhtech.sismonakdev.dialogfragments.GoogleChildSignUpDialogFragment;
import com.mzhtech.sismonakdev.dialogfragments.InformationDialogFragment;
import com.mzhtech.sismonakdev.dialogfragments.LoadingDialogFragment;
import com.mzhtech.sismonakdev.interfaces.OnConfirmationListener;
import com.mzhtech.sismonakdev.interfaces.OnGoogleChildSignUp;
import com.mzhtech.sismonakdev.models.Child;
import com.mzhtech.sismonakdev.models.Parent;
import com.mzhtech.sismonakdev.utils.Constant;
import com.mzhtech.sismonakdev.utils.SharedPrefsUtils;
import com.mzhtech.sismonakdev.utils.Validators;

import de.hdodenhof.circleimageview.CircleImageView;


public class SignUpActivity extends AppCompatActivity implements OnConfirmationListener, OnGoogleChildSignUp {
	private static final String TAG = "SignUpActivityTAG";
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private FirebaseStorage firebaseStorage;
	private StorageReference storageReference;
	private Uri imageUri;
	private FirebaseAuth auth;
	private EditText txtSignUpEmail;
	private EditText txtParentEmail;
	private EditText txtSignUpPassword;
	private EditText txtSignUpName;
	private Button btnSignUp;
	private Button btnSignUpWithGoogle;
	private CircleImageView imgProfile;
	private FragmentManager fragmentManager;
	private String uid;
	private String userEmail;
	private String parentEmail;
	private boolean googleAuth = false;
	private boolean parent = true;
	private boolean validParent = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		fragmentManager = getSupportFragmentManager();
		
		Intent intent = getIntent();
		parent = intent.getBooleanExtra(Constant.PARENT_SIGN_UP, true);
		
		auth = FirebaseAuth.getInstance();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("users");
		firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference("profileImages");
		
		txtSignUpEmail = findViewById(R.id.txtSignUpEmail);
		txtParentEmail = findViewById(R.id.txtParentEmail);
		txtParentEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			
			}
			
			@Override
			public void afterTextChanged(Editable editable) {
				Query query = databaseReference.child("parentsList").orderByChild("email").equalTo(txtParentEmail.getText().toString().toLowerCase());
				query.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						validParent = dataSnapshot.exists();
						Log.i(TAG, "onDataChange: " + validParent);
					}
					
					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
					
					}
				});
			}
		});
		if (!parent) {
			txtParentEmail.setVisibility(View.VISIBLE);
		}

		txtSignUpPassword = findViewById(R.id.txtSignUpPassword);
		txtSignUpName = findViewById(R.id.txtSignUpName);
		
		imgProfile = findViewById(R.id.imgProfile);
		imgProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(TAG, "Sampai onClick imgProfile");
				openFileChooser();
			}
		});
		btnSignUp = findViewById(R.id.btnSignUp);
		btnSignUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String email = txtSignUpEmail.getText().toString().toLowerCase();
				String password = txtSignUpPassword.getText().toString();
				Log.i(TAG, "passwordUser: " + password);
				signUp(email, password);
			}
		});
		
		btnSignUpWithGoogle = findViewById(R.id.btnSignUpWithGoogle);
		btnSignUpWithGoogle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signInWithGoogle();
			}
		});
		
	}
	
	
	private void signUp(String email, String password) {
		Log.i(TAG, "emailUser: " + email + " " + "passwordUser: " + password);
		if (isValid()) {
			final LoadingDialogFragment loadingDialogFragment = new LoadingDialogFragment();
			startLoadingFragment(loadingDialogFragment);
			auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					stopLoadingFragment(loadingDialogFragment);
					if (task.isSuccessful()) {
						signUpRoutine(txtParentEmail.getText().toString().toLowerCase());
					} else {
						String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
						switch (errorCode) {
							case "ERROR_INVALID_EMAIL":
								txtSignUpEmail.setError(getString(R.string.enter_valid_email));
								break;
							case "ERROR_EMAIL_ALREADY_IN_USE":
								txtSignUpEmail.setError(getString(R.string.email_is_already_in_use));
								break;
							case "ERROR_WEAK_PASSWORD":
								txtSignUpPassword.setError(getString(R.string.weak_password));
								break;
							default:
								Toast.makeText(SignUpActivity.this, getString(R.string.sign_up_falied), Toast.LENGTH_SHORT).show();
							
						}
						
					}
				}
			});
		}
	}
	
	private void signUpRoutine(String userEmail) {
		uid = auth.getCurrentUser().getUid();
		Log.i(TAG, "signUpRoutine: UID: " + uid);
//		addUserToDB(parentEmail, parent);
//		uploadProfileImage(parent);
		startAccountVerificationActivity(userEmail, uid, parentEmail);
	}

	private void signUpRoutineChild(String parentEmail) {
		uid = auth.getCurrentUser().getUid();
		userEmail = auth.getCurrentUser().getEmail();
		Log.i(TAG, "signUpRoutineChild: UID: " + uid);
//		addUserToDB(parentEmail, parent);
//		uploadProfileImage(parent);
		startAccountVerificationActivity(userEmail, uid, parentEmail);
	}
	
	private void startAccountVerificationActivity(String userEmail, String uid, String parentEmail) {
		Intent intent = new Intent(this, AccountVerificationActivity.class);
//		intent.putExtra("parentEmail",parentEmail);
//		intent.putExtra("isParent", parent);
//		intent.putExtra("googleAuth", googleAuth);
//		intent.putExtra("signUpEmail", txtSignUpEmail.getText().toString().toLowerCase());
//		intent.putExtra("signUpName", txtSignUpName.getText().toString().replaceAll("\\s+$", ""));
//		intent.putExtra("uid", uid);
		if (!googleAuth){
			intent.putExtra("imageUri",imageUri.toString());
		} else {
			intent.putExtra("imageUri",auth.getCurrentUser().getPhotoUrl().toString());
		}
		SharedPrefsUtils.setStringPreference(this, "parentEmail", parentEmail);
		SharedPrefsUtils.setStringPreference(this, "userEmail", userEmail);
		SharedPrefsUtils.setBooleanPreference(this, "isParent", parent);
		SharedPrefsUtils.setBooleanPreference(this, "googleAuth", googleAuth);
		SharedPrefsUtils.setStringPreference(this, "signUpEmail", txtSignUpEmail.getText().toString().toLowerCase());
		SharedPrefsUtils.setStringPreference(this, "signUpName", txtSignUpName.getText().toString().replaceAll("\\s+$", ""));
		SharedPrefsUtils.setStringPreference(this, "uid", uid);
		startActivity(intent);
	}
	
//	private void uploadProfileImage(final boolean parent) {
//		if (googleAuth && imageUri == null) {
//			imageUri = auth.getCurrentUser().getPhotoUrl();
//			if (parent)
//				databaseReference.child("parents").child(uid).child("profileImage").setValue(imageUri.toString());
//			else
//				databaseReference.child("childs").child(uid).child("profileImage").setValue(imageUri.toString());
//
//		} else if (!googleAuth) {
//			final StorageReference profileImageStorageReference = storageReference.child(uid + "_profileImage");
//			profileImageStorageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//				@Override
//				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//					profileImageStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//						@Override
//						public void onSuccess(Uri uri) {
//							if (uri != null) {
//								if (parent)
//									databaseReference.child("parents").child(uid).child("profileImage").setValue(uri.toString());
//								else
//									databaseReference.child("childs").child(uid).child("profileImage").setValue(uri.toString());
//							}
////							Toast.makeText(SignUpActivity.this, getString(R.string.image_uploaded_successfully), Toast.LENGTH_SHORT).show();
//						}
//					});
//				}
//			}).addOnFailureListener(new OnFailureListener() {
//				@Override
//				public void onFailure(@NonNull Exception e) {
//					Toast.makeText(SignUpActivity.this, getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show();
//				}
//			});
//		}
//	}
	
//	private void addUserToDB(String parentEmail, boolean parent) {
//		String email;
//		String name;
//		if (googleAuth) {
//			email = auth.getCurrentUser().getEmail();
//			name = auth.getCurrentUser().getDisplayName();
//		} else {
//			email = txtSignUpEmail.getText().toString().toLowerCase();
//			name = txtSignUpName.getText().toString().replaceAll("\\s+$", "");
//		}
//		Log.i(TAG, "signUpRoutine: UID: " + uid);
//
//		if (parent) {
//			Parent p = new Parent(name, email);
//			databaseReference.child("parents").child(uid).setValue(p);
//		} else {
//			Child c = new Child(name, email, parentEmail);
//			databaseReference.child("childs").child(uid).setValue(c);
//		}
//	}
	
	private void startLoadingFragment(LoadingDialogFragment loadingDialogFragment) {
		loadingDialogFragment.setCancelable(false);
		loadingDialogFragment.show(fragmentManager, Constant.LOADING_FRAGMENT);
	}
	
	private void stopLoadingFragment(LoadingDialogFragment loadingDialogFragment) {
		loadingDialogFragment.dismiss();
	}
	
	private boolean isValid() {
		if (!Validators.isValidName(txtSignUpName.getText().toString())) {
			txtSignUpName.setError(getString(R.string.name_validation));
			txtSignUpName.requestFocus();
			return false;
		}
		
		if (!Validators.isValidEmail(txtSignUpEmail.getText().toString())) {
			txtSignUpEmail.setError(getString(R.string.enter_valid_email));
			txtSignUpEmail.requestFocus();
			return false;
		}
		
		if (!parent) {
			if (!Validators.isValidEmail(txtParentEmail.getText().toString().toLowerCase()) || !validParent) {
				txtParentEmail.setError(getString(R.string.this_email_isnt_registered_as_parent));
				txtParentEmail.requestFocus();
				Log.i("TAG", String.valueOf(!Validators.isValidEmail(txtParentEmail.getText().toString().toLowerCase())));

				return false;
			}
		}
		
		if (!Validators.isValidPassword(txtSignUpPassword.getText().toString())) {
			txtSignUpPassword.setError(getString(R.string.enter_valid_password));
			txtSignUpPassword.requestFocus();
			return false;
		}
		
		
		if (!Validators.isValidImageURI(imageUri)) {
			Log.i(TAG, "Sampai isValidImageURI");
			ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
			Bundle bundle = new Bundle();
			bundle.putString(Constant.CONFIRMATION_MESSAGE, getString(R.string.would_you_love_to_add_a_profile_image));
			confirmationDialogFragment.setArguments(bundle);
			confirmationDialogFragment.setCancelable(false);
			confirmationDialogFragment.show(fragmentManager, Constant.CONFIRMATION_FRAGMENT_TAG);
			return false;
		}
		
		if (!Validators.isInternetAvailable(this)) {
			startInformationDialogFragment();
			return false;
		}
		
		return true;
	}
	
	private void startInformationDialogFragment() {
		InformationDialogFragment informationDialogFragment = new InformationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.INFORMATION_MESSAGE, getResources().getString(R.string.you_re_offline_ncheck_your_connection_and_try_again));
		informationDialogFragment.setArguments(bundle);
		informationDialogFragment.setCancelable(false);
		informationDialogFragment.show(getSupportFragmentManager(), Constant.INFORMATION_DIALOG_FRAGMENT_TAG);
	}
	
	private void openFileChooser() {
		Log.i(TAG, "Sampai openFileChooser");
		requestStoragePermission();
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST);
		Log.i(TAG, "setelah start activity for result");
	}

	private void requestStoragePermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 102);
		}
	}
	
	private void signInWithGoogle() {
		if (Validators.isInternetAvailable(this)) {
			GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.id)).requestEmail().build();
			GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
			Intent signInIntent = googleSignInClient.getSignInIntent();
			startActivityForResult(signInIntent, Constant.RC_SIGN_IN);
		} else startInformationDialogFragment();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "Sampai onActivityResult");
		if (requestCode == Constant.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			imageUri = data.getData();
			imgProfile.setImageURI(imageUri);
		}
		
		if (requestCode == Constant.RC_SIGN_IN) {
			Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
			Log.d(TAG, "Sampai rc_sign_in");
			try {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = task.getResult(ApiException.class);
				firebaseAuthWithGoogle(account);
			} catch (ApiException e) {
				// Google Sign In failed, update UI appropriately
				Toast.makeText(this, getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
				Log.i(TAG, "Google sign in failed", e);
			}
		}
	}
	
	private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
		Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
		AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
		auth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {
					Log.i(TAG, "onComplete: Authentication Succeeded");
					//Toast.makeText(SignUpActivity.this, getString(R.string.authentication_succeeded), Toast.LENGTH_SHORT).show();
					FirebaseUser user = auth.getCurrentUser();
					String userEmail = user.getEmail();
					uid = user.getUid();
					googleAuth = true;
					if (!parent) {
						getParentEmail();
					}
					startAccountVerificationActivity(userEmail, uid, parentEmail);

				} else {
					Log.w(TAG, "onComplete: Authentication Failed", task.getException());
					Toast.makeText(SignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	private void getParentEmail() {
		GoogleChildSignUpDialogFragment googleChildSignUpDialogFragment = new GoogleChildSignUpDialogFragment();
		googleChildSignUpDialogFragment.setCancelable(false);
		googleChildSignUpDialogFragment.show(fragmentManager, Constant.GOOGLE_CHILD_SIGN_UP);
	}
	
	@Override
	public void onConfirm() {
		Log.i(TAG, "Sampai onConfirm");
		imgProfile.requestFocus();
		openFileChooser();
		Toast.makeText(this, getString(R.string.please_add_a_profile_image), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onConfirmationCancel() {
		imageUri = Uri.parse("android.resource://com.mzhtech.sismonakdev/drawable/ic_default_avatar");
		signUp(txtSignUpEmail.getText().toString().toLowerCase(), txtSignUpPassword.getText().toString());
		Log.i(TAG, "onConfirmationCancel: DONE");
	}
	
	@Override
	public void onModeSelected(String parentEmail) {
		signUpRoutineChild(parentEmail);
	}
	
}
