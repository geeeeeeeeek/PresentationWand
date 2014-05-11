package igeak.presentationwand.data_structure;

import android.widget.Toast;

import java.io.Serializable;

/**
 * Created by Tong on 2/7/14.
 */
public class ActionRequest implements Serializable {
    private static final int LEFT = 2, RIGHT = 3, UP = 0, DOWN = 1;

    private int requestKey;

    public ActionRequest(int requestKey) {
        super();
        this.requestKey = requestKey;
    }

    void setRequestKey(int requestKey) {
        this.requestKey = requestKey;
    }

    int getRequestKey() {
        return this.requestKey;
    }


}
