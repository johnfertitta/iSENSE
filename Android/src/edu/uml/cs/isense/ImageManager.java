package edu.uml.cs.isense;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * 
 * @author johnfertitta
 *
 * This class handles the loading and caching of all images for the application.  Methods are provided to fetch images and return them as drawables or bitmaps, for both threaded and blocking.
 *
 */

public class ImageManager {
    private final Map<String, Drawable> drawableMap;
    private final Map<String, Bitmap> bitmapMap;
    private static ImageManager instance = null;
    
    protected ImageManager() {
    	drawableMap = new HashMap<String, Drawable>();
    	bitmapMap = new HashMap<String, Bitmap>();
    }
    
    /**
     * Get the single instance of the ImageManager
     *
     * @return ImageManager
     */
    public static ImageManager getInstance() {
    	if (instance == null) {
    		instance = new ImageManager();
    	}
    	
    	return instance;
    }
    
    /**
     * Fetch an image from a URL and return it as a drawable.  This function is blocking.
     * 
     * @param urlString  The URL to fetch the image from
     * @return Drawable
     */
    public Drawable fetchDrawable(String urlString) {
    	if (drawableMap.containsKey(urlString)) {
    		return drawableMap.get(urlString);
    	}

    	try {
    		InputStream is = fetch(urlString);
    		Drawable drawable = Drawable.createFromStream(is, "src");
    		drawableMap.put(urlString, drawable);
    	
    		return drawable;
    	} catch (MalformedURLException e) {
    		return null;
    	} catch (IOException e) {
    		return null;
    	} catch (IllegalStateException e) {
    		return null;
    	} catch (OutOfMemoryError e) {
    		return null;
    	}
    }
    
    /**
     * Fetch an image from a URL on a thread.  Once fetched the image will be made into a drawable and placed in the provided image view.
     * 
     * @param urlString  The URL to fetch the image from
     * @param imageView  The imageView that will display the image after it is loaded
     */
    public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
    	if (drawableMap.containsKey(urlString)) {
    		imageView.setImageDrawable(drawableMap.get(urlString));
    		return;
    	}
    	
    	final Handler handler = new Handler() {
    		@Override
    		public void handleMessage(Message message) {
    			imageView.setImageDrawable((Drawable) message.obj);
    		}
    	};

    	Thread thread = new Thread() {
    		@Override
    		public void run() {
    			Drawable drawable = fetchDrawable(urlString);
    			if (drawable != null) {
    				Message message = handler.obtainMessage(1, drawable);
    				handler.sendMessage(message);
    				return;
    			} else {
    				return;
    			}
    		}
    	};
    	thread.start();
    }

    /**
     * Fetch an image from a URL and return it as a Bitmap
     * 
     * @param urlString
     * @return Bitmap
     */
    public Bitmap fetchBitmap(final String urlString) {
    	if (bitmapMap.containsKey(urlString)) {
    		return bitmapMap.get(urlString);
    	}
    	
    	try {
    		InputStream is = fetch(urlString);
    		Bitmap bitmap = BitmapFactory.decodeStream(is);
    		bitmapMap.put(urlString, bitmap);
    		
    		return bitmap;
    	} catch (MalformedURLException e) {
    		return null;
    	} catch (IOException e) {
    		return null;
    	} catch (IllegalStateException e) {
    		return null;
    	} catch (OutOfMemoryError e) {
    		return null;
    	}
    }
    
    
    /**
     * Fetch an image from a URL on a thread.  Once fetched it will be displayed in the provided image view.
     * 
     * @param urlString
     * @param imageView
     */
    public void fetchBitmapOnThread(final String urlString, final ImageView imageView) {
    	if (bitmapMap.containsKey(urlString)) {
			imageView.setImageBitmap(bitmapMap.get(urlString));
			return;
    	}

    	final Handler handler = new Handler() {
    		@Override
    		public void handleMessage(Message message) {
    			imageView.setImageBitmap((Bitmap) message.obj);
    		}
    	};

    	Thread thread = new Thread() {
    		@Override
    		public void run() {
    			Bitmap bitmap = fetchBitmap(urlString);
    			if (bitmap != null) {
    				Message message = handler.obtainMessage(1, bitmap);
    				handler.sendMessage(message);
    				return;
    			} else {
    				return;
    			}
    		}
    	};
    	thread.start();
    }

    private InputStream fetch(String urlString) throws MalformedURLException, IOException, IllegalStateException {
    	DefaultHttpClient httpClient = new DefaultHttpClient();
    	HttpGet request = new HttpGet(urlString);
    	HttpResponse response = httpClient.execute(request);
    	return response.getEntity().getContent();
    }
}
