package igeak.presentationwand.data_structure;

/**
 * Created by Tong on 2/7/14.
 */
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class GestureUtils {



    //get the size of the screen
    public static Screen getScreenPix(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        return new Screen(dm.widthPixels,dm.heightPixels);
    }

    public static class Screen{

        public int widthPixels;
        public int heightPixels;

        public Screen(){

        }

        public Screen(int widthPixels,int heightPixels){
            this.widthPixels=widthPixels;
            this.heightPixels=heightPixels;
        }

        @Override
        public String toString() {
            return "("+widthPixels+","+heightPixels+")";
        }

    }

}