package com.mzhtech.sismonakdev.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mzhtech.sismonakdev.R;
import com.mzhtech.sismonakdev.dialogfragments.AccountDeleteDialogFragment;
import com.mzhtech.sismonakdev.dialogfragments.ConfirmationDialogFragment;
import com.mzhtech.sismonakdev.dialogfragments.LanguageSelectionDialogFragment;
import com.mzhtech.sismonakdev.dialogfragments.PasswordChangingDialogFragment;
import com.mzhtech.sismonakdev.interfaces.OnConfirmationListener;
import com.mzhtech.sismonakdev.interfaces.OnDeleteAccountListener;
import com.mzhtech.sismonakdev.interfaces.OnLanguageSelectionListener;
import com.mzhtech.sismonakdev.interfaces.OnPasswordChangeListener;
import com.mzhtech.sismonakdev.utils.AccountUtils;
import com.mzhtech.sismonakdev.utils.Constant;
import com.mzhtech.sismonakdev.utils.LocaleUtils;
import com.mzhtech.sismonakdev.utils.SharedPrefsUtils;

public class SettingsActivity extends AppCompatActivity implements OnLanguageSelectionListener, OnConfirmationListener, OnPasswordChangeListener, OnDeleteAccountListener {
	private Button btnLanguageSelection;
	private Button btnLogout;
	private Button btnChangeProfPic;
	private Button btnChangePassword;
	private Button btnDeleteAccount;
	private Button btnAbout;
	private Button btnSendFeedBack;
	private Button btnVisitWebsite;
	private ImageButton btnBack;
	private ImageButton btnSettings;
	private TextView txtTitle;
	private FrameLayout toolbar;
	private FirebaseAuth auth;
	private FirebaseDatabase firebaseDatabase;
	private DatabaseReference databaseReference;
	private FirebaseStorage firebaseStorage;
	private StorageReference storageReference;
	private boolean googleAuth;
	private String imageUri;
	private boolean parent;
	private String uid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		auth = FirebaseAuth.getInstance();
		firebaseDatabase = FirebaseDatabase.getInstance();
		databaseReference = firebaseDatabase.getReference("users");
		firebaseStorage = FirebaseStorage.getInstance();
		storageReference = firebaseStorage.getReference("profileImages");

		googleAuth = SharedPrefsUtils.getBooleanPreference(this, "googleAuth", false);
		parent = SharedPrefsUtils.getBooleanPreference(this, "isParent", true);
		uid = auth.getUid();
		
		toolbar = findViewById(R.id.toolbar);
		btnBack = findViewById(R.id.btnBack);
		btnBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		btnSettings = findViewById(R.id.btnSettings);
		btnSettings.setVisibility(View.GONE);
		txtTitle = findViewById(R.id.txtTitle);
		txtTitle.setText(getString(R.string.settings));
		
		btnLanguageSelection = findViewById(R.id.btnLanguageSelection);
		btnLanguageSelection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectLanguage();
			}
		});
		
		btnLogout = findViewById(R.id.btnLogout);
		btnLogout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				logout();
			}
		});

		btnChangeProfPic = findViewById(R.id.btnChangeProfPic);
		btnChangeProfPic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openFileChooser();
			}
		});
		
		btnChangePassword = findViewById(R.id.btnChangePassword);
		btnChangePassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				changePassword();
			}
		});
		
		
		btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
		btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				deleteAccount();
			}
		});
		
		
		btnAbout = findViewById(R.id.btnAbout);
		btnAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showAbout();
			}
		});

		btnSendFeedBack = findViewById(R.id.btnSendFeedBack);
		btnSendFeedBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				SendFeedBack();
			}
		});
		
		
		btnVisitWebsite = findViewById(R.id.btnVisitWebsite);
		btnVisitWebsite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				visitWebsite();
			}
		});
		
	}
	
	private void showAbout() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	private void SendFeedBack() {
		String body = null;
		try {
			body = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " + Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND + "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("message/rfc822");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"jeprunajah@gmail.com"});
		intent.putExtra(Intent.EXTRA_SUBJECT, "SisMoNak Feedback");
		intent.putExtra(Intent.EXTRA_TEXT, body);
		startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)));
	}
	
	private void visitWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/mzhafranh/TA_Sismonak"));
		startActivity(intent);
	}
	
	private void deleteAccount() {
		AccountDeleteDialogFragment accountDeleteDialogFragment = new AccountDeleteDialogFragment();
		accountDeleteDialogFragment.setCancelable(false);
		accountDeleteDialogFragment.show(getSupportFragmentManager(), Constant.ACCOUNT_DELETE_DIALOG_FRAGMENT_TAG);
	}
	
	private void changePassword() {
		PasswordChangingDialogFragment passwordChangingDialogFragment = new PasswordChangingDialogFragment();
		passwordChangingDialogFragment.setCancelable(false);
		passwordChangingDialogFragment.show(getSupportFragmentManager(), Constant.PASSWORD_CHANGING_DIALOG_FRAGMENT_TAG);
	}
	
	private void logout() {
		ConfirmationDialogFragment confirmationDialogFragment = new ConfirmationDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(Constant.CONFIRMATION_MESSAGE, getString(R.string.do_you_want_to_logout));
		confirmationDialogFragment.setArguments(bundle);
		confirmationDialogFragment.setCancelable(false);
		confirmationDialogFragment.show(getSupportFragmentManager(), Constant.CONFIRMATION_FRAGMENT_TAG);
	}
	
	private void selectLanguage() {
		LanguageSelectionDialogFragment languageSelectionDialogFragment = new LanguageSelectionDialogFragment();
		languageSelectionDialogFragment.setCancelable(false);
		languageSelectionDialogFragment.show(getSupportFragmentManager(), Constant.LANGUAGE_SELECTION_DIALOG_FRAGMENT_TAG);
	}
	
	@Override
	public void onLanguageSelection(String language) {
		String appLanguage = SharedPrefsUtils.getStringPreference(this, Constant.APP_LANGUAGE, "en");
		if (language.equals("English (EN)") && !appLanguage.equals("en")) {
			LocaleUtils.setLocale(this, "en");
		} else if (language.equals("Bahasa Indonesia (ID)") && !appLanguage.equals("in")) {
			LocaleUtils.setLocale(this, "in");
		}
		restartApp();
		
	}
	
	private void restartApp() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		
	}

	private void openFileChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, Constant.PICK_IMAGE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constant.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			imageUri = data.getData().toString();
			uploadProfileImage(imageUri);
		}
	}

	private void uploadProfileImage(String imageUri) {
		if (googleAuth && imageUri == null) {
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
							Toast.makeText(SettingsActivity.this, getString(R.string.image_uploaded_successfully), Toast.LENGTH_SHORT).show();
						}
					});
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Toast.makeText(SettingsActivity.this, getString(R.string.image_upload_error), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	@Override
	public void onConfirm() {
		AccountUtils.logout(this);
	}
	
	@Override
	public void onConfirmationCancel() {
	}
	
	@Override
	public void onPasswordChange(String newPassword) {
		AccountUtils.changePassword(this, newPassword);
		
	}
	
	@Override
	public void onDeleteAccount(String password) {
		AccountUtils.deleteAccount(this, password);
		
	}
}
