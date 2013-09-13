package com.cmd.uploadtoserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UploadToServer extends Activity {
	
	TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
        
    String upLoadServerBase = null;
    String upLoadServerUri = null;
    String upLoadServerTargetPath = null;
    
    /**********  File Path *************/
    final String uploadFilePath = "/mnt/sdcard/data/";
    final String uploadFileName = "test.jpg";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_to_server);
		
		// Setup layout
		uploadButton = (Button)findViewById(R.id.uploadButton);
        messageText  = (TextView)findViewById(R.id.messageText);
         
        // Layout values
        messageText.setText("Uploading file path :- '" + uploadFilePath + uploadFileName + "'");
        
        /************* Php script path ****************/
        //upLoadServerUri = "http://www.androidexample.com/media/UploadToServer.php";
        //upLoadServerUri = "http://exorbitance.net/fileup.aspx";
        upLoadServerBase = "http://192.168.96.179:9191/";
        upLoadServerUri = upLoadServerBase + "fileup.aspx";
        upLoadServerTargetPath = "uploads/";
        
        uploadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                 
                dialog = ProgressDialog.show(UploadToServer.this, "", "Uploading file...", true);
                 
                new Thread(new Runnable() {
                        public void run() {
                             runOnUiThread(new Runnable() {
                                    public void run() {
                                        messageText.setText("uploading started.....");
                                    }
                                });                     
                           
                             //uploadFile(uploadFilePath + uploadFileName);
                             UploadFile("/data/" + uploadFileName);
                        }
                      }).start();
                }
        });
	}
	
	public void UploadFile(String sourceFileUri){
		try {
		    // Set your file path here
		    FileInputStream fstrm = new FileInputStream(Environment.getExternalStorageDirectory().toString() + sourceFileUri);
	
		    // Set your server page url (and the file title/description)
		    HttpFileUpload hfu = new HttpFileUpload(upLoadServerUri, upLoadServerTargetPath, "my file title","my file description");
	
		    serverResponseCode = hfu.Send_Now(fstrm);
		    
		    if(serverResponseCode == 200){
	            
	            runOnUiThread(new Runnable() {
	                 public void run() {
	                      
//	                     String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
//	                                   +" http://www.androidexample.com/media/uploads/"
//	                                   +uploadFileName;
	                	 
	                	 String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                 +" " + upLoadServerBase + upLoadServerTargetPath
                                 +uploadFileName;
	                      
	                     messageText.setText(msg);
	                     Toast.makeText(UploadToServer.this, "File Upload Complete.",
	                                  Toast.LENGTH_SHORT).show();
	                 }
	             });               
	        } 
	
		} catch (FileNotFoundException e) {
		    // Error: File not found
		}
	
		dialog.dismiss();
	}
	
	public int uploadFile(String sourceFileUri) {        
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null; 
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        
        String Title = "title of file";
        String Description = "description of file";
         
        if (!sourceFile.isFile()) {
             
             dialog.dismiss();
              
             Log.e("uploadFile", "Source File not exist :"
                                 +uploadFilePath + "" + uploadFileName);
              
             runOnUiThread(new Runnable() {
                 public void run() {
                     messageText.setText("Source File not exist :"
                             +uploadFilePath + "" + uploadFileName);
                 }
             });
              
             return 0;
        }
        else
        {
             try {
                  
                   // open a URL connection to the Servlet
                 FileInputStream fileInputStream = new FileInputStream(sourceFile);
                 URL url = new URL(upLoadServerUri);
                  
                 // Open a HTTP  connection to  the URL
                 conn = (HttpURLConnection) url.openConnection();
                 conn.setDoInput(true); // Allow Inputs
                 conn.setDoOutput(true); // Allow Outputs
                 conn.setUseCaches(false); // Don't use a Cached Copy
                 conn.setRequestMethod("POST");
                 conn.setRequestProperty("Connection", "Keep-Alive");
                 conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                 conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                 //conn.setRequestProperty("uploaded_file", fileName);
                  
                 dos = new DataOutputStream(conn.getOutputStream());
        
                 dos.writeBytes(twoHyphens + boundary + lineEnd);
                 dos.writeBytes("Content-Disposition: form-data; name=\"title\""+ lineEnd);
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(Title);
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(twoHyphens + boundary + lineEnd);
                     
                 dos.writeBytes("Content-Disposition: form-data; name=\"description\""+ lineEnd);
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(Description);
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(twoHyphens + boundary + lineEnd);
                 
                 
                 dos.writeBytes(twoHyphens + boundary + lineEnd);
                 dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename="
                                           + uploadFileName + "" + lineEnd);
                  
                 dos.writeBytes(lineEnd);
        
                 // create a buffer of  maximum size
                 bytesAvailable = fileInputStream.available();
        
                 bufferSize = Math.min(bytesAvailable, maxBufferSize);
                 buffer = new byte[bufferSize];
        
                 // read file and write it into form...
                 bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
                    
                 while (bytesRead > 0) {
                      
                   dos.write(buffer, 0, bufferSize);
                   bytesAvailable = fileInputStream.available();
                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
                   bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
                    
                  }
        
                 // send multipart form data necesssary after file data...
                 dos.writeBytes(lineEnd);
                 dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
        
                 // Responses from the server (code and message)
                 serverResponseCode = conn.getResponseCode();
                 String serverResponseMessage = conn.getResponseMessage();
                   
                 Log.i("uploadFile", "HTTP Response is : "
                         + serverResponseMessage + ": " + serverResponseCode);
                  
                 if(serverResponseCode == 200){
                      
                     runOnUiThread(new Runnable() {
                          public void run() {
                               
                              String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                            +" http://www.androidexample.com/media/uploads/"
                                            +uploadFileName;
                               
                              messageText.setText(msg);
                              Toast.makeText(UploadToServer.this, "File Upload Complete.",
                                           Toast.LENGTH_SHORT).show();
                          }
                      });               
                 }   
                  
                 //close the streams //
                 fileInputStream.close();
                 dos.flush();
                 dos.close();
                   
            } catch (MalformedURLException ex) {
                 
                dialog.dismiss(); 
                ex.printStackTrace();
                 
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Toast.makeText(UploadToServer.this, "MalformedURLException",
                                                            Toast.LENGTH_SHORT).show();
                    }
                });
                 
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
            } catch (Exception e) {
                 
                dialog.dismiss(); 
                e.printStackTrace();
                 
                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : "
                                                 + e.getMessage(), e); 
            }
            dialog.dismiss();      
            return serverResponseCode;
             
         } // End else block
   }

}
