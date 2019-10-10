package com.zp.androidx.base.custom;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.zp.androidx.base.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class ImagePickerDialog extends DialogFragment {
    private static final String TAG = "ImagePickerDialog";
    @VisibleForTesting
    static final int REQUEST_CODE_CHOOSE_IMAGE_FROM_GALLERY = 12342;
    @VisibleForTesting
    static final int REQUEST_CODE_TAKE_IMAGE_WITH_CAMERA = 12343;
    @VisibleForTesting
    static final int REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION = 12344;
    @VisibleForTesting
    static final int REQUEST_CAMERA_IMAGE_PERMISSION = 12345;

    public static double MAX_WIDTH = 2048;
    public static double MAX_HEIGHT = 2048;

    public interface OnPickImageListener {
        void onPick(String path);
    }

    private OnPickImageListener onPickImageListener;
    private PermissionManager permissionManager;
    private File externalFilesDirectory;
    private ExifDataCopier exifDataCopier;
    private ImageResizer imageResizer;
    private IntentResolver intentResolver;
    private FileUriResolver fileUriResolver;
    private FileUtils fileUtils;
    private Uri pendingCameraMediaUri;

    TextView tvAlbum;
    TextView tvCamera;
    TextView tvCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.base_dialog_image_picker, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setCancelable(false);
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.base_menu_animation);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        init();
    }

    private void init() {
        permissionManager = new PermissionManager() {
            @Override
            public boolean isPermissionGranted(String permissionName) {
                return ActivityCompat.checkSelfPermission(getContext(), permissionName)
                        == PackageManager.PERMISSION_GRANTED;
            }

            @Override
            public void askForPermission(String permissionName, int requestCode) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{permissionName}, requestCode);
            }
        };

        externalFilesDirectory = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        exifDataCopier = new ExifDataCopier();
        imageResizer = new ImageResizer(externalFilesDirectory, exifDataCopier);
        intentResolver = new IntentResolver() {
            @Override
            public boolean resolveActivity(Intent intent) {
                return intent.resolveActivity(getActivity().getPackageManager()) != null;
            }
        };

        fileUriResolver = new FileUriResolver() {
            @Override
            public Uri resolveFileProviderUriForFile(String fileProviderName, File file) {
                return FileProvider.getUriForFile(getContext(), fileProviderName, file);
            }

            @Override
            public void getFullImagePath(final Uri imageUri, final OnPathReadyListener listener) {
                MediaScannerConnection.scanFile(
                        getContext(),
                        new String[]{imageUri.getPath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Observable.just(path)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(s -> {
                                            listener.onPathReady(path);
                                        });
                            }
                        });
            }
        };

        fileUtils = new FileUtils();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvAlbum = view.findViewById(com.zp.androidx.base.R.id.tv_to_album);
        tvCamera = view.findViewById(com.zp.androidx.base.R.id.tv_to_camera);
        tvCancel = view.findViewById(com.zp.androidx.base.R.id.tv_cancel);
        tvCancel.setOnClickListener(v -> dismiss());

        tvAlbum.setOnClickListener(v -> {
            chooseImageFromGallery();
            //dismiss(); //注意: 此处不能dismiss(), 需要等待选择完成后才可以.
        });

        tvCamera.setOnClickListener(v -> {
            takeImageWithCamera();
        });
    }

    public void setOnPickImageListener(OnPickImageListener listener) {
        this.onPickImageListener = listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_CHOOSE_IMAGE_FROM_GALLERY:
                handleChooseImageResult(resultCode, data);
                break;
            case REQUEST_CODE_TAKE_IMAGE_WITH_CAMERA:
                handleCaptureImageResult(resultCode);
                break;
        }
        dismiss();
    }

    private void handleImageResult(String path) {
        String finalImagePath = imageResizer.resizeImageIfNeeded(path, MAX_WIDTH, MAX_HEIGHT);
        if (null != onPickImageListener) {
            onPickImageListener.onPick(finalImagePath);
        }
    }

    private void handleChooseImageResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            String path = fileUtils.getPathFromUri(getContext(), data.getData());
            handleImageResult(path);
        }
    }

    private void handleCaptureImageResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            fileUriResolver.getFullImagePath(
                    pendingCameraMediaUri,
                    new OnPathReadyListener() {
                        @Override
                        public void onPathReady(String path) {
                            handleImageResult(path);
                        }
                    });
        }
    }

    /**
     * 判断权限并打开Album.
     */
    private void chooseImageFromGallery() {
        if (!permissionManager.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionManager.askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION);
            return;
        }

        launchPickImageFromGalleryIntent();
    }

    private void launchPickImageFromGalleryIntent() {
        Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickImageIntent.setType("image/*");

        this.startActivityForResult(pickImageIntent, REQUEST_CODE_CHOOSE_IMAGE_FROM_GALLERY);
    }

    /**
     * 判断权限并打开Camera.
     */
    private void takeImageWithCamera() {
        if (!permissionManager.isPermissionGranted(Manifest.permission.CAMERA)) {
            permissionManager.askForPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_IMAGE_PERMISSION);
            return;
        }

        launchTakeImageWithCameraIntent();
    }

    private void launchTakeImageWithCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhotos = intentResolver.resolveActivity(intent);

        if (!canTakePhotos) {
            finishWithError("no_available_camera", "No cameras available for taking pictures.");
            return;
        }

        File imageFile = createTemporaryWritableImageFile();
        pendingCameraMediaUri = Uri.parse("file:" + imageFile.getAbsolutePath());
        Uri imageUri = fileUriResolver.resolveFileProviderUriForFile(getContext().getPackageName() + ".fileprovider", imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        grantUriPermissions(intent, imageUri);

        this.startActivityForResult(intent, REQUEST_CODE_TAKE_IMAGE_WITH_CAMERA);
    }

    private File createTemporaryWritableImageFile() {
        return createTemporaryWritableFile(".jpg");
    }

    private File createTemporaryWritableFile(String suffix) {
        String filename = UUID.randomUUID().toString();
        File image;

        try {
            image = File.createTempFile(filename, suffix, externalFilesDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return image;
    }

    private void grantUriPermissions(Intent intent, Uri imageUri) {
        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> compatibleActivities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo info : compatibleActivities) {
            getContext().grantUriPermission(
                    info.activityInfo.packageName,
                    imageUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }


    private void finishWithError(String errorCode, String errorMessage) {
        Log.e(TAG, errorCode + errorMessage);
    }


    interface PermissionManager {
        boolean isPermissionGranted(String permissionName);

        void askForPermission(String permissionName, int requestCode);
    }

    interface IntentResolver {
        boolean resolveActivity(Intent intent);
    }

    interface FileUriResolver {
        Uri resolveFileProviderUriForFile(String fileProviderName, File imageFile);

        void getFullImagePath(Uri imageUri, OnPathReadyListener listener);
    }

    interface OnPathReadyListener {
        void onPathReady(String path);
    }

    static class ImageResizer {
        private final File externalFilesDirectory;
        private final ExifDataCopier exifDataCopier;

        ImageResizer(File externalFilesDirectory, ExifDataCopier exifDataCopier) {
            this.externalFilesDirectory = externalFilesDirectory;
            this.exifDataCopier = exifDataCopier;
        }

        /**
         * If necessary, resizes the image located in imagePath and then returns the path for the scaled
         * image.
         *
         * <p>If no resizing is needed, returns the path for the original image.
         */
        String resizeImageIfNeeded(String imagePath, Double maxWidth, Double maxHeight) {
            boolean shouldScale = maxWidth != null || maxHeight != null;

            if (!shouldScale) {
                return imagePath;
            }

            try {
                File scaledImage = resizedImage(imagePath, maxWidth, maxHeight);
                exifDataCopier.copyExif(imagePath, scaledImage.getPath());

                return scaledImage.getPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private File resizedImage(String path, Double maxWidth, Double maxHeight) throws IOException {
            Bitmap bmp = BitmapFactory.decodeFile(path);
            double originalWidth = bmp.getWidth() * 1.0;
            double originalHeight = bmp.getHeight() * 1.0;

            boolean hasMaxWidth = maxWidth != null;
            boolean hasMaxHeight = maxHeight != null;

            Double width = hasMaxWidth ? Math.min(originalWidth, maxWidth) : originalWidth;
            Double height = hasMaxHeight ? Math.min(originalHeight, maxHeight) : originalHeight;

            boolean shouldDownscaleWidth = hasMaxWidth && maxWidth < originalWidth;
            boolean shouldDownscaleHeight = hasMaxHeight && maxHeight < originalHeight;
            boolean shouldDownscale = shouldDownscaleWidth || shouldDownscaleHeight;

            if (shouldDownscale) {
                double downscaledWidth = (height / originalHeight) * originalWidth;
                double downscaledHeight = (width / originalWidth) * originalHeight;

                if (width < height) {
                    if (!hasMaxWidth) {
                        width = downscaledWidth;
                    } else {
                        height = downscaledHeight;
                    }
                } else if (height < width) {
                    if (!hasMaxHeight) {
                        height = downscaledHeight;
                    } else {
                        width = downscaledWidth;
                    }
                } else {
                    if (originalWidth < originalHeight) {
                        width = downscaledWidth;
                    } else if (originalHeight < originalWidth) {
                        height = downscaledHeight;
                    }
                }
            }

            Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, width.intValue(), height.intValue(), false);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            String[] pathParts = path.split("/");
            String imageName = pathParts[pathParts.length - 1];

            File imageFile = new File(externalFilesDirectory, "/scaled_" + imageName);
            FileOutputStream fileOutput = new FileOutputStream(imageFile);
            fileOutput.write(outputStream.toByteArray());
            fileOutput.close();

            return imageFile;
        }
    }


    class ExifDataCopier {
        void copyExif(String filePathOri, String filePathDest) {
            try {
                ExifInterface oldExif = new ExifInterface(filePathOri);
                ExifInterface newExif = new ExifInterface(filePathDest);

                List<String> attributes =
                        Arrays.asList(
                                "FNumber",
                                "ExposureTime",
                                "ISOSpeedRatings",
                                "GPSAltitude",
                                "GPSAltitudeRef",
                                "FocalLength",
                                "GPSDateStamp",
                                "WhiteBalance",
                                "GPSProcessingMethod",
                                "GPSTimeStamp",
                                "DateTime",
                                "Flash",
                                "GPSLatitude",
                                "GPSLatitudeRef",
                                "GPSLongitude",
                                "GPSLongitudeRef",
                                "Make",
                                "Model",
                                "Orientation");
                for (String attribute : attributes) {
                    setIfNotNull(oldExif, newExif, attribute);
                }

                newExif.saveAttributes();

            } catch (Exception ex) {
                Log.e("ExifDataCopier", "Error preserving Exif data on selected image: " + ex);
            }
        }

        private void setIfNotNull(ExifInterface oldExif, ExifInterface newExif, String property) {
            if (oldExif.getAttribute(property) != null) {
                newExif.setAttribute(property, oldExif.getAttribute(property));
            }
        }
    }


    static class FileUtils {

        String getPathFromUri(final Context context, final Uri uri) {
            String path = getPathFromLocalUri(context, uri);
            if (path == null) {
                path = getPathFromRemoteUri(context, uri);
            }
            return path;
        }

        @SuppressLint("NewApi")
        private String getPathFromLocalUri(final Context context, final Uri uri) {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);

                    if (!TextUtils.isEmpty(id)) {
                        try {
                            final Uri contentUri =
                                    ContentUris.withAppendedId(
                                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                            return getDataColumn(context, contentUri, null, null);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }

                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {

                // Return the remote address
                if (isGooglePhotosUri(uri)) {
                    return uri.getLastPathSegment();
                }

                return getDataColumn(context, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

            return null;
        }

        private static String getDataColumn(
                Context context, Uri uri, String selection, String[] selectionArgs) {
            Cursor cursor = null;

            final String column = "_data";
            final String[] projection = {column};

            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return null;
        }

        private static String getPathFromRemoteUri(final Context context, final Uri uri) {
            // The code below is why Java now has try-with-resources and the Files utility.
            File file = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            boolean success = false;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                file = File.createTempFile("image_picker", "jpg", context.getCacheDir());
                outputStream = new FileOutputStream(file);
                if (inputStream != null) {
                    copy(inputStream, outputStream);
                    success = true;
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                } catch (IOException ignored) {
                }
                try {
                    if (outputStream != null) outputStream.close();
                } catch (IOException ignored) {
                    // If closing the output stream fails, we cannot be sure that the
                    // target file was written in full. Flushing the stream merely moves
                    // the bytes into the OS, not necessarily to the file.
                    success = false;
                }
            }
            return success ? file.getPath() : null;
        }

        private static void copy(InputStream in, OutputStream out) throws IOException {
            final byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        }

        private static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        private static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        private static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }

        private static boolean isGooglePhotosUri(Uri uri) {
            return "com.google.android.apps.photos.content".equals(uri.getAuthority());
        }
    }
}