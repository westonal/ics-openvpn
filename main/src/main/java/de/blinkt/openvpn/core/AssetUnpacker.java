package de.blinkt.openvpn.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Unpacks assets matching the supplied extension
 * Created by aevans on 2017-07-08.
 */
class AssetUnpacker {

    private final String TAG = "Assets";

    public static class Builder {

        private final Context context;
        private String extension;

        public Builder(final Context context) {
            this.context = context;
        }

        Builder filesOfExtension(final String extension) {
            this.extension = extension;
            return this;
        }

        public AssetUnpacker build() {
            return new AssetUnpacker(this);
        }
    }

    private final AssetManager assetManager;
    private final String withExpansion;
    private final File externalFilesDir;

    private AssetUnpacker(final Builder builder) {
        assetManager = builder.context.getAssets();
        externalFilesDir = builder.context.getExternalFilesDir(null);
        withExpansion = builder.extension;
    }

    public void unpack() {
        Log.d(TAG, "Unpacking");
        try {
            final String[] files = assetManager.list("");
            if (files == null || files.length == 0) {
                Log.e(TAG, "No assets found (null)");
                return;
            }
            for (final String filename : files) {
                if (filename.endsWith(withExpansion)) {
                    Log.d(TAG, "Asset " + filename);
                    unpackAsset(filename);
                }
            }
        } catch (final IOException e) {
            Log.e(TAG, "Failed to get asset file list.", e);
        }
    }

    private void unpackAsset(final String filename) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            final File outFile = new File(externalFilesDir, filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            Log.d(TAG, "written: " + filename + " to: " + outFile);
        } catch (final IOException e) {
            Log.e(TAG, "Failed to copy asset file: " + filename, e);
        } finally {
            closeStream(in);
            closeStream(out);
        }
    }

    private void closeStream(final Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (final IOException e) {
            Log.e(TAG, "Error closing stream", e);
        }
    }

    private static void copyFile(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
