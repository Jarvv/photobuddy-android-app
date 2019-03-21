package uk.ac.shef.oak.com4510.async;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import uk.ac.shef.oak.com4510.adapters.ImageAdapter;
import uk.ac.shef.oak.com4510.objects.ImagePlaceholder;

/**
 * ImageLoader is an AsyncTask in which given a filepath, will create a bitmap of the image and save
 * this to internal storage to be used for quicker loading.
 */
public class ImageLoader extends AsyncTask <ImagePlaceholder, Void, Bitmap> {
    private ImagePlaceholder imgPlaceholder;

    @Override
    protected Bitmap doInBackground(ImagePlaceholder... holderAndPosition) {
        imgPlaceholder = holderAndPosition[0];
        File file = imgPlaceholder.items.get(imgPlaceholder.position).file;

        // Try and retrieve the saved bitmap if already created.
        Bitmap myBitmap = getSavedBitmap(file.getName());
        // If it doesn't exist, create one
        if(myBitmap == null){
            // Create a bitmap from the file
            myBitmap = decodeSampledBitmapFromResource(file.getAbsolutePath(), 100, 100);
            // Write the bitmap to a file in internal storage
            writeBitmapToFile(imgPlaceholder.context,myBitmap,file.getName());
        }

        return myBitmap;
    }

    @Override
    protected void onPostExecute( Bitmap result )  {
        // Assign the bitmap to the correct View_Holder
        if(imgPlaceholder.imgHolder == null) {
            imgPlaceholder.srHolder.imageView.setImageBitmap(result);
        }else {
            imgPlaceholder.imgHolder.imageView.setImageBitmap(result);
            if (!imgPlaceholder.items.get(imgPlaceholder.position).isTakenWithCamera()) {
                imgPlaceholder.items.get(imgPlaceholder.position).setFileLocationFromAbsolute();
                imgPlaceholder.photoBuddyViewModel.insertSingleton(imgPlaceholder.items.get(imgPlaceholder.position));
            }
        }

    }

    /**
     * Given an image file, decode this and create a bitmap.
     * @param filePath - The filepath of the image.
     * @param reqWidth - The width to set the bitmap to.
     * @param reqHeight - The height to set the bitmap to.
     * @return - The sub-sampled bitmap.
     */
    private static Bitmap decodeSampledBitmapFromResource(String filePath, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * Calculates the value that the decoder can sub-sample the original image.
     * @param options - The BitmapFactory options.
     * @param reqWidth - The width to set the bitmap to.
     * @param reqHeight - The height to set the bitmap to.
     * @return - The int of the inSampleSize.
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Writes a bitmap to a file in internal storage.
     * @param context - The context of the activity.
     * @param bitmap - The bitmap to write to internal storage.
     * @param imgName - The name of the file to save the bitmap to.
     */
    private void writeBitmapToFile(Context context, Bitmap bitmap, String imgName){
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        File file = cw.getDir("Images", Context.MODE_PRIVATE);
        file = new File(file, "img_"+ imgName);

        OutputStream stream = null;
        try{
            stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempts to retrieve the saved bitmap for the photo, if it is present return the bitmap.
     * @param fileName - The filename for the photo.
     * @return - The saved bitmap.
     */
    private Bitmap getSavedBitmap(String fileName){
        try {
            File f = new File(imgPlaceholder.context.getDir("Images", Context.MODE_PRIVATE), "img_" + fileName);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        }
        catch (FileNotFoundException e) {
            return null;
        }
    }

}
