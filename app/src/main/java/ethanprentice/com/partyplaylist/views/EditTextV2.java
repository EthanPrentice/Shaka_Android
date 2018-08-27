package ethanprentice.com.partyplaylist.views;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by Ethan on 2018-06-17.
 */

public class EditTextV2 extends AppCompatEditText {

    public EditTextV2(Context context) {
        super(context);
    }

    public EditTextV2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(event != null){
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_BACK){
                setSelected(false);
                clearFocus();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
