
package server.http.android.androidhttpserver.server;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import server.http.android.androidhttpserver.server.interfaces.ServerInterface;

/**
 * Created by andrei on 7/30/15.
 */
public class MyServer extends NanoHTTPD {
    private final static int PORT = 8080;
    private final String TAG = MyServer.class.getSimpleName();
    private ServerInterface serverInterface;
    private String lastPhotoPath;
    private boolean waitPhoto = true;

    public void setServerInterface(ServerInterface serverInterface) {
        this.serverInterface = serverInterface;
    }

    public MyServer() throws IOException {
        super(PORT);
        start();
        System.out.println("\nRunning! Point your browers to http://localhost:8080/ \n");
    }


    @Override
    public Response serve(IHTTPSession session) {
//        String msg = "<html><body><h1>Hello server robotino</h1>\n";
//        msg += "<p>We serve " + session.getUri() + " !</p>";
//        return newFixedLengthResponse( msg + "</body></html>\n" );
        File file;
        long size = 0;
        FileInputStream fis = null;
        serverInterface.newConnection();
        while (waitPhoto) {
         // just wait for Android take a picture
        }

        if (!lastPhotoPath.isEmpty()) {
            try {

                file = new File(lastPhotoPath);

                if (file.exists()) {
                    fis = new FileInputStream(file);
                    Log.d(TAG, "File exists: " + file.getAbsolutePath());
                } else {
                    Log.d(TAG, "File doesn't exist!");
                }
                size = file.length();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

        waitPhoto = true;
        return new NanoHTTPD.Response(Response.Status.OK, "image/jpeg", fis, size);
    }

    public void newImage(String lastPhotoPath) {
        this.lastPhotoPath = lastPhotoPath;
        waitPhoto = false;
    }
}
