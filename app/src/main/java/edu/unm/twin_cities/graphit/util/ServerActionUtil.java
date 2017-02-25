package edu.unm.twin_cities.graphit.util;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.google.common.collect.Lists;

import org.apache.commons.codec.binary.Hex;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import edu.umn.twin_cities.ErrorCode;
import edu.umn.twin_cities.FileAdapter;
import edu.umn.twin_cities.ServerAction;
import edu.unm.twin_cities.graphit.fragments.FileBrowserFragment;

/**
 * Created by aman on 30/11/15.
 */
public class ServerActionUtil {

    private final String TAG = this.getClass().getSimpleName();
    private ConnectionResourceBundle connectionResourceBundle;

    public ServerActionUtil(ConnectionResourceBundle connectionResourceBundle) {
        this.connectionResourceBundle = connectionResourceBundle;
    }

    public List<FileBrowserFragment.FileInfo> listFiles(String path) throws ClassNotFoundException, IOException {
        try {
            ObjectOutputStream objectOutputStream = connectionResourceBundle.getObjectOutputStream();
            ObjectInputStream objectInputStream = connectionResourceBundle.getObjectInputStream();

            objectOutputStream.writeObject(ServerAction.LIST_FILES_IN_DIR);
            objectOutputStream.writeObject(path);
            objectOutputStream.flush();
            Object object = objectInputStream.readObject();
            FileAdapter[] files = null;
            if (object instanceof ErrorCode) {
                ErrorCode errorCode = (ErrorCode) object;
                throw new IllegalArgumentException(errorCode.getErrorMsg());
            } else if (object instanceof FileAdapter[]) {
                files = (FileAdapter[]) object;
            } else {
                throw new IllegalStateException("Unrecognized object sent by the server.");
            }
            List<FileBrowserFragment.FileInfo> fileInfo = Lists.newArrayListWithCapacity(files.length);
            for (FileAdapter fileAdapter : files) {
                fileInfo.add(new FileBrowserFragment.FileInfo(fileAdapter));
            }
            return fileInfo;
        } catch (EOFException eofe) {
            /**Excpected when the remote closes the stream and there is nothing more to read.
             * Igonre this exception.**/
        }
        return Lists.newArrayList();
    }

    public List<Measurement<Long, Float>> transferFile(String path) throws IOException, ClassNotFoundException {
        try {
            ObjectOutputStream objectOutputStream = connectionResourceBundle.getObjectOutputStream();
            ObjectInputStream objectInputStream = connectionResourceBundle.getObjectInputStream();

            objectOutputStream.writeObject(ServerAction.TRANSFER_FILE);
            objectOutputStream.writeObject(path);
            objectOutputStream.writeObject(BluetoothAdapter.getDefaultAdapter().getAddress());
            objectOutputStream.flush();
            Object object = objectInputStream.readObject();
            String receivedMd5Sum;
            if (object instanceof ErrorCode) {
                ErrorCode errorCode = (ErrorCode) object;
                throw new IllegalArgumentException(errorCode.getErrorMsg());
            } else if (object instanceof String) {
                receivedMd5Sum = (String) object;
            } else {
                throw new IllegalStateException("Unrecognized object sent by server");
            }

            byte[] fileContents;
            object = objectInputStream.readObject();
            if (object instanceof ErrorCode) {
                //TODO: test this bit.
                ErrorCode errorCode = (ErrorCode) object;
                throw new IllegalArgumentException(errorCode.getErrorMsg());
            } else if (object.getClass().isArray()
                    && byte.class.isAssignableFrom(object.getClass().getComponentType())) {
                fileContents = (byte[]) object;
            } else {
                throw new IllegalStateException("Unrecognized object sent by server");
            }

            //inefficient as input is parsed twice but fail fast.
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            String calculatedMd5Sum = new String(Hex.encodeHex(messageDigest.digest(fileContents)));
            if (!calculatedMd5Sum.equals(receivedMd5Sum)) {
                throw new IllegalStateException("Checksum verification failed. Please download again");
            }

            if (fileContents.length != 0) {
                FileParser parser = new FileParserImpl();
                return parser.parseAndPrepareReadingRecords(fileContents,
                        connectionResourceBundle.getResourceIdentifier());
            } else {
                return Lists.newArrayList();
            }
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("Could not get Algorithm to calcuate checksum");
        } catch (EOFException eofe) {
            /**Excpected when the remote closes the stream and there is nothing more to read.
             * Igonre this exception.**/
        }
        return null;         //TODO: see if there can be a case in which after exception you have some meaningful bytes in the buffer.
    }

    public boolean ping() {
        try {
            ObjectOutputStream objectOutputStream = connectionResourceBundle.getObjectOutputStream();
            ObjectInputStream objectInputStream = connectionResourceBundle.getObjectInputStream();
            objectOutputStream.writeObject(ServerAction.PING);
            objectOutputStream.flush();
            objectInputStream.readObject(); //ignore what is returned,
            return true;    //if the device listens, it means it is valis server.
        } catch (IOException e) {
            //We do not speak the same language.
            return false;
        } catch (ClassNotFoundException cnfe) {
            Log.e(TAG, "Class mismatch between client and server.", cnfe);
            return false;
        }
    }
}
