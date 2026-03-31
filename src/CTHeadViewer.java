import java.io.IOException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

/**
 * CTHeadViewer is a JavaFX application that displays CT scan data.
 * It loads the dataset using images through SliceRenderer.
 * This class serves as the entry point for the application and is responsible for
 * setting up the GUI and linking user interactions with image rendering methods.
 *
 * @author Hiasham, Cameron
 * @version 1.4
 * @since 2025-02-10
 * @see VolumeData
 * @see SliceRenderer
 */
public class CTHeadViewer extends Application {

    private VolumeData volumeData; // The 3D volume data loaded from the binary file.
    private SliceRenderer renderer; // The renderer used to generate slices, MIPs, and volume-rendered images.

    // Current slice indices for X, Y, and Z axes.
    private int currZSlice = 128;
    private int currYSlice = 128;
    private int currXSlice = 128;

    /**
     * The main entry point for the JavaFX application. This method initialises
     * the volume data, sets up the GUI, and displays the CT scan slices, MIPs,
     * and volume-rendered images.
     *
     * @param stage The primary stage for this application, onto which
     *              the application scene can be set.
     * @throws Exception If an error occurs during the loading of the volume data
     *                   or the initialisation of the GUI.
     */
    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Load the volume data from the binary file.
            volumeData = new VolumeData("CThead-256cubed.bin");
        } catch (IOException e) {
            // Handle the case where the file is not found.
            System.out.println("Error: The CThead file is not in the working directory");
            System.out.println("Working Directory = " + System.getProperty("user.dir"));
            return;
        }

        // Initialise the renderer with the loaded volume data.
        renderer = new SliceRenderer(volumeData);
        int dim = volumeData.getDimension(); // Get the dimension of the volume data.

        // Create WritableImages for slices, MIPs, and volume-rendered images.
        WritableImage sliceZImage = new WritableImage(dim, dim);
        renderer.GetZSlice(currZSlice, sliceZImage);
        WritableImage sliceYImage = new WritableImage(dim, dim);
        renderer.GetYSlice(currYSlice, sliceYImage);
        WritableImage sliceXImage = new WritableImage(dim, dim);
        renderer.GetXSlice(currXSlice, sliceXImage);
        WritableImage MIPZImage = new WritableImage(dim, dim);
        renderer.GetZMIP(MIPZImage);
        WritableImage MIPYImage = new WritableImage(dim, dim);
        renderer.GetYMIP(MIPYImage);
        WritableImage MIPXImage = new WritableImage(dim, dim);
        renderer.GetXMIP(MIPXImage);
        WritableImage volZImage = new WritableImage(dim, dim);
        renderer.GetZVol(volZImage, 0.5);
        WritableImage volYImage = new WritableImage(dim, dim);
        renderer.GetYVol(volYImage, 0.5);
        WritableImage volXImage = new WritableImage(dim, dim);
        renderer.GetXVol(volXImage, 0.5);

        // Link images to ImageView objects for display in the GUI.
        ImageView sliceZView = new ImageView(sliceZImage);
        ImageView sliceYView = new ImageView(sliceYImage);
        ImageView sliceXView = new ImageView(sliceXImage);
        ImageView MIPZView = new ImageView(MIPZImage);
        ImageView MIPYView = new ImageView(MIPYImage);
        ImageView MIPXView = new ImageView(MIPXImage);
        ImageView volZView = new ImageView(volZImage);
        ImageView volYView = new ImageView(volYImage);
        ImageView volXView = new ImageView(volXImage);

        // Create sliders for navigating slices and adjusting opacity.
        Slider sliceZSlider = new Slider(0, dim - 1, currZSlice);
        Slider sliceYSlider = new Slider(0, dim - 1, currYSlice);
        Slider sliceXSlider = new Slider(0, dim - 1, currXSlice);
        Slider opacitySlider = new Slider(0, 100, 50);

        // Add listeners to update the displayed slice when the slider value changes.
        sliceXSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                currXSlice = newValue.intValue();
                renderer.GetXSlice(currXSlice, sliceXImage);
            }
        });

        sliceYSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                currYSlice = newValue.intValue();
                renderer.GetYSlice(currYSlice, sliceYImage);
            }
        });

        sliceZSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                currZSlice = newValue.intValue();
                renderer.GetZSlice(currZSlice, sliceZImage);
            }
        });

        opacitySlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> observable, Number oldVal, Number newVal) {
                double skinOpacity = newVal.doubleValue() / 100.0;
                // Update all volume-rendered images with the new opacity.
                renderer.GetZVol(volZImage, skinOpacity);
                renderer.GetYVol(volYImage, skinOpacity);
                renderer.GetXVol(volXImage, skinOpacity);
            }
        });

        // Set up the GUI layout using GridPane.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Create arrays for sliders, slice views, and MIP views.
        Node[] sliders = { sliceXSlider, sliceYSlider, sliceZSlider };
        Node[] sliceViews = { sliceXView, sliceYView, sliceZView };
        Node[] mipViews = { MIPXView, MIPYView, MIPZView };
        Node[] volViews = { volXView, volYView, volZView };

        // Add nodes to the grid using a loop.
        for (int col = 0; col < sliders.length; col++) {
            grid.add(sliders[col], col, 0);
            grid.add(sliceViews[col], col, 1);
            grid.add(mipViews[col], col, 2);
            grid.add(volViews[col], col, 3);
        }

        grid.add(opacitySlider, 1, 4, 1, 1);

        // Create and display the scene.
        Scene scene = new Scene(grid, 800, 840);
        stage.setTitle("CT Data Viewer");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch();
    }
}