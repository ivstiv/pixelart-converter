package core;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;

public class DisplayedImage {

    private ImageView imagePreview;
    private ImageView originalImageView;
    private double previewImageWidth, previewImageHeight;
    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    public DisplayedImage(ImageView imageView) {
        this.imagePreview = imageView;
        this.originalImageView = imageView;
    }

    public void setup() {
        previewImageHeight = imagePreview.getImage().getHeight();
        previewImageWidth = imagePreview.getImage().getWidth();
        imagePreview.setFitWidth(zoomProperty.get() * 6);
        imagePreview.setFitHeight(zoomProperty.get() * 6);

        resetZoom(imagePreview, previewImageWidth, previewImageHeight);

        // listener for changes of the zoom
        zoomProperty.addListener(arg0 -> {
            imagePreview.setFitWidth(zoomProperty.get() * 6);
            imagePreview.setFitHeight(zoomProperty.get() * 6);
        });

        ObjectProperty<Point2D> mouseDown = new SimpleObjectProperty<>();

        imagePreview.setOnMousePressed(e -> {
            Point2D mousePress = imageViewToImage(imagePreview, new Point2D(e.getX(), e.getY()));
            imagePreview.setCursor(Cursor.CLOSED_HAND);
            mouseDown.set(mousePress);
        });

        imagePreview.setOnMouseReleased(e -> {
            imagePreview.setCursor(Cursor.OPEN_HAND);
        });

        imagePreview.setOnMouseDragged(e -> {
            Point2D dragPoint = imageViewToImage(imagePreview, new Point2D(e.getX(), e.getY()));
            shift(imagePreview, dragPoint.subtract(mouseDown.get()));
            mouseDown.set(imageViewToImage(imagePreview, new Point2D(e.getX(), e.getY())));
        });

        imagePreview.setOnScroll(e -> {
            e.consume();
            double delta = e.getDeltaY()*-1;
            Rectangle2D viewport = imagePreview.getViewport();

            double scale = clamp(Math.pow(1.01, delta),

                    // don't scale so we're zoomed in to fewer than MIN_PIXELS in any direction:
                    Math.min(10 / viewport.getWidth(), 10 / viewport.getHeight()),

                    // don't scale so that we're bigger than image dimensions:
                    Math.max(previewImageWidth / viewport.getWidth(), previewImageHeight / viewport.getHeight())

            );

            Point2D mouse = imageViewToImage(imagePreview, new Point2D(e.getX(), e.getY()));

            double newWidth = viewport.getWidth() * scale;
            double newHeight = viewport.getHeight() * scale;

            // To keep the visual point under the mouse from moving, we need
            // (x - newViewportMinX) / (x - currentViewportMinX) = scale
            // where x is the mouse X coordinate in the image

            // solving this for newViewportMinX gives

            // newViewportMinX = x - (x - currentViewportMinX) * scale

            // we then clamp this value so the image never scrolls out
            // of the imageview:

            double newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) * scale,
                    0, previewImageWidth - newWidth);
            double newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) * scale,
                    0, previewImageHeight - newHeight);

            imagePreview.setViewport(new Rectangle2D(newMinX, newMinY, newWidth, newHeight));
        });

        imagePreview.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                resetZoom(imagePreview, previewImageWidth, previewImageHeight);
            }
        });
    }

    public void zoomIn() {
        zoomProperty.set(zoomProperty.get() * 1.1);
    }

    public void zoomOut() {
        zoomProperty.set(zoomProperty.get() / 1.1);
    }

    // reset to the top left:
    public void resetZoom(ImageView imageView, double width, double height) {
        imageView.setViewport(new Rectangle2D(0, 0, width, height));
    }

    // convert mouse coordinates in the imageView to coordinates in the actual image:
    private Point2D imageViewToImage(ImageView imageView, Point2D imageViewCoordinates) {
        double xProportion = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth();
        double yProportion = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight();

        Rectangle2D viewport = imageView.getViewport();
        return new Point2D(
                viewport.getMinX() + xProportion * viewport.getWidth(),
                viewport.getMinY() + yProportion * viewport.getHeight());
    }

    // shift the viewport of the imageView by the specified delta, clamping so
    // the viewport does not move off the actual image:
    private void shift(ImageView imageView, Point2D delta) {
        Rectangle2D viewport = imageView.getViewport();

        double width = imageView.getImage().getWidth() ;
        double height = imageView.getImage().getHeight() ;

        double maxX = width - viewport.getWidth();
        double maxY = height - viewport.getHeight();

        double minX = clamp(viewport.getMinX() - delta.getX(), 0, maxX);
        double minY = clamp(viewport.getMinY() - delta.getY(), 0, maxY);

        imageView.setViewport(new Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()));
    }

    private double clamp(double value, double min, double max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    public void setConvertedImage(BufferedImage newImage) {
        // I am writing it myself instead of using SwingFXUtils.toFXImage() because
        // the library has been moved between java 8 and java 11 so the path is different
        WritableImage wr = null;
        if (newImage != null) {
            wr = new WritableImage(newImage.getWidth(), newImage.getHeight());
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < newImage.getWidth(); x++) {
                for (int y = 0; y < newImage.getHeight(); y++) {
                    pw.setArgb(x, y, newImage.getRGB(x, y));
                }
            }
        }

        imagePreview.setImage(wr);
        previewImageHeight = imagePreview.getImage().getHeight();
        previewImageWidth = imagePreview.getImage().getWidth();
        imagePreview.setFitWidth(zoomProperty.get() * 6);
        imagePreview.setFitHeight(zoomProperty.get() * 6);

        resetZoom(imagePreview, previewImageWidth, previewImageHeight);
    }
}
