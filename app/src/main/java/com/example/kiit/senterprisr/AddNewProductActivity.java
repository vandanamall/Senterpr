package com.example.kiit.senterprisr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.kiit.senterprisr.model.Products;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddNewProductActivity extends AppCompatActivity {
private String SellerName,CategoryName,Description,Price,Pname,Stock,savecurrentdate,savecurrenttime;
private Button AddNewProduct;
private EditText InputProductName,InputProductPrice,InputProductDescription,InputProductQuantity;
private ImageView InputProductImage;
private static final int GalleryPick=1;
private Uri ImageUri;
private String productrandomkey,downloadImageUrl;
private StorageReference ProductImagesRef;
private DatabaseReference ProductRef,CategoryRef,TotalCategory;

    private ProgressDialog lodingbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_product);
        SellerName=getIntent().getExtras().get("category").toString();
        ProductImagesRef= FirebaseStorage.getInstance().getReference().child("Product image");
        ProductRef=FirebaseDatabase.getInstance().getReference().child("Products");
        CategoryRef=FirebaseDatabase.getInstance().getReference().child("Categories");
        TotalCategory=FirebaseDatabase.getInstance().getReference().child("TotalCategory");
        lodingbar=new ProgressDialog(this);

        AddNewProduct=(Button)findViewById(R.id.add_new_product);
        InputProductImage=(ImageView)findViewById(R.id.select_product_image);
        InputProductName=(EditText)findViewById(R.id.productname);
        InputProductQuantity=(EditText)findViewById(R.id.productquantity);
        InputProductDescription=(EditText)findViewById(R.id.productdescription);
        InputProductPrice=(EditText)findViewById(R.id.productprice);

        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        AddNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });


        Spinner dynamicSpinner = (Spinner) findViewById(R.id.categories);

        String[] items = new String[] { "Branded Top Article","Staples","Vegetable and fruit","Cleaning Material","Fast Food-Instant or Ready Food","Bathing Soap","Shaving Products","Disposable","Dental or Oral Care","Tea or Coffee","Farshan or Chiki or Mithai","Hair Care","Home Care", "Dairy & Bakery","HouseHold","Beverages","Cosmetics","Medicine","Insecticides","Talcum Powder","Choclate or Confectionery","Baby Products","Jam or Jelly or Ketchup or Syrup","Sanitary Napkins or Diapers","Chips or Upvas","Crockery","Kitchenware","Home Appliances"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, items);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                CategoryName=(String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
    private void OpenGallery(){
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri=data.getData();
            InputProductImage.setImageURI(ImageUri);
        }
    }
    private void ValidateProductData()
    {
        Description=InputProductDescription.getText().toString();
        Price=InputProductPrice.getText().toString();
        Pname=InputProductName.getText().toString();
        Stock=InputProductQuantity.getText().toString();
        if(ImageUri==null || TextUtils.isEmpty(Description)|| TextUtils.isEmpty(Pname) || TextUtils.isEmpty(Price)|| TextUtils.isEmpty(Stock))
        {
            Toast.makeText(this,"Fill every feild",Toast.LENGTH_SHORT).show();
        }
        else
        {
            StoreProductInformation();
        }
    }




    private void StoreProductInformation() {
        lodingbar.setTitle("Adding New Product");
        lodingbar.setMessage("Please wait while we are adding the product");
        lodingbar.setCanceledOnTouchOutside(false);
        lodingbar.show();

        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        savecurrentdate=currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm:ss a");
        savecurrenttime=currentTime.format(calendar.getTime());
        productrandomkey=savecurrentdate+savecurrenttime;
        final StorageReference filepath=ProductImagesRef.child(ImageUri.getLastPathSegment()+productrandomkey+".jpg");
        final UploadTask uploadTask=filepath.putFile(ImageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message=e.toString();
                Toast.makeText(AddNewProductActivity.this,"Error",Toast.LENGTH_SHORT).show();
                lodingbar.dismiss();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddNewProductActivity.this,"Image uploaded successfully",Toast.LENGTH_SHORT).show();
                Task<Uri>urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();


                        }
                        downloadImageUrl=filepath.getDownloadUrl().toString();
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            downloadImageUrl=task.getResult().toString();
                           SaveProductInfotoDatabase();
                        }
                    }
                });
            }
        });
    }
    private void SaveProductInfotoDatabase()
    {
        HashMap<String,Object> productMap=new HashMap<>();
        productMap.put("pid",productrandomkey);
        productMap.put("seller",SellerName);
        productMap.put("date",savecurrentdate);
        productMap.put("time",savecurrenttime);
        productMap.put("description",Description);
        productMap.put("image",downloadImageUrl);
        productMap.put("category",CategoryName);
        productMap.put("price",Price);
        productMap.put("stock",Stock);
        productMap.put("name",Pname);
        ProductRef.child(Pname).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Intent intent = new Intent(AddNewProductActivity.this,AdminCategoryActivity.class);
                            startActivity(intent);
                            lodingbar.dismiss();
                            Toast.makeText(AddNewProductActivity.this,"Product added suceesfully",Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            lodingbar.dismiss();
                            String message=task.getException().toString();

                            Toast.makeText(AddNewProductActivity.this,"Error while uploading",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
        HashMap<String,Object> categoryM=new HashMap<>();
        categoryM.put("category",CategoryName);
        HashMap<String,Object> categoryMap=new HashMap<>();
        categoryMap.put("pid",productrandomkey);
        categoryMap.put("seller",SellerName);
        categoryMap.put("date",savecurrentdate);
        categoryMap.put("time",savecurrenttime);
        categoryMap.put("description",Description);
        categoryMap.put("image",downloadImageUrl);
        categoryMap.put("category",CategoryName);
        categoryMap.put("price",Price);
        categoryMap.put("name",Pname);
        categoryMap.put("stock",Stock);
        TotalCategory.child(CategoryName).updateChildren(categoryMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Intent intent = new Intent(AddNewProductActivity.this,AdminCategoryActivity.class);
                    startActivity(intent);
                    lodingbar.dismiss();
                    Toast.makeText(AddNewProductActivity.this,"Product added suceesfully",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    lodingbar.dismiss();
                    String message=task.getException().toString();

                    Toast.makeText(AddNewProductActivity.this,"Error while uploading",Toast.LENGTH_SHORT).show();

                }
            }
        });
        TotalCategory.child(CategoryName).child(Pname).updateChildren(categoryMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Intent intent = new Intent(AddNewProductActivity.this,AdminCategoryActivity.class);
                    startActivity(intent);
                    lodingbar.dismiss();
                    Toast.makeText(AddNewProductActivity.this,"Product added suceesfully",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    lodingbar.dismiss();
                    String message=task.getException().toString();

                    Toast.makeText(AddNewProductActivity.this,"Error while uploading",Toast.LENGTH_SHORT).show();

                }
            }
        });

        CategoryRef.child(CategoryName).child(Pname).updateChildren(categoryMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Intent intent = new Intent(AddNewProductActivity.this,AdminCategoryActivity.class);
                            startActivity(intent);
                            lodingbar.dismiss();
                            Toast.makeText(AddNewProductActivity.this,"Product added suceesfully",Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            lodingbar.dismiss();
                            String message=task.getException().toString();

                            Toast.makeText(AddNewProductActivity.this,"Error while uploading",Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }
}
