import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;

/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
* Uploads a file to Google Cloud Storage to the bucket specified in the
* BUCKET_NAME environment variable, appending a timestamp to end of the
* uploaded filename.
* 
* @throws IOException
* @throws FileNotFoundException
*/
@SuppressWarnings("static-access")
public class GoogleCloudStorage {

	Variables variables = new Variables();
	
	public GoogleCloudStorage() {		
		setDefaultStorageCredentials();
	}
	
	private static Storage storage = null;	
	private static Credentials credentials = null;
	//Project Id can be obtained from your GCP console dashboard.
	private static String projectId = "Your project Id";
	
	//Create the bucket using the REST API or manually using the Google Cloud Storage Browser Interface.
	private static String bucketName = "Your bucket name";
	
	//Following 4 parameters can be obtained from the Private Key file.
	//Client Id will usually be a numeric string
	private static String clientId = "Your Client Id From the Key File.";
	
	//Client Email Id is the email Id generated when you create the service account. This will be in the format of: *.iam.gserviceaccount.com
	private static String clientEmailId = "Your Client Email Id From the Key File.";
	
	//Private key can be obtained from the key file. This will be a very long string within the file. Paste the entire string here.
	private static String privateKey = "Your Private Key From the Key File.";
	
	//Private Key Id can be obtained from the key file. This is ususally a numeric string.
	private static String privateKeyId = "Your Client Id From the Key File.";
	
	/**
	 * This method sets the storage credentials for the default storage object. 
	 */
	private void setDefaultStorageCredentials() {
		try {			
			credentials = ServiceAccountCredentials.fromPkcs8(clientId, clientEmailId, privateKey, privateKeyId, null);			
			storage = StorageOptions.newBuilder()
					.setCredentials(credentials)
					.setProjectId(projectId).build().getService();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Uploads a given file to Google Cloud Storage.
	 * 
	 * @param filePath The desired file path for the file to be uploaded. File path should be absolute path and should include folders, sub-folders, and file name
	 * @param file The file to be uploaded in byte array format
	 * @return true if the file has been successfully uploaded; false otherwise
	 */
	public boolean uploadFile(String filePath, byte[] file) {
		try {
			setDefaultStorageCredentials();
			storage.create(BlobInfo.newBuilder(bucketName, filePath).build(),
					new ByteArrayInputStream(file));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Downloads a given file from Google Cloud Storage.
	 * 
	 * @param filePath The desired file path for the file to be downloaded. File path should be absolute path and should include folders, sub-folders, and file name
	 * @return the downloaded file in byte array format
	 */
	public byte[] downloadFile(String filePath) throws FileNotFoundException, IOException {
		setDefaultStorageCredentials();		
		return storage.get(bucketName).get(filePath).getContent();
	}
	
	/**
	 * Generates a temporary link to a file in Google Cloud Storage. 
	 * This will allow temporary access to the file without actually exposing the file.
	 * Users accessing this link need not sign in using any credentials.
	 * <p>
	 * After the expiry time, this link will be expired and general public cannot access the file.
	 * 
	 * @param filePath The desired file path for the file to be uploaded. File path should be absolute path and should include folders, sub-folders, and file name
	 * @return String containing the signed url for the file specified.
	 */
	public String getTemporaryFileLink(String filePath) throws Exception{
		setDefaultStorageCredentials();		
		Blob blob = storage.get(bucketName).get(filePath);		
		String blobName = blob.getName();	    
	    URL signedUrl = storage.signUrl(BlobInfo.newBuilder(bucketName, blobName).build(), 5,TimeUnit.MINUTES);
		return signedUrl.toExternalForm();
	}
	
	/**
	 * Deletes a given file from Google Cloud Storage.
	 * 
	 * @param filePath The desired file path for the file to be deleted. File path should be absolute path and should include folders, sub-folders, and file name
	 * @return true if the file has been successfully deleted; false otherwise
	 */
	public boolean deleteFile(String filePath){
		setDefaultStorageCredentials();						
		return storage.delete(storage.get(bucketName).get(filePath).getBlobId());		
	}		
	    
}
