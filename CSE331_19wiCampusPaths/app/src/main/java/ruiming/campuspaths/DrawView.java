package ruiming.campuspaths;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.content.Context;
import android.util.AttributeSet;

import java.util.List;

import hw5.Edge;
import hw8.Coordinate;

/**
 * Class that draws building markings and paths between two buildings on the campus map.
 */
public class DrawView extends AppCompatImageView {

    // Scale down all coordinates on the map
    private static final float SCALE = 0.25f;
    // Store the coordinate of the starting point
    private Coordinate start = null;
    // Store the coordinate of the ending point
    private Coordinate end = null;
    // Store the shortest path
    private List<Edge<Coordinate, Double>> path = null;
    // Store whether the starting point should be drawn
    private boolean drawStartPoint = false;
    // Store whether the ending point should be drawn
    private boolean drawEndPoint = false;
    // Store whether the path should be drawn
    private boolean drawPath = false;
    // Store whether the map should be reset
    private boolean reset = false;

    /**
     * Constructs a DrawView.
     *
     * @param context The context
     * @spec.effects Constructs a new DrawView.
     */
    public DrawView(Context context) {
        super(context);
    }

    /**
     * Constructs a DrawView.
     *
     * @param context The context
     * @param attrs   The set of attributes
     * @spec.effects Constructs a new DrawView.
     */
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructs a DrawView.
     *
     * @param context  The context
     * @param attrs    The set of attributes
     * @param defStyle The default style
     * @spec.effects Constructs a new DrawView.
     */
    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeMiter(0f);

        if (reset) {
            // reset this view
            reset = false;
            // zoom out to the its original size
            this.setScaleX(1f);
            this.setScaleY(1f);
        } else {
            // draw the start point on the map
            if (drawStartPoint) {
                float scaledX = (float) start.getX() * SCALE;
                float scaledY = (float) start.getY() * SCALE;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5f);
                paint.setColor(Color.BLUE);
                canvas.drawCircle(scaledX, scaledY, 7f, paint);
                // zoom out to the its original size
                this.setScaleX(1f);
                this.setScaleY(1f);
            }
            // draw the end point on the map
            if (drawEndPoint) {
                float scaledX = (float) end.getX() * SCALE;
                float scaledY = (float) end.getY() * SCALE;
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(5f);
                paint.setColor(Color.YELLOW);
                canvas.drawCircle(scaledX, scaledY, 7f, paint);
                // zoom out to the its original size
                this.setScaleX(1f);
                this.setScaleY(1f);
            }
            // draw the shortest path on the map
            if (drawPath) {
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(4f);
                paint.setColor(Color.RED);

                // draw each edge
                for (Edge<Coordinate, Double> edge : path) {
                    Coordinate startPoint = edge.getParent();
                    Coordinate endPoint = edge.getChild();
                    float curStartX = (float) startPoint.getX() * SCALE;
                    float curStartY = (float) startPoint.getY() * SCALE;
                    float curEndX = (float) endPoint.getX() * SCALE;
                    float curEndY = (float) endPoint.getY() * SCALE;
                    canvas.drawLine(curStartX, curStartY, curEndX, curEndY, paint);
                }

                // get the starting point
                Edge<Coordinate, Double> firstEdge = path.get(0);
                Coordinate start = firstEdge.getParent();
                // get the ending point
                Edge<Coordinate, Double> lastEdge = path.get(path.size() - 1);
                Coordinate end = lastEdge.getChild();
                // the maximum x coordinate of this path
                float endX = (float) end.getX() * SCALE;
                // the minimum x coordinate of this path
                float startX = (float) start.getX() * SCALE;
                // the maximum y coordinate of this path
                float endY = (float) end.getY() * SCALE;
                // the minimum y coordinate of this path
                float startY = (float) start.getY() * SCALE;

                // set the pivot to zoom in
                this.setPivotX((startX + endX) / 2);
                this.setPivotY((startY + endY) / 2);

                // compute the scale to zoom in
                float xScale = this.getWidth() * 0.55f / Math.abs(startX - endX);
                float yScale = this.getHeight() * 0.55f / Math.abs(startY - endY);
                float scale = Math.min(xScale, yScale);

                // zoom in to show the path clearly
                // the maximum scale should be 5.5f to not zoom in too much
                if (scale > 5.5f) {
                    this.setScaleX(5.5f);
                    this.setScaleY(5.5f);
                } else if (scale > 1f) { // 1f means the original size
                    this.setScaleX(scale);
                    this.setScaleY(scale);
                }
            }
        }
    }

    /**
     * Draws a marking of the given starting point on the map.
     *
     * @param start The given starting point on the map
     * @spec.modifies this
     * @spec.effects Draws the marking of the given starting point
     */
    public void drawStartMarking(Coordinate start) {
        this.start = start;
        drawStartPoint = true;
        drawPath = false;
        this.invalidate();
    }

    /**
     * Clear the marking of the starting point.
     *
     * @spec.modifies this
     * @spec.effects Clears the marking of the starting point
     */
    public void clearStartMarking() {
        drawStartPoint = false;
        drawPath = false;
        this.invalidate();
    }

    /**
     * Draws a marking of the given ending point on the map.
     *
     * @param end The given ending point on the map
     * @spec.modifies this
     * @spec.effects Draws the marking of the given ending point
     */
    public void drawEndMarking(Coordinate end) {
        this.end = end;
        drawEndPoint = true;
        drawPath = false;
        this.invalidate();
    }

    /**
     * Clear the marking of the ending point.
     *
     * @spec.modifies this
     * @spec.effects Clears the marking of the ending point
     */
    public void clearEndMarking() {
        drawEndPoint = false;
        drawPath = false;
        this.invalidate();
    }

    /**
     * Draws the given path on the map.
     *
     * @param path The given path
     * @spec.modifies this
     * @spec.effects Draws the given path on the map
     */
    public void drawPath(List<Edge<Coordinate, Double>> path) {
        this.path = path;
        drawStartPoint = true;
        drawEndPoint = true;
        drawPath = true;
        this.invalidate();
    }

    /**
     * Clears all the markings on the map.
     *
     * @spec.modifies this
     * @spec.effects Clears all the markings and paths on the map.
     */
    public void resetMap() {
        reset = true;
        drawStartPoint = false;
        drawEndPoint = false;
        drawPath = false;
        this.invalidate();
    }
}
