package git.artdeell.skymodloader;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class LoadVideoView extends VideoView {
    public LoadVideoView(Context context) {
        super(context);
    }

    public LoadVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

}