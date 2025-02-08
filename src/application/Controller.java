package application;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javafx.scene.control.Toggle;

import javax.imageio.ImageIO;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller extends AnchorPane {
    @FXML
    private Button myGraphicButton;    // Button for uploading an image

    @FXML
    private ImageView myImageView;    // Controller for displaying an image on the screen

    @FXML
    private ProgressBar myProgressBar;     // ProgressBar to display progress during the image conversion process

    @FXML
    private Button myColorButton;    // A colored button

    @FXML
    private TextField myTextField;    // Controller for a TextField input field

    @FXML
    private ChoiceBox myChoiceBox;    // ChoiceBox controller for selecting the type of image to display

    @FXML
    private CheckBox myCheckButton;    // CheckBox button for selection

    @FXML
    private ProgressIndicator myProgressIndicator; // ProgressIndicator for showing progress while saving an image

    @FXML
    private AnchorPane anchorPaneTab2;    // AnchorPane for the second tab

    private Image image = null;    // Initialize the image variable (to store the image object)
    private String imageName = null;     // Initialize the image name variable (to store the image's name)
    private RadioButton radioButton = new RadioButton();    // Initialize the first radio button
    private RadioButton radioButton2 = new RadioButton();    // Initialize the second radio button
    private TextArea textarea = new TextArea();                // Initialize the TextArea (for multi-line text input)
    private TextField textf = new TextField();                // Initialize the TextField (for single-line text input)
    private Label advancedLabel = new Label(": ");        // Initialize the label (for displaying advanced information)
    private Hyperlink hyperlink = new Hyperlink();        // Initialize the Hyperlink (for clickable links)

    ToggleGroup group = new ToggleGroup();    // Initialize a ToggleGroup that will contain the two radio buttons above

    public Controller() {
        // Create a new instance of FXMLLoader
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("GrayScale.fxml"));
        fxmlLoader.setRoot(this);     // Set the root of the object hierarchy (this class as the root)
        fxmlLoader.setController(this); // Set the controller for the FXML file

        try {        // Try to load the object hierarchy from the FXML file
            fxmlLoader.load();
        } catch (IOException exception) {        // If loading fails, an exception is thrown
            throw new RuntimeException(exception);
        }
    }

    // Initial configuration method
    public void configureStart() {
        // Set the graphic for the graphic button
        // Load the logo image
        ImageView viewButtonUpload = new ImageView(new Image(getClass().getResourceAsStream("upload_button_logo.png")));
        viewButtonUpload.setFitHeight(25);        // Set the height of the image
        viewButtonUpload.setPreserveRatio(true);    // Ensure the aspect ratio of the image is preserved
        myGraphicButton.setGraphic(viewButtonUpload);    // Set the image as the graphic for the button

        // Disable the text field since it is used to display whether an image is uploaded or not
        myTextField.setDisable(true);
        myTextField.setText("No picture uploaded"); // Set the initial text when no image is uploaded
        this.handleColorButton();        // Method to handle the color button functionality
        this.handleValuesChoiceBox();    // Method to handle setting values in the ChoiceBox

        this.handleHyperlink();         // Method to handle the hyperlink functionality
        this.handleRadioButton();    // Method to handle the radio buttons
    }

    public void handleValuesChoiceBox() {
        // Add values to the ChoiceBox
        this.myChoiceBox.setItems(FXCollections.observableArrayList("Default image",
                new Separator(), "Grayscale image"));

        // Select the first value by default
        this.myChoiceBox.getSelectionModel().selectFirst();
    }

    // Method to handle the functionality of the image display button
    public void handleColorButton() {
        // Set the action for when the button is pressed
        myColorButton.setOnAction(    // If the button is pressed
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        // Pass the selected value from the ChoiceBox to the image conversion method
                        processConvertingImage(myChoiceBox.getSelectionModel().getSelectedIndex());
                    }
                });
    }

    private void processConvertingImage(int index) {
        // Assign the task returned by the method processImageWorker to the processImage variable
        processImage = processImageWorker(index);
        // Unbind the previous binding of the progress bar's progress property
        myProgressBar.progressProperty().unbind();
        // Bind the progress bar to the progress property of the new task
        myProgressBar.progressProperty().bind(processImage.progressProperty());
        // Start a new thread to execute the image conversion task
        new Thread(processImage).start();
    }

    // Define a variable to hold the task for processing the image
    private Task processImage;

    private Task processImageWorker(int index) {
        // Return a new task that will perform the image processing
        return new Task() {
            @Override
            protected Object call() throws Exception {
                // Switch to handle different cases based on the selected value from the ChoiceBox
                switch (index) {
                    // Default image case
                    case 0: {
                        // Set the previously loaded default image
                        myImageView.setImage(image);

                        // Convert the Image object to a BufferedImage for further processing
                        BufferedImage img = SwingFXUtils.fromFXImage(image, null);

                        // Update the progress bar with the current width and height of the image
                        updateProgress(image.getWidth(), image.getHeight());

                        // If the checkbox is selected, save the image
                        if (myCheckButton.isSelected()) {
                            String newFileName = "default_" + imageName;     // Create a new file name by prefixing the default image name

                            Platform.runLater(new Runnable() {  // Use Platform.runLater to ensure the image saving happens on the JavaFX Application Thread

                                @Override
                                public void run() {
                                    /* The task 'saveImage' is assigned the task returned by the method saveImageWorker.
                                     * The saveImageWorker method is responsible for saving the image on the computer
                                     * and takes two parameters: the image itself and the new file name for the saved image.
                                     */
                                    saveImage = saveImageWorker(img, newFileName);

                                    // Unbind the previous binding of the progress indicator’s progress property
                                    myProgressIndicator.progressProperty().unbind();

                                    // Bind the progress indicator to the progress property of the new task
                                    myProgressIndicator.progressProperty().bind(saveImage.progressProperty());

                                    // Start a new thread to execute the save image task
                                    new Thread(saveImage).start();
                                }
                            });
                        }
                        break;
                    }
                    // Case for converting the image from color to grayscale
                    case 2: {
                        // Convert the Image object to a BufferedImage for pixel manipulation
                        BufferedImage img = SwingFXUtils.fromFXImage(image, null);

                        // Get the width and height of the image and store them in variables
                        int width = img.getWidth();
                        int height = img.getHeight();

                        // Loop through each pixel in the image
                        for (int y = 0; y < height; y++) {  // Iterate through each row (height)
                            for (int x = 0; x < width; x++) {    // Iterate through each column (width)
                                int p = img.getRGB(x, y);   // Get the color value of the pixel at (x, y)

                                // Extract the values for alpha, red, green, and blue from the current pixel
                                int a = (p >> 24) & 0xff;    // Alpha value (controls the transparency of the pixel) - it remains unchanged
                                int r = (p >> 16) & 0xff;   // Red value
                                int g = (p >> 8) & 0xff;    // Green value
                                int b = p & 0xff;    // Blue value

                                // Calculate the average of the red, green, and blue values for grayscale effect
                                int avg = (r + g + b) / 3;

                                // Replace the original pixel value with the new grayscale value
                                p = (a << 24) | (avg << 16) | (avg << 8) | avg; // Reassemble the pixel with the grayscale value for RGB
                                img.setRGB(x, y, p); // Set the new pixel value back to the image at position (x, y)
                            }
                            // Update the progress bar by passing the current row (y + 1) and the total image height
                            updateProgress(y + 1, height);
                        }
                        // Set the updated grayscale image to the ImageView for display
                        myImageView.setImage(SwingFXUtils.toFXImage(img, null));

                        // If the checkbox is selected, the image will be saved
                        if (myCheckButton.isSelected()) {
                            // Create a new filename for the saved image by prefixing "grayscale_" to the original image name
                            String newFileName = "grayscale_" + imageName;
                            // Use Platform.runLater to ensure image saving happens on the JavaFX Application Thread
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    /* Assign the task 'saveImage' the task returned by the method saveImageWorker.
                                     * The saveImageWorker method is responsible for saving the image on the computer.
                                     * It takes two parameters: the image itself and the new filename for the image.
                                     */
                                    saveImage = saveImageWorker(img, newFileName);

                                    // Unbind the previous progress indicator binding
                                    myProgressIndicator.progressProperty().unbind();

                                    // Bind the progress indicator to the new task's progress property
                                    myProgressIndicator.progressProperty().bind(saveImage.progressProperty());

                                    // Start the execution of the image saving task in a new thread
                                    new Thread(saveImage).start();
                                }
                            });
                        }
                        break;
                    }
                }
                return true;
            }
        };
    }

    // Define a variable to hold the task for saving the image
    private Task saveImage;

    // Method for saving the image to the current directory
    private Task saveImageWorker(BufferedImage img, String imgName) {
        // Return a new task that will save the image
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    // Get the current working directory of the user
                    String workingdir = System.getProperty("user.dir");

                    // Create a new File object with the full path for the image (working directory + image name)
                    File f = new File(workingdir + "\\" + imgName);

                    // Update the progress for the progress indicator based on the image dimensions
                    updateProgress(img.getWidth(), img.getHeight());

                    // Save the image to the file in JPG format
                    ImageIO.write(img, "jpg", f);
                } catch (IOException e) {
                    // If an error occurs (e.g., IO exception), print the error
                    System.out.println(e);
                }
                return true;    // Return true to indicate the task completed successfully
            }
        };
    }

    // Method for selecting the first tab and handling the image upload
    public void selectTab1(Stage stage) {
        // Create a new FileChooser to allow the user to select a file
        final FileChooser fileChooser = new FileChooser();

        // Set the action for the 'upload' button
        myGraphicButton.setOnAction(
                new EventHandler<ActionEvent>() {   // When the button is clicked
                    @Override
                    public void handle(final ActionEvent e) {
                        // Open the file chooser and get the selected file
                        File file = fileChooser.showOpenDialog(stage);

                        // If the user selected a file, proceed
                        if (file != null) {
                            image = new Image(file.toURI().toString()); // Create a new Image object from the selected file's URI
                            imageName = file.getName(); // Save the name of the selected file
                            myTextField.setText("PICTURE UPLOADED SUCCESSFULLY");   // Update the TextField with a success message
                            // Set the text color to green and font size to 14px for the success message
                            myTextField.setStyle("-fx-text-fill: green; -fx-font-size: 14px;");
                        }
                    }
                });
    }

    // Method to display the content for the second tab (tab2)
    public void selectTab2(Stage stage) {
        // Set the text for the TextField to display the conversion instructions
        textf.setText("Converting a color image to grayscale");
        textf.setMinWidth(500d);     // Set the minimum width of the TextField
        textf.setDisable(true); // Disable the TextField so the user cannot edit it
        textf.setStyle("-fx-text-fill: black; -fx-font-size: 20px;");    // Style the text to be black with a font size of 20px
        // Position the TextField within the AnchorPane using anchors
        anchorPaneTab2.setLeftAnchor(textf, 300d);  // Set the left anchor
        anchorPaneTab2.setTopAnchor(textf, 20d);    // Set the top anchor

        // Create a new ImageView and load the logo image
        ImageView viewLabel = new ImageView(new Image(getClass().getResourceAsStream("link_logo.png")));
        viewLabel.setFitHeight(25); // Set the height to 25 pixels
        viewLabel.setPreserveRatio(true);   // Keep the original aspect ratio of the image when resizing
        advancedLabel.setGraphic(viewLabel);    // Display the logo image in the label
        // Position the label inside the AnchorPane using anchors
        anchorPaneTab2.setLeftAnchor(advancedLabel, 50d);
        anchorPaneTab2.setTopAnchor(advancedLabel, 80d);

        // Hyperlink setup
        // Set the text of the hyperlink to provide information about color-to-grayscale conversion
        hyperlink.setText("Click to learn more about color-to-grayscale conversion.");
        // Set the font size for the hyperlink text
        hyperlink.setStyle("-fx-font-size: 16px;");
        // Position the hyperlink inside the AnchorPane using anchors
        anchorPaneTab2.setLeftAnchor(hyperlink, 90d);
        anchorPaneTab2.setTopAnchor(hyperlink, 80d);

        // Radio buttons setup

        // Configure the first radio button
        radioButton.setText("Display information about the project.");  // Set the text for the first radio button
        radioButton.setToggleGroup(group);   // Add the radio button to the ToggleGroup to allow only one selection at a time
        // Position the first radion button inside the AnchorPane using anchors
        anchorPaneTab2.setLeftAnchor(radioButton, 50d);
        anchorPaneTab2.setTopAnchor(radioButton, 120d);

        // Configure the second radio button
        radioButton2.setText("Delete information about the project.");     // Set the text for the second radio button
        radioButton2.setToggleGroup(group); // Add the second radio button to the same ToggleGroup
        // Position the second radion button inside the AnchorPane using anchors
        anchorPaneTab2.setLeftAnchor(radioButton2, 50d);
        anchorPaneTab2.setTopAnchor(radioButton2, 150d);

        // Add the above elements to the children of the anchorPaneTab2
        anchorPaneTab2.getChildren().addAll(textf, advancedLabel, hyperlink, radioButton, radioButton2);
    }

    // Set up the functionality for the hyperlink
    public void handleHyperlink() {
        // When the hyperlink is clicked
        hyperlink.setOnAction(e -> {
            Desktop desktop = Desktop.getDesktop(); // Get the default desktop application (browser) to open the URL
            try {
                // Open the URL in the default web browser
                desktop.browse(URI.create("https://www.pixelmator.com/support/guide/pixelmator-pro/1028/"));
            } catch (IOException e1) {  // If there is an issue opening the link, catch and print the exception
                e1.printStackTrace();
            }
        });
    }

    // Functionality for handling the radio button selection
    public void handleRadioButton() {
        // Add a listener for the selected toggle in the group
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                // Check if a radio button is selected
                if (group.getSelectedToggle() != null) {
                    // Get the selected radio button
                    RadioButton button = (RadioButton) group.getSelectedToggle();

                    // If the selected button is the first radio button in the group
                    if (button.getText() == radioButton.getText()) {
                        // Display information about the color-to-grayscale conversion process
                        textarea.setText("The method of converting a color image to a grayscale image involves calculating the average "
                                + "of the red, green, and blue values for each pixel in order to obtain a grayscale value.");
                        textarea.setEditable(false);    // Make the textarea non-editable
                        textarea.setWrapText(true);     // Enable text wrapping if it exceeds the width
                        textarea.setPrefRowCount(5);    // Set the preferred row count to 5 lines
                        textarea.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");    // Set the text color to black and font size to 14px
                        anchorPaneTab2.setLeftAnchor(textarea, 50d);    // Set the position of the textarea on the anchorPane
                        anchorPaneTab2.setTopAnchor(textarea, 180d);
                        anchorPaneTab2.getChildren().add(textarea);     // Add the textarea to the AnchorPane’s children
                    }
                    // If the second radio button is selected
                    else if (button.getText() == radioButton2.getText()) {
                        // Remove the textarea from the AnchorPane if the second radio button is selected
                        anchorPaneTab2.getChildren().remove(textarea);
                    }
                }
            }
        });
    }
}