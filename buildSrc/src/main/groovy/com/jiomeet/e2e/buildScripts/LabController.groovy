package com.jiomeet.e2e.buildScripts

import com.ssts.pcloudy.Connector
import com.ssts.pcloudy.dto.file.PDriveFileDTO

class LabController {
    private final String authToken;
    private final Connector connector;
    private final String authenticationKey;
    private final String emailID;

    LabController(def labUrl, def emailID, def authenticationKey) {
        println "getAuthToken: " + authToken
        this.emailID = emailID
        this.authenticationKey = authenticationKey
        if (null == authToken) {
            connector = new Connector(labUrl);
            authToken = connector.authenticateUser(emailID, authenticationKey);
        } else {
            authToken = "authToken already set. Seems like an error: " + authToken
        }
        println "getAuthToken: (after establishing connection)" + authToken
    }

    void uploadAPK(def apkPath) {
        println "uploadAPK: " + apkPath
        File fileToBeUploaded = new File(apkPath);
        PDriveFileDTO alreadyUploadedApp = connector.getAvailableAppIfUploaded(authToken, fileToBeUploaded.getName());
        if (alreadyUploadedApp == null) {
            System.out.println("Uploading Apk: " + fileToBeUploaded.getAbsolutePath());
            PDriveFileDTO uploadedApp = connector.uploadApp(authToken, fileToBeUploaded, true);
            System.out.println("Apk uploaded");
            alreadyUploadedApp = new PDriveFileDTO();
            alreadyUploadedApp.file = uploadedApp.file;
            System.out.println("Apk uploaded");
        } else {
            System.out.println("Apk already present. Not uploading... ");
        }
    }
}
